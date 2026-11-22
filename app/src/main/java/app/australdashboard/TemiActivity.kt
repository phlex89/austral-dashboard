package app.australdashboard

import android.os.Bundle
import app.australdashboard.temi.Temi

class TemiActivity : Schermata() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val corrente = Temi.scelto(this)
        val radice = pagina("Tema")
        val elenco = scheda()
        Temi.elenco.forEachIndexed { i, tema ->
            val valore = if (tema.id == corrente.id) "In uso · ${tema.idea}" else tema.idea
            elenco.addView(riga("${i + 1} · ${tema.nome}", valore, freccia = false) {
                Temi.scegli(this, tema)
                finish()
            }.view)
        }
        radice.addView(elenco)
    }
}
