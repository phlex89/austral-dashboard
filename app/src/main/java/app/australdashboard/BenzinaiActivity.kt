package app.australdashboard

import android.os.Bundle
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Ordine
import app.australdashboard.funzioni.Posizione
import app.australdashboard.temi.AzioniBenzinai
import app.australdashboard.temi.Temi
import app.australdashboard.temi.VistaBenzinai

class BenzinaiActivity : Schermata(), AzioniBenzinai {
    private lateinit var vista: VistaBenzinai
    private var ordine = Ordine.CONVIENE
    private var luogo: Luogo? = null
    private var richiesta = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vista = Temi.scelto(this).benzinai(this, this)
        setContentView(vista.view)
        mostra(ElencoBenzinai.Caricamento)
        val subito = Posizione.attuale(this)
        if (subito != null) {
            luogo = subito
            carica()
        }
        Posizione.aggiorna(this) { fresco ->
            if (isDestroyed) return@aggiorna
            if (fresco == null) {
                if (luogo == null) mostra(ElencoBenzinai.SenzaPosizione)
                return@aggiorna
            }
            val primo = luogo == null
            luogo = fresco
            if (primo) carica()
        }
    }

    private fun mostra(elenco: ElencoBenzinai) = vista.mostra(elenco)

    private fun carica() {
        val dove = luogo ?: return
        val numero = ++richiesta
        val scelto = ordine
        Thread {
            val elenco = Benzinai.elenco(applicationContext, dove, scelto)
            runOnUiThread { if (!isDestroyed && numero == richiesta) mostra(elenco) }
        }.start()
    }

    override fun onDestroy() {
        Benzinai.libera()
        super.onDestroy()
    }

    override fun indietro() = finish()

    override fun ordina(ordine: Ordine) {
        this.ordine = ordine
        carica()
    }
}
