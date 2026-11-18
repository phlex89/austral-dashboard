package app.australdashboard.aggiornamenti

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ImprontaTest {
    @get:Rule
    val cartella = TemporaryFolder()

    private val shaDiAbc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"

    @Test
    fun calcolaLoSha256() {
        val file = cartella.newFile().apply { writeText("abc") }
        assertEquals(shaDiAbc, Impronta.sha256(file))
    }

    @Test
    fun accettaLImprontaGiustaAncheInMaiuscolo() {
        val file = cartella.newFile().apply { writeText("abc") }
        assertTrue(Impronta.corrisponde(file, shaDiAbc.uppercase()))
    }

    @Test
    fun rifiutaUnFileDiverso() {
        val file = cartella.newFile().apply { writeText("abd") }
        assertFalse(Impronta.corrisponde(file, shaDiAbc))
    }

    @Test
    fun rifiutaUnFileTroncato() {
        val file = cartella.newFile().apply { writeText("ab") }
        assertFalse(Impronta.corrisponde(file, shaDiAbc))
    }
}
