package app.australdashboard.funzioni

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper

object Posizione {
    private const val PREFERENZE = "posizione"

    fun permessoConcesso(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun attuale(context: Context): Luogo? {
        if (permessoConcesso(context)) {
            val gestore = context.getSystemService(LocationManager::class.java)
            val piuRecente = providerAbilitati(gestore)
                .mapNotNull { ultima(gestore, it) }
                .maxByOrNull { it.time }
            if (piuRecente != null) return Luogo(piuRecente.latitude, piuRecente.longitude)
            salvata(context)?.let { return it }
        }
        return cittaDiRiserva(context)?.second
    }

    fun aggiorna(context: Context, ripiego: Boolean = true, fatto: (Luogo?) -> Unit) {
        val altrimenti = { fatto(if (ripiego) attuale(context) else null) }
        if (!permessoConcesso(context)) {
            altrimenti()
            return
        }
        val gestore = context.getSystemService(LocationManager::class.java)
        val provider = providerAbilitati(gestore).firstOrNull()
        if (provider == null) {
            altrimenti()
            return
        }
        try {
            gestore.getCurrentLocation(provider, null, context.mainExecutor) { posizione ->
                if (posizione == null) {
                    altrimenti()
                } else {
                    val luogo = Luogo(posizione.latitude, posizione.longitude)
                    salva(context, luogo)
                    fatto(luogo)
                }
            }
        } catch (e: SecurityException) {
            altrimenti()
        } catch (e: IllegalArgumentException) {
            altrimenti()
        }
    }

    fun ascolta(context: Context, ascoltatore: LocationListener): Boolean {
        if (!permessoConcesso(context)) return false
        val gestore = context.getSystemService(LocationManager::class.java)
        val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { gestore.isProviderEnabled(it) } ?: return false
        return try {
            gestore.requestLocationUpdates(provider, 1000L, 3f, ascoltatore, Looper.getMainLooper())
            true
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun smetti(context: Context, ascoltatore: LocationListener) {
        context.getSystemService(LocationManager::class.java).removeUpdates(ascoltatore)
    }

    fun cittaDiRiserva(context: Context): Pair<String, Luogo>? = Luoghi.riserva(context)

    private fun providerAbilitati(gestore: LocationManager): List<String> =
        listOf(LocationManager.FUSED_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { gestore.isProviderEnabled(it) }

    private fun ultima(gestore: LocationManager, provider: String): Location? = try {
        gestore.getLastKnownLocation(provider)
    } catch (e: SecurityException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun salva(context: Context, luogo: Luogo) {
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putLong("lat", luogo.lat.toRawBits())
            .putLong("lon", luogo.lon.toRawBits())
            .apply()
    }

    private fun salvata(context: Context): Luogo? {
        val preferenze = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE)
        if (!preferenze.contains("lat") || !preferenze.contains("lon")) return null
        return Luogo(Double.fromBits(preferenze.getLong("lat", 0)), Double.fromBits(preferenze.getLong("lon", 0)))
    }
}
