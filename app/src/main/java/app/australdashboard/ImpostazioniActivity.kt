package app.australdashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import app.australdashboard.aggiornamenti.Aggiornatore

class ImpostazioniActivity : Schermata() {
    private lateinit var esito: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Impostazioni", 40f))
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
        setContentView(radice)
    }

    override fun onResume() {
        super.onResume()
        esito.text = Stato.esito(this)
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
