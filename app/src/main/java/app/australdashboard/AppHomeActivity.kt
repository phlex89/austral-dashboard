package app.australdashboard

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import app.australdashboard.temi.AppHome

class AppHomeActivity : Schermata() {
    private lateinit var contenuto: LinearLayout
    private var installate: List<Pair<String, String>>? = null
    private var posizione: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mostraPosizioni()
    }

    private fun mostraPosizioni() {
        posizione = null
        contenuto = pagina("App della Home")
        val elenco = scheda()
        AppHome.elenco(this).forEachIndexed { indice, app ->
            elenco.addView(riga("Posizione ${indice + 1}", app.nome) { scegli(indice) }.view)
        }
        contenuto.addView(elenco)
    }

    private fun scegli(indice: Int) {
        posizione = indice
        contenuto = pagina("Posizione ${indice + 1}")
        val note = secondario("Caricamento…")
        contenuto.addView(note)
        val pronte = installate
        if (pronte != null) {
            contenuto.removeView(note)
            mostraApp(indice, pronte)
            return
        }
        Thread {
            val pm = packageManager
            val app = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                .filter { it.activityInfo.packageName != packageName }
                .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
                .sortedBy { it.first.lowercase() }
            runOnUiThread {
                if (isDestroyed || posizione != indice) return@runOnUiThread
                installate = app
                contenuto.removeView(note)
                mostraApp(indice, app)
            }
        }.start()
    }

    private fun mostraApp(indice: Int, app: List<Pair<String, String>>) {
        val attuale = AppHome.elenco(this).getOrNull(indice)?.nome
        val elenco = scheda()
        app.forEach { (nome, pacchetto) ->
            elenco.addView(riga(nome, if (nome == attuale) "In uso" else null, freccia = false) {
                AppHome.scegli(this, indice, pacchetto)
                mostraPosizioni()
            }.view)
        }
        contenuto.addView(elenco)
    }
}
