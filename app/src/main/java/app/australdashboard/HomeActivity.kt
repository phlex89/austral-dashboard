package app.australdashboard

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.view.Choreographer
import android.view.FrameMetrics
import android.view.ViewTreeObserver
import android.view.Window
import app.australdashboard.aggiornamenti.AggiornamentoJob
import app.australdashboard.funzioni.FonteMeteo
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Posizione
import app.australdashboard.temi.AppHome
import app.australdashboard.temi.AzioniHome
import app.australdashboard.temi.Temi
import app.australdashboard.temi.VistaHome
import java.util.Date

class HomeActivity : Schermata(), AzioniHome {
    private val principale = Handler(Looper.getMainLooper())
    private lateinit var vista: VistaHome
    private var temaCostruito = ""
    private var appCostruite = ""
    private var lettore: Lettore? = null

    private val frameListener = Window.OnFrameMetricsAvailableListener { _, metriche, _ ->
        if (metriche.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0L) {
            Misure.frameTotali++
            if (metriche.getMetric(FrameMetrics.TOTAL_DURATION) > metriche.getMetric(FrameMetrics.DEADLINE)) {
                Misure.framePersi++
            }
        }
    }

    private val orologio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = vista.mostraOra(Date())
    }

    private val controlloMeteo = object : Runnable {
        override fun run() {
            vista.mostraMeteo(FonteMeteo.ultimo(this@HomeActivity))
            aggiornaMeteo()
            principale.postDelayed(this, INTERVALLO_METEO_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val creazione = SystemClock.uptimeMillis()
        super.onCreate(savedInstanceState)
        AggiornamentoJob.programma(this)
        costruisci()
        if (Misure.homePrimoFrameMs == null) misuraPrimoFrame(creazione)
    }

    private fun costruisci() {
        val tema = Temi.scelto(this)
        temaCostruito = tema.id
        appCostruite = AppHome.firma(this)
        vista = tema.home(this, this)
        vista.mostraOra(Date())
        vista.mostraMeteo(FonteMeteo.ultimo(this))
        setContentView(vista.view)
    }

    override fun onStart() {
        super.onStart()
        if (Temi.scelto(this).id != temaCostruito || AppHome.firma(this) != appCostruite) costruisci()
        vista.mostraOra(Date())
        registerReceiver(orologio, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        lettore = Lettore(this) { vista.mostraMusica(it) }.also { it.avvia() }
        if (!Posizione.permessoConcesso(this) && !permessoChiesto) {
            permessoChiesto = true
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
        principale.post(controlloMeteo)
    }

    override fun onStop() {
        principale.removeCallbacks(controlloMeteo)
        lettore?.ferma()
        lettore = null
        unregisterReceiver(orologio)
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (Posizione.permessoConcesso(this)) aggiornaMeteo(forza = true)
    }

    private fun aggiornaMeteo(forza: Boolean = false) {
        if (!forza && !FonteMeteo.daAggiornare(this)) return
        Posizione.aggiorna(this) { luogo ->
            if (luogo == null) return@aggiorna
            Thread {
                val meteo = FonteMeteo.scarica(applicationContext, luogo) ?: return@Thread
                runOnUiThread { if (!isDestroyed) vista.mostraMeteo(meteo) }
            }.start()
        }
    }

    override fun onResume() {
        super.onResume()
        window.addOnFrameMetricsAvailableListener(frameListener, principale)
    }

    override fun onPause() {
        window.removeOnFrameMetricsAvailableListener(frameListener)
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() = Unit

    override fun benzinai() = startActivity(Intent(this, BenzinaiActivity::class.java))

    override fun tutteLeApp() = startActivity(Intent(this, TutteLeAppActivity::class.java))

    override fun impostazioniAndroid() = startActivity(Intent(Settings.ACTION_SETTINGS))

    override fun impostazioniLauncher() = startActivity(Intent(this, ImpostazioniActivity::class.java))

    override fun apri(app: AppHome) {
        app.intent(this)?.let { startActivity(it) }
    }

    override fun apriMusica() {
        lettore?.apriApp()
    }

    override fun precedente() {
        lettore?.precedente()
    }

    override fun playPausa() {
        lettore?.playPausa()
    }

    override fun successivo() {
        lettore?.successivo()
    }

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

    private companion object {
        const val INTERVALLO_METEO_MS = 5 * 60 * 1000L
        var permessoChiesto = false
    }
}
