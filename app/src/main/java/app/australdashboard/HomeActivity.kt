package app.australdashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.view.Choreographer
import android.view.FrameMetrics
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.TextClock
import app.australdashboard.aggiornamenti.AggiornamentoJob

class HomeActivity : Schermata() {
    private val frameListener = Window.OnFrameMetricsAvailableListener { _, metriche, _ ->
        if (metriche.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0L) {
            Misure.frameTotali++
            if (metriche.getMetric(FrameMetrics.TOTAL_DURATION) > metriche.getMetric(FrameMetrics.DEADLINE)) {
                Misure.framePersi++
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val creazione = SystemClock.uptimeMillis()
        super.onCreate(savedInstanceState)

        val radice = colonna().apply { gravity = Gravity.CENTER_HORIZONTAL }
        radice.addView(TextClock(this).apply {
            format12Hour = "HH:mm"
            format24Hour = "HH:mm"
            textSize = 160f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        radice.addView(testo("Austral Dashboard ${versione()}", 28f).apply { gravity = Gravity.CENTER })
        radice.addView(pulsante("Impostazioni") {
            startActivity(Intent(this, ImpostazioniActivity::class.java))
        })
        setContentView(radice)

        if (Misure.homePrimoFrameMs == null) misuraPrimoFrame(creazione)
        AggiornamentoJob.programma(this)
    }

    override fun onResume() {
        super.onResume()
        window.addOnFrameMetricsAvailableListener(frameListener, Handler(Looper.getMainLooper()))
    }

    override fun onPause() {
        window.removeOnFrameMetricsAvailableListener(frameListener)
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = Unit

    private fun misuraPrimoFrame(creazione: Long) {
        val osservatore = window.decorView.viewTreeObserver
        osservatore.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                window.decorView.viewTreeObserver.removeOnPreDrawListener(this)
                Choreographer.getInstance().postFrameCallback {
                    val ora = SystemClock.uptimeMillis()
                    Misure.processoPrimoFrameMs = ora - Process.getStartUptimeMillis()
                    Misure.homePrimoFrameMs = ora - creazione
                }
                return true
            }
        })
    }

    private fun versione(): String = packageManager.getPackageInfo(packageName, 0).versionName
}
