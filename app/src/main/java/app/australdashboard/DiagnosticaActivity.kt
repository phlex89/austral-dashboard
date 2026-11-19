package app.australdashboard

import android.os.Bundle
import android.os.Debug
import android.widget.TextView
import java.io.File

class DiagnosticaActivity : Schermata() {
    private lateinit var dati: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Diagnostica", 40f))
        dati = testo("")
        radice.addView(dati)
        radice.addView(pulsante("Aggiorna") { mostra() })
        setContentView(scorrevole(radice))
    }

    override fun onResume() {
        super.onResume()
        mostra()
    }

    private fun mostra() {
        val info = packageManager.getPackageInfo(packageName, 0)
        val memoria = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        val apkKb = File(applicationInfo.sourceDir).length() / 1024
        dati.text = listOf(
            "Versione: ${info.versionName} (${info.longVersionCode})",
            "APK: $apkKb KB",
            "Memoria (PSS): ${memoria.totalPss / 1024} MB",
            "Avvio processo → primo frame: ${Misure.processoPrimoFrameMs?.let { "$it ms" } ?: "—"}",
            "Creazione Home → primo frame: ${Misure.homePrimoFrameMs?.let { "$it ms" } ?: "—"}",
            "Frame della Home: ${Misure.frameTotali}, in ritardo: ${Misure.framePersi}",
            "Aggiornamenti: ${Stato.esito(this)}",
            "Ultimo crash: ${Stato.crash(this)}",
        ).joinToString("\n")
    }
}
