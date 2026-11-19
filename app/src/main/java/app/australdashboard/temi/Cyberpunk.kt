package app.australdashboard.temi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import java.util.Calendar
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
    private const val CONTORNO = 0xFF3A3A4D.toInt()
    private const val ONDA = 0x33FCEE0A
    private const val ONDA_SCURA = 0x330A0A0F

    private fun font(context: Context, grassetto: Boolean): Typeface =
        Ui.font(context, if (grassetto) R.font.rajdhani_bold else R.font.rajdhani_semibold)

    override fun palette(context: Context) =
        Palette(SFONDO, TESTO, GIALLO, TASTO, font(context, true), font(context, false))

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

    private fun taglia(v: View, colore: Int, alto: Int, basso: Int = alto, contorno: Int = 0, linguetta: Int = 0): View {
        v.background = SmussoCyb(colore, alto.toFloat(), basso.toFloat(), contorno, linguetta, GIALLO)
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
        contorno: Int = 0,
        azione: () -> Unit,
    ): FrameLayout {
        val f = FrameLayout(c)
        f.addView(icona, lpf(lato, lato, Gravity.CENTER))
        taglia(f, sfondo, taglio, contorno = contorno)
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
        contorno: Int = 0,
        azione: () -> Unit,
    ): FrameLayout = tasto(c, Ui.icona(c, icona, colore), lato, sfondo, taglio, descrizione, onda, contorno, azione)

    private fun filetto(c: Context, linea: Int, tacca: Int): View = FormaCyb(c) { canvas, w, _, p ->
        p.color = linea
        canvas.drawRect(0f, 2f, w, 4f, p)
        p.color = tacca
        canvas.drawRect(0f, 0f, 48f, 6f, p)
    }

    private fun triangolo(c: Context): View = FormaCyb(c) { canvas, w, h, p ->
        p.color = GIALLO
        val t = Path()
        t.moveTo(w, 0f)
        t.lineTo(w, h)
        t.lineTo(0f, h)
        t.close()
        canvas.drawPath(t, p)
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeCyberpunk(s, azioni)

    private class HomeCyberpunk(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val ora = testo(s, 168, TESTO, true, 150)
        private val data = Ui.adatta(testo(s, 32, TESTO, false, 38, maiuscolo = true, spaziatura = 2), 22, 32)
        private val gradi = testo(s, 124, TESTO, true, 112, "--°")
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, GIALLO)
        private val cielo = Ui.adatta(testo(s, 34, TESTO, true, 38, "In attesa", maiuscolo = true), 20, 34)
        private val estremi = Ui.adatta(testo(s, 30, SECONDARIO, false, 34, "max --° · min --°", maiuscolo = true), 20, 30)
        private val copertina = CopertinaCyb(s, GIALLO).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val inAscolto = Ui.colonna(s)
        private val fermo = Ui.colonna(s)
        private val chipAscolto = etichetta(s, "// IN RIPRODUZIONE")
        private val titolo = Ui.adatta(
            testo(s, 70, TESTO, true, 66, maiuscolo = true).apply { gravity = Gravity.CENTER_VERTICAL },
            30,
            70,
        )
        private val artista = testo(s, 34, SECONDARIO, false, 40, maiuscolo = true, spaziatura = 4)
        private val appMusica = Ui.adatta(codice(s, "").apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL }, 18, 28)
        private val settimana = Ui.riga(s, 2)
        private val giorni = Array(7) { i ->
            testo(s, 28, SECONDARIO, true, 28, "LMMGVSD"[i].toString()).apply { gravity = Gravity.CENTER }
        }
        private val tacche = TaccheCyb(s, TESTO, GIALLO, CONTORNO)
        private val asta = AstaCyb(s, CONTORNO, TESTO, SECONDARIO, GIALLO)
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
            val larghezze = listOf(252, 265, 366, 252)
            voci.forEachIndexed { i, (icona, nome, azione) ->
                val cella = Ui.riga(s, 12)
                cella.addView(Ui.icona(s, icona, GIALLO), lp(44, 44))
                val etichetta = Ui.adatta(
                    testo(s, 30, TESTO, true, 32, nome, maiuscolo = true, righe = if (' ' in nome) 2 else 1, spaziatura = 1)
                        .apply { gravity = Gravity.CENTER_VERTICAL },
                    20,
                    30,
                )
                cella.addView(etichetta, lp(0, TUTTO, 1f))
                taglia(cella, PANNELLO, 22, linguetta = 6)
                cella.setPadding(26, 0, 16, 0)
                val descrizione = if (i == 3) "Impostazioni del launcher" else nome
                Ui.premibile(cella, ONDA, descrizione) { azione() }
                riga.addView(cella, lp(larghezze[i], TUTTO))
            }
            return riga
        }

        private fun boxOra(): View {
            val p = Ui.colonna(s)
            val intestazione = Ui.riga(s)
            intestazione.addView(codice(s, "// 01 · ORA"), lp(0, ADATTA, 1f))
            giorni.forEach {
                it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                settimana.addView(it, lp(30, 32))
            }
            intestazione.addView(settimana, lp(ADATTA, 32))
            p.addView(intestazione, lp(TUTTO, 32))
            p.addView(ora.apply { gravity = Gravity.CENTER_VERTICAL }, lp(ADATTA, 172).apply { topMargin = 6 })
            p.addView(tacche, lp(TUTTO, 14).apply { topMargin = -14 })
            p.addView(View(s), lp(TUTTO, 0, 1f))
            p.addView(data, lp(TUTTO, 38))
            taglia(p, PANNELLO, 28, 0, CONTORNO)
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
            p.addView(riga, lp(TUTTO, 112))
            p.addView(asta, lp(TUTTO, 14).apply { topMargin = 10 })
            taglia(p, PANNELLO, 28, 0, CONTORNO)
            p.setPadding(30, 22, 30, 22)
            return p
        }

        private fun griglia(): View {
            val g = Ui.colonna(s, 20)
            AppHome.elenco(s).withIndex().chunked(2).forEach { coppia ->
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                coppia.forEach { riga.addView(cellaApp(it.value, it.index + 1), lp(0, TUTTO, 1f)) }
                g.addView(riga, lp(TUTTO, 0, 1f))
            }
            return g
        }

        private fun cellaApp(app: AppHome, numero: Int): View {
            val c = FrameLayout(s)
            val centro = Ui.colonna(s, 8, Gravity.CENTER)
            val sigla = testo(s, 44, GIALLO, true, 44, app.sigla).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(GIALLO, 2)
            }
            centro.addView(sigla, lp(64, 64))
            val nome = Ui.adatta(
                testo(s, 30, TESTO, true, 32, app.nome, maiuscolo = true, spaziatura = 1).apply { gravity = Gravity.CENTER },
                18,
                30,
            )
            centro.addView(nome, lp(TUTTO, 34))
            c.addView(centro, lpf(TUTTO, TUTTO))
            c.addView(
                testo(s, 28, SECONDARIO, false, 30, numero.toString().padStart(2, '0')).apply {
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                },
                lpf(ADATTA, ADATTA).apply {
                    leftMargin = 12
                    topMargin = 6
                },
            )
            c.addView(triangolo(s), lpf(16, 16, Gravity.BOTTOM or Gravity.END))
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
            val testata = Ui.riga(s, 16)
            testata.addView(chipAscolto, lp(ADATTA, 42))
            testata.addView(appMusica, lp(0, 32, 1f))
            inAscolto.addView(testata, lp(TUTTO, 42))
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
            inAscolto.addView(filetto(s, CONTORNO, GIALLO), lp(TUTTO, 6))
            inAscolto.addView(View(s), lp(TUTTO, 0, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(
                tasto(s, R.drawable.ic_precedente, 52, TASTO, TESTO, 18, "Brano precedente", ONDA, CONTORNO) { azioni.precedente() },
                lp(160, 140),
            )
            play = tasto(s, iconaPlay, 64, GIALLO, 18, "Pausa", ONDA_SCURA) { azioni.playPausa() }
            comandi.addView(play, lp(0, 140, 1f))
            comandi.addView(
                tasto(s, R.drawable.ic_successivo, 52, TASTO, TESTO, 18, "Brano successivo", ONDA, CONTORNO) { azioni.successivo() },
                lp(160, 140),
            )
            inAscolto.addView(comandi, lp(TUTTO, 140))

            fermo.setPadding(30, 30, 30, 30)
            val testataFermo = Ui.riga(s, 16)
            testataFermo.addView(etichetta(s, "// SPOTIFY"), lp(ADATTA, 42))
            testataFermo.addView(
                codice(s, "// 03 · MUSICA").apply { gravity = Gravity.END or Gravity.CENTER_VERTICAL },
                lp(0, 32, 1f),
            )
            fermo.addView(testataFermo, lp(TUTTO, 42))
            val grande = Ui.adatta(testo(s, 150, TESTO, true, 140, "Spotify", maiuscolo = true), 80, 150)
            fermo.addView(grande, lp(TUTTO, 140).apply { topMargin = 40 })
            fermo.addView(View(s), lp(TUTTO, 0, 1f))
            fermo.addView(filetto(s, CONTORNO, GIALLO), lp(TUTTO, 6))
            fermo.addView(View(s), lp(TUTTO, 0, 1f))
            val comandiFermo = Ui.riga(s, 20)
            comandiFermo.addView(
                tasto(s, R.drawable.ic_play, 64, GIALLO, SFONDO, 18, "Riproduci", ONDA_SCURA) { azioni.playPausa() },
                lp(0, 140, 1f),
            )
            val apri = testo(s, 34, TESTO, true, 38, "Apri Spotify", maiuscolo = true, spaziatura = 2).apply {
                gravity = Gravity.CENTER
                taglia(this, TASTO, 18, contorno = CONTORNO)
            }
            comandiFermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(0, 140, 1.4f))
            fermo.addView(comandiFermo, lp(TUTTO, 140))

            p.addView(inAscolto, lpf(TUTTO, TUTTO))
            p.addView(fermo, lpf(TUTTO, TUTTO))
            taglia(p, PANNELLO, 40, contorno = GIALLO)
            mostraMusica(null)
            return p
        }

        override fun mostraOra(adesso: Date) {
            val t = SpannableString(SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso))
            t.setSpan(ForegroundColorSpan(GIALLO), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ora.text = t
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
            val calendario = Calendar.getInstance().apply { time = adesso }
            val oggi = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7
            giorni.forEachIndexed { i, g ->
                g.setTextColor(if (i == oggi) SFONDO else SECONDARIO)
                g.setBackgroundColor(if (i == oggi) GIALLO else Color.TRANSPARENT)
            }
            settimana.contentDescription = "Giorno della settimana: ${SimpleDateFormat("EEEE", Locale.ITALIAN).format(adesso)}"
            val ore = calendario.get(Calendar.HOUR_OF_DAY)
            tacche.mostra(ore)
            tacche.contentDescription = "Avanzamento della giornata: ore $ore su 24"
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) {
                asta.mostra(null)
                asta.contentDescription = null
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "In attesa"
                estremi.text = "max --° · min --°"
                return
            }
            val escursione = meteo.massima - meteo.minima
            asta.mostra(if (escursione > 0) Ui.frazione(meteo.gradi, meteo.minima, meteo.massima) else null)
            asta.contentDescription =
                "Temperatura attuale ${meteo.gradi}° tra minima ${meteo.minima}° e massima ${meteo.massima}°"
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            chipAscolto.text = if (musica.inRiproduzione) "// IN RIPRODUZIONE" else "// IN PAUSA"
            appMusica.text = "// 03 · ${musica.app.uppercase(Locale.ITALIAN)}"
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
        private val stato = StatoBenzinai()

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
                tasto(s, R.drawable.ic_indietro, 52, PANNELLO, TESTO, 20, "Indietro", ONDA, CONTORNO) { azioni.indietro() },
                lp(120, 120),
            )
            val titoli = Ui.colonna(s)
            titoli.addView(codice(s, "// CARBURANTE · SELF"))
            titoli.addView(testo(s, 84, TESTO, true, 78, "Benzinai", maiuscolo = true, spaziatura = 1))
            riga.addView(titoli, lp(0, ADATTA, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in Ui.ORDINI) {
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
                taglia(t, if (scelto) GIALLO else PANNELLO, 18, contorno = if (scelto) 0 else CONTORNO)
                t.setTextColor(if (scelto) SFONDO else TESTO)
                t.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            val p = Ui.riga(s, 28)
            val testi = Ui.colonna(s, 8)
            testi.addView(codiceScheda, lp(TUTTO, 34))
            testi.addView(indirizzo, lp(TUTTO, 0, 1f))
            testi.addView(filetto(s, SFONDO, SFONDO), lp(TUTTO, 6))
            testi.addView(dettagli, lp(TUTTO, 36))
            p.addView(testi, lp(0, TUTTO, 1f))
            val comandi = Ui.riga(s, 20)
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

        override fun mostra(elenco: ElencoBenzinai) {
            stato.ultimo = elenco
            disegna()
        }

        private fun seleziona(d: Distributore?) {
            stato.alterna(d)
            disegna()
        }

        private fun disegna() {
            elencoView.removeAllViews()
            when (val e = stato.ultimo) {
                is ElencoBenzinai.Caricamento -> messaggio("// BENZINAI · AGGIORNAMENTO", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione ->
                    messaggio("// POSIZIONE", Ui.POSIZIONE_NON_DISPONIBILE)
                is ElencoBenzinai.Errore -> messaggio("// ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) {
                        sezione("★ ${e.preferiti.size.toString().padStart(2, '0')}", "Preferiti", e.preferiti, true)
                    }
                    val nomeOrdine = when (e.ordine) {
                        Ordine.CONVIENE -> "CONVIENE"
                        Ordine.PREZZO -> "PREZZO"
                        Ordine.DISTANZA -> "DISTANZA"
                    }
                    if (e.vicini.isNotEmpty()) sezione("ORDINE: $nomeOrdine", "Vicini", e.vicini, false)
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

        private fun sezione(chip: String, titolo: String, distributori: List<Distributore>, luogo: Boolean) {
            val g = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 16)
            intestazione.addView(etichetta(s, chip), lp(ADATTA, 42))
            intestazione.addView(testo(s, 42, TESTO, true, 48, titolo, maiuscolo = true, spaziatura = 2), lp(ADATTA, ADATTA))
            intestazione.addView(View(s).apply { setBackgroundColor(CONTORNO) }, lp(0, 2, 1f))
            g.addView(intestazione, lp(TUTTO, 48))
            val righe = Ui.colonna(s, 20)
            distributori.forEachIndexed { i, d -> righe.addView(riga(d, luogo, i + 1), lp(TUTTO, 120)) }
            g.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(g, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, posizione: Int): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24)
            corpo.addView(testo(s, 28, SECONDARIO, false, 32, posizione.toString().padStart(2, '0')), lp(40, ADATTA))
            val sinistra = Ui.colonna(s)
            sinistra.addView(testo(s, 44, TESTO, true, 48, if (luogo) d.comune else d.nome, maiuscolo = true), lp(TUTTO, 48))
            sinistra.addView(
                testo(s, 28, SECONDARIO, false, 34, "${d.marchio} // aggiornato ${Benzinai.quando(d.aggiornato, adesso)}", spaziatura = 1),
                lp(TUTTO, 34),
            )
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(testo(s, 34, TESTO, true, 38, Benzinai.distanza(d.distanzaKm)), lp(ADATTA, ADATTA))
            val cifre = Benzinai.prezzo(d.prezzo)
            val prezzo = SpannableString("$cifre €/L")
            prezzo.setSpan(AbsoluteSizeSpan(46), cifre.length - 1, cifre.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val inizio = prezzo.length - 4
            prezzo.setSpan(AbsoluteSizeSpan(28), inizio, prezzo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            prezzo.setSpan(ForegroundColorSpan(SECONDARIO), inizio, prezzo.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            corpo.addView(testo(s, 68, GIALLO, true, 64).apply { text = prezzo }, lp(ADATTA, ADATTA))
            taglia(corpo, if (scelto) SELEZIONATO else PANNELLO, 20, linguetta = if (scelto) 8 else 0)
            corpo.setPadding(28, 0, 30, 0)
            Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
            riga.addView(corpo, lp(0, TUTTO, 1f))
            return riga
        }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            val adesso = System.currentTimeMillis()
            codiceScheda.text = "// SCHEDA · ${d.nome.uppercase(Locale.ITALIAN)} · ${d.marchio.uppercase(Locale.ITALIAN)}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            dettagli.text = "self ${Benzinai.prezzo(d.prezzo)} €/L // ${Benzinai.distanza(d.distanzaKm)} // aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"
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

private const val SPESSORE_CONTORNO_CYB = 2f

private class SmussoCyb(
    colore: Int,
    private val alto: Float,
    private val basso: Float,
    contorno: Int = 0,
    private val linguetta: Int = 0,
    coloreLinguetta: Int = 0,
) : Drawable() {
    private val percorso = Path()
    private val interno = Path()
    private val pennello = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = colore }
    private val pennelloContorno = if (contorno != 0) Paint(pennello).apply { color = contorno } else null
    private val pennelloLinguetta = if (linguetta > 0) Paint(pennello).apply { color = coloreLinguetta } else null

    override fun onBoundsChange(b: Rect) {
        val l = b.left.toFloat()
        val t = b.top.toFloat()
        val r = b.right.toFloat()
        val bt = b.bottom.toFloat()
        percorso.set(percorsoSmussoCyb(l, t, r, bt, alto, basso))
        if (pennelloContorno != null) {
            val s = SPESSORE_CONTORNO_CYB
            interno.set(
                percorsoSmussoCyb(l + s, t + s, r - s, bt - s, maxOf(alto - 1f, 0f), if (basso > 0f) basso - 1f else 0f),
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (Color.alpha(pennello.color) != 0) canvas.drawPath(percorso, pennello)
        if (pennelloLinguetta != null) {
            canvas.save()
            canvas.clipRect(bounds.left.toFloat(), bounds.top.toFloat(), (bounds.left + linguetta).toFloat(), bounds.bottom.toFloat())
            canvas.drawPath(percorso, pennelloLinguetta)
            canvas.restore()
        }
        if (pennelloContorno != null) {
            canvas.save()
            canvas.clipOutPath(interno)
            canvas.drawPath(percorso, pennelloContorno)
            canvas.restore()
        }
    }

    override fun setAlpha(alpha: Int) {
        pennello.alpha = alpha
        pennelloContorno?.alpha = alpha
        pennelloLinguetta?.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        pennello.colorFilter = colorFilter
        pennelloContorno?.colorFilter = colorFilter
        pennelloLinguetta?.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

private class FormaCyb(c: Context, private val disegna: (Canvas, Float, Float, Paint) -> Unit) : View(c) {
    private val pennello = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    override fun onDraw(canvas: Canvas) {
        disegna(canvas, width.toFloat(), height.toFloat(), pennello)
    }
}

private class CopertinaCyb(c: Context, colore: Int) : ImageView(c) {
    private val anello = SmussoCyb(Color.TRANSPARENT, 24f, 24f, colore)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        anello.setBounds(0, 0, width, height)
        anello.draw(canvas)
    }
}

private fun parallelogrammaCyb(forma: Path, x: Float, larghezza: Float, altezza: Float) {
    val inclinazione = altezza / 2f * 0.364f
    forma.rewind()
    forma.moveTo(x + inclinazione, 0f)
    forma.lineTo(x + larghezza + inclinazione, 0f)
    forma.lineTo(x + larghezza - inclinazione, altezza)
    forma.lineTo(x - inclinazione, altezza)
    forma.close()
}

private class TaccheCyb(c: Context, private val passata: Int, private val corrente: Int, private val futura: Int) : View(c) {
    private val pennello = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val forma = Path()
    private var ora = -1

    fun mostra(nuova: Int) {
        if (nuova == ora) return
        ora = nuova
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until 24) {
            pennello.color = if (ora < 0 || i > ora) futura else if (i == ora) corrente else passata
            parallelogrammaCyb(forma, 3f + i * 17f, 12f, 14f)
            canvas.drawPath(forma, pennello)
        }
    }
}

private class AstaCyb(
    c: Context,
    private val base: Int,
    private val riempimento: Int,
    private val estremi: Int,
    private val cursore: Int,
) : View(c) {
    private val pennello = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val forma = Path()
    private var frazione: Float? = null

    fun mostra(nuova: Float?) {
        if (nuova == frazione) return
        frazione = nuova
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        pennello.color = base
        canvas.drawRect(0f, 6f, w, 8f, pennello)
        pennello.color = estremi
        canvas.drawRect(0f, 0f, 2f, 14f, pennello)
        canvas.drawRect(w - 2f, 0f, w, 14f, pennello)
        val f = frazione ?: return
        pennello.color = riempimento
        canvas.drawRect(0f, 5f, w * f, 9f, pennello)
        pennello.color = cursore
        parallelogrammaCyb(forma, w * f - 6f, 12f, 14f)
        canvas.drawPath(forma, pennello)
    }
}
