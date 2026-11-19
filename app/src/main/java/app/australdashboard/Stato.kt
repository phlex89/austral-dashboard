package app.australdashboard

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Stato {
    private const val FILE = "stato"
    private const val FILE_ESITO = "aggiornamenti"

    @Suppress("DEPRECATION")
    private fun esiti(context: Context) = context.getSharedPreferences(FILE_ESITO, Context.MODE_MULTI_PROCESS)

    fun salvaEsito(context: Context, esito: String) {
        esiti(context).edit()
            .putString("esito", esito)
            .putLong("ora", System.currentTimeMillis())
            .commit()
    }

    fun esito(context: Context): String {
        val prefs = esiti(context)
        val esito = prefs.getString("esito", null) ?: return "Nessun controllo ancora eseguito"
        val ora = SimpleDateFormat("dd/MM HH:mm", Locale.ITALY).format(Date(prefs.getLong("ora", 0)))
        return "$ora · $esito"
    }

    fun salvaCrash(context: Context, versione: String?, traccia: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putLong("crashOra", System.currentTimeMillis())
            .putString("crashVersione", versione)
            .putString("crashTraccia", traccia.take(2000))
            .commit()
    }

    fun crash(context: Context): String {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val traccia = prefs.getString("crashTraccia", null) ?: return "nessuno"
        val ora = SimpleDateFormat("dd/MM HH:mm", Locale.ITALY).format(Date(prefs.getLong("crashOra", 0)))
        return "$ora · ${prefs.getString("crashVersione", "?")}\n$traccia"
    }
}
