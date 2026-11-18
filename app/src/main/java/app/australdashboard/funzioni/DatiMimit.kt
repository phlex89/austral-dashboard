package app.australdashboard.funzioni

import java.io.BufferedReader
import java.io.Reader
import java.io.Writer
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object DatiMimit {
    const val RAGGIO_KM = 15.0
    const val MASSIMO_VICINI = 30

    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss")
    private val formatoOra = DateTimeFormatter.ofPattern("HH:mm")
    private val formatoGiorno = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    private val tipiImpianto = setOf("Stradale", "Autostradale")

    class Prezzo(val valore: Double, val aggiornato: Long)

    class Compatto(val data: LocalDate, val impianti: List<Distributore>)

    fun leggiPrezzi(lettore: Reader, zona: ZoneId = ZoneId.systemDefault()): Map<Long, Prezzo> {
        val prezzi = HashMap<Long, Prezzo>()
        BufferedReader(lettore).useLines { righe ->
            righe.forEach { riga ->
                val campi = riga.split('|')
                if (campi.size != 5) return@forEach
                val id = campi[0].trim().toLongOrNull() ?: return@forEach
                if (campi[1].trim() != "Benzina" || campi[3].trim() != "1") return@forEach
                val valore = numero(campi[2]) ?: return@forEach
                if (valore <= 0.0) return@forEach
                val aggiornato = dataComunicata(campi[4], zona) ?: return@forEach
                val attuale = prezzi[id]
                if (attuale == null || aggiornato > attuale.aggiornato) prezzi[id] = Prezzo(valore, aggiornato)
            }
        }
        return prezzi
    }

    fun leggiImpianti(lettore: Reader, prezzi: Map<Long, Prezzo>): List<Distributore> {
        val impianti = ArrayList<Distributore>()
        BufferedReader(lettore).useLines { righe ->
            righe.forEach { riga ->
                val campi = riga.split('|')
                val n = campi.size
                if (n < 10) return@forEach
                val id = campi[0].trim().toLongOrNull() ?: return@forEach
                val prezzo = prezzi[id] ?: return@forEach
                val tipo = (2 until n - 5).firstOrNull { campi[it].trim() in tipiImpianto } ?: return@forEach
                val lat = numero(campi[n - 2]) ?: return@forEach
                val lon = numero(campi[n - 1]) ?: return@forEach
                if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || (lat == 0.0 && lon == 0.0)) return@forEach
                impianti.add(
                    Distributore(
                        id = id,
                        nome = pulisci(campi.subList(tipo + 1, n - 5).joinToString("|")),
                        marchio = pulisci(campi[tipo - 1]),
                        indirizzo = pulisci(campi[n - 5]),
                        comune = pulisci(campi[n - 4]),
                        luogo = Luogo(lat, lon),
                        prezzo = prezzo.valore,
                        aggiornato = prezzo.aggiornato,
                        distanzaKm = 0.0,
                    ),
                )
            }
        }
        return impianti
    }

    fun scriviCompatto(uscita: Writer, data: LocalDate, impianti: List<Distributore>) {
        uscita.write(data.toString())
        uscita.write("\n")
        impianti.forEach {
            val campi = listOf(it.id, it.nome, it.marchio, it.indirizzo, it.comune, it.luogo.lat, it.luogo.lon, it.prezzo, it.aggiornato)
            uscita.write(campi.joinToString("\t"))
            uscita.write("\n")
        }
    }

    fun leggiCompatto(lettore: Reader): Compatto? {
        val righe = BufferedReader(lettore).useLines { it.toList() }
        val data = righe.firstOrNull()?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val impianti = righe.drop(1).mapNotNull { riga ->
            val c = riga.split('\t')
            if (c.size != 9) return@mapNotNull null
            val lat = c[5].toDoubleOrNull() ?: return@mapNotNull null
            val lon = c[6].toDoubleOrNull() ?: return@mapNotNull null
            Distributore(
                id = c[0].toLongOrNull() ?: return@mapNotNull null,
                nome = c[1],
                marchio = c[2],
                indirizzo = c[3],
                comune = c[4],
                luogo = Luogo(lat, lon),
                prezzo = c[7].toDoubleOrNull() ?: return@mapNotNull null,
                aggiornato = c[8].toLongOrNull() ?: return@mapNotNull null,
                distanzaKm = 0.0,
            )
        }
        return Compatto(data, impianti)
    }

    fun filtra(tutti: List<Distributore>, luogo: Luogo, preferiti: Set<Long>, ordine: Ordine): ElencoBenzinai.Pronto {
        val confronto = confronto(ordine)
        val conDistanza = tutti.map { it.copy(distanzaKm = distanzaKm(luogo, it.luogo)) }
        val (scelti, altri) = conDistanza.partition { it.id in preferiti }
        val vicini = altri.filter { it.distanzaKm <= RAGGIO_KM }.sortedWith(confronto).take(MASSIMO_VICINI)
        return ElencoBenzinai.Pronto(scelti.sortedWith(confronto), vicini, ordine)
    }

    fun distanzaKm(a: Luogo, b: Luogo): Double {
        val fi1 = Math.toRadians(a.lat)
        val fi2 = Math.toRadians(b.lat)
        val dFi = fi2 - fi1
        val dLam = Math.toRadians(b.lon - a.lon)
        val h = sin(dFi / 2) * sin(dFi / 2) + cos(fi1) * cos(fi2) * sin(dLam / 2) * sin(dLam / 2)
        return 2 * 6371.0088 * asin(min(1.0, sqrt(max(0.0, h))))
    }

    fun costo(d: Distributore): Double = d.prezzo * (40 + d.distanzaKm * 2 * 0.06)

    private fun confronto(ordine: Ordine): Comparator<Distributore> = when (ordine) {
        Ordine.PREZZO -> compareBy<Distributore> { it.prezzo }.thenBy { it.distanzaKm }
        Ordine.DISTANZA -> compareBy<Distributore> { it.distanzaKm }.thenBy { it.prezzo }
        Ordine.CONVIENE -> compareBy<Distributore> { costo(it) }.thenBy { it.distanzaKm }
    }

    fun prezzo(valore: Double): String = String.format(Locale.ITALY, "%.3f", valore)

    fun distanza(km: Double): String =
        if (km < 10) String.format(Locale.ITALY, "%.1f km", km) else String.format(Locale.ITALY, "%.0f km", km)

    fun quando(aggiornato: Long, adesso: Long, zona: ZoneId = ZoneId.systemDefault()): String {
        val momento = Instant.ofEpochMilli(aggiornato).atZone(zona)
        val oggi = Instant.ofEpochMilli(adesso).atZone(zona).toLocalDate()
        return when (momento.toLocalDate()) {
            oggi -> "oggi ${formatoOra.format(momento)}"
            oggi.minusDays(1) -> "ieri ${formatoOra.format(momento)}"
            else -> formatoGiorno.format(momento)
        }
    }

    private fun numero(testo: String): Double? =
        testo.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun dataComunicata(testo: String, zona: ZoneId): Long? =
        runCatching { LocalDateTime.parse(testo.trim(), formatoData).atZone(zona).toInstant().toEpochMilli() }.getOrNull()

    private fun pulisci(testo: String): String = testo.replace(Regex("\\s+"), " ").trim()
}
