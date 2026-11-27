package app.australdashboard.funzioni

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TestiTest {

    @Test
    fun leggeLeRigheLrc() {
        val righe = FonteTesti.analizzaLrc("[ar:Queen]\n[00:01.33] Is this the real life?\n[01:02.5]Caught\n[00:07]\n")
        assertEquals(
            listOf(RigaTesto(1330, "Is this the real life?"), RigaTesto(7000, ""), RigaTesto(62500, "Caught")),
            righe,
        )
    }

    @Test
    fun unaRigaConPiuTempiSiRipete() {
        val righe = FonteTesti.analizzaLrc("[00:10.00][00:30.00]Ritornello")
        assertEquals(listOf(RigaTesto(10_000, "Ritornello"), RigaTesto(30_000, "Ritornello")), righe)
    }

    @Test
    fun rigaCorrente() {
        val righe = listOf(RigaTesto(1000, "a"), RigaTesto(5000, "b"), RigaTesto(9000, "c"))
        assertEquals(-1, FonteTesti.rigaCorrente(righe, 500))
        assertEquals(0, FonteTesti.rigaCorrente(righe, 1000))
        assertEquals(1, FonteTesti.rigaCorrente(righe, 8999))
        assertEquals(2, FonteTesti.rigaCorrente(righe, 60_000))
    }

    @Test
    fun preferisceLaDurataVicinaESincronizzata() {
        val json = """[
            {"duration": 300.0, "instrumental": false, "plainLyrics": "lontano", "syncedLyrics": "[00:01.00]lontano"},
            {"duration": 355.0, "instrumental": false, "plainLyrics": "solo testo", "syncedLyrics": null},
            {"duration": 356.0, "instrumental": false, "plainLyrics": "giusto", "syncedLyrics": "[00:02.00]giusto"}
        ]"""
        val testo = FonteTesti.scegli(json, 355.0)!!
        assertTrue(testo.sincronizzato)
        assertEquals(listOf(RigaTesto(2000, "giusto")), testo.righe)
    }

    @Test
    fun senzaTempiUsaIlTestoSemplice() {
        val json = """[{"duration": 200.0, "instrumental": false, "plainLyrics": "\nuno\n\ndue\n", "syncedLyrics": null}]"""
        val testo = FonteTesti.scegli(json, 200.0)!!
        assertFalse(testo.sincronizzato)
        assertEquals(listOf("uno", "", "due"), testo.righe.map { it.testo })
    }

    @Test
    fun strumentaleEVuoto() {
        assertTrue(FonteTesti.scegli("""[{"duration": 200.0, "instrumental": true, "plainLyrics": null, "syncedLyrics": null}]""", null)!!.strumentale)
        assertNull(FonteTesti.scegli("[]", 200.0))
    }

    @Test
    fun titoloPulito() {
        assertEquals("Come Together", FonteTesti.titoloPulito("Come Together - Remastered 2009"))
        assertEquals("Stay", FonteTesti.titoloPulito("Stay (feat. Justin Bieber)"))
        assertEquals("Vita", FonteTesti.titoloPulito("Vita"))
    }

    @Test
    fun posizioneStimata() {
        assertEquals(5000L, posizioneStimata(5000, 1000, 1f, false, 9000))
        assertEquals(13_000L, posizioneStimata(5000, 1000, 1f, true, 9000))
        assertNull(posizioneStimata(-1, 1000, 1f, true, 9000))
    }

    @Test
    fun blocchiSottoIlLimite() {
        val righe = List(30) { "riga numero $it con un po' di testo" }
        val blocchi = FonteTraduzioni.blocchi(righe)
        assertEquals(righe, blocchi.flatten())
        assertTrue(blocchi.size > 1)
        assertTrue(blocchi.all { b -> b.joinToString("\n").toByteArray().size <= 450 })
    }

    @Test
    fun rispostaTradotta() {
        val json = """{"responseData":{"translatedText":"Non ho &#39;rimpianti&quot;\nE tu?"},"quotaFinished":false,"responseStatus":200}"""
        assertEquals("Non ho 'rimpianti\"\nE tu?", FonteTraduzioni.leggiRisposta(json))
    }

    @Test
    fun rispostaGiaInItaliano() {
        val json = """{"responseData":{"translatedText":"PLEASE SELECT TWO DISTINCT LANGUAGES"},"quotaFinished":null,"responseDetails":"PLEASE SELECT TWO DISTINCT LANGUAGES","responseStatus":"403"}"""
        assertNull(FonteTraduzioni.leggiRisposta(json))
    }

    @Test
    fun limiteRaggiunto() {
        val json = """{"responseData":{"translatedText":"MYMEMORY WARNING"},"quotaFinished":true,"responseStatus":429}"""
        assertThrows(LimiteTraduzioni::class.java) { FonteTraduzioni.leggiRisposta(json) }
    }
}
