package app.australdashboard.temi

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.australdashboard.R
import app.australdashboard.Schermata
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.FonteMeteo
import app.australdashboard.funzioni.Meteo
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.Ordine
import app.australdashboard.temi.Ui.ADATTA
import app.australdashboard.temi.Ui.TUTTO
import app.australdashboard.temi.Ui.lp
import app.australdashboard.temi.Ui.lpf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object Matrix : Tema {
    override val id = "matrix"
    override val nome = "Matrix"
    override val idea = "Terminale a fosfori verdi, riquadri alla tmux"

    private const val NERO = 0xFF020A04.toInt()
    private const val VERDE = 0xFF39FF7A.toInt()
    private const val ATTENUATO = 0xFF2FB85C.toInt()
    private const val BORDO = 0xFF1E7A3E.toInt()
    private const val ONDA = 0x5539FF7A
    private const val ONDA_SCURA = 0x55020A04

    private fun vt(context: Context): Typeface = Ui.font(context, R.font.vt323)

    override fun palette(context: Context) =
        Palette(NERO, VERDE, ATTENUATO, VERDE, NERO, BORDO, vt(context), Ui.mono)

    private class Riquadro(val esterno: FrameLayout, val interno: LinearLayout, val etichetta: TextView)

    private fun riquadro(
        context: Context,
        titolo: String,
        verticale: Boolean = true,
        bordo: Int = BORDO,
        alto: Int = 22,
    ): Riquadro {
        val interno = (if (verticale) Ui.colonna(context) else Ui.riga(context)).apply {
            background = Ui.bordo(bordo, 2)
            setPadding(28, 34, 28, 22)
        }
        val etichetta = Ui.testo(context, 40, VERDE, vt(context), 40, titolo).apply {
            setBackgroundColor(NERO)
            setPadding(12, 0, 12, 0)
            translationY = -alto.toFloat()
        }
        val esterno = FrameLayout(context).apply {
            addView(interno, lpf(TUTTO, TUTTO))
            addView(etichetta, lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.START).apply { leftMargin = 22 })
        }
        Ui.senzaRitaglio(esterno)
        return Riquadro(esterno, interno, etichetta)
    }

    private fun tasto(context: Context, icona: Int, pieno: Boolean, latoIcona: Int, descrizione: String, azione: () -> Unit): View {
        val cornice = FrameLayout(context).apply {
            background = if (pieno) Ui.pieno(VERDE) else Ui.bordo(VERDE, 2)
            addView(Ui.icona(context, icona, if (pieno) NERO else VERDE), lpf(latoIcona, latoIcona, Gravity.CENTER))
        }
        return Ui.premibile(cornice, if (pieno) ONDA_SCURA else ONDA, descrizione, azione)
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeMatrix(s, azioni)

    private class HomeMatrix(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val vt = vt(s)
        private val ora = Ui.testo(s, 250, VERDE, vt, 210).apply { letterSpacing = -0.016f }
        private val data = Ui.testo(s, 34, VERDE, Ui.mono, 40)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, VERDE)
        private val gradi = Ui.testo(s, 170, VERDE, vt, 150, "--°")
        private val cielo = Ui.testo(s, 34, VERDE, Ui.monoGrassetto, 40, "IN ATTESA", maiuscolo = true)
        private val estremi = Ui.testo(s, 30, ATTENUATO, Ui.mono, 36, " ")
        private val minima = Ui.testo(s, 28, VERDE, Ui.mono, 30)
        private val massima = Ui.testo(s, 28, VERDE, Ui.mono, 30)
        private val celle = List(8) { View(s) }
        private val barra = Ui.riga(s, 6)
        private lateinit var musica: Riquadro
        private val inAscolto = Ui.riga(s, 24)
        private val fermo = Ui.riga(s, 24)
        private val copertina = ImageView(s).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val titolo = Ui.testo(s, 68, VERDE, vt, 64, maiuscolo = true)
        private val artista = Ui.testo(s, 30, ATTENUATO, Ui.mono, 36)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, NERO)

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 40).apply {
                setBackgroundColor(NERO)
                setPadding(20, 40, 20, 20)
            }
            val primaRiga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            primaRiga.addView(boxOra(), lp(0, TUTTO, 1f))
            primaRiga.addView(boxMeteo(), lp(440, TUTTO))
            radice.addView(primaRiga, lp(TUTTO, 420))
            radice.addView(boxMusica(), lp(TUTTO, 220))
            radice.addView(boxApp(), lp(TUTTO, 250))
            radice.addView(servizi(), lp(TUTTO, 124))
            Ui.senzaRitaglio(radice, primaRiga)
            return radice
        }

        private fun prompt(testo: String) = Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, testo)

        private fun boxOra(): View {
            val r = riquadro(s, "[ 01 · ORA ]")
            r.interno.addView(prompt("~$ date \"+%H:%M\""))
            r.interno.addView(ora, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            val rigaData = Ui.riga(s, 14)
            rigaData.addView(data)
            rigaData.addView(View(s).apply { setBackgroundColor(VERDE) }, lp(20, 36))
            r.interno.addView(rigaData, lp(ADATTA, ADATTA).apply { topMargin = 14 })
            return r.esterno
        }

        private fun boxMeteo(): View {
            val r = riquadro(s, "[ 02 · METEO ]")
            r.interno.addView(prompt("~$ meteo --qui"))
            val rigaGradi = Ui.riga(s, 20)
            rigaGradi.addView(iconaMeteo, lp(72, 72))
            rigaGradi.addView(gradi)
            r.interno.addView(rigaGradi, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            r.interno.addView(cielo)
            r.interno.addView(estremi, lp(ADATTA, ADATTA).apply { topMargin = 10 })
            barra.addView(minima, lp(ADATTA, ADATTA).apply { rightMargin = 8 })
            celle.forEach { barra.addView(it, lp(26, 30)) }
            barra.addView(massima, lp(ADATTA, ADATTA).apply { leftMargin = 8 })
            barra.visibility = View.INVISIBLE
            r.interno.addView(barra, lp(ADATTA, ADATTA).apply { topMargin = 10 })
            return r.esterno
        }

        private fun boxMusica(): View {
            musica = riquadro(s, "[ 03 · SPOTIFY ]", verticale = false, alto = 24)
            musica.interno.setPadding(28, 30, 28, 22)
            musica.interno.gravity = Gravity.CENTER_VERTICAL

            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            copertina.background = Ui.bordo(BORDO, 2)
            inAscolto.addView(copertina, lp(160, 160))
            val info = Ui.colonna(s)
            info.addView(titolo)
            info.addView(artista, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tasto(s, R.drawable.ic_precedente, false, 52, "Brano precedente") { azioni.precedente() }, lp(140, 140))
            val play = FrameLayout(s).apply {
                background = Ui.pieno(VERDE)
                addView(iconaPlay, lpf(60, 60, Gravity.CENTER))
            }
            comandi.addView(Ui.premibile(play, ONDA_SCURA, "Riproduci o metti in pausa") { azioni.playPausa() }, lp(140, 140))
            comandi.addView(tasto(s, R.drawable.ic_successivo, false, 52, "Brano successivo") { azioni.successivo() }, lp(140, 140))
            inAscolto.addView(comandi)

            val testi = Ui.colonna(s)
            testi.addView(prompt("~$ spotify"))
            testi.addView(Ui.testo(s, 96, VERDE, vt, 84, "Spotify"))
            fermo.addView(testi, lp(0, ADATTA, 1f))
            val apri = Ui.testo(s, 48, VERDE, vt, 44, "Apri Spotify", maiuscolo = true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(VERDE, 2)
                setPadding(36, 0, 36, 0)
            }
            fermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 140))
            fermo.addView(tasto(s, R.drawable.ic_play, true, 60, "Riproduci") { azioni.playPausa() }, lp(140, 140))

            musica.interno.addView(inAscolto, lp(TUTTO, ADATTA))
            musica.interno.addView(fermo, lp(TUTTO, ADATTA))
            mostraMusica(null)
            return musica.esterno
        }

        private fun boxApp(): View {
            val r = riquadro(s, "[ 04 · APP ]", verticale = false)
            r.interno.apply {
                dividerDrawable = GradientDrawable().apply { setSize(20, 20) }
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            }
            for (app in AppHome.elenco) {
                val cella = Ui.colonna(s, 6, Gravity.CENTER).apply {
                    background = Ui.bordo(BORDO, 2)
                    setPadding(6, 0, 6, 0)
                    addView(Ui.testo(s, 88, VERDE, vt, 72, "[${app.sigla}]"))
                    addView(Ui.testo(s, 28, VERDE, Ui.mono, 32, app.nome, righe = 2).apply { gravity = Gravity.CENTER })
                }
                val installata = app.intent(s) != null
                if (!installata) cella.alpha = 0.35f
                Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }
                r.interno.addView(cella, lp(0, TUTTO, 1f))
            }
            return r.esterno
        }

        private fun servizi(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val voci = listOf(
                Pair("F1", "Benzinai") to azioni::benzinai,
                Pair("F2", "Tutte le app") to azioni::tutteLeApp,
                Pair("F3", "Impostazioni Android") to azioni::impostazioniAndroid,
                Pair("F4", "Launcher") to azioni::impostazioniLauncher,
            )
            val pesi = listOf(1f, 1f, 1.5f, 1f)
            voci.forEachIndexed { i, (voce, azione) ->
                val (tasto, nome) = voce
                val cella = Ui.riga(s, 14).apply {
                    setBackgroundColor(VERDE)
                    setPadding(22, 0, 22, 0)
                    addView(Ui.testo(s, 28, NERO, Ui.monoGrassetto, 28, tasto).apply {
                        background = Ui.bordo(NERO, 2)
                        setPadding(6, 4, 6, 4)
                    })
                    addView(Ui.testo(s, 44, NERO, vt, 40, nome, maiuscolo = true, righe = 2))
                }
                riga.addView(Ui.premibile(cella, ONDA_SCURA, nome) { azione() }, lp(0, TUTTO, pesi[i]))
            }
            return riga
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) return
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
            minima.text = "${meteo.minima}°"
            massima.text = "${meteo.massima}°"
            val ampiezza = (meteo.massima - meteo.minima).coerceAtLeast(1)
            val accese = ((meteo.gradi - meteo.minima) * 8f / ampiezza).roundToInt().coerceIn(0, 8)
            celle.forEachIndexed { i, cella ->
                cella.background = if (i < accese) Ui.pieno(VERDE) else Ui.bordo(ATTENUATO, 2)
            }
            barra.visibility = View.VISIBLE
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) {
                this.musica.etichetta.text = "[ 03 · SPOTIFY ]"
                return
            }
            val stato = if (musica.inRiproduzione) "IN RIPRODUZIONE" else "IN PAUSA"
            this.musica.etichetta.text = "[ 03 · ${musica.app.uppercase(Locale.ITALIAN)} ▸ $stato ]"
            titolo.text = musica.titolo
            artista.text = "artista: ${musica.artista}"
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiMatrix(s, azioni)

    private class BenzinaiMatrix(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val vt = vt(s)
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 40).apply { setPadding(0, 24, 0, 0) }
        private lateinit var scheda: Riquadro
        private val indirizzo = Ui.adatta(Ui.testo(s, 82, VERDE, vt, contenuto = "", maiuscolo = true, righe = 2), 40, 82)
        private val dettagli = Ui.testo(s, 30, VERDE, Ui.mono, 36)
        private val stellaScheda = Ui.icona(s, R.drawable.ic_stella, ATTENUATO)
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
        private var preferiti: Set<Long> = emptySet()
        private var selezionato: Distributore? = null

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 40).apply {
                setBackgroundColor(NERO)
                setPadding(20, 28, 20, 20)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 270))
            Ui.senzaRitaglio(radice, elencoView)
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            val esc = Ui.riga(s, 10).apply {
                background = Ui.bordo(VERDE, 2)
                setPadding(24, 0, 24, 0)
                addView(Ui.icona(s, R.drawable.ic_indietro, VERDE), lp(44, 44))
                addView(Ui.testo(s, 44, VERDE, vt, 40, "ESC"))
            }
            riga.addView(Ui.premibile(esc, ONDA, "Indietro") { azioni.indietro() }, lp(ADATTA, TUTTO))
            riga.addView(Ui.testo(s, 76, VERDE, vt, 70, "~$ benzinai"), lp(0, ADATTA, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val tasto = Ui.testo(s, 44, VERDE, vt, 40, nome, maiuscolo = true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                gruppo.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(200, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, tasto) in ordini) {
                val scelto = ordine == attivo
                tasto.background = if (scelto) Ui.pieno(VERDE) else Ui.bordo(VERDE, 2)
                tasto.setTextColor(if (scelto) NERO else VERDE)
                tasto.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            scheda = riquadro(s, "[ SCHEDA ]", verticale = false, bordo = VERDE)
            scheda.interno.background = Ui.bordo(VERDE, 2, NERO)
            scheda.interno.gravity = Gravity.CENTER_VERTICAL
            val testi = Ui.colonna(s)
            testi.addView(indirizzo, lp(TUTTO, 140))
            testi.addView(dettagli, lp(ADATTA, ADATTA).apply { topMargin = 12 })
            scheda.interno.addView(testi, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            val stella = FrameLayout(s).apply {
                background = Ui.bordo(VERDE, 2)
                addView(stellaScheda, lpf(56, 56, Gravity.CENTER))
            }
            comandi.addView(Ui.premibile(stella, ONDA, "Preferito") { selezionato?.let(azioni::cambiaPreferito) }, lp(120, 120))
            comandi.addView(tasto(s, R.drawable.ic_chiudi, false, 52, "Chiudi scheda") { seleziona(null) }, lp(120, 120))
            scheda.interno.addView(comandi, lp(ADATTA, ADATTA).apply { leftMargin = 24 })
            scheda.esterno.visibility = View.GONE
            return scheda.esterno
        }

        override fun mostra(elenco: ElencoBenzinai, preferiti: Set<Long>) {
            ultimo = elenco
            this.preferiti = preferiti
            disegna()
        }

        private fun seleziona(d: Distributore?) {
            selezionato = if (d != null && d.id == selezionato?.id) null else d
            disegna()
        }

        private fun disegna() {
            elencoView.removeAllViews()
            when (val e = ultimo) {
                is ElencoBenzinai.Caricamento -> messaggio("~$ benzinai --aggiorna", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("~$ posizione", "Posizione non disponibile. Scegli una città di riserva nelle Impostazioni del launcher.")
                is ElencoBenzinai.Errore -> messaggio("~$ benzinai", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
                    if (e.preferiti.isNotEmpty()) sezione("[ ★ PREFERITI · ${e.preferiti.size} ]", e.preferiti)
                    val nomeOrdine = when (e.ordine) {
                        Ordine.CONVIENE -> "CONVIENE"
                        Ordine.PREZZO -> "PREZZO"
                        Ordine.DISTANZA -> "DISTANZA"
                    }
                    if (e.vicini.isNotEmpty()) sezione("[ VICINI · ORDINE: $nomeOrdine ]", e.vicini)
                    if (tutti.isEmpty()) messaggio("~$ benzinai", "Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(comando: String, testo: String) {
            val r = riquadro(s, "[ STATO ]")
            r.interno.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, comando))
            r.interno.addView(Ui.testo(s, 54, VERDE, vt, 50, testo, righe = 3), lp(TUTTO, ADATTA).apply { topMargin = 12 })
            elencoView.addView(r.esterno, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, distributori: List<Distributore>) {
            val r = riquadro(s, titolo)
            r.interno.apply {
                dividerDrawable = GradientDrawable().apply { setSize(20, 20) }
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            }
            distributori.forEach { r.interno.addView(riga(it), lp(TUTTO, 120)) }
            elencoView.addView(r.esterno, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = if (scelto) Ui.bordo(VERDE, 3) else Ui.bordo(BORDO, 2)
                setPadding(24, 0, 24, 0)
            }
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 54, VERDE, vt, 50, d.nome, maiuscolo = true))
            sinistra.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            val destra = Ui.colonna(s, 0, Gravity.END)
            val prezzo = Ui.riga(s, 0, Gravity.BOTTOM)
            prezzo.addView(Ui.testo(s, 76, VERDE, vt, 62, Benzinai.prezzo(d.prezzo)))
            prezzo.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 28, " €/l"))
            destra.addView(prezzo)
            destra.addView(Ui.testo(s, 30, ATTENUATO, Ui.mono, 36, Benzinai.distanza(d.distanzaKm)))
            corpo.addView(destra)
            riga.addView(Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }, lp(0, TUTTO, 1f))
            val preferito = d.id in preferiti
            val stella = FrameLayout(s).apply {
                background = Ui.bordo(VERDE, 2)
                addView(
                    Ui.icona(s, if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella, if (preferito) VERDE else ATTENUATO),
                    lpf(56, 56, Gravity.CENTER),
                )
            }
            val descrizione = if (preferito) "Rimuovi dai preferiti" else "Salva tra i preferiti"
            riga.addView(Ui.premibile(stella, ONDA, descrizione) { azioni.cambiaPreferito(d) }, lp(120, 120))
            return riga
        }

        private fun aggiornaScheda() {
            val d = selezionato
            if (d == null) {
                scheda.esterno.visibility = View.GONE
                return
            }
            scheda.esterno.visibility = View.VISIBLE
            scheda.etichetta.text = "[ SCHEDA ▸ ${d.nome.uppercase(Locale.ITALIAN)} · ${d.marchio.uppercase(Locale.ITALIAN)} ]"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            dettagli.text = "self ${Benzinai.prezzo(d.prezzo)} €/l · ${Benzinai.distanza(d.distanzaKm)} · aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
            val preferito = d.id in preferiti
            stellaScheda.setImageResource(if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella)
            stellaScheda.imageTintList = android.content.res.ColorStateList.valueOf(if (preferito) VERDE else ATTENUATO)
        }
    }
}
