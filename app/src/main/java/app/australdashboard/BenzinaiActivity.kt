package app.australdashboard

import android.content.Intent
import android.os.Bundle
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Ordine
import app.australdashboard.funzioni.Posizione
import app.australdashboard.funzioni.Rifornimenti
import app.australdashboard.temi.AzioniBenzinai
import app.australdashboard.temi.Temi
import app.australdashboard.temi.VistaBenzinai
import java.util.Calendar

class BenzinaiActivity : Schermata(), AzioniBenzinai {
    private lateinit var vista: VistaBenzinai
    private var ordine = Ordine.CONVIENE
    private var luogo: Luogo? = null
    private var richiesta = 0
    private var intornoAMe = false

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
        val soloVicini = intornoAMe
        Thread {
            val elenco = Benzinai.elenco(applicationContext, dove, scelto, soloVicini)
            runOnUiThread { if (!isDestroyed && numero == richiesta) mostra(elenco) }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        val adesso = Calendar.getInstance()
        vista.mostraSpesa(Rifornimenti.spesaDelMese(Rifornimenti.elenco(this), adesso.get(Calendar.YEAR), adesso.get(Calendar.MONTH) + 1))
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

    override fun rifornimenti() = rifornimenti(null)

    override fun rifornimenti(selezionato: Distributore?) {
        val intent = Intent(this, RifornimentiActivity::class.java)
        if (selezionato != null) {
            intent.putExtra("prezzo", selezionato.prezzo).putExtra("distributore", selezionato.nome.ifBlank { selezionato.marchio })
        }
        startActivity(intent)
    }

    override fun intornoAMe() {
        intornoAMe = !intornoAMe
        if (!intornoAMe) {
            carica()
            return
        }
        mostra(ElencoBenzinai.Caricamento)
        Posizione.aggiorna(this) { fresco ->
            if (isDestroyed) return@aggiorna
            if (fresco != null) luogo = fresco
            if (luogo == null) mostra(ElencoBenzinai.SenzaPosizione) else carica()
        }
    }

    override fun raggio() {
        Benzinai.prossimoRaggio(this)
        carica()
    }

    override fun mappa() = startActivity(Intent(this, MappaActivity::class.java).putExtra("intornoAMe", intornoAMe))
}
