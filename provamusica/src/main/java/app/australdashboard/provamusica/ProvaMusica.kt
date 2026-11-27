package app.australdashboard.provamusica

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView

class ProvaMusica : Activity() {
    private lateinit var sessione: MediaSession
    private lateinit var scritta: TextView
    private var posizione = 0L
    private var aggiornata = 0L
    private var suona = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scritta = TextView(this).apply { textSize = 28f }
        setContentView(scritta)
        sessione = MediaSession(this, "prova").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = imposta(adesso(), true)
                override fun onPause() = imposta(adesso(), false)
                override fun onSeekTo(pos: Long) = imposta(pos, suona)
            })
            isActive = true
        }
        brano(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        brano(intent)
    }

    override fun onDestroy() {
        sessione.release()
        super.onDestroy()
    }

    private fun brano(intent: Intent) {
        val titolo = intent.getStringExtra("titolo") ?: "Bohemian Rhapsody"
        val artista = intent.getStringExtra("artista") ?: "Queen"
        val durata = intent.getLongExtra("durata", 355_000)
        sessione.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, titolo)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artista)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, durata)
                .build(),
        )
        imposta(intent.getLongExtra("posizione", 0), true)
        scritta.text = "$titolo · $artista"
    }

    private fun adesso(): Long =
        if (suona) posizione + SystemClock.elapsedRealtime() - aggiornata else posizione

    private fun imposta(nuova: Long, inRiproduzione: Boolean) {
        posizione = nuova
        aggiornata = SystemClock.elapsedRealtime()
        suona = inRiproduzione
        sessione.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_SEEK_TO)
                .setState(if (inRiproduzione) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED, nuova, 1f, aggiornata)
                .build(),
        )
    }
}
