package app.australdashboard.funzioni

import org.junit.Assert.assertEquals
import org.junit.Test

class TessereTest {
    @Test
    fun ilCentroDelMondoStaAlCentroDellaTessera() {
        assertEquals(128.0, Mercatore.pixelX(0.0, 0), 1e-9)
        assertEquals(128.0, Mercatore.pixelY(0.0, 0), 1e-9)
        assertEquals(0.0, Mercatore.pixelX(-180.0, 0), 1e-9)
        assertEquals(2048.0 * 256, Mercatore.pixelX(180.0, 11), 1e-6)
    }

    @Test
    fun trovaLaTesseraDiMilano() {
        assertEquals(1076, Mercatore.tessera(Mercatore.pixelX(9.19, 11), 11))
        assertEquals(732, Mercatore.tessera(Mercatore.pixelY(45.4642, 11), 11))
    }

    @Test
    fun laConversioneSiInverte() {
        val x = Mercatore.pixelX(9.19, 15)
        val y = Mercatore.pixelY(45.4642, 15)
        assertEquals(9.19, Mercatore.lon(x, 15), 1e-9)
        assertEquals(45.4642, Mercatore.lat(y, 15), 1e-9)
    }

    @Test
    fun laTesseraRestaNeiLimiti() {
        assertEquals(0, Mercatore.tessera(-10.0, 11))
        assertEquals(2047, Mercatore.tessera(2048.0 * 256 + 5, 11))
    }

    @Test
    fun scegliLoZoomPerIlRaggio() {
        assertEquals(Mercatore.ZOOM_MAX, Mercatore.zoomPer(0.1, 45.46, 1000))
        assertEquals(Mercatore.ZOOM_MIN, Mercatore.zoomPer(500.0, 45.46, 1000))
        val z5 = Mercatore.zoomPer(5.0, 45.46, 1000)
        val z30 = Mercatore.zoomPer(30.0, 45.46, 1000)
        assertEquals(true, z5 > z30)
        assertEquals(true, 2 * 5000 / Mercatore.metriPerPixel(45.46, z5) <= 800)
    }

    @Test
    fun imetriPerPixelDimezzanoAdOgniZoom() {
        assertEquals(
            Mercatore.metriPerPixel(45.0, 12) / 2,
            Mercatore.metriPerPixel(45.0, 13),
            1e-9,
        )
    }
}
