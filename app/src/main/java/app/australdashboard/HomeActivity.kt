package app.australdashboard

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Luoghi
import app.australdashboard.funzioni.Playlist
import app.australdashboard.funzioni.Playlists
import app.australdashboard.funzioni.Sistema
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
    private var smettiDiOsservare: (() -> Unit)? = null

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
        appCostruite = firma()
        vista = tema.home(this, this)
        vista.mostraOra(Date())
        vista.mostraMeteo(FonteMeteo.ultimo(this))
        vista.mostraSistema(Sistema.stato(this))
        setContentView(vista.view)
    }

    override fun onStart() {
        super.onStart()
        if (Temi.scelto(this).id != temaCostruito || firma() != appCostruite) costruisci()
        vista.mostraOra(Date())
        registerReceiver(orologio, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        lettore = Lettore(this) { vista.mostraMusica(it) }.also { it.avvia() }
        vista.mostraSistema(Sistema.stato(this))
        smettiDiOsservare = Sistema.osserva(this) { principale.post { vista.mostraSistema(Sistema.stato(this)) } }
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
        smettiDiOsservare?.invoke()
        smettiDiOsservare = null
        unregisterReceiver(orologio)
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Sistema.CODICE_BLUETOOTH) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) Sistema.alternaBluetooth(this)
            return
        }
        if (Posizione.permessoConcesso(this)) aggiornaMeteo(forza = true)
    }

    private fun aggiornaMeteo(forza: Boolean = false) {
        if (!forza && !FonteMeteo.daAggiornare(this)) return
        val fisso = Luoghi.attivo(this)
        if (fisso != null) {
            scaricaMeteo(fisso.second, fisso.first)
        } else {
            Posizione.aggiorna(this) { luogo -> if (luogo != null) scaricaMeteo(luogo, null) }
        }
    }

    private fun scaricaMeteo(luogo: Luogo, nome: String?) {
        Thread {
            val nomeLuogo = nome ?: Luoghi.nomeDi(applicationContext, luogo)
            val meteo = FonteMeteo.scarica(applicationContext, luogo, nomeLuogo) ?: return@Thread
            runOnUiThread { if (!isDestroyed) vista.mostraMeteo(meteo) }
        }.start()
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

    override fun testo() = startActivity(Intent(this, TestoActivity::class.java))

    override fun precedente() {
        lettore?.precedente()
    }

    override fun playPausa() {
        lettore?.playPausa()
    }

    override fun successivo() {
        lettore?.successivo()
    }

    override fun wifi() = Sistema.apriWifi(this)

    override fun bluetooth() = Sistema.alternaBluetooth(this)

    override fun wifiDettaglio() = Sistema.dettaglioWifi(this)

    override fun bluetoothDettaglio() = Sistema.apriBluetooth(this)

    override fun temaSuccessivo() {
        val elenco = Temi.elenco
        val attuale = elenco.indexOfFirst { it.id == Temi.scelto(this).id }
        Temi.scegli(this, elenco[(attuale + 1) % elenco.size])
        costruisci()
        vista.mostraMusica(lettore?.ultima)
    }


    override fun playlist(playlist: Playlist) = Playlists.apri(this, playlist)

    private fun firma() = AppHome.firma(this) + "|" + Playlists.aTesto(Playlists.elenco(this))

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
