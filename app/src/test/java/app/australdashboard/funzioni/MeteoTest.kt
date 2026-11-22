package app.australdashboard.funzioni

import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeteoTest {
    private val risposta = """{"latitude":45.461365,"longitude":9.179014,"generationtime_ms":0.12087821960449219,"utc_offset_seconds":3600,"timezone":"Europe/Rome","timezone_abbreviation":"GMT+1","elevation":138.0,"current_units":{"time":"iso8601","interval":"seconds","temperature_2m":"°C","weather_code":"wmo code"},"current":{"time":"2026-11-18T19:15","interval":900,"temperature_2m":7.9,"weather_code":3},"daily_units":{"time":"iso8601","temperature_2m_max":"°C","temperature_2m_min":"°C"},"daily":{"time":["2026-11-18"],"temperature_2m_max":[10.0],"temperature_2m_min":[5.0]}}"""

    @Test
    fun daJsonLeggeRispostaReale() {
        assertEquals(Meteo(gradi = 8, minima = 5, massima = 10, codice = 3), FonteMeteo.daJson(risposta))
    }

    @Test
    fun daJsonLeggeLaPrevisioneDiDomani() {
        val due = risposta.replace(
            "\"time\":[\"2026-11-18\"],\"temperature_2m_max\":[10.0],\"temperature_2m_min\":[5.0]",
            "\"time\":[\"2026-11-18\",\"2026-11-19\"],\"weather_code\":[3,61],\"temperature_2m_max\":[10.0,12.4],\"temperature_2m_min\":[5.0,6.6]",
        )
        assertEquals(Previsione(codice = 61, minima = 7, massima = 12), FonteMeteo.daJson(due).domani)
        assertNull(FonteMeteo.daJson(risposta).domani)
    }

    @Test
    fun ilMeteoVecchioDiPiuDiTreOreNonVale() {
        val ora = 10_000_000_000L
        val treOre = 3 * 60 * 60 * 1000L
        assertEquals(false, FonteMeteo.troppoVecchio(ora - treOre, ora))
        assertEquals(true, FonteMeteo.troppoVecchio(ora - treOre - 1, ora))
        assertEquals(false, FonteMeteo.troppoVecchio(ora, ora))
    }

    @Test
    fun cieloDaiCodici() {
        assertEquals(Cielo.SERENO, FonteMeteo.cielo(0))
        assertEquals(Cielo.POCO_NUVOLOSO, FonteMeteo.cielo(1))
        assertEquals(Cielo.POCO_NUVOLOSO, FonteMeteo.cielo(2))
        assertEquals(Cielo.NUVOLOSO, FonteMeteo.cielo(3))
        assertEquals(Cielo.NEBBIA, FonteMeteo.cielo(45))
        assertEquals(Cielo.PIOGGIA, FonteMeteo.cielo(51))
        assertEquals(Cielo.PIOGGIA, FonteMeteo.cielo(61))
        assertEquals(Cielo.PIOGGIA, FonteMeteo.cielo(80))
        assertEquals(Cielo.NEVE, FonteMeteo.cielo(71))
        assertEquals(Cielo.TEMPORALE, FonteMeteo.cielo(95))
        assertEquals(Cielo.NUVOLOSO, FonteMeteo.cielo(1234))
    }

    @Test
    fun descrizioneDaiCodici() {
        assertEquals("Sereno", FonteMeteo.descrizione(0))
        assertEquals("Poco nuvoloso", FonteMeteo.descrizione(1))
        assertEquals("Poco nuvoloso", FonteMeteo.descrizione(2))
        assertEquals("Nuvoloso", FonteMeteo.descrizione(3))
        assertEquals("Nebbia", FonteMeteo.descrizione(45))
        assertEquals("Pioviggine", FonteMeteo.descrizione(51))
        assertEquals("Pioggia", FonteMeteo.descrizione(61))
        assertEquals("Rovesci", FonteMeteo.descrizione(80))
        assertEquals("Neve", FonteMeteo.descrizione(71))
        assertEquals("Temporale", FonteMeteo.descrizione(95))
        assertEquals("Nuvoloso", FonteMeteo.descrizione(1234))
    }

    private val minuto = 60_000L
    private val inizio = 1_000_000_000_000L

    @Test
    fun pioggiaAdessoDaZero() {
        assertEquals(0, FonteMeteo.pioggiaTra(inizio, listOf(0.0, 0.5), inizio + 20 * minuto))
    }

    @Test
    fun pioggiaFuturaArrotondataAiCinqueMinuti() {
        assertEquals(20, FonteMeteo.pioggiaTra(inizio, listOf(0.0, 0.0, 0.3), inizio + 10 * minuto))
        assertEquals(5, FonteMeteo.pioggiaTra(inizio, listOf(0.0, 0.3), inizio + 14 * minuto + 30_000))
    }

    @Test
    fun pioggiaOltreDueOreONessunaDaNull() {
        val mm = List(12) { if (it == 10) 1.0 else 0.0 }
        assertNull(FonteMeteo.pioggiaTra(inizio, mm, inizio))
        assertNull(FonteMeteo.pioggiaTra(inizio, emptyList(), inizio))
        assertNull(FonteMeteo.pioggiaTra(inizio, listOf(0.05, 0.09), inizio))
    }

    @Test
    fun pioggiaPassataIgnorata() {
        assertNull(FonteMeteo.pioggiaTra(inizio, listOf(2.0, 0.0), inizio + 20 * minuto))
    }

    @Test
    fun pioggiaConAdessoPrimaDellInizioDellaSerie() {
        assertEquals(35, FonteMeteo.pioggiaTra(inizio, listOf(0.0, 1.0), inizio - 20 * minuto))
    }

    @Test
    fun pioggiaDaJsonConvertePerFuso() {
        val testo = risposta.dropLast(1) +
            ""","minutely_15":{"time":["2026-11-18T19:15","2026-11-18T19:30"],"precipitation":[0.0,0.4]}}"""
        val pioggia = FonteMeteo.pioggiaDaJson(testo)!!
        assertEquals(LocalDateTime.of(2026, 11, 18, 18, 15).toEpochSecond(ZoneOffset.UTC) * 1000, pioggia.inizio)
        assertEquals(listOf(0.0, 0.4), pioggia.mm)
        assertNull(FonteMeteo.pioggiaDaJson(risposta))
    }

    @Test
    fun testoPioggia() {
        assertNull(FonteMeteo.testoPioggia(null))
        assertEquals("Piove", FonteMeteo.testoPioggia(0))
        assertEquals("Pioggia tra 20 min", FonteMeteo.testoPioggia(20))
    }
}
