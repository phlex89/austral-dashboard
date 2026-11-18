package app.australdashboard.aggiornamenti

import java.io.File
import java.security.MessageDigest

object Impronta {
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val letti = input.read(buffer)
                if (letti < 0) break
                digest.update(buffer, 0, letti)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun corrisponde(file: File, attesa: String): Boolean = sha256(file) == attesa.lowercase()
}
