package app.australdashboard.aggiornamenti

import org.json.JSONObject

data class InfoVersione(
    val versionCode: Long,
    val versionName: String,
    val apk: String,
    val sha256: String,
) {
    fun piuRecenteDi(installata: Long): Boolean = versionCode > installata

    companion object {
        fun daJson(testo: String): InfoVersione {
            val json = JSONObject(testo)
            val info = InfoVersione(
                versionCode = json.getLong("versionCode"),
                versionName = json.getString("versionName"),
                apk = json.getString("apk"),
                sha256 = json.getString("sha256").lowercase(),
            )
            require(info.apk.startsWith("https://")) { "indirizzo dell'APK non https" }
            require(info.sha256.matches(Regex("[0-9a-f]{64}"))) { "impronta SHA-256 non valida" }
            return info
        }
    }
}
