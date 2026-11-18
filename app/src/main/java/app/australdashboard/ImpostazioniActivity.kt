package app.australdashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.view.View
import android.widget.Button
import app.australdashboard.aggiornamenti.Aggiornatore
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Posizione
import app.australdashboard.temi.Temi

class ImpostazioniActivity : Schermata() {
    private lateinit var esito: TextView
    private lateinit var tema: Button
    private lateinit var musica: Button
    private lateinit var citta: Button

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
    }

    private fun controllaOra() {
        esito.text = "Controllo in corso…"
        Thread {
            val avviato = Aggiornatore.controlla(applicationContext)
            runOnUiThread {
                esito.text = if (avviato) Stato.esito(this) else "Un controllo è già in corso"
            }
        }.start()
    }
}
