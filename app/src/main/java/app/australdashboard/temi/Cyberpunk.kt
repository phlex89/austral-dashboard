package app.australdashboard.temi

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
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

object Cyberpunk : Tema {
    override val id = "cyberpunk"
    override val nome = "Cyberpunk"
    override val idea = "Giallo neon, angoli smussati"

    private const val SFONDO = 0xFF0A0A0F.toInt()
    private const val PANNELLO = 0xFF14141C.toInt()
    private const val TASTO = 0xFF22222E.toInt()
    private const val SELEZIONATO = 0xFF2C2C3D.toInt()
    private const val TESTO = 0xFFF2F2F7.toInt()
    private const val SECONDARIO = 0xFFA3A3B8.toInt()
    private const val GIALLO = 0xFFFCEE0A.toInt()
    private const val ONDA = 0x33FCEE0A
    private const val ONDA_SCURA = 0x330A0A0F

    private fun font(context: Context, grassetto: Boolean): Typeface =
        Ui.font(context, if (grassetto) R.font.rajdhani_bold else R.font.rajdhani_semibold)

    override fun palette(context: Context) =
        Palette(SFONDO, TESTO, SECONDARIO, GIALLO, SFONDO, TASTO, font(context, true), font(context, false))

    private fun testo(
        c: Context,
        px: Int,
        colore: Int,
        grassetto: Boolean,
        riga: Int,
        contenuto: String = "",
        maiuscolo: Boolean = false,
        righe: Int = 1,
        spaziatura: Int = 0,
    ): TextView = Ui.testo(c, px, colore, font(c, grassetto), riga, contenuto, maiuscolo, righe).apply {
        if (spaziatura > 0) letterSpacing = spaziatura / px.toFloat()
    }

    private fun taglia(v: View, colore: Int, alto: Int, basso: Int = alto): View {
        v.background = SmussoCyb(colore, alto.toFloat(), basso.toFloat())
        return tagliaImmagine(v, alto, basso)
    }

    private fun tagliaImmagine(v: View, alto: Int, basso: Int = alto): View {
        v.outlineProvider = ContornoCyb(alto.toFloat(), basso.toFloat())
        v.clipToOutline = true
        return v
    }

    private fun codice(c: Context, contenuto: String) = testo(c, 28, SECONDARIO, false, 32, contenuto, spaziatura = 3)

    private fun etichetta(c: Context, contenuto: String): TextView =
        testo(c, 28, SFONDO, true, 28, contenuto, spaziatura = 2).apply {
            gravity = Gravity.CENTER_VERTICAL
            taglia(this, GIALLO, 12)
            setPadding(18, 0, 18, 0)
        }

    private fun tasto(
        c: Context,
        icona: ImageView,
        lato: Int,
        sfondo: Int,
        taglio: Int,
        descrizione: String?,
        onda: Int,
        azione: () -> Unit,
    ): FrameLayout {
        val f = FrameLayout(c)
        f.addView(icona, lpf(lato, lato, Gravity.CENTER))
        taglia(f, sfondo, taglio)
        Ui.premibile(f, onda, descrizione, azione)
        return f
    }

    private fun tasto(
        c: Context,
        icona: Int,
        lato: Int,
        sfondo: Int,
        colore: Int,
        taglio: Int,
        descrizione: String?,
        onda: Int,
        azione: () -> Unit,
    ): FrameLayout = tasto(c, Ui.icona(c, icona, colore), lato, sfondo, taglio, descrizione, onda, azione)

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeCyberpunk(s, azioni)

