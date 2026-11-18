package app.australdashboard.funzioni

import java.io.StringReader
import java.io.StringWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BenzinaiTest {
    private val zona = ZoneId.systemDefault()
    private val milano = Luogo(45.4642, 9.19)

    private val prezziCsv = """
        Estrazione del 2026-11-17
        idImpianto|descCarburante|prezzo|isSelf|dtComu
        1|Benzina|2.319|0|11/11/2026 14:00:12
        1|Benzina|1.969|1|11/11/2026 14:00:14
        1|Gasolio|1.899|1|11/11/2026 14:00:14
        2|Benzina|1,729|1|14/11/2026 10:03:07
        3|Gasolio|1.800|1|14/11/2026 10:03:07
        4|Benzina|2.049|0|13/11/2026 09:59:38
        6|Benzina|1.850|1
        7|Benzina|abc|1|13/11/2026 09:59:38
        9|Benzina Plus 98|1.700|1|13/11/2026 09:59:38
        13|Benzina|1.700|1|13/11/2026 09:59:38
    """.trimIndent()

    private val anagraficaCsv = """
        Estrazione del 2026-11-17
        idImpianto|Gestore|Bandiera|Tipo Impianto|Nome Impianto|Indirizzo|Comune|Provincia|Latitudine|Longitudine
        1|ENIMOOV S.P.A.|Agip Eni|Stradale|19829 AGRIGENTO|SS.189 KM. 64+649 - C.DA SAN MICHELE  S.N.C  |AGRIGENTO|AG|37.333935|13.595533
        2|Mario Rossi|Q8|Stradale|19834${'\t'}MONTALLEGRO|Via Roma 1|MILANO|MI|45.4642|9.19
        3|Gestore|IP|Stradale|SOLO GASOLIO|Via Po 3|TORINO|TO|45.07|7.68
        4|Gestore|IP|Stradale|SOLO SERVITO|Via Po 4|TORINO|TO|45.07|7.68
        6|Gestore|IP|Stradale|SENZA DATA|Via Po 6|TORINO|TO|45.07|7.68
        8|Gestore|IP|Stradale|TRONCATA|Via Po 8
        10|Gestore|IP|Stradale|SENZA PREZZO|Via Po 10|TORINO|TO|45.07|7.68
        11|Gestore|IP|Stradale|Via Po 11|TORINO|TO|45.07
        13|Gestore|IP|Stradale|COORDINATE ERRATE|Via Po 13|TORINO|TO|xx|7.68
        9|STOIL SIMPLE | gestori.prezzibenzina.it|Pompe Bianche|Stradale|STOIL SIMPLE | gestori.prezzibenzina.it|STR. PROV.LE 82 15122|ALESSANDRIA|AL|44.9164|8.7012
    """.trimIndent()

    private fun prezzi() = DatiMimit.leggiPrezzi(StringReader(prezziCsv), zona)

    private fun impianti() = DatiMimit.leggiImpianti(StringReader(anagraficaCsv), prezzi())

    private fun d(id: Long, prezzo: Double, lat: Double, lon: Double = 9.19, comune: String = "c") =
        Distributore(id, "n$id", "m", "i", comune, Luogo(lat, lon), prezzo, 0L, 0.0)

    private fun istante(giorno: Int, ora: Int, minuti: Int) =
        LocalDateTime.of(2026, 11, giorno, ora, minuti).atZone(zona).toInstant().toEpochMilli()

    @Test
    fun leggeSoloBenzinaSelf() {
        val prezzi = prezzi()
        assertEquals(setOf(1L, 2L, 13L), prezzi.keys)
        assertEquals(1.969, prezzi.getValue(1).valore, 0.0)
        assertEquals(istante(11, 14, 0).plus(14_000), prezzi.getValue(1).aggiornato)
    }

    @Test
    fun accettaLaVirgolaDecimale() {
        assertEquals(1.729, prezzi().getValue(2).valore, 0.0)
    }

    @Test
    fun scartaRigheTroncateOConValoriNonValidi() {
        val prezzi = prezzi()
        assertNull(prezzi[6])
        assertNull(prezzi[7])
        assertNull(prezzi[9])
    }

    @Test
    fun leggeGliImpiantiConPrezzo() {
        val primo = impianti().first { it.id == 1L }
        assertEquals("19829 AGRIGENTO", primo.nome)
        assertEquals("Agip Eni", primo.marchio)
        assertEquals("SS.189 KM. 64+649 - C.DA SAN MICHELE S.N.C", primo.indirizzo)
        assertEquals("AGRIGENTO", primo.comune)
        assertEquals(Luogo(37.333935, 13.595533), primo.luogo)
        assertEquals(1.969, primo.prezzo, 0.0)
    }

    @Test
    fun ripulisceITabulatoriNelNome() {
        assertEquals("19834 MONTALLEGRO", impianti().first { it.id == 2L }.nome)
    }

    @Test
    fun scartaImpiantiSenzaPrezzoBenzinaSelf() {
        assertEquals(listOf(1L, 2L), impianti().map { it.id })
    }

    @Test
    fun gestisceIlSeparatoreDentroIlNome() {
        val stoil = DatiMimit.leggiImpianti(
            StringReader(anagraficaCsv),
            mapOf(9L to DatiMimit.Prezzo(1.7, 5L)),
        ).single()
        assertEquals("Pompe Bianche", stoil.marchio)
        assertEquals("STOIL SIMPLE | gestori.prezzibenzina.it", stoil.nome)
        assertEquals("ALESSANDRIA", stoil.comune)
        assertEquals(Luogo(44.9164, 8.7012), stoil.luogo)
    }

    @Test
    fun ilFileCompattoSiRileggeUguale() {
        val originali = impianti()
        val scritto = StringWriter().also { DatiMimit.scriviCompatto(it, LocalDate.of(2026, 11, 18), originali) }.toString()
        val letto = DatiMimit.leggiCompatto(StringReader(scritto))
        assertNotNull(letto)
        assertEquals(LocalDate.of(2026, 11, 18), letto!!.data)
        assertEquals(originali, letto.impianti)
    }

    @Test
    fun ilFileCompattoVuotoOGuastoNonSiLegge() {
        assertNull(DatiMimit.leggiCompatto(StringReader("")))
        assertNull(DatiMimit.leggiCompatto(StringReader("non una data\n")))
    }

    @Test
    fun calcolaLaDistanza() {
        assertEquals(0.0, DatiMimit.distanzaKm(milano, milano), 1e-9)
        assertEquals(477.0, DatiMimit.distanzaKm(milano, Luogo(41.9028, 12.4964)), 5.0)
        assertEquals(1.0, DatiMimit.distanzaKm(Luogo(45.0, 9.0), Luogo(45.0 + 1 / 111.195, 9.0)), 0.01)
    }

    @Test
    fun perOgniComunePreferitoIlPiuEconomicoAncheLontano() {
        val vicino = d(1, 1.9, 45.47)
        val caroTorino = d(2, 1.8, 45.07, 7.68, "TORINO")
        val economicoTorino = d(3, 1.7, 45.08, 7.69, "TORINO")
        val valenzano = d(4, 1.75, 41.04, 16.88, "VALENZANO")
        val fuori = d(5, 1.6, 45.9)
        val pronto = DatiMimit.filtra(
            listOf(vicino, caroTorino, economicoTorino, valenzano, fuori),
            milano,
            listOf("Valenzano", "Cellamare", "Torino"),
            Ordine.PREZZO,
        )
        assertEquals(listOf(4L, 3L), pronto.preferiti.map { it.id })
        assertEquals(listOf("Valenzano", "Torino"), pronto.preferiti.map { it.comune })
        assertTrue(pronto.preferiti.all { it.distanzaKm > 100 })
        assertEquals(listOf(1L), pronto.vicini.map { it.id })
        assertEquals(Ordine.PREZZO, pronto.ordine)
    }

    @Test
    fun ilPreferitoNonCompareAncheTraIVicini() {
        val pronto = DatiMimit.filtra(listOf(d(1, 1.9, 45.47, comune = "MILANO"), d(2, 2.0, 45.47)), milano, listOf("Milano"), Ordine.PREZZO)
        assertEquals(listOf(1L), pronto.preferiti.map { it.id })
        assertEquals(listOf(2L), pronto.vicini.map { it.id })
    }

    @Test
    fun limitaITrentaVicini() {
        val molti = (1L..40L).map { d(it, 1.5 + it / 1000.0, 45.47) }
        val pronto = DatiMimit.filtra(molti, milano, emptyList(), Ordine.PREZZO)
        assertEquals(30, pronto.vicini.size)
        assertEquals(1L, pronto.vicini.first().id)
    }

    private fun ordinati(ordine: Ordine, vararg impianti: Distributore) =
        DatiMimit.filtra(impianti.toList(), milano, emptyList(), ordine).vicini.map { it.id }

    @Test
    fun ordinaPerPrezzo() {
        assertEquals(
            listOf(3L, 2L, 1L),
            ordinati(Ordine.PREZZO, d(1, 2.0, 45.4642 + 0.009), d(2, 1.9, 45.4642 + 0.06), d(3, 1.8, 45.4642 + 0.09)),
        )
    }

    @Test
    fun ordinaPerDistanza() {
        assertEquals(
            listOf(1L, 2L, 3L),
            ordinati(Ordine.DISTANZA, d(3, 1.8, 45.4642 + 0.09), d(1, 2.0, 45.4642 + 0.009), d(2, 1.9, 45.4642 + 0.06)),
        )
    }

    @Test
    fun ordinaPerConvenienza() {
        val vicinoCaro = d(1, 1.800, 45.4642 + 0.009)
        val lontanoEconomico = d(2, 1.790, 45.4642 + 0.09)
        val lontanoMoltoEconomico = d(3, 1.700, 45.4642 + 0.09)
        assertEquals(
            listOf(3L, 1L, 2L),
            ordinati(Ordine.CONVIENE, vicinoCaro, lontanoEconomico, lontanoMoltoEconomico),
        )
        assertEquals(
            listOf(1L, 2L),
            ordinati(Ordine.CONVIENE, lontanoEconomico, vicinoCaro),
        )
    }

    @Test
    fun ilCostoComprendeIlViaggio() {
        val impianto = d(1, 2.0, 45.0).copy(distanzaKm = 10.0)
        assertEquals(2.0 * (40 + 10 * 2 * 0.06), DatiMimit.costo(impianto), 1e-9)
    }

    @Test
    fun formattaIlPrezzo() {
        assertEquals("1,729", Benzinai.prezzo(1.729))
        assertEquals("2,000", Benzinai.prezzo(2.0))
    }

    @Test
    fun formattaLaDistanza() {
        assertEquals("1,2 km", Benzinai.distanza(1.24))
        assertEquals("9,9 km", Benzinai.distanza(9.9))
        assertEquals("12 km", Benzinai.distanza(12.4))
        assertEquals("0,0 km", Benzinai.distanza(0.0))
    }

    @Test
    fun descriveQuandoERilevato() {
        val adesso = istante(18, 12, 0)
        assertEquals("oggi 08:10", Benzinai.quando(istante(18, 8, 10), adesso))
        assertEquals("ieri 18:30", Benzinai.quando(istante(17, 18, 30), adesso))
        assertEquals("11/11 08:10", Benzinai.quando(istante(11, 8, 10), adesso))
        assertEquals("ieri 23:59", Benzinai.quando(istante(17, 23, 59), istante(18, 0, 1)))
    }
}
