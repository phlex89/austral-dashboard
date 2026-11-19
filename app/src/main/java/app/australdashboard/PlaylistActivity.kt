package app.australdashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import app.australdashboard.funzioni.Playlist
import app.australdashboard.funzioni.Playlists

class PlaylistActivity : Schermata() {
    private lateinit var elenco: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Playlist preferite", 40f))
        if (intent.action == Intent.ACTION_SEND) {
            condivisione(radice)
        } else {
            radice.addView(testo("Per aggiungerne una: in Spotify apri la playlist, Condividi → Altro → Austral Dashboard"))
            elenco = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            radice.addView(elenco)
        }
        setContentView(scorrevole(radice))
    }

    override fun onResume() {
        super.onResume()
        if (intent.action != Intent.ACTION_SEND) mostraElenco()
    }

    private fun condivisione(radice: LinearLayout) {
        val contenuto = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val link = Playlists.linkDaTesto(contenuto)
        if (link == null) {
            radice.addView(testo("Nel testo condiviso non c'è nessun link"))
            radice.addView(pulsante("Chiudi") { finish() })
            return
        }
        val nome = EditText(this).apply {
            textSize = 28f
            setTextColor(Color.WHITE)
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT
            setText(Playlists.nomeDaCondivisione(contenuto, intent.getStringExtra(Intent.EXTRA_SUBJECT)))
        }
        radice.addView(nome)
        radice.addView(pulsante("Salva") {
            val scelto = nome.text.toString().trim().ifEmpty { "Playlist" }.take(40)
            Playlists.aggiungi(this, Playlist(scelto, link))
            finish()
        })
    }

    private fun mostraElenco() {
        elenco.removeAllViews()
        val salvate = Playlists.elenco(this)
        if (salvate.isEmpty()) elenco.addView(testo("Nessuna playlist salvata"))
        salvate.forEachIndexed { indice, playlist ->
            elenco.addView(pulsante("Togli ${playlist.nome}") {
                Playlists.togli(this, indice)
                mostraElenco()
            })
        }
    }
}
