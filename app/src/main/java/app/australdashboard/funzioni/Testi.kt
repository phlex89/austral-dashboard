package app.australdashboard.funzioni

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

data class RigaTesto(val ms: Long, val testo: String)

data class TestoBrano(val righe: List<RigaTesto>, val sincronizzato: Boolean, val strumentale: Boolean = false)

object FonteTesti {
    private const val TOLLERANZA_S = 3.0
    private const val MASSIMO_IN_MEMORIA = 20
    private val tempo = Regex("""\[(\d+):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val inMemoria = LinkedHashMap<String, TestoBrano?>()

    fun scarica(titolo: String, artista: String, durataMs: Long): TestoBrano? {
        val chiave = "$titolo|$artista"
        synchronized(inMemoria) { if (inMemoria.containsKey(chiave)) return inMemoria[chiave] }
        val durataS = if (durataMs > 0) durataMs / 1000.0 else null
        var trovato = scegli(cerca(titolo, artista), durataS)
        val pulito = titoloPulito(titolo)
        val primoArtista = artista.substringBefore(',').trim()
        if (trovato == null && (pulito != titolo || primoArtista != artista)) {
            trovato = scegli(cerca(pulito, primoArtista), durataS)
        }
        synchronized(inMemoria) {
            inMemoria[chiave] = trovato
            if (inMemoria.size > MASSIMO_IN_MEMORIA) inMemoria.remove(inMemoria.keys.first())
        }
        return trovato
    }

    fun scegli(json: String, durataS: Double?): TestoBrano? {
        val elenco = JSONArray(json)
        val candidati = List(elenco.length()) { elenco.getJSONObject(it) }
            .filter { it.optBoolean("instrumental") || testoOVuoto(it, "plainLyrics").isNotBlank() || testoOVuoto(it, "syncedLyrics").isNotBlank() }
        val vicini = if (durataS == null) candidati else candidati.filter { abs(it.optDouble("duration", 0.0) - durataS) <= TOLLERANZA_S }
        val scelto = (vicini.ifEmpty { candidati }).maxByOrNull { punteggio(it) } ?: return null
        if (scelto.optBoolean("instrumental")) return TestoBrano(emptyList(), sincronizzato = false, strumentale = true)
        val sincronizzate = analizzaLrc(testoOVuoto(scelto, "syncedLyrics"))
        if (sincronizzate.isNotEmpty()) return TestoBrano(sincronizzate, sincronizzato = true)
        val righe = testoOVuoto(scelto, "plainLyrics").lines().map { RigaTesto(0, it.trim()) }
        return TestoBrano(righe.dropWhile { it.testo.isEmpty() }.dropLastWhile { it.testo.isEmpty() }, sincronizzato = false)
    }

    fun analizzaLrc(lrc: String): List<RigaTesto> =
        lrc.lines().flatMap { riga ->
            val tempi = tempo.findAll(riga).toList()
            if (tempi.isEmpty() || tempi.first().range.first != 0) return@flatMap emptyList()
            val testo = riga.substring(tempi.last().range.last + 1).trim()
            tempi.map { RigaTesto(millisecondi(it), testo) }
        }.sortedBy { it.ms }

    fun rigaCorrente(righe: List<RigaTesto>, posizioneMs: Long): Int =
        righe.indexOfLast { it.ms <= posizioneMs }

    fun titoloPulito(titolo: String): String =
        titolo.substringBefore(" - ").replace(Regex("""\s*[(\[][^)\]]*[)\]]"""), "").trim().ifEmpty { titolo }

    private fun millisecondi(m: MatchResult): Long {
        val (minuti, secondi, frazione) = m.destructured
        val frazioneMs = when (frazione.length) {
            0 -> 0L
            1 -> frazione.toLong() * 100
            2 -> frazione.toLong() * 10
            else -> frazione.toLong()
        }
        return minuti.toLong() * 60_000 + secondi.toLong() * 1000 + frazioneMs
    }

    private fun punteggio(o: JSONObject): Int = if (testoOVuoto(o, "syncedLyrics").isNotBlank()) 1 else 0

    private fun testoOVuoto(o: JSONObject, campo: String): String = if (o.isNull(campo)) "" else o.optString(campo)

    private fun cerca(titolo: String, artista: String): String {
        val indirizzo = "https://lrclib.net/api/search?track_name=" + URLEncoder.encode(titolo, "UTF-8") +
            "&artist_name=" + URLEncoder.encode(artista, "UTF-8")
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 10_000
        connessione.readTimeout = 15_000
        connessione.setRequestProperty("User-Agent", "AustralDashboard")
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice")
            return connessione.inputStream.use { it.bufferedReader().readText() }
        } finally {
            connessione.disconnect()
        }
    }
}
