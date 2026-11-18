package app.australdashboard.aggiornamenti

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import app.australdashboard.Misure
import app.australdashboard.Stato

class EsitoInstallazioneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val stato = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val conferma = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
                chiediConferma(context, conferma.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS -> Stato.salvaEsito(context, "Aggiornamento installato")
            else -> {
                val messaggio = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "codice $stato"
                Stato.salvaEsito(context, "Installazione non riuscita: $messaggio")
            }
        }
    }

    private fun chiediConferma(context: Context, conferma: Intent) {
        if (Misure.schermateVisibili > 0) {
            context.startActivity(conferma)
            return
        }
        val notifiche = context.getSystemService(NotificationManager::class.java)
        notifiche.createNotificationChannel(
            NotificationChannel(CANALE, "Aggiornamenti", NotificationManager.IMPORTANCE_HIGH)
        )
        val apri = PendingIntent.getActivity(
            context, 0, conferma, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notifica = Notification.Builder(context, CANALE)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Aggiornamento pronto")
            .setContentText("Tocca per installare la nuova versione di Austral Dashboard")
            .setContentIntent(apri)
            .setAutoCancel(true)
            .build()
        notifiche.notify(1, notifica)
    }

    private companion object {
        const val CANALE = "aggiornamenti"
    }
}
