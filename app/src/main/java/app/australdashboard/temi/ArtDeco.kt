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
import android.graphics.RectF
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ArtDeco : Tema {
    override val id = "artdeco"
    override val nome = "Art Déco"
    override val idea = "Oro su nero, simmetria a trittico"

    private const val NERO = 0xFF0B0A08.toInt()
    private const val AVORIO = 0xFFF3E9D2.toInt()
    private const val SABBIA = 0xFFB9A983.toInt()
    private const val ORO = 0xFFD9B45A.toInt()
    private const val FILETTO_ESTERNO = 0xFF8C7440.toInt()
    private const val FILETTO_MEDIO = 0xFF5E4D28.toInt()
    private const val FILETTO_INTERNO = 0xFF3A311C.toInt()
    private const val ONDA = 0x55D9B45A
    private const val ONDA_SCURA = 0x550B0A08

    private val serif: Typeface = Typeface.SERIF
    private val corsivo: Typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

    private fun limelight(context: Context): Typeface = Ui.font(context, R.font.limelight)

    override fun palette(context: Context) =
        Palette(NERO, AVORIO, ORO, FILETTO_ESTERNO, limelight(context), serif)

    private class Cornice(
        private val colore: Int,
        private val filetto: Int,
        private val distanza: Int,
        private val rombi: Boolean,
        private val sfondo: Int = Color.TRANSPARENT,
    ) : Drawable() {
        private val tratto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        private val riempimento = Paint(Paint.ANTI_ALIAS_FLAG)
        private val oro = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO }
        private val rettangolo = RectF()
        private val rombo = Path().apply {
            moveTo(0f, -SEMIDIAGONALE)
            lineTo(SEMIDIAGONALE, 0f)
            lineTo(0f, SEMIDIAGONALE)
            lineTo(-SEMIDIAGONALE, 0f)
            close()
        }

        override fun draw(canvas: Canvas) {
            val b: Rect = bounds
            if (sfondo != Color.TRANSPARENT) {
                riempimento.color = sfondo
                canvas.drawRect(b, riempimento)
            }
            tratto.color = colore
            rettangolo.set(b.left + 1f, b.top + 1f, b.right - 1f, b.bottom - 1f)
            canvas.drawRect(rettangolo, tratto)
            tratto.color = filetto
            val d = distanza + 1f
            rettangolo.set(b.left + d, b.top + d, b.right - d, b.bottom - d)
            canvas.drawRect(rettangolo, tratto)
            if (rombi) {
                for (x in floatArrayOf(b.left + 1f, b.right - 1f)) {
                    for (y in floatArrayOf(b.top + 1f, b.bottom - 1f)) {
                        val posizione = canvas.save()
                        canvas.translate(x, y)
                        canvas.drawPath(rombo, oro)
                        canvas.restoreToCount(posizione)
                    }
                }
            }
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

        private companion object {
            const val SEMIDIAGONALE = 12.73f
        }
    }

    private fun disegnaRombo(canvas: Canvas, tratto: Paint, x: Float, y: Float, semidiagonale: Float) {
        val percorso = Path().apply {
            moveTo(x, y - semidiagonale)
            lineTo(x + semidiagonale, y)
            lineTo(x, y + semidiagonale)
            lineTo(x - semidiagonale, y)
            close()
        }
        canvas.drawPath(percorso, tratto)
    }

    private fun vernice(colore: Int) = Paint().apply { color = colore }

    private class FilettoRombo(context: Context) : View(context) {
        private val filetto = vernice(FILETTO_MEDIO)
        private val oro = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO }

        override fun onDraw(canvas: Canvas) {
            val centro = width / 2f
            val meta = height / 2f
            canvas.drawRect(0f, meta - 1f, centro - 22f, meta + 1f, filetto)
            canvas.drawRect(centro + 22f, meta - 1f, width.toFloat(), meta + 1f, filetto)
            disegnaRombo(canvas, oro, centro, meta, 8.5f)
        }
    }

    private class FilettoServizio(context: Context) : View(context) {
        private val spesso = vernice(ORO)
        private val sottile = vernice(FILETTO_ESTERNO)

        override fun onDraw(canvas: Canvas) {
            canvas.drawRect(0f, 0f, width.toFloat(), 3f, spesso)
            val margine = (width - 36) / 2f
            canvas.drawRect(margine, 7f, margine + 36f, 9f, sottile)
        }
    }

    private class FregioOre(context: Context) : View(context) {
        private val passate = vernice(FILETTO_ESTERNO)
        private val corrente = vernice(AVORIO)
        private val future = vernice(FILETTO_INTERNO)
        private var ora = -1

        fun imposta(nuova: Int) {
            if (nuova == ora) return
            ora = nuova
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            if (ora < 0) return
            for (i in 0 until 24) {
                val altezza = (8 + 4 * (minOf(i, 23 - i) / 2)).toFloat()
                val sinistra = i * 14f
                canvas.drawRect(sinistra, height - altezza, sinistra + 6f, height.toFloat(), if (i < ora) passate else if (i == ora) corrente else future)
            }
        }
    }

    private class AstaMeteo(context: Context) : View(context) {
        private val filetto = vernice(FILETTO_ESTERNO)
        private val oro = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ORO }
        private var posizione = 0.5f

        fun imposta(meteo: Meteo?) {
            visibility = if (meteo == null) INVISIBLE else VISIBLE
            if (meteo == null) return
            val escursione = meteo.massima - meteo.minima
            posizione = if (escursione <= 0) 0.5f else Ui.frazione(meteo.gradi, meteo.minima, meteo.massima)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val larghezza = width.toFloat()
            canvas.drawRect(0f, 8f, larghezza, 10f, filetto)
            canvas.drawRect(0f, 0f, 2f, 18f, oro)
            canvas.drawRect(larghezza - 2f, 0f, larghezza, 18f, oro)
            disegnaRombo(canvas, oro, posizione * larghezza, 9f, 9.9f)
        }
    }

    private fun romano(numero: Int): String {
        val valori = intArrayOf(10, 9, 5, 4, 1)
        val simboli = arrayOf("X", "IX", "V", "IV", "I")
        var resto = numero
        val risultato = StringBuilder()
        for (i in valori.indices) {
            while (resto >= valori[i]) {
                risultato.append(simboli[i])
                resto -= valori[i]
            }
        }
        return risultato.toString()
    }

    private fun ovale(colore: Int, spessore: Int, sfondo: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(sfondo)
            if (spessore > 0) setStroke(spessore, colore)
        }

    private fun pillola(colore: Int, spessore: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(spessore, colore)
            cornerRadius = 60f
        }

    private fun rombo(context: Context, lato: Int): View = View(context).apply {
        setBackgroundColor(ORO)
        rotation = 45f
        layoutParams = lp(lato, lato)
    }

    private fun tasto(context: Context, icona: ImageView, latoIcona: Int, pieno: Boolean, descrizione: String, azione: () -> Unit): View {
        val cerchio = FrameLayout(context).apply {
            background = if (pieno) {
                LayerDrawable(arrayOf(ovale(ORO, 2), ovale(ORO, 0, ORO))).apply { setLayerInset(1, 10, 10, 10, 10) }
            } else {
                LayerDrawable(arrayOf(ovale(ORO, 2), ovale(FILETTO_MEDIO, 2))).apply { setLayerInset(1, 8, 8, 8, 8) }
            }
            addView(icona, lpf(latoIcona, latoIcona, Gravity.CENTER))
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }
        return Ui.premibile(cerchio, if (pieno) ONDA_SCURA else ONDA, descrizione, azione)
    }

    private fun maiuscoletto(testo: TextView, px: Int, spaziatura: Int): TextView = testo.apply {
        letterSpacing = spaziatura.toFloat() / px
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeArtDeco(s, azioni)

    private class HomeArtDeco(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val limelight = limelight(s)
        private val ora = Ui.testo(s, 176, AVORIO, limelight, 160).apply {
            letterSpacing = 2f / 176
            gravity = Gravity.CENTER_HORIZONTAL
        }
        private val giorno = maiuscoletto(Ui.testo(s, 30, SABBIA, serif, 36, maiuscolo = true), 30, 5)
        private val data = maiuscoletto(Ui.testo(s, 30, SABBIA, serif, 36, maiuscolo = true), 30, 5)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, ORO)
        private val condizione = Ui.testo(s, 36, AVORIO, corsivo, 42, "In attesa", righe = 2).apply { gravity = Gravity.CENTER }
        private val gradi = Ui.adatta(Ui.testo(s, 124, AVORIO, limelight, 124, "--°"), 80, 124).apply { gravity = Gravity.CENTER }
        private val massima = Ui.testo(s, 32, AVORIO, serif, 40, "max --°")
        private val minima = Ui.testo(s, 32, SABBIA, serif, 40, "min --°")
        private val inAscolto = Ui.colonna(s, 0, Gravity.CENTER_HORIZONTAL)
        private val fermo = Ui.colonna(s, 36, Gravity.CENTER)
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = Ui.pieno(FILETTO_INTERNO)
        }
        private val titolo = Ui.testo(s, 46, AVORIO, limelight, 52).apply { gravity = Gravity.CENTER_HORIZONTAL }
        private val artista = Ui.testo(s, 30, SABBIA, corsivo, 38).apply { gravity = Gravity.CENTER_HORIZONTAL }
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, NERO)
        private val fregio = FregioOre(s)
        private val asta = AstaMeteo(s).apply { visibility = View.INVISIBLE }
        private lateinit var pulsantePlay: View

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(NERO)
                setPadding(24, 24, 24, 24)
            }
            val alto = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            alto.addView(servizio("I", R.drawable.ic_benzinaio, "Benzinai", 30, "Benzinai") { azioni.benzinai() }, lp(220, TUTTO))
            alto.addView(boxOra(), lp(0, TUTTO, 1f))
            alto.addView(servizio("II", R.drawable.ic_griglia, "Tutte le app", 30, "Tutte le app") { azioni.tutteLeApp() }, lp(220, TUTTO))
            val centro = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            centro.addView(colonnaApp(AppHome.elenco(s).take(3)), lp(220, TUTTO))
            centro.addView(boxMusica(), lp(0, TUTTO, 1f))
            centro.addView(colonnaApp(AppHome.elenco(s).drop(3)), lp(220, TUTTO))
            val basso = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            basso.addView(servizio("III", R.drawable.ic_cursori, "Impostazioni Android", 28, "Impostazioni Android") { azioni.impostazioniAndroid() }, lp(220, TUTTO))
            basso.addView(boxMeteo(), lp(0, TUTTO, 1f))
            basso.addView(servizio("IV", R.drawable.ic_ingranaggio, "Launcher", 30, "Impostazioni del launcher") { azioni.impostazioniLauncher() }, lp(220, TUTTO))
            radice.addView(alto, lp(TUTTO, 290))
            radice.addView(centro, lp(TUTTO, 580))
            radice.addView(basso, lp(TUTTO, 236))
            Ui.senzaRitaglio(radice, alto, centro, basso)
            return radice
        }

        private fun servizio(numero: String, icona: Int, nome: String, dimensione: Int, descrizione: String, azione: () -> Unit): View {
            val cella = Ui.colonna(s, 8, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(14, 0, 14, 0)
                addView(Ui.testo(s, 40, ORO, limelight, 44, numero).apply { gravity = Gravity.CENTER_HORIZONTAL })
                addView(FilettoServizio(s), lp(64, 9))
                addView(Ui.icona(s, icona, ORO), lp(60, 60))
                addView(Ui.testo(s, dimensione, AVORIO, serif, 36, nome, righe = 2).apply { gravity = Gravity.CENTER })
            }
            return Ui.premibile(cella, ONDA, descrizione, azione)
        }

        private fun colonnaApp(app: List<AppHome>): View {
            val colonna = Ui.colonna(s, 20)
            app.forEach { colonna.addView(cellaApp(it), lp(TUTTO, 0, 1f)) }
            return colonna
        }

        private fun cellaApp(app: AppHome): View {
            val monogramma = FrameLayout(s).apply {
                addView(
                    View(s).apply {
                        background = Ui.bordo(ORO, 2)
                        rotation = 45f
                    },
                    lpf(70, 70).apply {
                        leftMargin = 15
                        topMargin = 15
                    },
                )
                addView(
                    View(s).apply {
                        background = Ui.bordo(FILETTO_MEDIO, 2)
                        rotation = 45f
                    },
                    lpf(54, 54).apply {
                        leftMargin = 23
                        topMargin = 23
                    },
                )
                addView(Ui.testo(s, 44, ORO, limelight, 44, app.sigla).apply { gravity = Gravity.CENTER }, lpf(100, 100))
            }
            Ui.senzaRitaglio(monogramma)
            val cella = Ui.colonna(s, 10, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(4, 0, 4, 0)
                addView(monogramma, lp(100, 100))
                addView(Ui.testo(s, 28, AVORIO, serif, 34, app.nome, righe = 2).apply { gravity = Gravity.CENTER })
            }
            val installata = app.intent(s) != null
            if (!installata) cella.alpha = 0.35f
            return Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }
        }

        private fun boxOra(): View {
            val riga = Ui.riga(s, 18).apply { gravity = Gravity.CENTER }
            riga.addView(giorno)
            riga.addView(rombo(s, 16))
            riga.addView(data)
            return Ui.colonna(s, 14, Gravity.CENTER).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, true)
                addView(ora, lp(TUTTO, 160))
                addView(riga)
                addView(fregio, lp(328, 28))
            }
        }

        private fun boxMeteo(): View {
            val sinistra = Ui.colonna(s, 6, Gravity.CENTER).apply {
                addView(iconaMeteo, lp(56, 56))
                addView(condizione)
            }
            val destra = FrameLayout(s)
            destra.addView(
                Ui.colonna(s, 4).apply {
                    addView(massima)
                    addView(minima)
                },
                lpf(ADATTA, ADATTA, Gravity.CENTER),
            )
            val riga = Ui.riga(s, 18).apply {
                gravity = Gravity.CENTER
                addView(sinistra, lp(200, ADATTA))
                addView(rombo(s, 16))
                addView(gradi, lp(200, 130))
                addView(rombo(s, 16))
                addView(destra, lp(140, ADATTA))
            }
            return Ui.colonna(s, 14, Gravity.CENTER).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, true)
                addView(riga, lp(ADATTA, ADATTA))
                addView(asta, lp(360, 18))
            }
        }

        private fun boxMusica(): View {
            val box = FrameLayout(s).apply { background = Cornice(ORO, FILETTO_MEDIO, 12, true) }

            val cornice = FrameLayout(s).apply {
                background = Ui.bordo(FILETTO_MEDIO, 2)
                setPadding(6, 6, 6, 6)
                addView(
                    FrameLayout(s).apply {
                        background = Ui.bordo(ORO, 2)
                        setPadding(8, 8, 8, 8)
                        addView(Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }, lpf(220, 220))
                    },
                    lpf(ADATTA, ADATTA),
                )
            }
            val info = Ui.colonna(s, 4, Gravity.CENTER_HORIZONTAL).apply {
                addView(titolo)
                addView(artista)
            }
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            val comandi = Ui.riga(s, 32)
            comandi.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_precedente, ORO), 44, false, "Brano precedente") { azioni.precedente() },
                lp(120, 120),
            )
            pulsantePlay = tasto(s, iconaPlay, 52, true, "Riproduci o metti in pausa") { azioni.playPausa() }
            comandi.addView(pulsantePlay, lp(140, 140))
            comandi.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_successivo, ORO), 44, false, "Brano successivo") { azioni.successivo() },
                lp(120, 120),
            )
            inAscolto.setPadding(26, 26, 26, 26)
            inAscolto.addView(cornice, lp(ADATTA, ADATTA))
            inAscolto.addView(info, lp(TUTTO, ADATTA).apply { topMargin = 16 })
            inAscolto.addView(View(s), lp(0, 0, 1f))
            inAscolto.addView(FilettoRombo(s), lp(420, 18))
            inAscolto.addView(View(s), lp(0, 0, 1f))
            inAscolto.addView(comandi, lp(ADATTA, ADATTA))

            val titoloFermo = Ui.riga(s, 28).apply {
                addView(rombo(s, 16))
                addView(Ui.testo(s, 104, AVORIO, limelight, 110, "Spotify"))
                addView(rombo(s, 16))
            }
            val azioniFermo = Ui.riga(s, 32)
            azioniFermo.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_play, NERO), 52, true, "Riproduci") { azioni.playPausa() },
                lp(140, 140),
            )
            val apri = Ui.testo(s, 34, AVORIO, corsivo, 40, "Apri Spotify").apply {
                gravity = Gravity.CENTER
                background = pillola(ORO, 2)
                setPadding(48, 0, 48, 0)
            }
            azioniFermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 120))
            fermo.setPadding(26, 26, 26, 26)
            fermo.addView(titoloFermo, lp(ADATTA, ADATTA))
            fermo.addView(FilettoRombo(s), lp(420, 18))
            fermo.addView(azioniFermo, lp(ADATTA, ADATTA))

            box.addView(inAscolto, lpf(TUTTO, TUTTO))
            box.addView(fermo, lpf(TUTTO, TUTTO))
            mostraMusica(null)
            return box
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            giorno.text = SimpleDateFormat("EEEE", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("d MMMM", Locale.ITALIAN).format(adesso)
            fregio.imposta(Calendar.getInstance().apply { time = adesso }.get(Calendar.HOUR_OF_DAY))
        }

        override fun mostraMeteo(meteo: Meteo?) {
            asta.imposta(meteo)
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                condizione.text = "In attesa"
                gradi.text = "--°"
                massima.text = "max --°"
                minima.text = "min --°"
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            condizione.text = FonteMeteo.descrizione(meteo.codice)
            gradi.text = "${meteo.gradi}°"
            massima.text = "max ${meteo.massima}°"
            minima.text = "min ${meteo.minima}°"
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            titolo.text = musica.titolo
            artista.text = musica.artista
            copertina.contentDescription = "Apri ${musica.app}"
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
            pulsantePlay.contentDescription = if (musica.inRiproduzione) "Metti in pausa" else "Riproduci"
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiArtDeco(s, azioni)

    private class BenzinaiArtDeco(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val limelight = limelight(s)
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: LinearLayout
        private val etichetta = maiuscoletto(Ui.testo(s, 28, SABBIA, serif, 34, maiuscolo = true), 28, 5)
        private val indirizzo = Ui.testo(s, 56, ORO, limelight, 64, maiuscolo = true, righe = 2).apply {
            ellipsize = null
            gravity = Gravity.CENTER_VERTICAL
        }
        private val dettagli = Ui.testo(s, 30, AVORIO, serif, 36)
        private val aggiornato = Ui.testo(s, 30, SABBIA, corsivo, 36)
        private val stato = StatoBenzinai()

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(NERO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 290))
            Ui.senzaRitaglio(radice, elencoView)
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 28)
            riga.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_indietro, ORO), 48, false, "Indietro") { azioni.indietro() },
                lp(120, 120),
            )
            val titolo = Ui.testo(s, 80, ORO, limelight, 88, "Benzinai")
            Ui.adatta(titolo, 48, 80)
            riga.addView(titolo, lp(0, 88, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = maiuscoletto(Ui.testo(s, 30, AVORIO, serif, 36, nome), 30, 2).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                gruppo.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(190, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, tasto) in ordini) {
                val scelto = ordine == attivo
                tasto.background = if (scelto) Cornice(ORO, NERO, 6, false, ORO) else Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                tasto.setTextColor(if (scelto) NERO else AVORIO)
                tasto.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            scheda = Ui.riga(s, 28).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, true, NERO)
                setPadding(40, 26, 40, 26)
                visibility = View.GONE
            }
            Ui.adatta(indirizzo, 32, 56)
            val testi = Ui.colonna(s, 6)
            testi.addView(etichetta)
            testi.addView(indirizzo, lp(TUTTO, 128))
            testi.addView(FilettoRombo(s), lp(420, 18))
            val riga = Ui.riga(s)
            riga.addView(dettagli)
            riga.addView(aggiornato)
            testi.addView(riga)
            scheda.addView(testi, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_chiudi, ORO), 48, false, "Chiudi scheda") { seleziona(null) },
                lp(120, 120),
            )
            scheda.addView(comandi)
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
                is ElencoBenzinai.Caricamento -> messaggio("Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio(Ui.POSIZIONE_NON_DISPONIBILE)
                is ElencoBenzinai.Errore -> messaggio(e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("NEI DINTORNI", e.vicini, false)
                    if (tutti.isEmpty()) messaggio("Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(testo: String) {
            val box = Ui.colonna(s, 0, Gravity.CENTER).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, false)
                setPadding(40, 40, 40, 40)
                addView(Ui.testo(s, 40, AVORIO, corsivo, 52, testo, righe = 4).apply { gravity = Gravity.CENTER })
            }
            elencoView.addView(box, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean) {
            val blocco = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 22, Gravity.CENTER).apply {
                addView(View(s).apply { setBackgroundColor(FILETTO_MEDIO) }, lp(0, 2, 1f))
                addView(rombo(s, 16))
                addView(maiuscoletto(Ui.testo(s, 40, AVORIO, serif, 48, titolo), 40, 8))
                addView(rombo(s, 16))
                addView(View(s).apply { setBackgroundColor(FILETTO_MEDIO) }, lp(0, 2, 1f))
            }
            blocco.addView(intestazione, lp(TUTTO, 48))
            val righe = Ui.colonna(s, 20)
            distributori.forEachIndexed { i, d -> righe.addView(riga(d, luogo, i + 1), lp(TUTTO, 120)) }
            blocco.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(blocco, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, posizione: Int): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = Cornice(if (scelto) ORO else FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(28, 0, 32, 0)
            }
            corpo.addView(Ui.testo(s, 40, ORO, limelight, 44, romano(posizione)).apply { gravity = Gravity.CENTER }, lp(56, ADATTA))
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 40, AVORIO, serif, 48, if (luogo) d.comune else d.nome, maiuscolo = true))
            sinistra.addView(Ui.testo(s, 28, SABBIA, corsivo, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(Ui.testo(s, 30, SABBIA, serif, 36, Benzinai.distanza(d.distanzaKm)))
            val prezzo = Ui.riga(s, 0, Gravity.BOTTOM)
            val cifre = Benzinai.prezzo(d.prezzo)
            val testoPrezzo = SpannableString(cifre).apply {
                if (cifre.length > 1) setSpan(RelativeSizeSpan(40f / 58f), cifre.length - 1, cifre.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            prezzo.addView(Ui.testo(s, 58, ORO, limelight, 60).apply { text = testoPrezzo })
            prezzo.addView(Ui.testo(s, 28, SABBIA, serif, 34, " €/l"))
            corpo.addView(prezzo)
            riga.addView(Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }, lp(0, TUTTO, 1f))
            return riga
        }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            etichetta.text = "${d.nome} · ${d.marchio}".uppercase(Locale.ITALIAN)
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            dettagli.text = "${Benzinai.prezzo(d.prezzo)} €/l self · ${Benzinai.distanza(d.distanzaKm)} · "
            aggiornato.text = "aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
        }
    }
}
