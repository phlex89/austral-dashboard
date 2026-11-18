package app.australdashboard.temi

import android.content.Context

object Temi {
    private const val FILE = "impostazioni"
    private const val CHIAVE = "tema"

    val elenco: List<Tema> = listOf(Matrix, Hud, Brutalismo, ArtDeco)

    fun scelto(context: Context): Tema {
        val id = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(CHIAVE, null)
        return elenco.firstOrNull { it.id == id } ?: elenco.first()
    }

    fun scegli(context: Context, tema: Tema) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(CHIAVE, tema.id).apply()
    }
}
