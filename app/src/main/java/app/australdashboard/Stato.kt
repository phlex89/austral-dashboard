package app.australdashboard

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Stato {
    private const val FILE = "stato"

    fun salvaEsito(context: Context, esito: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString("esito", esito)
            .putLong("ora", System.currentTimeMillis())
            .apply()
    }

    fun esito(context: Context): String {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val esito = prefs.getString("esito", null) ?: return "Nessun controllo ancora eseguito"
        val ora = SimpleDateFormat("dd/MM HH:mm", Locale.ITALY).format(Date(prefs.getLong("ora", 0)))
        return "$ora · $esito"
    }
}
