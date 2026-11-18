package app.australdashboard

import android.os.Bundle
import app.australdashboard.temi.Temi

class TemiActivity : Schermata() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val corrente = Temi.scelto(this)
        val radice = colonna()
        radice.addView(testo("Tema", 40f))
        Temi.elenco.forEachIndexed { i, tema ->
            val segno = if (tema.id == corrente.id) "● " else ""
            radice.addView(pulsante("$segno${i + 1} · ${tema.nome}\n${tema.idea}") {
                Temi.scegli(this, tema)
                finish()
            })
        }
        setContentView(scorrevole(radice))
    }
}
