package app.australdashboard.funzioni

import android.content.Context
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

object Benzinai {
    private const val ANAGRAFICA = "https://www.mimit.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv"
    private const val PREZZI = "https://www.mimit.gov.it/images/exportCSV/prezzo_alle_8.csv"
    private const val FILE_COMPATTO = "benzinai.txt"
    private const val ARCHIVIO = "benzinai"
    private const val CHIAVE_PREFERITI = "preferiti"

    @Synchronized
    fun elenco(context: Context, luogo: Luogo, ordine: Ordine): ElencoBenzinai {
        val file = File(context.filesDir, FILE_COMPATTO)
        var compatto = leggi(file)
        if (compatto == null || compatto.data != LocalDate.now()) {
            val nuovo = try {
                scarica(context, file)
            } catch (e: Exception) {
                null
            }
            if (nuovo != null) compatto = nuovo
        }
        if (compatto == null || compatto.impianti.isEmpty()) {
            return ElencoBenzinai.Errore("Prezzi non disponibili. Controlla la connessione.")
        }
        return DatiMimit.filtra(compatto.impianti, luogo, preferiti(context), ordine)
    }

    fun preferiti(context: Context): Set<Long> =
        archivio(context).getStringSet(CHIAVE_PREFERITI, emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()

    fun cambiaPreferito(context: Context, id: Long): Boolean {
        val attuali = preferiti(context).toMutableSet()
        val ora = attuali.add(id)
        if (!ora) attuali.remove(id)
        archivio(context).edit().putStringSet(CHIAVE_PREFERITI, attuali.map { it.toString() }.toSet()).apply()
        return ora
    }

    fun prezzo(valore: Double): String = DatiMimit.prezzo(valore)

    fun distanza(km: Double): String = DatiMimit.distanza(km)

    fun quando(aggiornato: Long, adesso: Long): String = DatiMimit.quando(aggiornato, adesso)

    private fun archivio(context: Context) = context.getSharedPreferences(ARCHIVIO, Context.MODE_PRIVATE)

    private fun leggi(file: File): DatiMimit.Compatto? =
        if (!file.exists()) null else runCatching { file.bufferedReader().use(DatiMimit::leggiCompatto) }.getOrNull()

    private fun scarica(context: Context, file: File): DatiMimit.Compatto {
        val anagrafica = File(context.cacheDir, "anagrafica.csv")
        val prezzi = File(context.cacheDir, "prezzi.csv")
        val temporaneo = File(context.filesDir, "$FILE_COMPATTO.tmp")
        try {
            apri(ANAGRAFICA) { input -> anagrafica.outputStream().use { input.copyTo(it) } }
            apri(PREZZI) { input -> prezzi.outputStream().use { input.copyTo(it) } }
            val elencoPrezzi = prezzi.bufferedReader().use { DatiMimit.leggiPrezzi(it) }
            val impianti = anagrafica.bufferedReader().use { DatiMimit.leggiImpianti(it, elencoPrezzi) }
            if (impianti.isEmpty()) error("Nessun impianto nel file scaricato")
            val oggi = LocalDate.now()
            temporaneo.bufferedWriter().use { DatiMimit.scriviCompatto(it, oggi, impianti) }
            if (!temporaneo.renameTo(file)) error("Salvataggio non riuscito")
            return DatiMimit.Compatto(oggi, impianti)
        } finally {
            anagrafica.delete()
            prezzi.delete()
            temporaneo.delete()
        }
    }

    private fun <T> apri(indirizzo: String, leggi: (InputStream) -> T): T {
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 15_000
        connessione.readTimeout = 60_000
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice da $indirizzo")
            return connessione.inputStream.use(leggi)
        } finally {
            connessione.disconnect()
        }
    }
}