    private class HomeCyberpunk(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val ora = testo(s, 168, TESTO, true, 150)
        private val data = Ui.adatta(testo(s, 32, TESTO, false, 38, maiuscolo = true, spaziatura = 2), 22, 32)
        private val gradi = testo(s, 124, TESTO, true, 112, "--°")
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, GIALLO)
        private val cielo = Ui.adatta(testo(s, 34, TESTO, true, 38, "In attesa", maiuscolo = true), 20, 34)
        private val estremi = Ui.adatta(testo(s, 30, SECONDARIO, false, 34, "max --° · min --°", maiuscolo = true), 20, 30)
        private val copertina = ImageView(s).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val inAscolto = Ui.colonna(s)
        private val fermo = Ui.colonna(s)
        private val chipAscolto = etichetta(s, "// IN RIPRODUZIONE")
        private val titolo = Ui.adatta(
            testo(s, 70, TESTO, true, 66, maiuscolo = true).apply { gravity = Gravity.CENTER_VERTICAL },
            30,
            70,
        )
        private val artista = testo(s, 34, SECONDARIO, false, 40, maiuscolo = true, spaziatura = 4)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, SFONDO)
        private lateinit var play: View

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(servizi(), lp(TUTTO, 120))
            val corpo = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            corpo.addView(pannelloMusica(), lp(700, TUTTO))
            val destra = Ui.colonna(s, 20)
            destra.addView(boxOra(), lp(TUTTO, 300))
            destra.addView(boxMeteo(), lp(TUTTO, 220))
            destra.addView(griglia(), lp(TUTTO, 0, 1f))
            corpo.addView(destra, lp(0, TUTTO, 1f))
            radice.addView(corpo, lp(TUTTO, 0, 1f))
            return radice
        }

        private fun servizi(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val voci = listOf(
                Triple(R.drawable.ic_benzinaio, "Benzinai", azioni::benzinai),
                Triple(R.drawable.ic_griglia, "Tutte le app", azioni::tutteLeApp),
                Triple(R.drawable.ic_cursori, "Impostazioni Android", azioni::impostazioniAndroid),
                Triple(R.drawable.ic_ingranaggio, "Launcher", azioni::impostazioniLauncher),
            )
            val larghezze = listOf(252, 278, 380, 226)
            voci.forEachIndexed { i, (icona, nome, azione) ->
                val cella = Ui.riga(s, 14).apply { setPadding(22, 0, 22, 0) }
                cella.addView(Ui.icona(s, icona, GIALLO), lp(48, 48))
                val etichetta = Ui.adatta(
                    testo(s, 32, TESTO, true, 34, nome, maiuscolo = true, righe = if (' ' in nome) 2 else 1, spaziatura = 1)
                        .apply { gravity = Gravity.CENTER_VERTICAL },
                    20,
                    32,
                )
                cella.addView(etichetta, lp(0, TUTTO, 1f))
                taglia(cella, PANNELLO, 22)
                cella.setPadding(22, 0, 22, 0)
                val descrizione = if (i == 3) "Impostazioni del launcher" else nome
                Ui.premibile(cella, ONDA, descrizione) { azione() }
                riga.addView(cella, lp(larghezze[i], TUTTO))
            }
            return riga
        }

        private fun boxOra(): View {
            val p = Ui.colonna(s)
            p.addView(codice(s, "// 01 · ORA LOCALE"))
            p.addView(ora.apply { gravity = Gravity.CENTER_VERTICAL }, lp(ADATTA, 172).apply { topMargin = 6 })
            p.addView(View(s), lp(TUTTO, 0, 1f))
            p.addView(data, lp(TUTTO, 38))
            taglia(p, PANNELLO, 28, 0)
            p.setPadding(30, 26, 30, 26)
            return p
        }

        private fun boxMeteo(): View {
            val p = Ui.colonna(s)
            p.addView(codice(s, "// 02 · METEO"))
            p.addView(View(s), lp(TUTTO, 0, 1f))
            val riga = Ui.riga(s, 24)
            riga.addView(gradi, lp(ADATTA, ADATTA))
            val colonna = Ui.colonna(s, 6)
            val rigaCielo = Ui.riga(s, 10)
            rigaCielo.addView(iconaMeteo, lp(48, 48))
            rigaCielo.addView(cielo, lp(0, 40, 1f))
            colonna.addView(rigaCielo, lp(TUTTO, 48))
            colonna.addView(estremi, lp(TUTTO, 36))
            riga.addView(colonna, lp(0, ADATTA, 1f))
            p.addView(riga, lp(TUTTO, ADATTA))
            taglia(p, PANNELLO, 28, 0)
            p.setPadding(30, 22, 30, 22)
            return p
        }

        private fun griglia(): View {
            val g = Ui.colonna(s, 20)
            AppHome.elenco.chunked(2).forEach { coppia ->
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                coppia.forEach { riga.addView(cellaApp(it), lp(0, TUTTO, 1f)) }
                g.addView(riga, lp(TUTTO, 0, 1f))
            }
            return g
        }

        private fun cellaApp(app: AppHome): View {
            val c = Ui.colonna(s, 8, Gravity.CENTER)
            val sigla = testo(s, 44, GIALLO, true, 44, app.sigla).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(GIALLO, 2)
            }
            c.addView(sigla, lp(64, 64))
            val nome = Ui.adatta(
                testo(s, 30, TESTO, true, 32, app.nome, maiuscolo = true, spaziatura = 1).apply { gravity = Gravity.CENTER },
                18,
                30,
            )
            c.addView(nome, lp(TUTTO, 34))
            taglia(c, PANNELLO, 16)
            c.setPadding(6, 0, 6, 0)
            val installata = app.intent(s) != null
            if (!installata) c.alpha = 0.35f
            Ui.premibile(c, ONDA, app.nome) { if (installata) azioni.apri(app) }
            return c
        }

        private fun pannelloMusica(): View {
            val p = FrameLayout(s)

            inAscolto.setPadding(30, 30, 30, 30)
            inAscolto.addView(chipAscolto, lp(ADATTA, 42))
            copertina.setBackgroundColor(TASTO)
            tagliaImmagine(copertina, 24)
            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            inAscolto.addView(copertina, lp(560, 560).apply { topMargin = 18 })
            val info = Ui.colonna(s)
            info.addView(titolo, lp(TUTTO, 72))
            info.addView(artista, lp(TUTTO, 40))
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(TUTTO, ADATTA).apply { topMargin = 20 })
            inAscolto.addView(View(s), lp(TUTTO, 0, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(
                tasto(s, R.drawable.ic_precedente, 52, TASTO, TESTO, 18, "Brano precedente", ONDA) { azioni.precedente() },
                lp(160, 140),
            )
            play = tasto(s, iconaPlay, 64, GIALLO, 18, "Pausa", ONDA_SCURA) { azioni.playPausa() }
            comandi.addView(play, lp(0, 140, 1f))
            comandi.addView(
                tasto(s, R.drawable.ic_successivo, 52, TASTO, TESTO, 18, "Brano successivo", ONDA) { azioni.successivo() },
                lp(160, 140),
            )
            inAscolto.addView(comandi, lp(TUTTO, 140))

            fermo.setPadding(30, 30, 30, 30)
            fermo.addView(etichetta(s, "// SPOTIFY"), lp(ADATTA, 42))
            val grande = Ui.adatta(testo(s, 150, TESTO, true, 140, "Spotify", maiuscolo = true), 80, 150)
            fermo.addView(grande, lp(TUTTO, 140).apply { topMargin = 40 })
            fermo.addView(View(s), lp(TUTTO, 0, 1f))
            val comandiFermo = Ui.riga(s, 20)
            comandiFermo.addView(
                tasto(s, R.drawable.ic_play, 64, GIALLO, SFONDO, 18, "Riproduci", ONDA_SCURA) { azioni.playPausa() },
                lp(0, 140, 1f),
            )
            val apri = testo(s, 34, TESTO, true, 38, "Apri Spotify", maiuscolo = true, spaziatura = 2).apply {
                gravity = Gravity.CENTER
                taglia(this, TASTO, 18)
            }
            comandiFermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(0, 140, 1.4f))
            fermo.addView(comandiFermo, lp(TUTTO, 140))

            p.addView(inAscolto, lpf(TUTTO, TUTTO))
            p.addView(fermo, lpf(TUTTO, TUTTO))
            taglia(p, PANNELLO, 40)
            mostraMusica(null)
            return p
        }

        override fun mostraOra(adesso: Date) {
            val t = SpannableString(SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso))
            t.setSpan(ForegroundColorSpan(GIALLO), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ora.text = t
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) return
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            val stato = if (musica.inRiproduzione) "IN RIPRODUZIONE" else "IN PAUSA"
            chipAscolto.text = "// $stato · ${musica.app.uppercase(Locale.ITALIAN)}"
            titolo.text = musica.titolo
            artista.text = musica.artista
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
            play.contentDescription = if (musica.inRiproduzione) "Pausa" else "Riproduci"
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiCyberpunk(s, azioni)

    private class BenzinaiCyberpunk(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: View
        private val codiceScheda = testo(s, 30, SFONDO, true, 34, maiuscolo = true, spaziatura = 3)
        private val indirizzo = Ui.adatta(testo(s, 72, SFONDO, true, 70, maiuscolo = true, righe = 2), 32, 72)
        private val dettagli = Ui.adatta(testo(s, 32, SFONDO, true, 36, maiuscolo = true), 20, 32)
        private val stellaScheda = Ui.icona(s, R.drawable.ic_stella, GIALLO)
        private lateinit var tastoStella: View
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
        private var preferiti: Set<Long> = emptySet()
        private var selezionato: Distributore? = null

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 270))
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(
                tasto(s, R.drawable.ic_indietro, 52, PANNELLO, TESTO, 20, "Indietro", ONDA) { azioni.indietro() },
                lp(120, 120),
            )
            val titoli = Ui.colonna(s)
            titoli.addView(codice(s, "// CARBURANTE · SELF"))
            titoli.addView(testo(s, 84, TESTO, true, 78, "Benzinai", maiuscolo = true, spaziatura = 1))
            riga.addView(titoli, lp(0, ADATTA, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val t = testo(s, 32, TESTO, true, 36, nome, maiuscolo = true, spaziatura = 2).apply { gravity = Gravity.CENTER }
                ordini[ordine] = t
                Ui.premibile(t, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }
                gruppo.addView(t, lp(190, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, t) in ordini) {
                val scelto = ordine == attivo
                taglia(t, if (scelto) GIALLO else PANNELLO, 18)
                t.setTextColor(if (scelto) SFONDO else TESTO)
                t.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            val p = Ui.riga(s, 28)
            val testi = Ui.colonna(s, 8)
            testi.addView(codiceScheda, lp(TUTTO, 34))
            testi.addView(indirizzo, lp(TUTTO, 0, 1f))
            testi.addView(dettagli, lp(TUTTO, 36))
            p.addView(testi, lp(0, TUTTO, 1f))
            val comandi = Ui.riga(s, 20)
            tastoStella = tasto(s, stellaScheda, 56, SFONDO, 18, "Salva tra i preferiti", ONDA) {
                selezionato?.let(azioni::cambiaPreferito)
            }
            comandi.addView(tastoStella, lp(120, 120))
            comandi.addView(
                tasto(s, R.drawable.ic_chiudi, 52, SFONDO, GIALLO, 18, "Chiudi scheda", ONDA) { seleziona(null) },
                lp(120, 120),
            )
            p.addView(comandi, lp(ADATTA, ADATTA))
            scheda = taglia(p, GIALLO, 40)
            p.setPadding(36, 24, 36, 24)
            scheda.visibility = View.GONE
            return scheda
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
                is ElencoBenzinai.Caricamento -> messaggio("// BENZINAI · AGGIORNAMENTO", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione ->
                    messaggio("// POSIZIONE", "Posizione non disponibile. Scegli una città di riserva nelle Impostazioni del launcher.")
                is ElencoBenzinai.Errore -> messaggio("// ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
                    if (e.preferiti.isNotEmpty()) {
                        sezione("★ ${e.preferiti.size.toString().padStart(2, '0')}", "Preferiti", e.preferiti)
                    }
                    val nomeOrdine = when (e.ordine) {
                        Ordine.CONVIENE -> "CONVIENE"
                        Ordine.PREZZO -> "PREZZO"
                        Ordine.DISTANZA -> "DISTANZA"
                    }
                    if (e.vicini.isNotEmpty()) sezione("ORDINE: $nomeOrdine", "Vicini", e.vicini)
                    if (tutti.isEmpty()) messaggio("// BENZINAI", "Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(comando: String, contenuto: String) {
            val p = Ui.colonna(s, 12)
            p.addView(codice(s, comando))
            p.addView(testo(s, 54, TESTO, true, 56, contenuto, righe = 3), lp(TUTTO, ADATTA))
            taglia(p, PANNELLO, 28, 0)
            p.setPadding(30, 26, 30, 26)
            elencoView.addView(p, lp(TUTTO, ADATTA))
        }

        private fun sezione(chip: String, titolo: String, distributori: List<Distributore>) {
            val g = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 16)
            intestazione.addView(etichetta(s, chip), lp(ADATTA, 42))
            intestazione.addView(testo(s, 42, TESTO, true, 48, titolo, maiuscolo = true, spaziatura = 2), lp(0, ADATTA, 1f))
            g.addView(intestazione, lp(TUTTO, 48))
            val righe = Ui.colonna(s, 20)
            distributori.forEach { righe.addView(riga(it), lp(TUTTO, 120)) }
            g.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(g, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpo = Ui.riga(s, 24)
            val sinistra = Ui.colonna(s)
            sinistra.addView(testo(s, 44, TESTO, true, 48, d.nome, maiuscolo = true), lp(TUTTO, 48))
            sinistra.addView(
                testo(s, 28, SECONDARIO, false, 34, "${d.marchio} // aggiornato ${Benzinai.quando(d.aggiornato, adesso)}", spaziatura = 1),
                lp(TUTTO, 34),
            )
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(testo(s, 34, TESTO, true, 38, Benzinai.distanza(d.distanzaKm)), lp(ADATTA, ADATTA))
            val prezzo = SpannableString("${Benzinai.prezzo(d.prezzo)} €/L")
            val inizio = prezzo.length - 4
            prezzo.setSpan(AbsoluteSizeSpan(28), inizio, prezzo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            prezzo.setSpan(ForegroundColorSpan(SECONDARIO), inizio, prezzo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            corpo.addView(testo(s, 68, GIALLO, true, 64).apply { text = prezzo }, lp(ADATTA, ADATTA))
            taglia(corpo, if (scelto) SELEZIONATO else PANNELLO, 20)
            corpo.setPadding(30, 0, 30, 0)
            Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
            riga.addView(corpo, lp(0, TUTTO, 1f))
            val preferito = d.id in preferiti
            val descrizione = if (preferito) "Rimuovi dai preferiti" else "Salva tra i preferiti"
            riga.addView(
                tasto(
                    s,
                    if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella,
                    56,
                    PANNELLO,
                    if (preferito) GIALLO else SECONDARIO,
                    20,
                    descrizione,
                    ONDA,
                ) { azioni.cambiaPreferito(d) },
                lp(120, 120),
            )
            return riga
        }

        private fun aggiornaScheda() {
            val d = selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            val adesso = System.currentTimeMillis()
            codiceScheda.text = "// SCHEDA · ${d.nome.uppercase(Locale.ITALIAN)} · ${d.marchio.uppercase(Locale.ITALIAN)}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            dettagli.text = "self ${Benzinai.prezzo(d.prezzo)} €/L // ${Benzinai.distanza(d.distanzaKm)} // aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"
            val preferito = d.id in preferiti
            stellaScheda.setImageResource(if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella)
            tastoStella.contentDescription = if (preferito) "Rimuovi dai preferiti" else "Salva tra i preferiti"
        }
    }
}

private fun percorsoSmussoCyb(l: Float, t: Float, r: Float, b: Float, alto: Float, basso: Float): Path = Path().apply {
    moveTo(l, t)
    lineTo(r - alto, t)
    lineTo(r, t + alto)
    lineTo(r, b)
    lineTo(l + basso, b)
    lineTo(l, b - basso)
    close()
}

private class ContornoCyb(private val alto: Float, private val basso: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        if (view.width <= 0 || view.height <= 0) return
        outline.setPath(percorsoSmussoCyb(0f, 0f, view.width.toFloat(), view.height.toFloat(), alto, basso))
    }
}

private class SmussoCyb(colore: Int, private val alto: Float, private val basso: Float) : Drawable() {
    private val percorso = Path()
    private val pennello = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = colore }

    override fun onBoundsChange(b: Rect) {
        percorso.set(percorsoSmussoCyb(b.left.toFloat(), b.top.toFloat(), b.right.toFloat(), b.bottom.toFloat(), alto, basso))
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(percorso, pennello)
    }

    override fun setAlpha(alpha: Int) {
        pennello.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        pennello.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
