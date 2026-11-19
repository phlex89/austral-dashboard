package app.australdashboard

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.australdashboard.aggiornamenti.EsitoInstallazioneReceiver

abstract class Schermata : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (gestoreInstallato) return
        gestoreInstallato = true
        val contesto = applicationContext
        val precedente = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { filo, errore ->
            runCatching {
                val versione = contesto.packageManager.getPackageInfo(contesto.packageName, 0).versionName
                Stato.salvaCrash(contesto, versione, Log.getStackTraceString(errore))
            }
            precedente?.uncaughtException(filo, errore)
        }
    }

    override fun onStart() {
        super.onStart()
        Misure.schermateVisibili++
        EsitoInstallazioneReceiver.riprendi(this)
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

    protected fun scorrevole(contenuto: LinearLayout): ScrollView = ScrollView(this).apply {
        setBackgroundColor(Color.BLACK)
        isFillViewport = true
        addView(contenuto)
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

    private companion object {
        var gestoreInstallato = false
    }
}
