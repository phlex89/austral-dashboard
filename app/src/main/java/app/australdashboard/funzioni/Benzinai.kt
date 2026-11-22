package app.australdashboard.funzioni

import android.content.Context
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

object Benzinai {
    private const val ANAGRAFICA = "https://www.mimit.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv"
    private const val PREZZI = "https://www.mimit.gov.it/images/exportCSV/prezzo_alle_8.csv"
    private const val FILE_COMPATTO = "benzinai.txt"
    private const val FILE_PRIMA = "benzinai-prima.txt"
    private const val PREFERENZE = "benzinai"
    private const val ATTESA_DOPO_ERRORE_MS = 10 * 60 * 1000L
    private val RAGGI = listOf(5, 10, 20, 30)
    private var memoria: DatiMimit.Compatto? = null
    private var ultimoErrore = 0L
    private val prezziPrima = HashMap<Long, Double?>()

    fun preferiti(context: Context): List<String> =
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getString("comuni", "")!!
            .split('\n').filter { it.isNotBlank() }

    fun salvaPreferiti(context: Context, comuni: List<String>) {
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putString("comuni", comuni.joinToString("\n"))
            .apply()
    }

    fun raggio(context: Context): Double =
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getInt("raggio", 10)
            .takeIf { it in RAGGI }?.toDouble() ?: 10.0

    fun prossimoRaggio(context: Context): Double {
        val attuale = raggio(context).toInt()
        val prossimo = RAGGI[(RAGGI.indexOf(attuale) + 1) % RAGGI.size]
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit().putInt("raggio", prossimo).apply()
        return prossimo.toDouble()
    }

    @Synchronized
    fun comuni(context: Context, testo: String): List<String> {
        val compatto = memoria ?: leggi(File(context.filesDir, FILE_COMPATTO)).also { memoria = it } ?: return emptyList()
        return DatiMimit.comuniContenenti(compatto.impianti, testo)
    }

    fun disponibile(context: Context): Boolean = File(context.filesDir, FILE_COMPATTO).exists()

    @Synchronized
    fun libera() {
        memoria = null
        prezziPrima.clear()
    }

    @Synchronized
    fun elenco(context: Context, luogo: Luogo, ordine: Ordine, intornoAMe: Boolean = false): ElencoBenzinai {
        val file = File(context.filesDir, FILE_COMPATTO)
        var compatto = memoria ?: leggi(file).also { memoria = it }
        val adesso = System.currentTimeMillis()
        val vecchio = compatto == null || DatiMimit.daAggiornare(compatto.scaricato, LocalDateTime.now())
        if (vecchio && adesso - ultimoErrore > ATTESA_DOPO_ERRORE_MS) {
            try {
                compatto = scarica(context, file)
                memoria = compatto
                prezziPrima.clear()
            } catch (e: Exception) {
                ultimoErrore = adesso
            }
        }
        if (compatto == null || compatto.impianti.isEmpty()) {
            return ElencoBenzinai.Errore("Prezzi non disponibili. Controlla la connessione.")
        }
        val pronto = DatiMimit.filtra(compatto.impianti, luogo, preferiti(context), ordine, raggio(context), intornoAMe)
        val richiesti = (pronto.preferiti + pronto.vicini).map { it.id }.toSet()
        val mancanti = richiesti.filter { it !in prezziPrima }.toSet()
        if (mancanti.isNotEmpty()) {
            val letti = File(context.filesDir, FILE_PRIMA).takeIf { it.exists() }
                ?.let { runCatching { it.bufferedReader().use { r -> DatiMimit.prezziDi(r, mancanti) } }.getOrNull() }
                .orEmpty()
            mancanti.forEach { prezziPrima[it] = letti[it] }
        }
        return pronto.copy(
            preferiti = pronto.preferiti.map { it.copy(prezzoPrima = prezziPrima[it.id]) },
            vicini = pronto.vicini.map { it.copy(prezzoPrima = prezziPrima[it.id]) },
        )
    }

    fun comuneVicino(context: Context, luogo: Luogo): String? {
        val file = File(context.filesDir, FILE_COMPATTO)
        if (!file.exists()) return null
        return runCatching { file.bufferedReader().use { DatiMimit.comunePiuVicino(it, luogo) } }.getOrNull()
    }

    fun prezzo(valore: Double): String = DatiMimit.prezzo(valore)

    fun distanza(km: Double): String = DatiMimit.distanza(km)

    fun quando(aggiornato: Long, adesso: Long): String = DatiMimit.quando(aggiornato, adesso)

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
            val adesso = LocalDateTime.now()
            temporaneo.bufferedWriter().use { DatiMimit.scriviCompatto(it, adesso, impianti) }
            if (file.exists()) file.renameTo(File(context.filesDir, FILE_PRIMA))
            if (!temporaneo.renameTo(file)) error("Salvataggio non riuscito")
            return DatiMimit.Compatto(adesso, impianti)
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
