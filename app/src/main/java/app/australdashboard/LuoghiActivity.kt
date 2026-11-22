package app.australdashboard

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.Citta
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Luoghi
import app.australdashboard.funzioni.Posizione

class LuoghiActivity : Schermata() {
    private val principale = Handler(Looper.getMainLooper())
    private lateinit var elenco: LinearLayout
    private lateinit var messaggio: TextView
    private lateinit var risultati: LinearLayout
    private var daTogliere: String? = null
    private val annullaTogli = Runnable {
        daTogliere = null
        mostraElenco()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contenuto = pagina("Luogo del meteo")
        elenco = scheda()
        contenuto.addView(elenco)
        contenuto.addView(secondario("Il primo luogo è la riserva quando manca il GPS"))
        contenuto.addView(pulsante("Aggiungi dove sono") { aggiungiDoveSono() })
        messaggio = secondario("")
        contenuto.addView(messaggio)
        contenuto.addView(sezione("Cerca un luogo"))
        val ricerca = campo("Nome del luogo")
        ricerca.imeOptions = EditorInfo.IME_ACTION_SEARCH
        ricerca.setOnEditorActionListener { _, azione, _ ->
            if (azione == EditorInfo.IME_ACTION_SEARCH) cerca(ricerca.text.toString().trim())
            azione == EditorInfo.IME_ACTION_SEARCH
        }
        contenuto.addView(ricerca)
        contenuto.addView(pulsante("Cerca", secondario = true) { cerca(ricerca.text.toString().trim()) })
        risultati = scheda()
        contenuto.addView(risultati)
        mostraElenco()
    }

    override fun onDestroy() {
        principale.removeCallbacks(annullaTogli)
        super.onDestroy()
    }

    private fun mostraElenco() {
        elenco.removeAllViews()
        val attivo = Luoghi.attivo(this)?.first
        elenco.addView(
            riga(Luoghi.NOME_GPS, if (attivo == null) "✓ In uso" else null, freccia = false) {
                Luoghi.scegli(this, null)
                finish()
            }.view,
        )
        Luoghi.elenco(this).forEach { (nome, _) ->
            val voce = riga(nome, if (nome == attivo) "✓ In uso" else null, freccia = false) {
                Luoghi.scegli(this, nome)
                finish()
            }.view
            voce.addView(togli(nome))
            elenco.addView(voce)
        }
    }

    private fun togli(nome: String): TextView = TextView(this).apply {
        val confermare = daTogliere == nome
        text = if (confermare) "Tocca ancora per togliere" else "Togli"
        textSize = 20f
        gravity = Gravity.CENTER
        setTextColor(if (confermare) Color.WHITE else accento)
        minHeight = dp(64)
        setPadding(dp(20), dp(8), dp(8), dp(8))
        setOnClickListener {
            principale.removeCallbacks(annullaTogli)
            if (daTogliere == nome) {
                daTogliere = null
                Luoghi.togli(this@LuoghiActivity, nome)
                mostraElenco()
            } else {
                daTogliere = nome
                mostraElenco()
                principale.postDelayed(annullaTogli, 3000)
            }
        }
    }

    private fun aggiungiDoveSono() {
        if (!Posizione.permessoConcesso(this)) {
            messaggio.text = "Permesso di posizione mancante"
            return
        }
        messaggio.text = "Cerco la posizione…"
        Posizione.aggiorna(this, ripiego = false) { luogo ->
            if (luogo == null) {
                messaggio.text = "Posizione non disponibile"
                return@aggiorna
            }
            messaggio.text = "Cerco il nome del luogo…"
            Thread {
                val nome = Luoghi.nomeDi(applicationContext, luogo).takeIf { it != Luoghi.NOME_SCONOSCIUTO }
                    ?: String.format(java.util.Locale.ITALY, "%.3f, %.3f", luogo.lat, luogo.lon)
                runOnUiThread { if (!isDestroyed) aggiungi(nome, luogo) }
            }.start()
        }
    }

    private fun aggiungi(nome: String, luogo: Luogo) {
        Luoghi.aggiungi(this, nome, luogo)
        Luoghi.scegli(this, nome)
        messaggio.text = "Aggiunto: $nome"
        risultati.removeAllViews()
        mostraElenco()
    }

    private fun cerca(nome: String) {
        if (nome.isEmpty()) return
        risultati.removeAllViews()
        messaggio.text = "Ricerca…"
        Thread {
            val trovate = try {
                Citta.cerca(nome)
            } catch (e: Exception) {
                null
            }
            runOnUiThread { if (!isDestroyed) mostra(trovate) }
        }.start()
    }

    private fun mostra(trovate: List<Pair<String, Luogo>>?) {
        risultati.removeAllViews()
        messaggio.text = when {
            trovate == null -> "Ricerca non riuscita"
            trovate.isEmpty() -> "Nessun luogo trovato"
            else -> ""
        }
        trovate?.forEach { (etichetta, luogo) ->
            risultati.addView(riga(etichetta, "Aggiungi e usa") { aggiungi(etichetta, luogo) }.view)
        }
    }
}
