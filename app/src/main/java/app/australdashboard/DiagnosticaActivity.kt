package app.australdashboard

import android.os.Bundle
import android.os.Debug
import android.widget.LinearLayout
import java.io.File

class DiagnosticaActivity : Schermata() {
    private lateinit var radice: LinearLayout
    private lateinit var dati: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        radice = pagina("Diagnostica")
        dati = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(dati)
        radice.addView(pulsante("Aggiorna") { mostra() }.apply {
            (layoutParams as LinearLayout.LayoutParams).topMargin = dp(24)
        })
    }

    override fun onResume() {
        super.onResume()
        mostra()
    }

    private fun mostra() {
        val info = packageManager.getPackageInfo(packageName, 0)
        val memoria = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        val apkKb = File(applicationInfo.sourceDir).length() / 1024
        dati.removeAllViews()
        dati.addView(sezione("App"))
        dati.addView(scheda(
            "Versione" to "${info.versionName} (${info.longVersionCode})",
            "APK" to "$apkKb KB",
            "Memoria (PSS)" to "${memoria.totalPss / 1024} MB",
        ))
        dati.addView(sezione("Prestazioni"))
        dati.addView(scheda(
            "Avvio processo → primo frame" to (Misure.processoPrimoFrameMs?.let { "$it ms" } ?: "—"),
            "Creazione Home → primo frame" to (Misure.homePrimoFrameMs?.let { "$it ms" } ?: "—"),
            "Frame della Home" to "${Misure.frameTotali}, in ritardo: ${Misure.framePersi}",
        ))
        dati.addView(sezione("Stato"))
        dati.addView(scheda(
            "Aggiornamenti" to Stato.esito(this),
            "Ultimo crash" to Stato.crash(this),
        ))
    }

    private fun scheda(vararg voci: Pair<String, String>): LinearLayout = scheda().apply {
        voci.forEach { (etichetta, valore) -> addView(riga(etichetta, valore, freccia = false).view) }
    }
}
