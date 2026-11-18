package app.australdashboard.aggiornamenti

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InfoVersioneTest {
    private val sha = "a".repeat(64)

    private fun json(apk: String = "https://example.org/a.apk", impronta: String = sha) =
        """{"versionCode":5,"versionName":"0.2.0","apk":"$apk","sha256":"$impronta"}"""

    @Test
    fun leggeIlFileDiVersione() {
        val info = InfoVersione.daJson(json())
        assertEquals(InfoVersione(5, "0.2.0", "https://example.org/a.apk", sha), info)
    }

    @Test
    fun portaLImprontaInMinuscolo() {
        assertEquals(sha, InfoVersione.daJson(json(impronta = "A".repeat(64))).sha256)
    }

    @Test
    fun confrontaLeVersioni() {
        val info = InfoVersione.daJson(json())
        assertTrue(info.piuRecenteDi(4))
        assertFalse(info.piuRecenteDi(5))
        assertFalse(info.piuRecenteDi(6))
    }

    @Test
    fun rifiutaUnApkNonHttps() {
        assertThrows(IllegalArgumentException::class.java) {
            InfoVersione.daJson(json(apk = "http://example.org/a.apk"))
        }
    }

    @Test
    fun rifiutaUnImprontaNonValida() {
        assertThrows(IllegalArgumentException::class.java) {
            InfoVersione.daJson(json(impronta = "abc"))
        }
    }
}
