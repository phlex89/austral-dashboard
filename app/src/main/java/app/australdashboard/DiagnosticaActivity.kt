package app.australdashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Debug
import android.view.KeyEvent
import android.widget.TextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiagnosticaActivity : Schermata() {
    private lateinit var dati: TextView
    private lateinit var audio: TextView
    private lateinit var registro: TextView
    private val eventi = ArrayDeque<String>()
    private val ora = SimpleDateFormat("HH:mm:ss", Locale.ITALY)

    private val volumeCambiato = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val canale = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
            val prima = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)
            val dopo = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
            annota("${nomeCanale(canale)}: $prima → $dopo")
            mostraAudio()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Diagnostica", 40f))
        dati = testo("")
        radice.addView(dati)
        radice.addView(pulsante("Aggiorna") { mostra() })
        radice.addView(testo("Audio", 32f))
        audio = testo("")
        radice.addView(audio)
        radice.addView(pulsante("Prova A: musica −") { prova("A") { it.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0) } })
        radice.addView(pulsante("Prova B: volume suggerito −") {
            prova("B") { it.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, AudioManager.USE_DEFAULT_STREAM_TYPE, AudioManager.FLAG_SHOW_UI) }
        })
        radice.addView(pulsante("Prova C: tasto volume −") {
            prova("C") {
                it.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN))
                it.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN))
            }
        })
        radice.addView(testo("Cambi di volume mentre questa schermata è aperta:"))
        registro = testo("nessuno")
        radice.addView(registro)
        setContentView(scorrevole(radice))
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(volumeCambiato, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        mostra()
    }

    override fun onPause() {
        unregisterReceiver(volumeCambiato)
        super.onPause()
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
        mostraAudio()
    }

    private fun mostraAudio() {
        val am = getSystemService(AudioManager::class.java)
        val canali = listOf(
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_VOICE_CALL,
        ).joinToString("\n") { "${nomeCanale(it)}: ${am.getStreamVolume(it)} / ${am.getStreamMaxVolume(it)}" }
        val uscite = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .joinToString(", ") { "${nomeUscita(it.type)} (${it.productName})" }.ifEmpty { "nessuna" }
        val inRiproduzione = am.activePlaybackConfigurations
            .joinToString(", ") { "uso ${it.audioAttributes.usage}, canale ${nomeCanale(it.audioAttributes.volumeControlStream)}" }
            .ifEmpty { "niente" }
        audio.text = listOf(
            "Volume fisso: ${if (am.isVolumeFixed) "sì" else "no"}",
            canali,
            "Uscite: $uscite",
            "In riproduzione: $inRiproduzione",
        ).joinToString("\n")
    }

    private fun prova(nome: String, azione: (AudioManager) -> Unit) {
        val am = getSystemService(AudioManager::class.java)
        val prima = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val esito = runCatching { azione(am) }.exceptionOrNull()?.let { " errore: ${it.message}" } ?: ""
        annota("Prova $nome: musica $prima → ${am.getStreamVolume(AudioManager.STREAM_MUSIC)}$esito")
        mostraAudio()
    }

    private fun annota(riga: String) {
        eventi.addFirst("${ora.format(Date())}  $riga")
        while (eventi.size > 12) eventi.removeLast()
        registro.text = eventi.joinToString("\n")
    }

    private fun nomeCanale(canale: Int): String = when (canale) {
        AudioManager.STREAM_MUSIC -> "musica"
        AudioManager.STREAM_SYSTEM -> "sistema"
        AudioManager.STREAM_RING -> "suoneria"
        AudioManager.STREAM_NOTIFICATION -> "notifiche"
        AudioManager.STREAM_ALARM -> "sveglia"
        AudioManager.STREAM_VOICE_CALL -> "chiamata"
        else -> "canale $canale"
    }

    private fun nomeUscita(tipo: Int): String = when (tipo) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "altoparlante"
        AudioDeviceInfo.TYPE_BUS -> "bus auto"
        AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_ACCESSORY, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
        AudioDeviceInfo.TYPE_REMOTE_SUBMIX -> "submix remoto"
        AudioDeviceInfo.TYPE_TELEPHONY -> "telefonia"
        else -> "tipo $tipo"
    }
}
