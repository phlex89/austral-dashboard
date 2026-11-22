package app.australdashboard.funzioni

import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LuoghiTest {
    private val milano = "Milano" to Luogo(45.4641, 9.1919)
    private val roma = "Roma" to Luogo(41.8919, 12.5113)

    @Test
    fun formattaEAnalizzaSiCorrispondono() {
        val testo = Luoghi.formatta(listOf(milano, roma))
        assertEquals("Milano\t45.464100\t9.191900\nRoma\t41.891900\t12.511300", testo)
        assertEquals(listOf(milano, roma), Luoghi.analizza(testo))
    }

    @Test
    fun analizzaSaltaRigheGuaste() {
        assertEquals(listOf(roma), Luoghi.analizza("Milano\tx\t9\n\nsolo nome\nRoma\t41.8919\t12.5113"))
        assertEquals(emptyList<Pair<String, Luogo>>(), Luoghi.analizza(""))
    }

    @Test
    fun stessoNomeSostituisceSenzaSpostare() {
        val nuovo = Luogo(1.0, 2.0)
        assertEquals(listOf("Milano" to nuovo, roma), Luoghi.conLuogo(listOf(milano, roma), " Milano ", nuovo))
        assertEquals(listOf(milano, roma), Luoghi.conLuogo(listOf(milano), "Roma", roma.second))
        assertEquals(listOf(milano), Luoghi.conLuogo(listOf(milano), "  ", roma.second))
    }

    @Test
    fun senzaLuogoToglieIlNome() {
        assertEquals(listOf(roma), Luoghi.senzaLuogo(listOf(milano, roma), "Milano"))
    }

    @Test
    fun comunePiuVicinoLeggeIlFileCompatto() {
        val file = "2026-11-18T08:30:00\n" +
            "1\tA\tM\tVia 1\tMILANO\t45.46\t9.19\t1.8\t1\n" +
            "2\tB\tM\tVia 2\tMONZA\t45.58\t9.27\t1.8\t1\n" +
            "3\tC\tM\tVia 3\tREGGIO NELL'EMILIA\t44.70\t10.63\t1.8\t1\n"
        assertEquals("Milano", DatiMimit.comunePiuVicino(StringReader(file), Luogo(45.47, 9.2)))
        assertEquals("Reggio Nell'emilia", DatiMimit.comunePiuVicino(StringReader(file), Luogo(44.7, 10.6)))
        assertNull(DatiMimit.comunePiuVicino(StringReader(file), Luogo(40.0, 15.0)))
    }
}
