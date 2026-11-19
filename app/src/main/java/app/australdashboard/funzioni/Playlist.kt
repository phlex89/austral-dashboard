package app.australdashboard.funzioni

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

data class Playlist(val nome: String, val link: String)

object Playlists {
    const val MASSIMO = 3
    private const val ARCHIVIO = "playlist"
    private const val CHIAVE = "elenco"
    private val urlRegex = Regex("""https?://[^\s]+""")
    private val spotifyRegex = Regex(
        """^https://open\.spotify\.com/(?:intl-[a-z]+/)?(playlist|album|artist|track|show|episode)/([A-Za-z0-9]+)(?:[?#/].*)?$"""
    )

    fun elenco(context: Context): List<Playlist> {
        val testo = context.getSharedPreferences(ARCHIVIO, Context.MODE_PRIVATE).getString(CHIAVE, "") ?: ""
        return daTesto(testo)
    }

    fun aggiungi(context: Context, playlist: Playlist) =
        salva(context, (elenco(context) + playlist).takeLast(MASSIMO))

    fun togli(context: Context, indice: Int) {
        val attuali = elenco(context)
        if (indice in attuali.indices) salva(context, attuali.filterIndexed { i, _ -> i != indice })
    }

    fun apri(context: Context, playlist: Playlist) {
        if (!avvia(context, uriDaAprire(playlist.link))) avvia(context, playlist.link)
    }

    private fun avvia(context: Context, uri: String): Boolean = try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    private fun salva(context: Context, voci: List<Playlist>) {
        context.getSharedPreferences(ARCHIVIO, Context.MODE_PRIVATE).edit()
            .putString(CHIAVE, aTesto(voci)).apply()
    }

    fun aTesto(voci: List<Playlist>): String =
        voci.joinToString("\n") { "${it.nome.replace(Regex("\\s+"), " ")}\t${it.link}" }

    fun daTesto(testo: String): List<Playlist> = testo.lines().mapNotNull { riga ->
        val campi = riga.split('\t')
        if (campi.size == 2 && campi[1].isNotEmpty()) Playlist(campi[0], campi[1]) else null
    }

    fun linkDaTesto(testo: String): String? = urlRegex.find(testo)?.value

    fun nomeDaCondivisione(testo: String, oggetto: String?): String {
        val scelto = oggetto?.trim()?.takeIf { it.isNotEmpty() }
            ?: testo.substringBefore("http").replace(Regex("[\\s:\\-–—]+$"), "").trim().takeIf { it.isNotEmpty() }
            ?: "Playlist"
        return scelto.take(40)
    }

    fun uriDaAprire(link: String): String {
        val trovato = spotifyRegex.matchEntire(link) ?: return link
        return "spotify:${trovato.groupValues[1]}:${trovato.groupValues[2]}:play"
    }
}
