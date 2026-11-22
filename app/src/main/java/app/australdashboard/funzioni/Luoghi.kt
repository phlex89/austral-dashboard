package app.australdashboard.funzioni

import android.content.Context
import android.location.Geocoder
import java.util.Locale

object Luoghi {
    private const val PREFERENZE = "luoghi"
    private const val POSIZIONE = "posizione"
    const val NOME_GPS = "La mia posizione"
    const val CHIAVE_GPS = "gps"
    const val NOME_SCONOSCIUTO = "Posizione attuale"

    fun formatta(elenco: List<Pair<String, Luogo>>): String =
        elenco.joinToString("\n") { (nome, luogo) ->
            String.format(Locale.US, "%s\t%.6f\t%.6f", nome, luogo.lat, luogo.lon)
        }

    fun analizza(testo: String): List<Pair<String, Luogo>> =
        testo.split('\n').mapNotNull { riga ->
            val campi = riga.split('\t')
            if (campi.size != 3 || campi[0].isBlank()) return@mapNotNull null
            val lat = campi[1].toDoubleOrNull() ?: return@mapNotNull null
            val lon = campi[2].toDoubleOrNull() ?: return@mapNotNull null
            campi[0] to Luogo(lat, lon)
        }

    fun conLuogo(elenco: List<Pair<String, Luogo>>, nome: String, luogo: Luogo): List<Pair<String, Luogo>> {
        val pulito = nome.replace(Regex("\\s+"), " ").trim()
        if (pulito.isEmpty()) return elenco
        val indice = elenco.indexOfFirst { it.first == pulito }
        return if (indice < 0) {
            elenco + (pulito to luogo)
        } else {
            elenco.toMutableList().also { it[indice] = pulito to luogo }
        }
    }

    fun senzaLuogo(elenco: List<Pair<String, Luogo>>, nome: String): List<Pair<String, Luogo>> =
        elenco.filter { it.first != nome }

    @Synchronized
    fun elenco(context: Context): List<Pair<String, Luogo>> {
        val preferenze = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE)
        var elenco = analizza(preferenze.getString("elenco", "").orEmpty())
        val vecchie = context.getSharedPreferences(POSIZIONE, Context.MODE_PRIVATE)
        val nome = vecchie.getString("citta_nome", null)
        if (nome != null) {
            if (vecchie.contains("citta_lat") && vecchie.contains("citta_lon")) {
                val luogo = Luogo(
                    Double.fromBits(vecchie.getLong("citta_lat", 0)),
                    Double.fromBits(vecchie.getLong("citta_lon", 0)),
                )
                elenco = listOf(nome to luogo) + senzaLuogo(elenco, nome)
                preferenze.edit().putString("elenco", formatta(elenco)).apply()
            }
            vecchie.edit().remove("citta_nome").remove("citta_lat").remove("citta_lon").apply()
        }
        return elenco
    }

    @Synchronized
    fun aggiungi(context: Context, nome: String, luogo: Luogo) {
        val nuovo = conLuogo(elenco(context), nome, luogo)
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putString("elenco", formatta(nuovo))
            .apply()
    }

    @Synchronized
    fun togli(context: Context, nome: String) {
        val modifica = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putString("elenco", formatta(senzaLuogo(elenco(context), nome)))
        if (nomeAttivo(context) == nome) modifica.putString("attivo", "")
        modifica.apply()
    }

    fun scegli(context: Context, nome: String?) {
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putString("attivo", nome.orEmpty())
            .apply()
    }

    fun attivo(context: Context): Pair<String, Luogo>? {
        val nome = nomeAttivo(context)
        return if (nome.isEmpty()) null else elenco(context).firstOrNull { it.first == nome }
    }

    fun chiave(context: Context): String = attivo(context)?.first ?: CHIAVE_GPS

    fun riserva(context: Context): Pair<String, Luogo>? = elenco(context).firstOrNull()

    fun nomeDi(context: Context, luogo: Luogo): String {
        if (Geocoder.isPresent()) {
            val nome = try {
                Geocoder(context, Locale.ITALY).getFromLocation(luogo.lat, luogo.lon, 1)
                    ?.firstOrNull()?.let { it.locality ?: it.subAdminArea }
            } catch (e: Exception) {
                null
            }
            if (!nome.isNullOrBlank()) return nome
        }
        return Benzinai.comuneVicino(context, luogo) ?: NOME_SCONOSCIUTO
    }

    private fun nomeAttivo(context: Context): String =
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getString("attivo", "").orEmpty()
}
