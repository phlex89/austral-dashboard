package app.australdashboard.funzioni

import android.media.session.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Test

class LettoreTest {

    @Test
    fun nessunControllerDaNessuno() {
        assertEquals(-1, scegliControllerIndice(emptyList()))
    }

    @Test
    fun preferisceQuelloInRiproduzione() {
        val elenco = listOf(
            Pair(PlaybackState.STATE_PAUSED, true),
            Pair(PlaybackState.STATE_PLAYING, true),
        )
        assertEquals(1, scegliControllerIndice(elenco))
    }

    @Test
    fun ilBufferingContaComeRiproduzione() {
        val elenco = listOf(
            Pair(PlaybackState.STATE_PAUSED, true),
            Pair(PlaybackState.STATE_BUFFERING, false),
        )
        assertEquals(1, scegliControllerIndice(elenco))
    }

    @Test
    fun senzaRiproduzioneUsaIlPrimoSeHaTitolo() {
        val elenco = listOf(
            Pair(PlaybackState.STATE_PAUSED, true),
            Pair(PlaybackState.STATE_PAUSED, true),
        )
        assertEquals(0, scegliControllerIndice(elenco))
    }

    @Test
    fun senzaRiproduzioneNeTitoloNessuno() {
        val elenco = listOf(
            Pair(PlaybackState.STATE_PAUSED, false),
            Pair(PlaybackState.STATE_STOPPED, true),
        )
        assertEquals(-1, scegliControllerIndice(elenco))
    }
}
