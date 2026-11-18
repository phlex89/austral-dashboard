package app.australdashboard.funzioni

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent

class Lettore(private val context: Context, private val ascolto: (Musica?) -> Unit) {

    private val main = Handler(Looper.getMainLooper())
    private val componente = ComponentName(context, AscoltoNotifiche::class.java)
    private val gestore = context.getSystemService(MediaSessionManager::class.java)
    private var attivo = false
    private var controller: MediaController? = null
    private var emesso = false
    private var ultima: Musica? = null

    private val cambioSessioni = MediaSessionManager.OnActiveSessionsChangedListener { ricalcola() }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = ricalcola()
        override fun onPlaybackStateChanged(state: PlaybackState?) = ricalcola()
        override fun onSessionDestroyed() = ricalcola()
    }

    fun avvia() {
        if (attivo) return
        if (!permessoConcesso(context)) {
            ascolto(null)
            return
        }
        try {
            gestore.addOnActiveSessionsChangedListener(cambioSessioni, componente, main)
        } catch (e: SecurityException) {
            ascolto(null)
            return
        }
        attivo = true
        emesso = false
        ricalcola()
    }

    fun ferma() {
        main.removeCallbacksAndMessages(null)
        if (attivo) {
            try {
                gestore.removeOnActiveSessionsChangedListener(cambioSessioni)
            } catch (e: SecurityException) {
            }
        }
        controller?.unregisterCallback(callback)
        controller = null
        attivo = false
        emesso = false
        ultima = null
    }

    fun precedente() {
        controller?.transportControls?.skipToPrevious()
    }

    fun successivo() {
        controller?.transportControls?.skipToNext()
    }

    fun playPausa() {
        val attuale = controller
        if (attuale != null) {
            if (inRiproduzione(attuale)) attuale.transportControls.pause() else attuale.transportControls.play()
            return
        }
        val audio = context.getSystemService(AudioManager::class.java)
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY))
        main.postDelayed({
            if (sessioni().none { inRiproduzione(it) }) apri(SPOTIFY)
        }, ATTESA_AVVIO_MS)
    }

    fun apriApp() {
        apri(controller?.packageName ?: SPOTIFY)
    }

    private fun apri(pacchetto: String) {
        val intento = context.packageManager.getLaunchIntentForPackage(pacchetto) ?: return
        intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intento)
    }

    private fun sessioni(): List<MediaController> =
        try {
            gestore.getActiveSessions(componente)
        } catch (e: SecurityException) {
            emptyList()
        }

    private fun inRiproduzione(c: MediaController): Boolean = statoInRiproduzione(c.playbackState?.state)

    private fun ricalcola() {
        if (!attivo) return
        val elenco = sessioni()
        val indice = scegliControllerIndice(
            elenco.map { Pair(it.playbackState?.state ?: PlaybackState.STATE_NONE, titolo(it.metadata) != null) },
        )
        val scelto = elenco.getOrNull(indice)
        if (scelto?.sessionToken != controller?.sessionToken) {
            controller?.unregisterCallback(callback)
            controller = scelto
            scelto?.registerCallback(callback, main)
        }
        val musica = scelto?.let { costruisci(it) }
        if (emesso && uguali(ultima, musica)) return
        emesso = true
        ultima = musica
        ascolto(musica)
    }

    private fun costruisci(c: MediaController): Musica {
        val meta = c.metadata
        return Musica(
            titolo = titolo(meta) ?: "",
            artista = testo(
                meta,
                MediaMetadata.METADATA_KEY_ARTIST,
                MediaMetadata.METADATA_KEY_ALBUM_ARTIST,
                MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
            ) ?: "",
            copertina = copertina(meta),
            inRiproduzione = inRiproduzione(c),
            app = etichetta(c.packageName),
            pacchetto = c.packageName,
        )
    }

    private fun titolo(meta: MediaMetadata?): String? =
        testo(meta, MediaMetadata.METADATA_KEY_TITLE, MediaMetadata.METADATA_KEY_DISPLAY_TITLE)

    private fun testo(meta: MediaMetadata?, vararg chiavi: String): String? {
        if (meta == null) return null
        for (chiave in chiavi) {
            val valore = meta.getString(chiave)
            if (!valore.isNullOrBlank()) return valore
        }
        return null
    }

    private fun copertina(meta: MediaMetadata?): Bitmap? {
        if (meta == null) return null
        val originale = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: return null
        val lato = maxOf(originale.width, originale.height)
        if (lato <= LATO_MASSIMO) return originale
        val scala = LATO_MASSIMO.toFloat() / lato
        return Bitmap.createScaledBitmap(
            originale,
            maxOf(1, (originale.width * scala).toInt()),
            maxOf(1, (originale.height * scala).toInt()),
            true,
        )
    }

    private fun etichetta(pacchetto: String): String =
        try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pacchetto, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pacchetto
        }

    private fun uguali(a: Musica?, b: Musica?): Boolean {
        if (a == null || b == null) return a == null && b == null
        return a.titolo == b.titolo && a.artista == b.artista && a.inRiproduzione == b.inRiproduzione &&
            a.pacchetto == b.pacchetto && a.copertina === b.copertina
    }

    companion object {
        const val SPOTIFY = "com.spotify.music"
        private const val LATO_MASSIMO = 600
        private const val ATTESA_AVVIO_MS = 1500L

        fun permessoConcesso(context: Context): Boolean =
            context.getSystemService(NotificationManager::class.java)
                .isNotificationListenerAccessGranted(ComponentName(context, AscoltoNotifiche::class.java))

        fun apriPermesso(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}

fun statoInRiproduzione(stato: Int?): Boolean =
    stato == PlaybackState.STATE_PLAYING || stato == PlaybackState.STATE_BUFFERING

fun scegliControllerIndice(controller: List<Pair<Int, Boolean>>): Int {
    val inCorso = controller.indexOfFirst { statoInRiproduzione(it.first) }
    if (inCorso >= 0) return inCorso
    return if (controller.firstOrNull()?.second == true) 0 else -1
}
