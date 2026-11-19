package app.australdashboard

import android.graphics.Color
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
        val radice = colonna()
        radice.addView(testo("Comuni preferiti", 40f))
        scelti = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(scelti)
        campo = EditText(this).apply {
            textSize = 28f
            setTextColor(Color.WHITE)
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, azione, _ ->
                if (azione == EditorInfo.IME_ACTION_SEARCH) cerca()
                azione == EditorInfo.IME_ACTION_SEARCH
            }
        }
        radice.addView(campo)
        radice.addView(pulsante("Cerca") { cerca() })
        messaggio = testo("")
        radice.addView(messaggio)
        risultati = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(risultati)
        setContentView(scorrevole(radice))
        mostraScelti()
    }

    override fun onDestroy() {
        Benzinai.libera()
        super.onDestroy()
    }

    private fun mostraScelti() {
        scelti.removeAllViews()
        val comuni = Benzinai.preferiti(this)
        if (comuni.isEmpty()) scelti.addView(testo("Nessun comune scelto"))
        comuni.forEach { comune ->
            scelti.addView(pulsante("Togli $comune") {
                Benzinai.salvaPreferiti(this, comuni - comune)
                mostraScelti()
            })
        }
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
        trovati.forEach { comune ->
            risultati.addView(pulsante(comune) {
                val attuali = Benzinai.preferiti(this)
                if (attuali.size < MASSIMO && comune !in attuali) Benzinai.salvaPreferiti(this, attuali + comune)
                risultati.removeAllViews()
                messaggio.text = ""
                mostraScelti()
            })
        }
    }

    private companion object {
        const val MASSIMO = 3
    }
}
