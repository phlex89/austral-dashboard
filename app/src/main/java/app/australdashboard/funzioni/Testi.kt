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
        connessione.setRequestProperty("User-Agent", "AustralDashboard (https://github.com/phlex89/austral-dashboard)")
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice")
            return connessione.inputStream.use { it.bufferedReader().readText() }
        } finally {
            connessione.disconnect()
        }
    }
}

class LimiteTraduzioni : Exception()

object FonteTraduzioni {
    private const val MASSIMO_BYTE = 450
    private const val MASSIMO_IN_MEMORIA = 20
    private val inMemoria = LinkedHashMap<String, List<String>?>()
    private val entita = Regex("""&(#\d+|#x[0-9a-fA-F]+|quot|amp|lt|gt|apos);""")

    fun traduci(chiave: String, righe: List<String>, email: String): List<String>? {
        synchronized(inMemoria) { if (inMemoria.containsKey(chiave)) return inMemoria[chiave] }
        val diverse = righe.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val tradotte = HashMap<String, String>()
        var tutteItaliane = true
        for (blocco in blocchi(diverse)) {
            val risposta = leggiRisposta(chiedi(blocco.joinToString("\n"), email))
            if (risposta == null) {
                blocco.forEach { tradotte[it] = it }
                continue
            }
            tutteItaliane = false
            val linee = risposta.lines()
            blocco.forEachIndexed { i, riga -> tradotte[riga] = linee.getOrNull(i)?.trim().orEmpty() }
        }
        val esito = if (tutteItaliane) null else righe.map { tradotte[it.trim()].orEmpty() }
        synchronized(inMemoria) {
            inMemoria[chiave] = esito
            if (inMemoria.size > MASSIMO_IN_MEMORIA) inMemoria.remove(inMemoria.keys.first())
        }
        return esito
    }

    fun blocchi(righe: List<String>): List<List<String>> {
        val esito = mutableListOf<MutableList<String>>()
        var byte = 0
        for (riga in righe) {
            val peso = riga.toByteArray(Charsets.UTF_8).size + 1
            if (esito.isEmpty() || byte + peso > MASSIMO_BYTE) {
                esito.add(mutableListOf())
                byte = 0
            }
            esito.last().add(riga)
            byte += peso
        }
        return esito
    }

    fun leggiRisposta(json: String): String? {
        val o = JSONObject(json)
        val stato = o.optInt("responseStatus")
        if (o.optBoolean("quotaFinished") || stato == 429) throw LimiteTraduzioni()
        if (o.optString("responseDetails").contains("DISTINCT LANGUAGES")) return null
        if (stato != 200) error("MyMemory $stato")
        return decodifica(o.getJSONObject("responseData").optString("translatedText"))
    }

    fun decodifica(testo: String): String = entita.replace(testo) {
        when (val nome = it.groupValues[1]) {
            "quot" -> "\""
            "amp" -> "&"
            "lt" -> "<"
            "gt" -> ">"
            "apos" -> "'"
            else -> {
                val codice = if (nome.startsWith("#x")) nome.drop(2).toInt(16) else nome.drop(1).toInt()
                String(Character.toChars(codice))
            }
        }
    }

    private fun chiedi(testo: String, email: String): String {
        var indirizzo = "https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(testo, "UTF-8") +
            "&langpair=" + URLEncoder.encode("autodetect|it", "UTF-8")
        if (email.isNotBlank()) indirizzo += "&de=" + URLEncoder.encode(email.trim(), "UTF-8")
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 10_000
        connessione.readTimeout = 15_000
        connessione.setRequestProperty("User-Agent", "AustralDashboard (https://github.com/phlex89/austral-dashboard)")
        try {
            val codice = connessione.responseCode
            if (codice == 429) throw LimiteTraduzioni()
            val flusso = if (codice < 400) connessione.inputStream else connessione.errorStream ?: error("HTTP $codice")
            return flusso.use { it.bufferedReader().readText() }
        } finally {
            connessione.disconnect()
        }
    }
}
