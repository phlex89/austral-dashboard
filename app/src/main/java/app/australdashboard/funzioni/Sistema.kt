package app.australdashboard.funzioni

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings

enum class StatoWifi { SPENTO, ACCESO, CONNESSO }

data class StatoSistema(val wifi: StatoWifi, val bluetooth: Boolean, val volume: Int, val volumeMassimo: Int)

object Sistema {
    @Suppress("DEPRECATION")
    fun stato(context: Context): StatoSistema {
        val app = context.applicationContext
        val wifi = app.getSystemService(WifiManager::class.java)
        val rete = app.getSystemService(ConnectivityManager::class.java)
        val capacita = rete.getNetworkCapabilities(rete.activeNetwork)
        val statoWifi = when {
            capacita?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> StatoWifi.CONNESSO
            wifi.isWifiEnabled -> StatoWifi.ACCESO
            else -> StatoWifi.SPENTO
        }
        val bluetooth = app.getSystemService(BluetoothManager::class.java).adapter?.isEnabled == true
        val audio = app.getSystemService(AudioManager::class.java)
        return StatoSistema(
            statoWifi,
            bluetooth,
            audio.getStreamVolume(AudioManager.STREAM_MUSIC),
            audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        )
    }

    fun apriWifi(context: Context) {
        try {
            context.startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun apriBluetooth(context: Context) {
        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun volume(context: Context, su: Boolean) {
        context.getSystemService(AudioManager::class.java).adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (su) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            0,
        )
    }

    fun osserva(context: Context, cambiato: () -> Unit): () -> Unit {
        val app = context.applicationContext
        val ricevitore = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = cambiato()
        }
        app.registerReceiver(
            ricevitore,
            IntentFilter().apply {
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction("android.media.VOLUME_CHANGED_ACTION")
            },
        )
        val rete = app.getSystemService(ConnectivityManager::class.java)
        val richiamo = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = cambiato()
            override fun onLost(network: Network) = cambiato()
            override fun onCapabilitiesChanged(network: Network, capacita: NetworkCapabilities) = cambiato()
        }
        rete.registerDefaultNetworkCallback(richiamo)
        return {
            runCatching { app.unregisterReceiver(ricevitore) }
            runCatching { rete.unregisterNetworkCallback(richiamo) }
        }
    }
}
