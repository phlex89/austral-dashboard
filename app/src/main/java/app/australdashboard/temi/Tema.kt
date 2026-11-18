package app.australdashboard.temi

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import app.australdashboard.Schermata
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.Meteo
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.Ordine
import java.util.Date

interface Tema {
    val id: String
    val nome: String
    val idea: String
    fun palette(context: Context): Palette
    fun home(s: Schermata, azioni: AzioniHome): VistaHome
    fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai
}

data class Palette(
    val sfondo: Int,
    val testo: Int,
    val secondario: Int,
    val accento: Int,
    val suAccento: Int,
    val bordo: Int,
    val titoli: Typeface,
    val corpo: Typeface,
)

interface VistaHome {
    val view: View
    fun mostraOra(adesso: Date)
    fun mostraMeteo(meteo: Meteo?)
    fun mostraMusica(musica: Musica?)
}

interface VistaBenzinai {
    val view: View
    fun mostra(elenco: ElencoBenzinai, preferiti: Set<Long>)
}

interface AzioniHome {
    fun benzinai()
    fun tutteLeApp()
    fun impostazioniAndroid()
    fun impostazioniLauncher()
    fun apri(app: AppHome)
    fun apriMusica()
    fun precedente()
    fun playPausa()
    fun successivo()
}

interface AzioniBenzinai {
    fun indietro()
    fun ordina(ordine: Ordine)
    fun cambiaPreferito(distributore: Distributore)
}

class AppHome(val nome: String, val sigla: String, private val pacchetti: List<String>) {
    fun intent(context: Context): Intent? =
        pacchetti.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }

    companion object {
        val elenco = listOf(
            AppHome("Netflix", "N", listOf("com.netflix.mediaclient")),
            AppHome("Prime Video", "P", listOf("com.amazon.avod.thirdpartyclient", "com.amazon.avod")),
            AppHome("Disney+", "D", listOf("com.disney.disneyplus")),
            AppHome("YouTube", "Y", listOf("com.google.android.youtube")),
            AppHome("Stremio", "S", listOf("com.stremio.one", "com.stremio.app")),
            AppHome("TikTok", "T", listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
        )
    }
}
