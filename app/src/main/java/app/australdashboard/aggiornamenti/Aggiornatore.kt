package app.australdashboard.aggiornamenti

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import app.australdashboard.Stato
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

object Aggiornatore {
    private const val INDIRIZZO =
        "https://github.com/phlex89/austral-dashboard/releases/latest/download/versione.json"
    private const val MASSIMO_APK = 50L * 1024 * 1024
    private val inCorso = AtomicBoolean(false)

    enum class Esito { OCCUPATO, FATTO, ERRORE }

    fun controlla(context: Context): Esito {
        if (!inCorso.compareAndSet(false, true)) return Esito.OCCUPATO
        try {
            Stato.salvaEsito(context, esegui(context))
            return Esito.FATTO
        } catch (e: Exception) {
            Stato.salvaEsito(context, "Errore: ${e.message ?: e.javaClass.simpleName}")
            return Esito.ERRORE
        } finally {
            inCorso.set(false)
        }
    }

    private fun esegui(context: Context): String {
        val info = InfoVersione.daJson(apri(INDIRIZZO) { it.bufferedReader().readText() })
        val installata = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        if (!info.piuRecenteDi(installata)) return "Nessun aggiornamento: ${info.versionName} è già installata"

        val apk = File(context.cacheDir, "aggiornamento.apk")
        try {
            apri(info.apk) { input -> apk.outputStream().use { copia(input, it) } }
            if (!Impronta.corrisponde(apk, info.sha256)) {
                return "Aggiornamento ${info.versionName} scartato: impronta SHA-256 diversa"
            }
            installa(context, apk)
        } finally {
            apk.delete()
        }
        return "Installazione di ${info.versionName} avviata"
    }

    private fun copia(ingresso: InputStream, uscita: OutputStream) {
        val buffer = ByteArray(64 * 1024)
        var totale = 0L
        while (true) {
            val letti = ingresso.read(buffer)
            if (letti < 0) return
            totale += letti
            if (totale > MASSIMO_APK) error("APK più grande di ${MASSIMO_APK / (1024 * 1024)} MB")
            uscita.write(buffer, 0, letti)
        }
    }

    private fun <T> apri(indirizzo: String, leggi: (InputStream) -> T): T {
        val connessione = URL(indirizzo).openConnection() as HttpURLConnection
        connessione.connectTimeout = 15_000
        connessione.readTimeout = 30_000
        try {
            val codice = connessione.responseCode
            if (codice != HttpURLConnection.HTTP_OK) error("HTTP $codice da $indirizzo")
            return connessione.inputStream.use(leggi)
        } finally {
            connessione.disconnect()
        }
    }

    private fun installa(context: Context, apk: File) {
        val installer = context.packageManager.packageInstaller
        val parametri = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val id = installer.createSession(parametri)
        installer.openSession(id).use { sessione ->
            sessione.openWrite("austral-dashboard.apk", 0, apk.length()).use { uscita ->
                apk.inputStream().use { it.copyTo(uscita) }
                sessione.fsync(uscita)
            }
            val esito = PendingIntent.getBroadcast(
                context,
                id,
                Intent(context, EsitoInstallazioneReceiver::class.java),
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            sessione.commit(esito.intentSender)
        }
    }
}
