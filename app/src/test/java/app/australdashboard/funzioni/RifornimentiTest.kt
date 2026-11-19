package app.australdashboard.funzioni

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RifornimentiTest {
    private fun giorno(anno: Int, mese: Int, giorno: Int): Long =
        Calendar.getInstance().apply { clear(); set(anno, mese - 1, giorno, 12, 0) }.timeInMillis

    private val pieni = listOf(
        Rifornimento(giorno(2026, 10, 30), 60.0, 40.0, 10_000),
        Rifornimento(giorno(2026, 11, 5), 50.0, 30.0, 10_600),
        Rifornimento(giorno(2026, 11, 15), 40.0, 20.0, 11_000),
    )

    @Test
    fun spesaMeseSommaSoloIlMese() {
        assertEquals(90.0, Rifornimenti.spesaMese(pieni, 2026, 11), 0.001)
        assertEquals(60.0, Rifornimenti.spesaMese(pieni, 2026, 10), 0.001)
        assertEquals(0.0, Rifornimenti.spesaMese(pieni, 2025, 11), 0.001)
    }

    @Test
    fun consumoEscludeIlPrimoPieno() {
        assertEquals(1000.0 / 50.0, Rifornimenti.consumo(pieni.reversed())!!, 0.001)
    }

    @Test
    fun costoCentoKm() {
        assertEquals(90.0 / 1000.0 * 100, Rifornimenti.costoCentoKm(pieni)!!, 0.001)
    }

    @Test
    fun serveroAlmenoDuePieniEDistanzaPositiva() {
        assertNull(Rifornimenti.consumo(emptyList()))
        assertNull(Rifornimenti.consumo(pieni.take(1)))
        assertNull(Rifornimenti.costoCentoKm(pieni.take(1)))
        val fermo = listOf(pieni[0], pieni[0].copy(quando = 1))
        assertNull(Rifornimenti.consumo(fermo))
        assertNull(Rifornimenti.costoCentoKm(fermo))
    }

    @Test
    fun numeroConVirgolaOPunto() {
        assertEquals(52.3, Rifornimenti.numero("52,30")!!, 0.0001)
        assertEquals(52.3, Rifornimenti.numero(" 52.3 ")!!, 0.0001)
        assertNull(Rifornimenti.numero("0"))
        assertNull(Rifornimenti.numero("-4"))
        assertNull(Rifornimenti.numero("abc"))
        assertNull(Rifornimenti.numero(""))
        assertNull(Rifornimenti.numero("NaN"))
    }

    @Test
    fun rigaAndataERitorno() {
        val r = Rifornimento(1_700_000_000_000, 52.3, 31.25, 123_456)
        assertEquals("1700000000000\t52.30\t31.25\t123456", Rifornimenti.riga(r))
        assertEquals(r, Rifornimenti.daRiga(Rifornimenti.riga(r)))
    }

    @Test
    fun rigaNonValidaDaNull() {
        assertNull(Rifornimenti.daRiga(""))
        assertNull(Rifornimenti.daRiga("1\t2\t3"))
        assertNull(Rifornimenti.daRiga("a\t1.0\t1.0\t5"))
    }

    @Test
    fun spesaDelMese() {
        assertEquals("90 € a novembre", Rifornimenti.spesaDelMese(pieni, 2026, 11))
        assertNull(Rifornimenti.spesaDelMese(pieni, 2026, 12))
    }
}
