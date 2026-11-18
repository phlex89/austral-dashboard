package app.australdashboard

import android.app.Activity
import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

abstract class Schermata : Activity() {
    override fun onStart() {
        super.onStart()
        Misure.schermateVisibili++
    }

    override fun onStop() {
        Misure.schermateVisibili--
        super.onStop()
    }

    protected fun dp(valore: Int): Int = (valore * resources.displayMetrics.density).toInt()

    protected fun colonna(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.BLACK)
        setPadding(dp(32), dp(32), dp(32), dp(32))
    }

    protected fun testo(contenuto: String, dimensione: Float = 24f): TextView = TextView(this).apply {
        text = contenuto
        textSize = dimensione
        setTextColor(Color.WHITE)
        setPadding(0, dp(8), 0, dp(8))
    }

    protected fun pulsante(etichetta: String, azione: () -> Unit): Button = Button(this).apply {
        text = etichetta
        textSize = 28f
        isAllCaps = false
        minHeight = dp(96)
        setOnClickListener { azione() }
    }
}
