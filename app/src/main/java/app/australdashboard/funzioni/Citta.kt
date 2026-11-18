package app.australdashboard.funzioni

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

object Citta {
    fun cerca(nome: String): List<Pair<String, Luogo>> {
        val indirizzo = "https://geocoding-api.open-meteo.com/v1/search?name=" +
            URLEncoder.encode(nome, "UTF-8") + "&count=6&language=it&format=json"
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 15_000
        connessione.readTimeout = 30_000
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice")
            return daJson(connessione.inputStream.use { it.bufferedReader().readText() })
        } finally {
            connessione.disconnect()
        }
    }

    fun daJson(testo: String): List<Pair<String, Luogo>> {
        val risultati = JSONObject(testo).optJSONArray("results") ?: return emptyList()
        return (0 until risultati.length()).map { i ->
            val voce = risultati.getJSONObject(i)
            val zona = voce.optString("admin2").ifEmpty { voce.optString("admin1") }
            val etichetta = if (zona.isEmpty()) voce.getString("name") else "${voce.getString("name")}, $zona"
            etichetta to Luogo(voce.getDouble("latitude"), voce.getDouble("longitude"))
        }
    }
}
