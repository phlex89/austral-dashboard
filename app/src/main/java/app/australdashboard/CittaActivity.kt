package app.australdashboard

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.Citta
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Posizione

class CittaActivity : Schermata() {
    private lateinit var campo: EditText
    private lateinit var messaggio: TextView
    private lateinit var risultati: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Città di riserva", 40f))
        radice.addView(testo(Posizione.cittaDiRiserva(this)?.first ?: "Nessuna città scelta"))
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
    }

    private fun cerca() {
        val nome = campo.text.toString().trim()
        if (nome.isEmpty()) return
        risultati.removeAllViews()
        messaggio.text = "Ricerca…"
        Thread {
            val trovate = try {
                Citta.cerca(nome)
            } catch (e: Exception) {
                null
            }
            runOnUiThread { mostra(trovate) }
        }.start()
    }

    private fun mostra(trovate: List<Pair<String, Luogo>>?) {
        risultati.removeAllViews()
        messaggio.text = when {
            trovate == null -> "Ricerca non riuscita"
            trovate.isEmpty() -> "Nessuna città trovata"
            else -> ""
        }
        trovate?.forEach { (etichetta, luogo) ->
            risultati.addView(pulsante(etichetta) {
                Posizione.salvaCitta(this, etichetta, luogo)
                finish()
            })
        }
    }
}
