package app.australdashboard

import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.Benzinai

class ComuniActivity : Schermata() {
    private lateinit var campo: EditText
    private lateinit var scelti: LinearLayout
    private lateinit var messaggio: TextView
    private lateinit var risultati: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = pagina("Comuni preferiti")
        scelti = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(scelti)
        radice.addView(sezione("Aggiungi un comune"))
        campo = campo("Nome del comune", InputType.TYPE_CLASS_TEXT).apply {
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, azione, _ ->
                if (azione == EditorInfo.IME_ACTION_SEARCH) cerca()
                azione == EditorInfo.IME_ACTION_SEARCH
            }
        }
        radice.addView(campo)
        radice.addView(pulsante("Cerca") { cerca() })
        messaggio = secondario("")
        radice.addView(messaggio)
        risultati = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(risultati)
        mostraScelti()
    }

    override fun onDestroy() {
        Benzinai.libera()
        super.onDestroy()
    }

    private fun mostraScelti() {
        scelti.removeAllViews()
        val comuni = Benzinai.preferiti(this)
        scelti.addView(sezione("Scelti"))
        if (comuni.isEmpty()) {
            scelti.addView(secondario("Nessun comune scelto"))
            return
        }
        val elenco = scheda()
        comuni.forEach { comune ->
            elenco.addView(rigaTogli(comune) {
                Benzinai.salvaPreferiti(this, comuni - comune)
                mostraScelti()
            }.view)
        }
        scelti.addView(elenco)
    }

    private fun cerca() {
        val nome = campo.text.toString().trim()
        if (nome.isEmpty()) return
        risultati.removeAllViews()
        if (Benzinai.preferiti(this).size >= MASSIMO) {
            messaggio.text = "Hai già tre comuni: togline uno per aggiungerne altri"
            return
        }
        messaggio.text = "Ricerca…"
        Thread {
            val trovati = Benzinai.comuni(applicationContext, nome)
            val scaricato = Benzinai.disponibile(applicationContext)
            runOnUiThread { if (!isDestroyed) mostra(trovati, scaricato) }
        }.start()
    }

    private fun mostra(trovati: List<String>, scaricato: Boolean) {
        risultati.removeAllViews()
        messaggio.text = when {
            !scaricato -> "Apri una volta Benzinai per scaricare l'elenco dei comuni"
            trovati.isEmpty() -> "Nessun comune trovato"
            else -> ""
        }
        if (trovati.isEmpty()) return
        val elenco = scheda()
        trovati.forEach { comune ->
            elenco.addView(riga(comune, freccia = false) {
                val attuali = Benzinai.preferiti(this)
                if (attuali.size < MASSIMO && comune !in attuali) Benzinai.salvaPreferiti(this, attuali + comune)
                risultati.removeAllViews()
                messaggio.text = ""
                mostraScelti()
            }.view)
        }
        risultati.addView(elenco)
    }

    private companion object {
        const val MASSIMO = 3
    }
}
