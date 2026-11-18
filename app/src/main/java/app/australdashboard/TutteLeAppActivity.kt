package app.australdashboard

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import app.australdashboard.temi.Temi
import app.australdashboard.temi.Ui
import app.australdashboard.temi.Ui.ADATTA
import app.australdashboard.temi.Ui.TUTTO
import app.australdashboard.temi.Ui.lp

class TutteLeAppActivity : Schermata() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val p = Temi.scelto(this).palette(this)
        val onda = (p.accento and 0x00FFFFFF) or 0x55000000

        val radice = Ui.colonna(this, 40).apply {
            setBackgroundColor(p.sfondo)
            setPadding(20, 28, 20, 20)
        }
        val intestazione = Ui.riga(this, 24)
        val indietro = Ui.riga(this, 10).apply {
            background = Ui.bordo(p.accento, 2)
            setPadding(24, 0, 24, 0)
            addView(Ui.icona(this@TutteLeAppActivity, R.drawable.ic_indietro, p.accento), lp(44, 44))
            addView(Ui.testo(this@TutteLeAppActivity, 44, p.accento, p.titoli, 40, "Indietro"))
        }
        intestazione.addView(Ui.premibile(indietro, onda, "Indietro") { finish() }, lp(ADATTA, TUTTO))
        intestazione.addView(Ui.testo(this, 76, p.testo, p.titoli, 70, "Tutte le app"), lp(0, ADATTA, 1f))
        radice.addView(intestazione, lp(TUTTO, 120))

        val griglia = Ui.colonna(this, 20)
        radice.addView(ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            addView(griglia)
        }, lp(TUTTO, 0, 1f))
        setContentView(radice)

        Thread {
            val pm = packageManager
            val app = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                .filter { it.activityInfo.packageName != packageName }
                .map { Triple(it, it.loadLabel(pm).toString(), it.loadIcon(pm)) }
                .sortedBy { it.second.lowercase() }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                app.chunked(COLONNE).forEach { gruppo ->
                    val riga = Ui.riga(this, 20, Gravity.NO_GRAVITY)
                    gruppo.forEach { (info, nome, icona) -> riga.addView(cella(info, nome, icona, p.testo, p.bordo, p.corpo, onda), lp(0, 220, 1f)) }
                    repeat(COLONNE - gruppo.size) { riga.addView(View(this), lp(0, 220, 1f)) }
                    griglia.addView(riga, lp(TUTTO, ADATTA))
                }
            }
        }.start()
    }

    private fun cella(
        info: ResolveInfo,
        nome: String,
        icona: Drawable,
        testo: Int,
        bordo: Int,
        font: android.graphics.Typeface,
        onda: Int,
    ): View {
        val cella = Ui.colonna(this, 12, Gravity.CENTER).apply {
            background = Ui.bordo(bordo, 2)
            setPadding(8, 0, 8, 0)
            addView(ImageView(this@TutteLeAppActivity).apply { setImageDrawable(icona) }, lp(110, 110))
            addView(Ui.testo(this@TutteLeAppActivity, 28, testo, font, 32, nome).apply { gravity = Gravity.CENTER }, lp(TUTTO, ADATTA))
        }
        return Ui.premibile(cella, onda, nome) {
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
            )
        }
    }

    private companion object {
        const val COLONNE = 5
    }
}
