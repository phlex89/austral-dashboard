package app.australdashboard

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import app.australdashboard.temi.AppHome

class AppHomeActivity : Schermata() {
    private lateinit var contenuto: LinearLayout
    private var installate: List<Pair<String, String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contenuto = colonna()
        setContentView(scorrevole(contenuto))
        mostraPosizioni()
    }

    private fun mostraPosizioni() {
        contenuto.removeAllViews()
        contenuto.addView(testo("App della Home", 40f))
        AppHome.elenco(this).forEachIndexed { indice, app ->
            contenuto.addView(pulsante("${indice + 1} · ${app.nome}") { scegli(indice) })
        }
    }

    private fun scegli(indice: Int) {
        contenuto.removeAllViews()
        contenuto.addView(testo("Posizione ${indice + 1}", 40f))
        val note = testo("Caricamento…")
        contenuto.addView(note)
        val pronte = installate
        if (pronte != null) {
            note.text = ""
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
                if (isDestroyed) return@runOnUiThread
                installate = app
                contenuto.removeView(note)
                mostraApp(indice, app)
            }
        }.start()
    }

    private fun mostraApp(indice: Int, app: List<Pair<String, String>>) {
        app.forEach { (nome, pacchetto) ->
            contenuto.addView(pulsante(nome) {
                AppHome.scegli(this, indice, pacchetto)
                mostraPosizioni()
            })
        }
    }
}
