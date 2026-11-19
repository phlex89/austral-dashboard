package app.australdashboard.funzioni

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistTest {
    @Test
    fun linkDaTestoPrendeIlPrimoUrl() {
        assertEquals(
            "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=abc",
            Playlists.linkDaTesto("Ascolta https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=abc e http://x.it")
        )
        assertNull(Playlists.linkDaTesto("nessun link qui"))
    }

    @Test
    fun nomePreferisceOggetto() {
        assertEquals("Viaggio", Playlists.nomeDaCondivisione("Ascolta https://x.it", " Viaggio "))
    }

    @Test
    fun nomeDalTestoPrimaDelLink() {
        assertEquals("Ascolta questa", Playlists.nomeDaCondivisione("Ascolta questa: https://x.it", null))
        assertEquals("Ascolta questa", Playlists.nomeDaCondivisione("Ascolta questa\nhttps://x.it", ""))
    }

    @Test
    fun nomeDiRiservaETroncato() {
        assertEquals("Playlist", Playlists.nomeDaCondivisione("https://x.it", null))
        assertEquals(40, Playlists.nomeDaCondivisione("a".repeat(80) + " https://x.it", null).length)
    }

    @Test
    fun uriSpotify() {
        assertEquals(
            "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M:play",
            Playlists.uriDaAprire("https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M")
        )
        assertEquals(
            "spotify:album:4aawyAB9vmqN3uQ7FjRGTy:play",
            Playlists.uriDaAprire("https://open.spotify.com/album/4aawyAB9vmqN3uQ7FjRGTy")
        )
        assertEquals(
            "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M:play",
            Playlists.uriDaAprire("https://open.spotify.com/intl-it/playlist/37i9dQZF1DXcBWIGoYBM5M?si=abc")
        )
    }

    @Test
    fun altriLinkRestanoCosiCome() {
        val link = "https://example.com/playlist/abc?x=1"
        assertEquals(link, Playlists.uriDaAprire(link))
        assertEquals("https://open.spotify.com/user/pippo", Playlists.uriDaAprire("https://open.spotify.com/user/pippo"))
    }

    @Test
    fun testoSalvatoAndataERitorno() {
        val voci = listOf(Playlist("Uno due", "https://a.it/1"), Playlist("Tre", "https://b.it/2"))
        assertEquals(voci, Playlists.daTesto(Playlists.aTesto(voci)))
        assertEquals(emptyList<Playlist>(), Playlists.daTesto(""))
    }
}
