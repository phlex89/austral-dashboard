package app.australdashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.view.View
import android.widget.Button
import app.australdashboard.aggiornamenti.Aggiornatore
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Playlists
import app.australdashboard.funzioni.Posizione
import app.australdashboard.temi.Temi

class ImpostazioniActivity : Schermata() {
    private lateinit var esito: TextView
    private lateinit var tema: Button
    private lateinit var musica: Button
    private lateinit var citta: Button
    private lateinit var comuni: Button
    private lateinit var playlist: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Impostazioni", 40f))
        tema = pulsante("") { startActivity(Intent(this, TemiActivity::class.java)) }
        radice.addView(tema)
        musica = pulsante("Consenti la lettura della musica") { Lettore.apriPermesso(this) }
        radice.addView(musica)
        citta = pulsante("") { startActivity(Intent(this, CittaActivity::class.java)) }
        radice.addView(citta)
        comuni = pulsante("") { startActivity(Intent(this, ComuniActivity::class.java)) }
        radice.addView(comuni)
        radice.addView(pulsante("App della Home") { startActivity(Intent(this, AppHomeActivity::class.java)) })
        playlist = pulsante("") { startActivity(Intent(this, PlaylistActivity::class.java)) }
        radice.addView(playlist)
        radice.addView(pulsante("Controlla aggiornamenti") { controllaOra() })
        esito = testo("")
        radice.addView(esito)
        radice.addView(pulsante("Diagnostica") {
            startActivity(Intent(this, DiagnosticaActivity::class.java))
        })
        radice.addView(pulsante("Impostazioni di sistema") {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        })
        radice.addView(pulsante("Cambia launcher predefinito") {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        })
        setContentView(scorrevole(radice))
    }

    override fun onResume() {
        super.onResume()
        esito.text = Stato.esito(this)
        tema.text = "Tema: ${Temi.scelto(this).nome}"
        musica.visibility = if (Lettore.permessoConcesso(this)) View.GONE else View.VISIBLE
        citta.text = "Città di riserva: ${Posizione.cittaDiRiserva(this)?.first ?: "nessuna"}"
        comuni.text = "Comuni preferiti: ${Benzinai.preferiti(this).joinToString(", ").ifEmpty { "nessuno" }}"
        playlist.text = "Playlist preferite: ${Playlists.elenco(this).joinToString(", ") { it.nome }.ifEmpty { "nessuna" }}"
    }

    private fun controllaOra() {
        esito.text = "Controllo in corso…"
        Thread {
            val occupato = Aggiornatore.controlla(applicationContext) == Aggiornatore.Esito.OCCUPATO
            runOnUiThread {
                esito.text = if (occupato) "Un controllo è già in corso" else Stato.esito(this)
            }
        }.start()
    }
}
