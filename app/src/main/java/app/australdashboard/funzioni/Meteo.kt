package app.australdashboard.funzioni

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.roundToInt
import org.json.JSONObject

object FonteMeteo {
    private const val PREFERENZE = "meteo"
    private const val VALIDITA_MS = 30 * 60 * 1000L
    private const val ETA_MASSIMA_MS = 3 * 60 * 60 * 1000L
    private const val PASSO_MS = 15 * 60 * 1000L
    private const val ORIZZONTE_PIOGGIA_MS = 120 * 60 * 1000L
    private const val SOGLIA_PIOGGIA_MM = 0.1

    class Pioggia(val inizio: Long, val mm: List<Double>)

    fun ultimo(context: Context): Meteo? {
        val preferenze = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE)
        if (!preferenze.contains("quando")) return null
        if (troppoVecchio(preferenze.getLong("quando", 0), System.currentTimeMillis())) return null
        return Meteo(
            gradi = preferenze.getInt("gradi", 0),
            minima = preferenze.getInt("minima", 0),
            massima = preferenze.getInt("massima", 0),
            codice = preferenze.getInt("codice", 0),
            pioggiaTraMinuti = preferenze.getString("pioggia_mm", null)?.let { mm ->
                pioggiaTra(
                    preferenze.getLong("pioggia_inizio", 0),
                    mm.split(',').mapNotNull { it.toDoubleOrNull() },
                    System.currentTimeMillis(),
                )
            },
        )
    }

    fun pioggiaTra(inizio: Long, mm: List<Double>, adesso: Long): Int? {
        mm.forEachIndexed { i, valore ->
            val passo = inizio + i * PASSO_MS
            if (passo > adesso + ORIZZONTE_PIOGGIA_MS) return null
            if (valore < SOGLIA_PIOGGIA_MM || passo + PASSO_MS <= adesso) return@forEachIndexed
            if (passo <= adesso) return 0
            return ((passo - adesso) / 300_000.0).roundToInt().times(5).coerceAtLeast(5)
        }
        return null
    }

    fun troppoVecchio(quando: Long, adesso: Long): Boolean = adesso - quando > ETA_MASSIMA_MS

    fun daAggiornare(context: Context): Boolean {
        val quando = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getLong("quando", 0)
        return quando == 0L || System.currentTimeMillis() - quando > VALIDITA_MS
    }

    fun scarica(context: Context, luogo: Luogo): Meteo? = try {
        val indirizzo = String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1" +
                "&minutely_15=precipitation&forecast_minutely_15=12",
            luogo.lat,
            luogo.lon,
        )
        val testo = leggi(indirizzo)
        val meteo = daJson(testo)
        val pioggia = pioggiaDaJson(testo)
        val adesso = System.currentTimeMillis()
        val modifica = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
        if (pioggia != null) {
            modifica.putLong("pioggia_inizio", pioggia.inizio).putString("pioggia_mm", pioggia.mm.joinToString(","))
        } else {
            modifica.remove("pioggia_inizio").remove("pioggia_mm")
        }
        modifica
            .putInt("gradi", meteo.gradi)
            .putInt("minima", meteo.minima)
            .putInt("massima", meteo.massima)
            .putInt("codice", meteo.codice)
            .putLong("quando", adesso)
            .apply()
        if (pioggia == null) meteo else meteo.copy(pioggiaTraMinuti = pioggiaTra(pioggia.inizio, pioggia.mm, adesso))
    } catch (e: Exception) {
        null
    }

    fun daJson(testo: String): Meteo {
        val radice = JSONObject(testo)
        val attuale = radice.getJSONObject("current")
        val giorno = radice.getJSONObject("daily")
        return Meteo(
            gradi = attuale.getDouble("temperature_2m").roundToInt(),
            minima = giorno.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
            massima = giorno.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
            codice = attuale.getInt("weather_code"),
        )
    }

    fun pioggiaDaJson(testo: String): Pioggia? {
        val radice = JSONObject(testo)
        val serie = radice.optJSONObject("minutely_15") ?: return null
        val orari = serie.optJSONArray("time") ?: return null
        val valori = serie.optJSONArray("precipitation") ?: return null
        if (orari.length() == 0 || valori.length() == 0 || !radice.has("utc_offset_seconds")) return null
        val fuso = ZoneOffset.ofTotalSeconds(radice.getInt("utc_offset_seconds"))
        val inizio = LocalDateTime.parse(orari.getString(0)).toEpochSecond(fuso) * 1000
        return Pioggia(inizio, List(valori.length()) { valori.optDouble(it, 0.0) })
    }

    fun cielo(codice: Int): Cielo = when (codice) {
        0 -> Cielo.SERENO
        1, 2 -> Cielo.POCO_NUVOLOSO
        3 -> Cielo.NUVOLOSO
        45, 48 -> Cielo.NEBBIA
        in 51..67, in 80..82 -> Cielo.PIOGGIA
        in 71..77, 85, 86 -> Cielo.NEVE
        95, 96, 99 -> Cielo.TEMPORALE
        else -> Cielo.NUVOLOSO
    }

    fun descrizione(codice: Int): String = when (codice) {
        0 -> "Sereno"
        1, 2 -> "Poco nuvoloso"
        3 -> "Nuvoloso"
        45, 48 -> "Nebbia"
        in 51..57 -> "Pioviggine"
        in 61..67 -> "Pioggia"
        in 80..82 -> "Rovesci"
        in 71..77, 85, 86 -> "Neve"
        95, 96, 99 -> "Temporale"
        else -> "Nuvoloso"
    }

    private fun leggi(indirizzo: String): String {
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 15_000
        connessione.readTimeout = 30_000
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice")
            return connessione.inputStream.use { it.bufferedReader().readText() }
        } finally {
            connessione.disconnect()
        }
    }
}
