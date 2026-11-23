package app.australdashboard.funzioni

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.tan

object Mercatore {
    const val LATO = 256
    const val ZOOM_MIN = 11
    const val ZOOM_MAX = 17
    private const val LATITUDINE_MASSIMA = 85.05112878
    private const val CIRCONFERENZA_M = 40075016.686

    private fun lato(zoom: Int): Double = LATO * 2.0.pow(zoom)

    fun pixelX(lon: Double, zoom: Int): Double = (lon + 180.0) / 360.0 * lato(zoom)

    fun pixelY(lat: Double, zoom: Int): Double {
        val fi = Math.toRadians(lat.coerceIn(-LATITUDINE_MASSIMA, LATITUDINE_MASSIMA))
        return (1.0 - ln(tan(fi) + 1.0 / cos(fi)) / PI) / 2.0 * lato(zoom)
    }

    fun lon(pixelX: Double, zoom: Int): Double = pixelX / lato(zoom) * 360.0 - 180.0

    fun lat(pixelY: Double, zoom: Int): Double =
        Math.toDegrees(atan(sinh(PI * (1.0 - 2.0 * pixelY / lato(zoom)))))

    fun tessera(coordinata: Double, zoom: Int): Int =
        Math.floor(coordinata / LATO).toInt().coerceIn(0, (1 shl zoom) - 1)

    fun metriPerPixel(lat: Double, zoom: Int): Double =
        CIRCONFERENZA_M * cos(Math.toRadians(lat)) / lato(zoom)

    fun zoomPer(raggioKm: Double, lat: Double, latoPixel: Int): Int {
        for (zoom in ZOOM_MAX downTo ZOOM_MIN) {
            if (2 * raggioKm * 1000 / metriPerPixel(lat, zoom) <= latoPixel * 0.8) return zoom
        }
        return ZOOM_MIN
    }

    fun ruota(dx: Double, dy: Double, gradi: Double): Pair<Double, Double> {
        val a = Math.toRadians(gradi)
        return Pair(dx * cos(a) - dy * sin(a), dx * sin(a) + dy * cos(a))
    }
}

class ArchivioTessere(context: Context, private val nuova: () -> Unit) {
    private val cartella = File(context.cacheDir, "tessere")
    private val agente = "AustralDashboard/" + runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty()
    private val esecutore = Executors.newFixedThreadPool(2)
    private val inCorso = HashSet<String>()
    private val opzioni = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
    private val memoria = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt()) {
        override fun sizeOf(chiave: String, valore: Bitmap): Int = valore.byteCount
    }

    fun cerca(zoom: Int, x: Int, y: Int): Bitmap? {
        val chiave = "${zoom}_${x}_$y"
        memoria.get(chiave)?.let { return it }
        synchronized(inCorso) { if (!inCorso.add(chiave)) return null }
        try {
            esecutore.execute { carica(zoom, x, y, chiave) }
        } catch (e: Exception) {
            synchronized(inCorso) { inCorso.remove(chiave) }
        }
        return null
    }

    fun chiudi() {
        esecutore.shutdownNow()
    }

    private fun carica(zoom: Int, x: Int, y: Int, chiave: String) {
        try {
            cartella.mkdirs()
            val file = File(cartella, "$chiave.png")
            val fresco = file.exists() && System.currentTimeMillis() - file.lastModified() < SETTE_GIORNI_MS
            if (!fresco) scarica(zoom, x, y, file)
            val bitmap = file.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path, opzioni) }
            if (bitmap != null) {
                memoria.put(chiave, bitmap)
                nuova()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
        } finally {
            synchronized(inCorso) { inCorso.remove(chiave) }
        }
    }

    private fun scarica(zoom: Int, x: Int, y: Int, file: File) {
        val temporaneo = File(cartella, "${file.name}.tmp")
        try {
            val connessione = URL("https://tile.openstreetmap.org/$zoom/$x/$y.png").openConnection() as HttpURLConnection
            connessione.connectTimeout = 10_000
            connessione.readTimeout = 15_000
            connessione.setRequestProperty("User-Agent", agente)
            try {
                if (connessione.responseCode != HttpURLConnection.HTTP_OK) return
                connessione.inputStream.use { input -> temporaneo.outputStream().use { input.copyTo(it) } }
            } finally {
                connessione.disconnect()
            }
            if (temporaneo.renameTo(file)) potaCache()
        } catch (e: Exception) {
        } finally {
            temporaneo.delete()
        }
    }

    @Synchronized
    private fun potaCache() {
        val file = cartella.listFiles { f -> f.name.endsWith(".png") }?.sortedBy { it.lastModified() } ?: return
        var totale = file.sumOf { it.length() }
        for (f in file) {
            if (totale <= MASSIMO_BYTE) break
            totale -= f.length()
            f.delete()
        }
    }

    private companion object {
        const val SETTE_GIORNI_MS = 7L * 24 * 60 * 60 * 1000
        const val MASSIMO_BYTE = 30L * 1024 * 1024
    }
}
