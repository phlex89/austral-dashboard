package app.australdashboard.funzioni

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        val parziali = pieni.map { it.copy(pieno = false) }
        assertNull(Rifornimenti.consumo(parziali))
    }

    @Test
    fun trattaConRifornimentoParzialeInMezzo() {
        val elenco = listOf(
            Rifornimento(1, 60.0, 40.0, 10_000),
            Rifornimento(2, 20.0, 10.0, 10_300, pieno = false),
            Rifornimento(3, 50.0, 30.0, 10_600),
        )
        val tratte = Rifornimenti.tratte(elenco)
        assertEquals(1, tratte.size)
        assertEquals(600, tratte[0].km)
        assertEquals(40.0, tratte[0].litri, 0.001)
        assertEquals(70.0, tratte[0].euro, 0.001)
        assertEquals(15.0, tratte[0].consumo, 0.001)
        assertEquals(70.0 / 600 * 100, tratte[0].costoCentoKm, 0.001)
    }

    @Test
    fun ultimoMiglioreEPeggiore() {
        val consumi = Rifornimenti.tratte(pieni).map { it.consumo }
        assertEquals(listOf(600.0 / 30.0, 400.0 / 20.0), consumi)
        assertEquals(20.0, consumi.max(), 0.001)
        assertEquals(20.0, consumi.min(), 0.001)
        val peggio = pieni + Rifornimento(giorno(2026, 11, 20), 80.0, 40.0, 11_200)
        val tutti = Rifornimenti.tratte(peggio).map { it.consumo }
        assertEquals(5.0, tutti.min(), 0.001)
        assertEquals(20.0, tutti.max(), 0.001)
        assertEquals(5.0, tutti.last(), 0.001)
    }

    @Test
    fun totali() {
        val t = Rifornimenti.totali(pieni)!!
        assertEquals(1000, t.km)
        assertEquals(150.0, t.euro, 0.001)
        assertEquals(90.0, t.litri, 0.001)
        assertEquals(150.0 / 90.0, t.euroAlLitro, 0.001)
        assertNull(Rifornimenti.totali(emptyList()))
    }

    @Test
    fun mesePerMese() {
        val mesi = Rifornimenti.mesi(pieni)
        assertEquals(2, mesi.size)
        assertEquals(11, mesi[0].mese)
        assertEquals(90.0, mesi[0].spesa, 0.001)
        assertEquals(1000, mesi[0].km)
        assertEquals(10, mesi[1].mese)
        assertEquals(60.0, mesi[1].spesa, 0.001)
        assertEquals(0, mesi[1].km)
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
        assertEquals("1700000000000\t52.30\t31.25\t123456\t1", Rifornimenti.riga(r))
        assertEquals(r, Rifornimenti.daRiga(Rifornimenti.riga(r)))
        val parziale = r.copy(pieno = false)
        assertEquals(parziale, Rifornimenti.daRiga(Rifornimenti.riga(parziale)))
    }

    @Test
    fun rigaVecchiaConQuattroCampiEPiena() {
        val r = Rifornimenti.daRiga("1700000000000\t52.30\t31.25\t123456")!!
        assertTrue(r.pieno)
        assertEquals(123_456, r.km)
    }

    @Test
    fun rigaNuovaConCinqueCampi() {
        assertFalse(Rifornimenti.daRiga("1700000000000\t52.30\t31.25\t123456\t0")!!.pieno)
        assertTrue(Rifornimenti.daRiga("1700000000000\t52.30\t31.25\t123456\t1")!!.pieno)
        assertNull(Rifornimenti.daRiga("1700000000000\t52.30\t31.25\t123456\t2"))
    }

    @Test
    fun rigaNonValidaDaNull() {
        assertNull(Rifornimenti.daRiga(""))
        assertNull(Rifornimenti.daRiga("1\t2\t3"))
        assertNull(Rifornimenti.daRiga("1\t2\t3\t4\t1\t6"))
        assertNull(Rifornimenti.daRiga("a\t1.0\t1.0\t5"))
    }

    @Test
    fun spesaDelMese() {
        assertEquals("90 € a novembre", Rifornimenti.spesaDelMese(pieni, 2026, 11))
        assertNull(Rifornimenti.spesaDelMese(pieni, 2026, 12))
    }
}
