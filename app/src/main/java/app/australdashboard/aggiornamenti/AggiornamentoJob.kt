package app.australdashboard.aggiornamenti

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context

class AggiornamentoJob : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        Thread {
            val esito = Aggiornatore.controlla(applicationContext)
            jobFinished(params, params.jobId == ID_AVVIO && esito == Aggiornatore.Esito.ERRORE)
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = params.jobId == ID_AVVIO

    companion object {
        private const val ID_AVVIO = 1
        private const val ID_PERIODICO = 2
        private const val ATTESA_DOPO_AVVIO_MS = 60_000L
        private const val PERIODO_MS = 6 * 60 * 60 * 1000L
        private const val FLESSIBILITA_MS = 60 * 60 * 1000L

        fun programma(context: Context) {
            val scheduler = context.getSystemService(JobScheduler::class.java)
            val componente = ComponentName(context, AggiornamentoJob::class.java)
            if (scheduler.getPendingJob(ID_AVVIO) == null) {
                scheduler.schedule(
                    JobInfo.Builder(ID_AVVIO, componente)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setMinimumLatency(ATTESA_DOPO_AVVIO_MS)
                        .build()
                )
            }
            if (scheduler.getPendingJob(ID_PERIODICO) == null) {
                scheduler.schedule(
                    JobInfo.Builder(ID_PERIODICO, componente)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(PERIODO_MS, FLESSIBILITA_MS)
                        .build()
                )
            }
        }
    }
}
