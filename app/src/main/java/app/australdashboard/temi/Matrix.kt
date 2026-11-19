package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
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
import app.australdashboard.funzioni.DatiMimit
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.FonteMeteo
import app.australdashboard.funzioni.Meteo
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.Ordine
import app.australdashboard.funzioni.Playlists
import app.australdashboard.funzioni.StatoSistema
import app.australdashboard.funzioni.StatoWifi
import app.australdashboard.temi.Ui.ADATTA
import app.australdashboard.temi.Ui.TUTTO
import app.australdashboard.temi.Ui.lp
import app.australdashboard.temi.Ui.lpf
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private const val TACCHE = 15

    private fun vt(context: Context): Typeface = Ui.font(context, R.font.vt323)

    override fun palette(context: Context) =
        Palette(NERO, VERDE, VERDE, BORDO, vt(context), Ui.mono)

    private class Riquadro(val esterno: FrameLayout, val interno: LinearLayout, val etichetta: TextView)

    private fun riquadro(
        context: Context,
        titolo: String,
        verticale: Boolean = true,
        bordo: Int = BORDO,
        alto: Int = 22,
        destra: String? = null,
        sopra: Int = 34,
        sotto: Int = 22,
        lati: Int = 28,
        margine: Int = 22,
    ): Riquadro {
        val interno = (if (verticale) Ui.colonna(context) else Ui.riga(context)).apply {
            background = Ui.bordo(bordo, 2)
            setPadding(lati + 2, sopra + 2, lati + 2, sotto + 2)
        }
        val etichetta = Ui.testo(context, 40, VERDE, vt(context), 40, titolo).apply {
            setBackgroundColor(NERO)
            setPadding(12, 0, 12, 0)
            translationY = -alto.toFloat()
        }
        val esterno = FrameLayout(context).apply {
            addView(interno, lpf(TUTTO, TUTTO))
            addView(etichetta, lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.START).apply { leftMargin = margine })
            if (destra != null) {
                val lato = Ui.testo(context, 40, ATTENUATO, vt(context), 40, destra).apply {
                    setBackgroundColor(NERO)
                    setPadding(12, 0, 12, 0)
                    translationY = -alto.toFloat()
                }
                addView(lato, lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.END).apply { rightMargin = 22 })
            }
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

    private class Interruttore(
        val cornice: FrameLayout,
        val icona: ImageView,
        val sigla: TextView,
        val stato: TextView,
        val nome: String,
    ) {
        fun mostra(risorsa: Int, testo: String, livello: Int) {
            val colore = if (livello == 2) NERO else if (livello == 1) VERDE else ATTENUATO
            cornice.background = when (livello) {
                2 -> Ui.pieno(VERDE)
                1 -> Ui.bordo(VERDE, 2)
                else -> Ui.bordo(BORDO, 2)
            }
            icona.setImageResource(risorsa)
            icona.imageTintList = ColorStateList.valueOf(colore)
            sigla.setTextColor(colore)
            stato.setTextColor(colore)
            stato.typeface = if (livello == 2) Ui.monoGrassetto else Ui.mono
            stato.text = testo
            cornice.contentDescription = "$nome $testo"
        }
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeMatrix(s, azioni)

    private class HomeMatrix(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val vt = vt(s)
        private val playlist = Playlists.elenco(s)
        private val ora = Ui.testo(s, 250, VERDE, vt, 210).apply { letterSpacing = -0.016f }
        private val data = Ui.testo(s, 34, VERDE, Ui.mono, 40)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, VERDE)
        private val gradi = Ui.testo(s, 170, VERDE, vt, 150, "--°")
        private val cielo = Ui.testo(s, 34, VERDE, Ui.monoGrassetto, 40, "IN ATTESA", maiuscolo = true)
        private val estremi = Ui.testo(s, 30, ATTENUATO, Ui.mono, 36, " ")
        private val minima = Ui.testo(s, 28, VERDE, Ui.mono, 30)
        private val massima = Ui.testo(s, 28, VERDE, Ui.mono, 30)
        private val celle = List(9) { View(s) }
        private val barra = Ui.riga(s, 8)
        private val pioggia = Ui.testo(s, 28, NERO, Ui.monoGrassetto, 34, maiuscolo = true).apply {
            background = Ui.pieno(VERDE)
            setPadding(4, 1, 4, 1)
            visibility = View.GONE
        }
        private val giorni = List(7) { Ui.testo(s, 28, ATTENUATO, Ui.mono, 28).apply { gravity = Gravity.CENTER } }
        private val strisciaGiorni = Ui.riga(s, 4)
        private val oreCelle = List(24) { View(s) }
        private var ultimaOra = -1
        private lateinit var musica: Riquadro
        private val inAscolto = Ui.riga(s, 20)
        private val fermo = Ui.riga(s, 20)
        private val copertina = ImageView(s).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val cornice = FrameLayout(s).apply {
            background = Ui.bordo(VERDE, 2)
            setPadding(6, 6, 6, 6)
        }
        private val titolo = Ui.testo(s, 60, VERDE, vt, 56, maiuscolo = true)
        private val artista = Ui.testo(s, 28, ATTENUATO, Ui.mono, 34)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, NERO)
        private val wifi = interruttore("WIFI", "Wi-Fi") { azioni.wifi() }
        private val bluetooth = interruttore("BT", "Bluetooth") { azioni.bluetooth() }
        private val numeroVolume = Ui.testo(s, 72, VERDE, vt, 60, "00").apply { gravity = Gravity.CENTER }
        private val tacche = List(TACCHE) { View(s) }

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(NERO)
                setPadding(20, 40, 20, 20)
            }
            val sopra = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val sinistra = Ui.colonna(s, 30)
            val primaRiga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            primaRiga.addView(boxOra(), lp(610, TUTTO))
            primaRiga.addView(boxMeteo(), lp(0, TUTTO, 1f))
            sinistra.addView(primaRiga, lp(TUTTO, 0, 1f))
            sinistra.addView(boxMusica(), lp(TUTTO, if (playlist.isEmpty()) 176 else 316))
            sinistra.addView(boxApp(), lp(TUTTO, 188))
            sopra.addView(sinistra, lp(0, TUTTO, 1f))
            sopra.addView(boxSistema(), lp(180, TUTTO))
            radice.addView(sopra, lp(TUTTO, 0, 1f))
            radice.addView(servizi(), lp(TUTTO, 124))
            Ui.senzaRitaglio(radice, sopra, sinistra, primaRiga)
            return radice
        }

        private fun prompt(testo: String) = Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, testo)

        private fun boxOra(): View {
            val r = riquadro(s, "[ 01 · ORA ]", destra = "[ 24H ]")
            r.interno.gravity = Gravity.CENTER_VERTICAL
            val rigaPrompt = Ui.riga(s)
            rigaPrompt.addView(prompt("~$ date \"+%H:%M\""), lp(0, ADATTA, 1f))
            giorni.forEachIndexed { i, g ->
                g.text = "LMMGVSD"[i].toString()
                g.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                strisciaGiorni.addView(g, lp(34, 34))
            }
            rigaPrompt.addView(strisciaGiorni)
            r.interno.addView(rigaPrompt, lp(TUTTO, 34))
            r.interno.addView(ora, lp(ADATTA, 218).apply { topMargin = -13 })
            val rigaData = Ui.riga(s, 14)
            rigaData.addView(data)
            rigaData.addView(View(s).apply { setBackgroundColor(VERDE) }, lp(20, 36))
            r.interno.addView(rigaData, lp(ADATTA, ADATTA).apply { topMargin = 24 })
            val rigaOre = Ui.riga(s, 10)
            rigaOre.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 30, "00"))
            val celleOre = Ui.riga(s, 4)
            oreCelle.forEach { celleOre.addView(it, lp(14, 30)) }
            rigaOre.addView(celleOre)
            rigaOre.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 30, "24"))
            r.interno.addView(rigaOre, lp(ADATTA, ADATTA).apply { topMargin = 12 })
            return r.esterno
        }

        private fun boxMeteo(): View {
            val r = riquadro(s, "[ 02 · METEO ]")
            r.interno.gravity = Gravity.CENTER_VERTICAL
            r.interno.addView(prompt("~$ meteo --qui"))
            val rigaGradi = Ui.riga(s, 20)
            rigaGradi.addView(iconaMeteo, lp(72, 72))
            rigaGradi.addView(gradi, lp(ADATTA, 150))
            r.interno.addView(rigaGradi, lp(ADATTA, 150).apply { topMargin = 6 })
            r.interno.addView(cielo)
            r.interno.addView(estremi, lp(ADATTA, ADATTA).apply { topMargin = 10 })
            val celleGradi = Ui.riga(s, 5)
            celle.forEach { celleGradi.addView(it, lp(19, 30)) }
            barra.addView(minima)
            barra.addView(celleGradi)
            barra.addView(massima)
            barra.visibility = View.INVISIBLE
            r.interno.addView(barra, lp(ADATTA, ADATTA).apply { topMargin = 10 })
            r.interno.addView(pioggia, lp(ADATTA, ADATTA).apply { topMargin = 10 })
            return r.esterno
        }

        private fun boxMusica(): View {
            musica = riquadro(s, "[ 03 · SPOTIFY ]", alto = 24, sopra = 30, sotto = 18)

            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            cornice.addView(copertina, lpf(TUTTO, TUTTO))
            inAscolto.addView(cornice, lp(124, 124))
            val info = Ui.colonna(s)
            info.addView(titolo)
            info.addView(artista, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tasto(s, R.drawable.ic_precedente, false, 48, "Brano precedente") { azioni.precedente() }, lp(124, 124))
            val play = FrameLayout(s).apply {
                background = Ui.pieno(VERDE)
                addView(iconaPlay, lpf(56, 56, Gravity.CENTER))
            }
            comandi.addView(Ui.premibile(play, ONDA_SCURA, "Riproduci o metti in pausa") { azioni.playPausa() }, lp(124, 124))
            comandi.addView(tasto(s, R.drawable.ic_successivo, false, 48, "Brano successivo") { azioni.successivo() }, lp(124, 124))
            inAscolto.addView(comandi)

            val testi = Ui.colonna(s)
            testi.addView(prompt("~$ spotify"))
            testi.addView(Ui.testo(s, 84, VERDE, vt, 74, "Spotify"))
            fermo.addView(testi, lp(0, ADATTA, 1f))
            val apri = Ui.testo(s, 44, VERDE, vt, 40, "Apri Spotify", maiuscolo = true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(VERDE, 2)
                setPadding(30, 0, 30, 0)
            }
            fermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 124))
            fermo.addView(tasto(s, R.drawable.ic_play, true, 56, "Riproduci") { azioni.playPausa() }, lp(124, 124))

            musica.interno.addView(inAscolto, lp(TUTTO, 124))
            musica.interno.addView(fermo, lp(TUTTO, 124))
            if (playlist.isNotEmpty()) musica.interno.addView(rigaPlaylist(), lp(TUTTO, 120).apply { topMargin = 20 })
            mostraMusica(null)
            return musica.esterno
        }

        private fun rigaPlaylist(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            playlist.forEach { p ->
                val voce = Ui.riga(s, 12).apply {
                    background = Ui.bordo(ATTENUATO, 2)
                    setPadding(16, 0, 16, 0)
                    addView(Ui.icona(s, R.drawable.ic_play, VERDE), lp(28, 28))
                    addView(Ui.testo(s, 28, VERDE, Ui.mono, 32, p.nome, righe = 3), lp(0, ADATTA, 1f))
                }
                riga.addView(Ui.premibile(voce, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) }, lp(0, TUTTO, 1f))
            }
            return riga
        }

        private fun boxApp(): View {
            val r = riquadro(s, "[ 04 · APP ]", verticale = false, destra = "[ ${AppHome.elenco(s).size} ]", sopra = 30, sotto = 18)
            r.interno.apply {
                dividerDrawable = GradientDrawable().apply { setSize(20, 20) }
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            }
            AppHome.elenco(s).forEach { app ->
                val cella = Ui.colonna(s, 4, Gravity.CENTER).apply {
                    background = Ui.bordo(BORDO, 2)
                    setPadding(6, 0, 6, 0)
                    addView(Ui.testo(s, 72, VERDE, vt, 56, "[${app.sigla}]"))
                    addView(Ui.testo(s, 28, VERDE, Ui.mono, 32, app.nome, righe = 2).apply { gravity = Gravity.CENTER })
                }
                val installata = app.intent(s) != null
                if (!installata) cella.alpha = 0.35f
                Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }
                r.interno.addView(cella, lp(0, TUTTO, 1f))
            }
            return r.esterno
        }

        private fun interruttore(sigla: String, nome: String, azione: () -> Unit): Interruttore {
            val icona = ImageView(s).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
            val siglaView = Ui.testo(s, 28, VERDE, Ui.monoGrassetto, 30, sigla).apply { gravity = Gravity.CENTER }
            val stato = Ui.testo(s, 28, VERDE, Ui.mono, 30).apply { gravity = Gravity.CENTER }
            val contenuto = Ui.colonna(s, 2, Gravity.CENTER).apply {
                addView(icona, lp(44, 44))
                addView(siglaView, lp(TUTTO, ADATTA))
                addView(stato, lp(TUTTO, ADATTA))
            }
            val cornice = FrameLayout(s).apply { addView(contenuto, lpf(TUTTO, TUTTO)) }
            Ui.premibile(cornice, ONDA, nome, azione)
            return Interruttore(cornice, icona, siglaView, stato, nome)
        }

        private fun boxSistema(): View {
            val r = riquadro(s, "[ SYS ]", lati = 18, margine = 6)
            r.interno.gravity = Gravity.CENTER_HORIZONTAL
            fun distanza() = View(s)
            r.interno.addView(wifi.cornice, lp(140, 124))
            r.interno.addView(distanza(), lp(0, 0, 1f))
            r.interno.addView(bluetooth.cornice, lp(140, 124))
            r.interno.addView(distanza(), lp(0, 0, 1f))
            r.interno.addView(tasto(s, R.drawable.ic_piu, false, 56, "Alza il volume") { azioni.volume(true) }, lp(140, 124))
            r.interno.addView(distanza(), lp(0, 0, 1f))
            val livello = Ui.colonna(s, 8, Gravity.CENTER_HORIZONTAL)
            livello.addView(numeroVolume)
            val scala = Ui.colonna(s, 6)
            scala.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            tacche.forEach { scala.addView(it, lp(100, 12)) }
            livello.addView(scala)
            r.interno.addView(livello, lp(ADATTA, ADATTA))
            r.interno.addView(distanza(), lp(0, 0, 1f))
            r.interno.addView(tasto(s, R.drawable.ic_meno, false, 56, "Abbassa il volume") { azioni.volume(false) }, lp(140, 124))
            return r.esterno
        }

        private fun servizi(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val voci = listOf(
                Triple("F1", "Benzinai", R.drawable.ic_benzinaio) to azioni::benzinai,
                Triple("F2", "Tutte le app", R.drawable.ic_griglia) to azioni::tutteLeApp,
                Triple("F3", "Impostazioni Android", R.drawable.ic_cursori) to azioni::impostazioniAndroid,
                Triple("F4", "Launcher", R.drawable.ic_ingranaggio) to azioni::impostazioniLauncher,
            )
            val pesi = listOf(0.85f, 1.1f, 1.7f, 0.85f)
            voci.forEachIndexed { i, (voce, azione) ->
                val (tasto, nome, icona) = voce
                val intestazione = Ui.riga(s).apply {
                    addView(Ui.testo(s, 28, NERO, Ui.monoGrassetto, 28, tasto).apply {
                        background = Ui.bordo(NERO, 2)
                        setPadding(6, 4, 6, 4)
                    })
                    addView(View(s), lp(0, 1, 1f))
                    addView(Ui.icona(s, icona, NERO), lp(40, 40))
                }
                val cella = Ui.colonna(s, 6, Gravity.CENTER_VERTICAL).apply {
                    setBackgroundColor(VERDE)
                    setPadding(16, 0, 16, 0)
                    addView(intestazione, lp(TUTTO, ADATTA))
                    addView(Ui.testo(s, 40, NERO, vt, 40, nome, maiuscolo = true))
                }
                riga.addView(Ui.premibile(cella, ONDA_SCURA, nome) { azione() }, lp(0, TUTTO, pesi[i]))
            }
            return riga
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
            val calendario = Calendar.getInstance().apply { time = adesso }
            val giorno = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val ore = calendario.get(Calendar.HOUR_OF_DAY)
            val segno = giorno * 24 + ore
            if (segno == ultimaOra) return
            ultimaOra = segno
            giorni.forEachIndexed { i, g ->
                val oggi = i == giorno
                g.setTextColor(if (oggi) NERO else ATTENUATO)
                g.typeface = if (oggi) Ui.monoGrassetto else Ui.mono
                g.background = if (oggi) Ui.pieno(VERDE) else null
            }
            strisciaGiorni.contentDescription = "Giorno della settimana: ${SimpleDateFormat("EEEE", Locale.ITALIAN).format(adesso)}"
            oreCelle.forEachIndexed { i, c ->
                c.background = when {
                    i < ore -> Ui.pieno(ATTENUATO)
                    i == ore -> Ui.pieno(VERDE)
                    else -> Ui.bordo(BORDO, 2)
                }
            }
        }

        private fun mostraPioggia(testo: String?) {
            if (testo == null) {
                pioggia.visibility = View.GONE
                return
            }
            pioggia.text = testo
            pioggia.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (testo.length > 18) 28f * 18 / testo.length else 28f)
            pioggia.visibility = View.VISIBLE
        }

        override fun mostraMeteo(meteo: Meteo?) {
            mostraPioggia(FonteMeteo.testoPioggia(meteo?.pioggiaTraMinuti))
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "IN ATTESA"
                estremi.text = " "
                barra.contentDescription = null
                barra.visibility = View.INVISIBLE
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
            minima.text = "${meteo.minima}°"
            massima.text = "${meteo.massima}°"
            val ampiezza = (meteo.massima - meteo.minima).coerceAtLeast(1)
            val attuale = ((meteo.gradi - meteo.minima) * 8f / ampiezza).roundToInt().coerceIn(0, 8)
            celle.forEachIndexed { i, cella ->
                cella.background = when {
                    i < attuale -> Ui.pieno(ATTENUATO)
                    i == attuale -> Ui.pieno(VERDE)
                    else -> Ui.bordo(ATTENUATO, 2)
                }
            }
            barra.contentDescription = "Temperatura attuale ${meteo.gradi}° tra minima ${meteo.minima}° e massima ${meteo.massima}°"
            barra.visibility = View.VISIBLE
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) {
                this.musica.interno.background = Ui.bordo(BORDO, 2)
                this.musica.etichetta.text = "[ 03 · SPOTIFY ]"
                return
            }
            val stato = if (musica.inRiproduzione) "IN RIPRODUZIONE" else "IN PAUSA"
            this.musica.interno.background = Ui.bordo(if (musica.inRiproduzione) VERDE else BORDO, 2)
            this.musica.etichetta.text = "[ 03 · ${musica.app.uppercase(Locale.ITALIAN)} ▸ $stato ]"
            titolo.text = musica.titolo
            artista.text = "artista: ${musica.artista}"
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
        }

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> wifi.mostra(R.drawable.ic_wifi_spento, "spento", 0)
                StatoWifi.ACCESO -> wifi.mostra(R.drawable.ic_wifi, "acceso", 1)
                StatoWifi.CONNESSO -> wifi.mostra(R.drawable.ic_wifi, "connesso", 2)
            }
            if (stato.bluetooth) bluetooth.mostra(R.drawable.ic_bluetooth, "acceso", 2)
            else bluetooth.mostra(R.drawable.ic_bluetooth_spento, "spento", 0)
            val accese = if (stato.volumeMassimo > 0) {
                (stato.volume * TACCHE.toFloat() / stato.volumeMassimo).roundToInt().coerceIn(0, TACCHE)
            } else 0
            numeroVolume.text = String.format(Locale.US, "%02d", stato.volume)
            tacche.forEachIndexed { i, tacca ->
                tacca.background = if (TACCHE - i <= accese) Ui.pieno(VERDE) else Ui.bordo(BORDO, 2)
            }
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiMatrix(s, azioni)

    private class BenzinaiMatrix(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val vt = vt(s)
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 30).apply { setPadding(0, 30, 0, 0) }
        private lateinit var scheda: Riquadro
        private val indirizzo = Ui.adatta(Ui.testo(s, 76, VERDE, vt, contenuto = "", maiuscolo = true, righe = 2), 40, 76)
        private val dettagli = Ui.testo(s, 28, VERDE, Ui.mono, 34)
        private val spesa = Ui.testo(s, 28, ATTENUATO, Ui.mono, 32).apply { visibility = View.GONE }
        private val stato = StatoBenzinai()

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s).apply {
                setBackgroundColor(NERO)
                setPadding(20, 28, 20, 20)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(rigaOrdini(), lp(TUTTO, 120).apply { topMargin = 20 })
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 172).apply { topMargin = 30 })
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
            val titolo = Ui.riga(s, 14)
            titolo.addView(Ui.testo(s, 68, VERDE, vt, 62, "~$ benzinai"))
            titolo.addView(View(s).apply { setBackgroundColor(VERDE) }, lp(24, 48))
            riga.addView(titolo, lp(0, ADATTA, 1f))
            val testi = Ui.colonna(s, 4)
            testi.addView(Ui.testo(s, 44, VERDE, vt, 40, "RIFORNIMENTI"))
            testi.addView(spesa)
            val rifornimenti = Ui.riga(s, 16).apply {
                background = Ui.bordo(VERDE, 2)
                setPadding(24, 0, 24, 0)
                addView(Ui.icona(s, R.drawable.ic_rifornimenti, VERDE), lp(48, 48))
                addView(testi)
            }
            riga.addView(Ui.premibile(rifornimenti, ONDA, "Rifornimenti") { azioni.rifornimenti() }, lp(ADATTA, TUTTO))
            return riga
        }

        private fun rigaOrdini(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, "ordina:"))
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = Ui.testo(s, 44, VERDE, vt, 40, nome, maiuscolo = true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                riga.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(0, TUTTO, 1f))
            }
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
            scheda = riquadro(s, "[ SCHEDA ]", verticale = false, bordo = VERDE, sopra = 26, sotto = 14)
            scheda.interno.gravity = Gravity.CENTER_VERTICAL
            val testi = Ui.colonna(s)
            testi.addView(indirizzo, lp(TUTTO, 88))
            testi.addView(dettagli, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            scheda.interno.addView(testi, lp(0, ADATTA, 1f))
            scheda.interno.addView(
                tasto(s, R.drawable.ic_chiudi, false, 52, "Chiudi scheda") { seleziona(null) },
                lp(120, 120).apply { leftMargin = 24 },
            )
            scheda.esterno.visibility = View.GONE
            return scheda.esterno
        }

        override fun mostra(elenco: ElencoBenzinai) {
            stato.ultimo = elenco
            disegna()
        }

        override fun mostraSpesa(testo: String?) {
            spesa.text = testo ?: ""
            spesa.visibility = if (testo == null) View.GONE else View.VISIBLE
        }

        private fun seleziona(d: Distributore?) {
            stato.alterna(d)
            disegna()
        }

        private fun disegna() {
            elencoView.removeAllViews()
            when (val e = stato.ultimo) {
                is ElencoBenzinai.Caricamento -> messaggio("~$ benzinai --aggiorna", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("~$ posizione", Ui.POSIZIONE_NON_DISPONIBILE)
                is ElencoBenzinai.Errore -> messaggio("~$ benzinai", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("[ ★ PREFERITI ]", "[ ${e.preferiti.size} ]", e.preferiti, true)
                    val nomeOrdine = when (e.ordine) {
                        Ordine.CONVIENE -> "CONVIENE"
                        Ordine.PREZZO -> "PREZZO"
                        Ordine.DISTANZA -> "DISTANZA"
                    }
                    if (e.vicini.isNotEmpty()) sezione("[ VICINI ]", "[ ORDINE: $nomeOrdine ]", e.vicini, false)
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

        private fun sezione(titolo: String, destra: String, distributori: List<Distributore>, luogo: Boolean) {
            val r = riquadro(s, titolo, destra = destra, sopra = 30, sotto = 18)
            r.interno.apply {
                dividerDrawable = GradientDrawable().apply { setSize(20, 20) }
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            }
            distributori.forEachIndexed { i, d -> r.interno.addView(riga(d, luogo, i), lp(TUTTO, 120)) }
            elencoView.addView(r.esterno, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, posizione: Int): View {
            val adesso = System.currentTimeMillis()
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 20).apply {
                background = if (scelto) Ui.bordo(VERDE, 3) else Ui.bordo(BORDO, 2)
                setPadding(18, 0, 26, 0)
            }
            val marcatore = when {
                scelto -> ">"
                luogo -> "*"
                else -> "${posizione + 1}"
            }
            corpo.addView(
                Ui.testo(s, 60, if (scelto) VERDE else ATTENUATO, vt, 50, marcatore).apply { gravity = Gravity.CENTER },
                lp(34, ADATTA),
            )
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 54, VERDE, vt, 50, if (luogo) d.comune else d.nome, maiuscolo = true))
            sinistra.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            val destra = Ui.colonna(s, 0, Gravity.END)
            val primaLinea = Ui.riga(s, 16)
            DatiMimit.andamento(d)?.let { a ->
                val voce = Ui.riga(s, 6).apply {
                    contentDescription = "Prezzo ${if (a.salito) "salito" else "sceso"} di ${a.differenza}"
                    addView(Ui.icona(s, if (a.salito) R.drawable.ic_su else R.drawable.ic_giu, ATTENUATO), lp(22, 22))
                    addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 34, a.differenza))
                }
                primaLinea.addView(voce)
            }
            val prezzo = Ui.riga(s, 0, Gravity.BOTTOM)
            prezzo.addView(Ui.testo(s, 76, VERDE, vt, 62).apply {
                val cifre = Benzinai.prezzo(d.prezzo)
                text = SpannableString(cifre).apply {
                    setSpan(RelativeSizeSpan(52f / 76f), cifre.length - 1, cifre.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            })
            prezzo.addView(Ui.testo(s, 28, ATTENUATO, Ui.mono, 28, " €/l"))
            primaLinea.addView(prezzo)
            destra.addView(primaLinea, lp(ADATTA, ADATTA).apply { gravity = Gravity.END })
            destra.addView(
                Ui.testo(s, 30, ATTENUATO, Ui.mono, 36, Benzinai.distanza(d.distanzaKm)),
                lp(ADATTA, ADATTA).apply { gravity = Gravity.END },
            )
            corpo.addView(destra)
            return Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
        }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.esterno.visibility = View.GONE
                return
            }
            scheda.esterno.visibility = View.VISIBLE
            scheda.etichetta.text = "[ > ${d.nome.uppercase(Locale.ITALIAN)} · ${d.marchio.uppercase(Locale.ITALIAN)} ]"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            val prezzo = "${Benzinai.prezzo(d.prezzo)} €/l"
            val aggiornato = "aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
            val testo = "self $prezzo · ${Benzinai.distanza(d.distanzaKm)} · $aggiornato"
            val inizio = testo.indexOf(prezzo)
            dettagli.text = SpannableString(testo).apply {
                setSpan(StyleSpan(Typeface.BOLD), inizio, inizio + prezzo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(ATTENUATO), testo.length - aggiornato.length, testo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
