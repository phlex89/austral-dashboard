package app.australdashboard.funzioni

import android.service.notification.NotificationListenerService

class AscoltoNotifiche : NotificationListenerService() {
    private var lettore: Lettore? = null
    private var ultimo: String? = null

    override fun onListenerConnected() {
        lettore = Lettore(this) { musica -> scaricaTesto(musica) }.also { it.avvia() }
    }

    override fun onListenerDisconnected() {
        lettore?.ferma()
        lettore = null
        ultimo = null
    }

    private fun scaricaTesto(musica: Musica?) {
        if (musica == null || musica.titolo.isEmpty()) return
        val chiave = "${musica.titolo}|${musica.artista}"
        if (chiave == ultimo) return
        ultimo = chiave
        val archivio = Archivio.di(this)
        Thread { runCatching { FonteTesti.scarica(musica.titolo, musica.artista, musica.durataMs, archivio) } }.start()
    }
}
