package app.australdashboard.funzioni

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager

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

    fun aggiorna(context: Context, fatto: (Luogo?) -> Unit) {
        if (!permessoConcesso(context)) {
            fatto(attuale(context))
            return
        }
        val gestore = context.getSystemService(LocationManager::class.java)
        val provider = providerAbilitati(gestore).firstOrNull()
        if (provider == null) {
            fatto(attuale(context))
            return
        }
        try {
            gestore.getCurrentLocation(provider, null, context.mainExecutor) { posizione ->
                if (posizione == null) {
                    fatto(attuale(context))
                } else {
                    val luogo = Luogo(posizione.latitude, posizione.longitude)
                    salva(context, luogo)
                    fatto(luogo)
                }
            }
        } catch (e: SecurityException) {
            fatto(attuale(context))
        } catch (e: IllegalArgumentException) {
            fatto(attuale(context))
        }
    }

    fun cittaDiRiserva(context: Context): Pair<String, Luogo>? {
        val preferenze = context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE)
        val nome = preferenze.getString("citta_nome", null) ?: return null
        if (!preferenze.contains("citta_lat") || !preferenze.contains("citta_lon")) return null
        val luogo = Luogo(
            Double.fromBits(preferenze.getLong("citta_lat", 0)),
            Double.fromBits(preferenze.getLong("citta_lon", 0)),
        )
        return nome to luogo
    }

    fun salvaCitta(context: Context, nome: String, luogo: Luogo) {
        context.getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit()
            .putString("citta_nome", nome)
            .putLong("citta_lat", luogo.lat.toRawBits())
            .putLong("citta_lon", luogo.lon.toRawBits())
            .apply()
    }

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
