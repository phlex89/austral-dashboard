package app.australdashboard.temi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.view.View
import app.australdashboard.Schermata
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.Meteo
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.Ordine
import app.australdashboard.funzioni.Playlist
import app.australdashboard.funzioni.StatoSistema
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
    val accento: Int,
    val bordo: Int,
    val titoli: Typeface,
    val corpo: Typeface,
)

interface VistaHome {
    val view: View
    fun mostraOra(adesso: Date)
    fun mostraMeteo(meteo: Meteo?)
    fun mostraMusica(musica: Musica?)
    fun mostraSistema(stato: StatoSistema)
}

interface VistaBenzinai {
    val view: View
    fun mostra(elenco: ElencoBenzinai)
    fun mostraSpesa(testo: String?)
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
    fun wifi()
    fun bluetooth()
    fun wifiDettaglio()
    fun bluetoothDettaglio()
    fun temaSuccessivo()
    fun playlist(playlist: Playlist)
}

interface AzioniBenzinai {
    fun indietro()
    fun ordina(ordine: Ordine)
    fun rifornimenti(selezionato: Distributore?)
    fun intornoAMe()
    fun raggio()
    fun mappa()
    fun visualizzaSullaMappa(distributore: Distributore)
}

class StatoBenzinai {
    var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
    var selezionato: Distributore? = null

    fun alterna(d: Distributore?) {
        selezionato = if (d != null && d.id == selezionato?.id) null else d
    }

    fun riallinea(tutti: List<Distributore>) {
        selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
    }
}

class AppHome(val nome: String, val sigla: String, private val pacchetti: List<String>) {
    fun intent(context: Context): Intent? =
        pacchetti.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }

    companion object {
        private const val FILE = "impostazioni"
        private const val CHIAVE = "app_home_"

        private val predefinite = listOf(
            AppHome("Netflix", "N", listOf("com.netflix.mediaclient")),
            AppHome("Prime Video", "P", listOf("com.amazon.avod.thirdpartyclient", "com.amazon.avod")),
            AppHome("Disney+", "D", listOf("com.disney.disneyplus")),
            AppHome("YouTube", "Y", listOf("com.google.android.youtube")),
            AppHome("Stremio", "S", listOf("com.stremio.one", "com.stremio.app")),
            AppHome("TikTok", "T", listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
        )

        fun elenco(context: Context): List<AppHome> {
            val preferenze = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            return predefinite.mapIndexed { i, predefinita ->
                val pacchetto = preferenze.getString(CHIAVE + i, null) ?: return@mapIndexed predefinita
                val nome = etichetta(context, pacchetto)
                AppHome(nome, nome.take(1).uppercase(), listOf(pacchetto))
            }
        }

        fun scegli(context: Context, indice: Int, pacchetto: String) {
            context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(CHIAVE + indice, pacchetto).apply()
        }

        fun firma(context: Context): String =
            elenco(context).joinToString("|") { "${it.nome}:${it.intent(context)?.component?.packageName}" }

        private fun etichetta(context: Context, pacchetto: String): String = try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pacchetto, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pacchetto
        }
    }
}
