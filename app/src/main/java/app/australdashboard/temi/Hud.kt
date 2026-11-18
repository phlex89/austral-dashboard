package app.australdashboard.temi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
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
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object Hud : Tema {
    override val id = "hud"
    override val nome = "HUD futuristico"
    override val idea = "Cruscotto sci-fi, quadrante a 60 tacche"

    private const val SFONDO = 0xFF03070D.toInt()
    private const val PANNELLO = 0xFF07111C.toInt()
    private const val LINEA = 0xFF1B3A55.toInt()
    private const val LINEA_FORTE = 0xFF2E6A93.toInt()
    private const val TESTO = 0xFFE6F4FF.toInt()
    private const val SECONDARIO = 0xFF8FB3CF.toInt()
    private const val CIANO = 0xFF3FD5FF.toInt()
    private const val ONDA = 0x333FD5FF
    private const val ONDA_SCURA = 0x3303070D

    private fun orbitron(context: Context): Typeface = Ui.font(context, R.font.orbitron)

    private val pesi = HashMap<Int, Typeface>()

    private fun orbitron(context: Context, peso: Int): Typeface = pesi.getOrPut(peso) {
        val font = Font.Builder(context.resources, R.font.orbitron).setFontVariationSettings("'wght' $peso").build()
        Typeface.CustomFallbackBuilder(FontFamily.Builder(font).build()).build()
    }

    private val condensato: Typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    private val condensatoGrassetto: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    override fun palette(context: Context) =
        Palette(SFONDO, TESTO, SECONDARIO, CIANO, SFONDO, LINEA_FORTE, orbitron(context), condensato)

    private fun orb(
        context: Context,
        px: Int,
        colore: Int,
        contenuto: String = "",
        peso: Int = 400,
        spaziatura: Int = 0,
        interlinea: Int = px,
    ): TextView = Ui.testo(context, px, colore, orbitron(context, peso), interlinea, contenuto).apply {
        if (spaziatura > 0) letterSpacing = spaziatura.toFloat() / px
    }

    private fun cond(
        context: Context,
        px: Int,
        colore: Int,
        contenuto: String = "",
        grassetto: Boolean = false,
        spaziatura: Int = 0,
        interlinea: Int = px,
        maiuscolo: Boolean = false,
        righe: Int = 1,
    ): TextView = Ui.testo(
        context, px, colore, if (grassetto) condensatoGrassetto else condensato, interlinea, contenuto, maiuscolo, righe,
    ).apply {
        if (spaziatura > 0) letterSpacing = spaziatura.toFloat() / px
    }

    private fun pannello(vista: View, lato: Int = 26, spessore: Int = 3) {
        vista.setBackgroundColor(PANNELLO)
        vista.foreground = Squadrette(CIANO, lato.toFloat(), spessore.toFloat())
    }

    private fun angoli(vista: View, colore: Int, lato: Float = 16f, spessore: Float = 2f): Squadrette {
        val squadrette = Squadrette(colore, lato, spessore)
        vista.foreground = LayerDrawable(arrayOf(vista.foreground, squadrette))
        return squadrette
    }

    private fun smussa(vista: View, taglio: Int) {
        vista.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                val w = v.width.toFloat()
                val h = v.height.toFloat()
                val t = taglio.toFloat()
                outline.setPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w - t, 0f)
                        lineTo(w, t)
                        lineTo(w, h)
                        lineTo(t, h)
                        lineTo(0f, h - t)
                        close()
                    },
                )
            }
        }
        vista.clipToOutline = true
    }

    private fun tastoContorno(context: Context, icona: Int, lato: Int, descrizione: String, azione: () -> Unit): View {
        val cornice = FrameLayout(context).apply {
            background = Ui.bordo(LINEA, 2)
            addView(Ui.icona(context, icona, TESTO), lpf(lato, lato, Gravity.CENTER))
        }
        return Ui.premibile(cornice, ONDA, descrizione, azione).also { angoli(it, CIANO) }
    }

    private fun tastoPieno(context: Context, icona: Int, lato: Int, descrizione: String, azione: () -> Unit): View {
        val cornice = FrameLayout(context).apply {
            background = Smusso(CIANO, 0, 0f, 20f)
            addView(Ui.icona(context, icona, SFONDO), lpf(lato, lato, Gravity.CENTER))
        }
        smussa(cornice, 20)
        return Ui.premibile(cornice, ONDA_SCURA, descrizione, azione)
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeHud(s, azioni)

    private class HomeHud(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val quadrante = Quadrante(s, orbitron(s))
        private val ora = orb(s, 124, TESTO, "", 700, 0, 124)
        private val data = cond(s, 34, SECONDARIO, "", false, 0, 40)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, CIANO)
        private val cielo = cond(s, 30, TESTO, "In attesa", true, 0, 34, true, 2)
        private val gradi = Ui.adatta(orb(s, 96, TESTO, "--°", 700, 0, 100), 56, 96)
        private val termometro = Termometro(s)
        private val lettereGiorno = ArrayList<TextView>()
        private val filettiGiorno = ArrayList<View>()
        private var giornoCorrente = -1
        private val inAscolto = Ui.riga(s, 28)
        private val fermo = Ui.riga(s, 28)
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(LINEA)
        }
        private val statoMusica = orb(s, 28, CIANO, "IN RIPRODUZIONE", 400, 3, 34)
        private val titolo = cond(s, 50, TESTO, "", true, 0, 56)
        private val artista = cond(s, 32, SECONDARIO, "", false, 0, 38)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, SFONDO)

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 24).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            val primaRiga = Ui.riga(s, 28, Gravity.NO_GRAVITY)
            primaRiga.addView(boxMeteo(), lp(250, TUTTO))
            primaRiga.addView(boxOra(), lp(640, TUTTO))
            primaRiga.addView(servizi(), lp(250, TUTTO))
            radice.addView(primaRiga, lp(TUTTO, 640))
            radice.addView(boxMusica(), lp(TUTTO, 230))
            radice.addView(boxApp(), lp(TUTTO, 0, 1f))
            return radice
        }

        private fun boxMeteo(): View {
            val colonna = Ui.colonna(s, 12).apply { setPadding(24, 24, 24, 24) }
            pannello(colonna)
            colonna.addView(orb(s, 28, SECONDARIO, "METEO", 400, 3, 34))
            val rigaCielo = Ui.riga(s, 12)
            rigaCielo.addView(iconaMeteo, lp(52, 52))
            rigaCielo.addView(cielo, lp(0, ADATTA, 1f))
            colonna.addView(rigaCielo, lp(TUTTO, ADATTA))
            colonna.addView(gradi, lp(202, 100))
            colonna.addView(View(s), lp(TUTTO, 0, 1f))
            colonna.addView(termometro, lp(202, 300))
            return colonna
        }

        private fun boxOra(): View {
            val testi = Ui.colonna(s, 10, Gravity.CENTER)
            testi.addView(orb(s, 28, SECONDARIO, "ORA LOCALE", 400, 4, 34).apply { gravity = Gravity.CENTER_HORIZONTAL }, lp(TUTTO, 34))
            testi.addView(ora.apply { gravity = Gravity.CENTER_HORIZONTAL }, lp(TUTTO, 124))
            testi.addView(data.apply { gravity = Gravity.CENTER_HORIZONTAL }, lp(TUTTO, 40))
            testi.addView(striscia(), lp(ADATTA, 36).apply { gravity = Gravity.CENTER_HORIZONTAL })
            return FrameLayout(s).apply {
                addView(quadrante, lpf(TUTTO, TUTTO))
                addView(testi, lpf(TUTTO, TUTTO))
            }
        }

        private fun striscia(): View {
            val riga = Ui.riga(s, 6, Gravity.NO_GRAVITY)
            riga.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            for (sigla in "LMMGVSD") {
                val cella = FrameLayout(s)
                val lettera = orb(s, 28, SECONDARIO, sigla.toString(), 400, 0, 28).apply { gravity = Gravity.CENTER }
                val filetto = View(s).apply { setBackgroundColor(LINEA) }
                cella.addView(lettera, lpf(TUTTO, 33, Gravity.TOP))
                cella.addView(filetto, lpf(TUTTO, 3, Gravity.BOTTOM))
                lettereGiorno.add(lettera)
                filettiGiorno.add(filetto)
                riga.addView(cella, lp(32, 36))
            }
            return riga
        }

        private fun servizi(): View {
            val colonna = Ui.colonna(s, 20)
            val voci = listOf(
                Triple("01", R.drawable.ic_benzinaio, "Benzinai") to azioni::benzinai,
                Triple("02", R.drawable.ic_griglia, "Tutte le app") to azioni::tutteLeApp,
                Triple("03", R.drawable.ic_cursori, "Impostazioni Android") to azioni::impostazioniAndroid,
                Triple("04", R.drawable.ic_ingranaggio, "Launcher") to azioni::impostazioniLauncher,
            )
            for ((voce, azione) in voci) {
                val (numero, icona, nome) = voce
                val descrizione = if (numero == "04") "Impostazioni del launcher" else nome
                val cella = FrameLayout(s).apply { background = Ui.bordo(LINEA, 2, PANNELLO) }
                cella.addView(
                    Ui.icona(s, icona, CIANO),
                    lpf(48, 48, Gravity.TOP or Gravity.START).apply {
                        leftMargin = 20
                        topMargin = 14
                    },
                )
                cella.addView(
                    orb(s, 28, SECONDARIO, numero, 400, 0, 30),
                    lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.END).apply {
                        rightMargin = 18
                        topMargin = 16
                    },
                )
                cella.addView(
                    cond(s, 30, TESTO, nome, true, 1, 32, true, 2),
                    lpf(TUTTO, ADATTA, Gravity.BOTTOM).apply {
                        leftMargin = 20
                        rightMargin = 20
                        bottomMargin = 14
                    },
                )
                colonna.addView(Ui.premibile(cella, ONDA, descrizione) { azione() }.also { angoli(it, CIANO) }, lp(TUTTO, 0, 1f))
            }
            return colonna
        }

        private fun boxMusica(): View {
            val box = FrameLayout(s)
            pannello(box)

            inAscolto.setPadding(20, 0, 28, 0)
            val cornice = FrameLayout(s).apply {
                setPadding(8, 8, 8, 8)
                foreground = Squadrette(TESTO, 22f, 3f)
            }
            cornice.addView(Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }, lpf(186, 186))
            inAscolto.addView(cornice, lp(202, 202))
            val info = Ui.colonna(s)
            val rigaStato = Ui.riga(s, 12)
            rigaStato.addView(View(s).apply { setBackgroundColor(CIANO) }, lp(14, 14))
            rigaStato.addView(statoMusica)
            info.addView(rigaStato)
            info.addView(titolo, lp(TUTTO, ADATTA).apply { topMargin = 6 })
            info.addView(artista)
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tastoContorno(s, R.drawable.ic_precedente, 52, "Brano precedente") { azioni.precedente() }, lp(140, 140))
            val play = FrameLayout(s).apply {
                background = Smusso(CIANO, 0, 0f, 20f)
                addView(iconaPlay, lpf(60, 60, Gravity.CENTER))
            }
            smussa(play, 20)
            comandi.addView(Ui.premibile(play, ONDA_SCURA, "Riproduci o metti in pausa") { azioni.playPausa() }, lp(180, 140))
            comandi.addView(tastoContorno(s, R.drawable.ic_successivo, 52, "Brano successivo") { azioni.successivo() }, lp(140, 140))
            inAscolto.addView(comandi)

            fermo.setPadding(40, 0, 28, 0)
            val testi = Ui.colonna(s)
            testi.addView(orb(s, 28, SECONDARIO, "SORGENTE AUDIO", 400, 3, 34))
            testi.addView(orb(s, 64, TESTO, "SPOTIFY", 700, 0, 72))
            fermo.addView(testi, lp(0, ADATTA, 1f))
            val apri = cond(s, 34, TESTO, "Apri Spotify", true, 1, 40, true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(LINEA, 2)
                setPadding(40, 0, 40, 0)
            }
            val cornicaApri = FrameLayout(s).apply { addView(apri, lpf(ADATTA, TUTTO)) }
            fermo.addView(Ui.premibile(cornicaApri, ONDA, null) { azioni.apriMusica() }.also { angoli(it, CIANO) }, lp(ADATTA, 140))
            fermo.addView(tastoPieno(s, R.drawable.ic_play, 60, "Riproduci") { azioni.playPausa() }, lp(180, 140))

            box.addView(inAscolto, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
            box.addView(fermo, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
            mostraMusica(null)
            return box
        }

        private fun boxApp(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            AppHome.elenco.forEachIndexed { i, app ->
                val cella = FrameLayout(s).apply { background = Ui.bordo(LINEA, 2, PANNELLO) }
                val lettera = FrameLayout(s).apply {
                    background = Smusso(0, CIANO, 2f, 18f)
                    addView(orb(s, 56, CIANO, app.sigla, 700, 0, 56), lpf(ADATTA, ADATTA, Gravity.CENTER))
                }
                smussa(lettera, 18)
                val centro = Ui.colonna(s, 14, Gravity.CENTER)
                centro.addView(lettera, lp(110, 110))
                centro.addView(cond(s, 30, TESTO, app.nome, true, 1, 34, true, 2).apply { gravity = Gravity.CENTER })
                cella.addView(centro, lpf(TUTTO, TUTTO))
                cella.addView(
                    orb(s, 28, SECONDARIO, "0${i + 1}", 400, 0, 30),
                    lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.END).apply {
                        rightMargin = 18
                        topMargin = 16
                    },
                )
                val installata = app.intent(s) != null
                if (!installata) cella.alpha = 0.35f
                angoli(Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }, LINEA_FORTE, 14f)
                riga.addView(cella, lp(0, TUTTO, 1f))
            }
            return riga
        }

        override fun mostraOra(adesso: Date) {
            val testo = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            ora.text = SpannableString(testo).apply {
                setSpan(ForegroundColorSpan(CIANO), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
            val calendario = Calendar.getInstance().apply { time = adesso }
            quadrante.imposta(calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE))
            val giorno = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7
            if (giorno != giornoCorrente) {
                giornoCorrente = giorno
                lettereGiorno.forEachIndexed { i, lettera ->
                    val oggi = i == giorno
                    lettera.setTextColor(if (oggi) CIANO else SECONDARIO)
                    lettera.typeface = orbitron(s, if (oggi) 700 else 400)
                    filettiGiorno[i].setBackgroundColor(if (oggi) CIANO else LINEA)
                }
            }
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) return
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            termometro.mostra(meteo)
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            statoMusica.text = if (musica.inRiproduzione) "IN RIPRODUZIONE" else "IN PAUSA"
            copertina.contentDescription = "Apri ${musica.app}"
            titolo.text = musica.titolo
            artista.text = musica.artista
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiHud(s, azioni)

    private class BenzinaiHud(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: FrameLayout
        private val angoliOrdine = HashMap<Ordine, Squadrette>()
        private val etichetta = orb(s, 28, SECONDARIO, "", 400, 3, 34)
        private val indirizzo = Ui.adatta(cond(s, 72, TESTO, "", true, 1, 76, true), 34, 72)
        private val prezzoScheda = orb(s, 40, CIANO, "", 700, 0, 46)
        private val distanzaScheda = orb(s, 40, TESTO, "", 700, 0, 46)
        private val aggiornatoScheda = orb(s, 40, TESTO, "", 700, 0, 46)
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
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
            radice.addView(boxScheda(), lp(TUTTO, 320))
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(tastoContorno(s, R.drawable.ic_indietro, 52, "Indietro") { azioni.indietro() }, lp(120, 120))
            riga.addView(orb(s, 60, TESTO, "BENZINAI", 700, 0, 66), lp(0, ADATTA, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val tasto = cond(s, 32, TESTO, nome, true, 2, 36, true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                val cornice = FrameLayout(s).apply { addView(tasto, lpf(TUTTO, TUTTO)) }
                val premibile = Ui.premibile(cornice, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }
                angoliOrdine[ordine] = angoli(premibile, CIANO)
                gruppo.addView(premibile, lp(200, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, tasto) in ordini) {
                val scelto = ordine == attivo
                tasto.background = if (scelto) Smusso(CIANO, 0, 0f, 18f) else Ui.bordo(LINEA, 2)
                angoliOrdine[ordine]?.apply { acceso = !scelto; invalidateSelf() }
                tasto.setTextColor(if (scelto) SFONDO else TESTO)
                tasto.isSelected = scelto
                if (scelto) smussa(tasto, 18) else tasto.clipToOutline = false
            }
        }

        private fun boxScheda(): View {
            scheda = FrameLayout(s)
            pannello(scheda, 40, 4)
            val riga = Ui.riga(s, 28).apply { setPadding(32, 26, 32, 26) }
            val testi = Ui.colonna(s, 12)
            testi.addView(etichetta)
            testi.addView(indirizzo, lp(TUTTO, 76))
            val dati = Ui.riga(s, 56, Gravity.NO_GRAVITY)
            dati.addView(dato("SELF €/L", prezzoScheda))
            dati.addView(dato("DISTANZA", distanzaScheda))
            dati.addView(dato("AGGIORNATO", aggiornatoScheda))
            testi.addView(dati)
            riga.addView(testi, lp(0, ADATTA, 1f))
            val comandi = Ui.colonna(s, 20)
            comandi.addView(tastoContorno(s, R.drawable.ic_chiudi, 52, "Chiudi scheda") { seleziona(null) }, lp(120, 120))
            riga.addView(comandi, lp(ADATTA, ADATTA))
            scheda.addView(riga, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
            scheda.visibility = View.GONE
            return scheda
        }

        private fun dato(titolo: String, valore: TextView): View =
            Ui.colonna(s, 2).apply {
                addView(orb(s, 28, SECONDARIO, titolo, 400, 2, 32))
                addView(valore)
            }

        override fun mostra(elenco: ElencoBenzinai) {
            ultimo = elenco
            disegna()
        }

        private fun seleziona(d: Distributore?) {
            selezionato = if (d != null && d.id == selezionato?.id) null else d
            disegna()
        }

        private fun disegna() {
            elencoView.removeAllViews()
            when (val e = ultimo) {
                is ElencoBenzinai.Caricamento -> messaggio("BENZINAI", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("POSIZIONE", "Posizione non disponibile. Scegli una città di riserva nelle Impostazioni del launcher.")
                is ElencoBenzinai.Errore -> messaggio("ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", "%02d".format(e.preferiti.size), e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("VICINI", "ORDINE: ${e.ordine.name}", e.vicini, false)
                    if (tutti.isEmpty()) messaggio("BENZINAI", "Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(titolo: String, testo: String) {
            val colonna = Ui.colonna(s, 12).apply { setPadding(32, 26, 32, 26) }
            pannello(colonna)
            colonna.addView(orb(s, 28, SECONDARIO, titolo, 400, 3, 34))
            colonna.addView(cond(s, 44, TESTO, testo, false, 0, 50, false, 3), lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, contatore: String, distributori: List<Distributore>, luogo: Boolean) {
            val colonna = Ui.colonna(s, 12)
            val testata = Ui.riga(s, 16)
            testata.addView(orb(s, 40, TESTO, titolo, 700, 0, 48))
            testata.addView(View(s).apply { setBackgroundColor(LINEA) }, lp(0, 2, 1f))
            testata.addView(orb(s, 28, SECONDARIO, contatore, 500, 0, 32))
            colonna.addView(testata, lp(TUTTO, ADATTA))
            val righe = Ui.colonna(s, 20)
            distributori.forEachIndexed { i, d -> righe.addView(riga(d, luogo, i + 1), lp(TUTTO, 120)) }
            colonna.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, posizione: Int): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = if (scelto) Ui.bordo(CIANO, 3, PANNELLO) else Ui.bordo(LINEA, 2, PANNELLO)
                setPadding(24, 0, 28, 0)
            }
            corpo.addView(orb(s, 28, SECONDARIO, "%02d".format(posizione), 400, 0, 32), lp(48, ADATTA))
            val sinistra = Ui.colonna(s, 2)
            sinistra.addView(cond(s, 40, TESTO, if (luogo) d.comune else d.nome, true, 1, 46, true))
            sinistra.addView(cond(s, 28, SECONDARIO, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}", false, 0, 34))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            val destra = Ui.colonna(s, 4, Gravity.END)
            val prezzo = Ui.riga(s, 0, Gravity.BOTTOM)
            val cifre = Benzinai.prezzo(d.prezzo)
            prezzo.addView(
                orb(s, 50, CIANO, "", 700, 0, 52).apply {
                    text = SpannableString(cifre).apply {
                        setSpan(RelativeSizeSpan(0.72f), cifre.length - 1, cifre.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                },
            )
            prezzo.addView(cond(s, 28, SECONDARIO, " €/l", false, 0, 32))
            destra.addView(prezzo)
            val rigaDistanza = Ui.riga(s, 14)
            val barra = FrameLayout(s).apply { setBackgroundColor(LINEA) }
            val riempimento = (110 * (d.distanzaKm / 5.0).coerceIn(0.0, 1.0)).roundToInt()
            barra.addView(View(s).apply { setBackgroundColor(TESTO) }, lpf(riempimento, 8, Gravity.START))
            rigaDistanza.addView(barra, lp(110, 8))
            rigaDistanza.addView(orb(s, 28, TESTO, Benzinai.distanza(d.distanzaKm), 400, 0, 32))
            destra.addView(rigaDistanza)
            corpo.addView(destra)
            riga.addView(Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }, lp(0, TUTTO, 1f))
            return riga
        }

        private fun aggiornaScheda() {
            val d = selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            val italiano = Locale.ITALIAN
            etichetta.text = "INDIRIZZO · ${d.nome.uppercase(italiano)} · ${d.marchio.uppercase(italiano)}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            prezzoScheda.text = Benzinai.prezzo(d.prezzo)
            distanzaScheda.text = Benzinai.distanza(d.distanzaKm).uppercase(italiano)
            aggiornatoScheda.text = Benzinai.quando(d.aggiornato, System.currentTimeMillis()).uppercase(italiano)
        }
    }
}

private class Squadrette(colore: Int, private val lato: Float, private val spessore: Float) : Drawable() {
    var acceso = true

    private val pennello = Paint().apply { this.color = colore }

    override fun draw(canvas: Canvas) {
        if (!acceso) return
        val b = bounds
        val l = b.left.toFloat()
        val t = b.top.toFloat()
        val r = b.right.toFloat()
        val bt = b.bottom.toFloat()
        canvas.drawRect(l, t, l + lato, t + spessore, pennello)
        canvas.drawRect(l, t, l + spessore, t + lato, pennello)
        canvas.drawRect(r - lato, t, r, t + spessore, pennello)
        canvas.drawRect(r - spessore, t, r, t + lato, pennello)
        canvas.drawRect(l, bt - spessore, l + lato, bt, pennello)
        canvas.drawRect(l, bt - lato, l + spessore, bt, pennello)
        canvas.drawRect(r - lato, bt - spessore, r, bt, pennello)
        canvas.drawRect(r - spessore, bt - lato, r, bt, pennello)
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

private class Smusso(
    private val riempimento: Int,
    private val bordo: Int,
    private val spessore: Float,
    private val taglio: Float,
) : Drawable() {
    private val percorso = Path()
    private val pennelloPieno = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = riempimento }
    private val pennelloBordo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = spessore
        color = bordo
    }

    override fun onBoundsChange(b: Rect) {
        val i = spessore / 2f
        val l = b.left + i
        val t = b.top + i
        val r = b.right - i
        val bt = b.bottom - i
        percorso.rewind()
        percorso.moveTo(l, t)
        percorso.lineTo(r - taglio, t)
        percorso.lineTo(r, t + taglio)
        percorso.lineTo(r, bt)
        percorso.lineTo(l + taglio, bt)
        percorso.lineTo(l, bt - taglio)
        percorso.close()
    }

    override fun draw(canvas: Canvas) {
        if (riempimento != 0) canvas.drawPath(percorso, pennelloPieno)
        if (spessore > 0f) canvas.drawPath(percorso, pennelloBordo)
    }

    override fun setAlpha(alpha: Int) {
        pennelloPieno.alpha = alpha
        pennelloBordo.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        pennelloPieno.colorFilter = colorFilter
        pennelloBordo.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

private class Quadrante(context: Context, carattere: Typeface) : View(context) {
    private var ore = 0
    private var minuti = 0

    fun imposta(ore: Int, minuti: Int) {
        if (this.ore != ore || this.minuti != minuti) {
            this.ore = ore
            this.minuti = minuti
            invalidate()
        }
    }

    private val tacche = FloatArray(60 * 4)
    private val taccheCorrenti = FloatArray(60 * 4)
    private val arcoEsterno = RectF(58f, 58f, 582f, 582f)
    private val arcoOre = RectF(94f, 94f, 546f, 546f)
    private val cerchio = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0xFF1B3A55.toInt()
    }
    private val segmento = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.BUTT
    }
    private val tacca = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val numeri = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = carattere
        textSize = 28f
        color = 0xFF8FB3CF.toInt()
        textAlign = Paint.Align.CENTER
    }
    private val baseNumeri = -(numeri.fontMetrics.ascent + numeri.fontMetrics.descent) / 2f

    init {
        for (i in 0 until 60) {
            val angolo = Math.toRadians(i * 6.0 - 90.0)
            val interno = if (i % 5 == 0) 282.0 else 294.0
            tacche[i * 4] = (320 + interno * cos(angolo)).toFloat()
            tacche[i * 4 + 1] = (320 + interno * sin(angolo)).toFloat()
            tacche[i * 4 + 2] = (320 + 312.0 * cos(angolo)).toFloat()
            tacche[i * 4 + 3] = (320 + 312.0 * sin(angolo)).toFloat()
            taccheCorrenti[i * 4] = (320 + 276.0 * cos(angolo)).toFloat()
            taccheCorrenti[i * 4 + 1] = (320 + 276.0 * sin(angolo)).toFloat()
            taccheCorrenti[i * 4 + 2] = (320 + 316.0 * cos(angolo)).toFloat()
            taccheCorrenti[i * 4 + 3] = (320 + 316.0 * sin(angolo)).toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val scala = width / 640f
        canvas.scale(scala, scala)
        for (quarto in 0 until 4) canvas.drawArc(arcoEsterno, -81f + 90f * quarto, 72f, false, cerchio)
        canvas.drawCircle(320f, 320f, 206f, cerchio)
        for (i in 0 until 24) {
            segmento.color = when {
                i < ore -> 0xFF8FB3CF.toInt()
                i == ore -> 0xFF3FD5FF.toInt()
                else -> 0xFF1B3A55.toInt()
            }
            canvas.drawArc(arcoOre, -88.5f + 15f * i, 12f, false, segmento)
        }
        for (i in 0 until 60) {
            if (i == minuti) {
                tacca.color = 0xFFE6F4FF.toInt()
                tacca.strokeWidth = 7f
                canvas.drawLine(taccheCorrenti[i * 4], taccheCorrenti[i * 4 + 1], taccheCorrenti[i * 4 + 2], taccheCorrenti[i * 4 + 3], tacca)
                continue
            }
            tacca.color = if (i < minuti) 0xFF3FD5FF.toInt() else 0xFF2E6A93.toInt()
            tacca.strokeWidth = if (i % 5 == 0) 5f else 3f
            canvas.drawLine(tacche[i * 4], tacche[i * 4 + 1], tacche[i * 4 + 2], tacche[i * 4 + 3], tacca)
        }
        canvas.drawText("00", 320f, 62f + baseNumeri, numeri)
        canvas.drawText("15", 578f, 320f + baseNumeri, numeri)
        canvas.drawText("30", 320f, 578f + baseNumeri, numeri)
        canvas.drawText("45", 62f, 320f + baseNumeri, numeri)
    }
}

private class Termometro(context: Context) : View(context) {
    private var meteo: Meteo? = null
    private val asse = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFF2E6A93.toInt()
    }
    private val fascia = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF1B3A55.toInt()
    }
    private val bordoFascia = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0xFF3FD5FF.toInt()
    }
    private val puntatore = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF3FD5FF.toInt() }
    private val testo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textSize = 28f
        color = 0xFFE6F4FF.toInt()
    }
    private val numeri = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        textSize = 28f
        color = 0xFF8FB3CF.toInt()
        textAlign = Paint.Align.RIGHT
    }
    private val baseTesto = -(testo.fontMetrics.ascent + testo.fontMetrics.descent) / 2f
    private val baseNumeri = -(numeri.fontMetrics.ascent + numeri.fontMetrics.descent) / 2f
    private val triangolo = Path()

    fun mostra(meteo: Meteo) {
        this.meteo = meteo
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val scala = width / 202f
        canvas.scale(scala, scala)
        asse.strokeWidth = 3f
        canvas.drawLine(46f, 16f, 46f, 284f, asse)
        for (i in 0..16) {
            val y = 284f - 16.75f * i
            val lunga = i % 8 == 0
            asse.strokeWidth = if (lunga) 3f else 2f
            canvas.drawLine(if (lunga) 34f else 39f, y, 46f, y, asse)
        }
        val m = meteo ?: return
        val passo = maxOf(1, ceil((m.massima - m.minima + 2) / 16.0).toInt())
        val base = ((m.minima + m.massima) / 2.0 / (4 * passo)).roundToInt() * 4 * passo - 8 * passo
        val pxPerGrado = 268f / (16 * passo)
        fun y(gradi: Int): Float = (284f - (gradi - base) * pxPerGrado).coerceIn(16f, 284f)
        for (i in 0..2) {
            val gradi = base + 8 * passo * i
            canvas.drawText("$gradi", 28f, y(gradi) + baseNumeri, numeri)
        }
        val yMassima = y(m.massima)
        val yMinima = y(m.minima)
        val yAttuale = y(m.gradi)
        canvas.drawRect(52f, yMassima, 66f, yMinima, fascia)
        canvas.drawRect(52f, yMassima, 66f, yMinima, bordoFascia)
        triangolo.rewind()
        triangolo.moveTo(72f, yAttuale)
        triangolo.lineTo(94f, yAttuale - 13f)
        triangolo.lineTo(94f, yAttuale + 13f)
        triangolo.close()
        canvas.drawPath(triangolo, puntatore)
        canvas.drawText("MAX ${m.massima}°", 104f, yMassima + baseTesto, testo)
        canvas.drawText("MIN ${m.minima}°", 104f, maxOf(yMinima, yMassima + 32f) + baseTesto, testo)
    }
}
