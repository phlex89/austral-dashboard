package app.australdashboard.aggiornamenti

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AvvioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) AggiornamentoJob.programma(context)
    }
}
