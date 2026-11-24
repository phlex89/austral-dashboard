package app.australdashboard.funzioni

import android.graphics.Bitmap
import java.io.Serializable

data class Meteo(
    val gradi: Int,
    val minima: Int,
    val massima: Int,
    val codice: Int,
    val pioggiaTraMinuti: Int? = null,
    val luogo: String = "",
    val domani: Previsione? = null,
    val gps: Boolean = false,
)

data class Previsione(val codice: Int, val minima: Int, val massima: Int)

enum class Cielo { SERENO, POCO_NUVOLOSO, NUVOLOSO, NEBBIA, PIOGGIA, NEVE, TEMPORALE }

data class Musica(
    val titolo: String,
    val artista: String,
    val copertina: Bitmap?,
    val inRiproduzione: Boolean,
    val app: String,
    val pacchetto: String,
)

data class Luogo(val lat: Double, val lon: Double) : Serializable

data class Distributore(
    val id: Long,
    val nome: String,
    val marchio: String,
    val indirizzo: String,
    val comune: String,
    val luogo: Luogo,
    val prezzo: Double,
    val aggiornato: Long,
    val distanzaKm: Double,
    val prezzoPrima: Double? = null,
) : Serializable

enum class Ordine { CONVIENE, PREZZO, DISTANZA }

sealed class ElencoBenzinai {
    object Caricamento : ElencoBenzinai()
    object SenzaPosizione : ElencoBenzinai()
    data class Errore(val messaggio: String) : ElencoBenzinai()
    data class Pronto(
        val preferiti: List<Distributore>,
        val vicini: List<Distributore>,
        val ordine: Ordine,
        val raggioKm: Double = 10.0,
        val intornoAMe: Boolean = false,
    ) : ElencoBenzinai()
}
