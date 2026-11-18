package app.australdashboard.funzioni

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CittaTest {
    @Test
    fun daJsonConRisultati() {
        val testo = """{"results":[{"id":3173435,"name":"Milano","latitude":45.46427,"longitude":9.18951,"country":"Italia","admin1":"Lombardia","admin2":"Provincia di Milano"},{"id":1,"name":"Piccolo","latitude":1.5,"longitude":2.5,"admin1":"Regione"},{"id":2,"name":"Isola","latitude":3.0,"longitude":4.0}],"generationtime_ms":0.16}"""
        val citta = Citta.daJson(testo)
        assertEquals(3, citta.size)
        assertEquals("Milano, Provincia di Milano" to Luogo(45.46427, 9.18951), citta[0])
        assertEquals("Piccolo, Regione" to Luogo(1.5, 2.5), citta[1])
        assertEquals("Isola" to Luogo(3.0, 4.0), citta[2])
    }

    @Test
    fun daJsonSenzaRisultati() {
        assertTrue(Citta.daJson("""{"generationtime_ms":0.057}""").isEmpty())
    }
}
