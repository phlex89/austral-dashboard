package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
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

    private const val ONDA_CHIARA = 0x55F3E9D2

    private class Targa(
        context: Context,
        nome: String,
        private val iconaAccesa: Int,
        private val iconaSpenta: Int,
        azione: () -> Unit,
    ) {
        private val simbolo = Ui.icona(context, iconaSpenta, SABBIA)
        private val titolo = Ui.testo(context, 28, AVORIO, serif, 32, nome).apply { maxWidth = 150 }
        private val stato = Ui.testo(context, 28, AVORIO, corsivo, 32).apply { maxWidth = 150 }
        private val contenuto = Ui.riga(context, 10, Gravity.CENTER).apply {
            addView(simbolo, lp(40, 40))
            addView(
                Ui.colonna(context).apply {
                    addView(titolo)
                    addView(stato)
                },
                lp(ADATTA, ADATTA),
            )
        }
        val view: View = Ui.premibile(contenuto, ONDA_CHIARA, nome, azione)

        init {
            imposta(0, "")
        }

        fun imposta(livello: Int, testo: String) {
            val colore = when (livello) {
                0 -> SABBIA
                1 -> AVORIO
                else -> NERO
            }
            contenuto.background = when (livello) {
                0 -> Cornice(FILETTO_MEDIO, FILETTO_INTERNO, 8, false)
                1 -> Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                else -> Cornice(ORO, NERO, 6, false, ORO)
            }
            simbolo.setImageResource(if (livello == 0) iconaSpenta else iconaAccesa)
            simbolo.imageTintList = ColorStateList.valueOf(if (livello == 1) ORO else colore)
            titolo.setTextColor(colore)
            stato.setTextColor(colore)
            stato.text = testo
        }
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeArtDeco(s, azioni)

    private class HomeArtDeco(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val limelight = limelight(s)
        private val playlist = Playlists.elenco(s)
        private val ora = Ui.testo(s, 140, AVORIO, limelight, 128).apply {
            letterSpacing = 2f / 140
            gravity = Gravity.CENTER_HORIZONTAL
        }
        private val giorno = maiuscoletto(Ui.testo(s, 30, SABBIA, serif, 36, maiuscolo = true), 30, 5)
        private val data = maiuscoletto(Ui.testo(s, 30, SABBIA, serif, 36, maiuscolo = true), 30, 5)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, ORO)
        private val condizione = Ui.testo(s, 32, AVORIO, corsivo, 38, "In attesa", righe = 2).apply { gravity = Gravity.CENTER }
        private val gradi = Ui.adatta(Ui.testo(s, 100, AVORIO, limelight, 100, "--°"), 64, 100).apply { gravity = Gravity.CENTER }
        private val massima = Ui.testo(s, 32, AVORIO, serif, 40, "max --°")
        private val minima = Ui.testo(s, 32, SABBIA, serif, 40, "min --°")
        private val pioggia = Ui.testo(s, 30, ORO, corsivo, 36)
        private val rigaPioggia = Ui.riga(s, 14, Gravity.CENTER).apply {
            visibility = View.GONE
            addView(Ui.icona(s, R.drawable.artdeco_pioggia, ORO), lp(32, 32))
            addView(pioggia)
        }
        private val inAscolto = Ui.colonna(s, 0, Gravity.CENTER)
        private val fermo = Ui.colonna(s, 0, Gravity.CENTER)
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = Ui.pieno(FILETTO_INTERNO)
        }
        private val titolo = Ui.testo(s, 46, AVORIO, limelight, 52)
        private val artista = Ui.testo(s, 30, SABBIA, corsivo, 38)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, NERO)
        private val fregio = FregioOre(s)
        private val asta = AstaMeteo(s).apply { visibility = View.INVISIBLE }
        private val wifi = Targa(s, "Wi-Fi", R.drawable.ic_wifi, R.drawable.ic_wifi_spento) { azioni.wifi() }
        private val bluetooth = Targa(s, "Bluetooth", R.drawable.ic_bluetooth, R.drawable.ic_bluetooth_spento) { azioni.bluetooth() }
        private val puntina = Ui.icona(s, R.drawable.ic_puntina, ORO).apply { visibility = View.GONE }
        private val luogo = maiuscoletto(Ui.testo(s, 28, SABBIA, serif, 34, maiuscolo = true), 28, 4).apply { maxWidth = 600 }
        private val rigaLuogo = Ui.riga(s, 12, Gravity.CENTER).apply {
            visibility = View.GONE
            addView(puntina, lp(28, 28))
            addView(luogo)
        }
        private val iconaDomani = Ui.icona(s, R.drawable.ic_nuvoloso, ORO)
        private val testoDomani = Ui.testo(s, 30, AVORIO, serif, 36)
        private val rigaDomani = Ui.riga(s, 10, Gravity.CENTER_VERTICAL).apply {
            visibility = View.GONE
            addView(Ui.testo(s, 30, SABBIA, corsivo, 36, "Domani"))
            addView(iconaDomani, lp(30, 30))
            addView(testoDomani)
        }
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
            val sistema = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            sistema.addView(wifi.view, lp(220, TUTTO))
            sistema.addView(dettaglio("Dettagli Wi-Fi") { azioni.wifiDettaglio() }, lp(100, TUTTO))
            sistema.addView(comandoTema(), lp(0, TUTTO, 1f))
            sistema.addView(dettaglio("Dettagli Bluetooth") { azioni.bluetoothDettaglio() }, lp(100, TUTTO))
            sistema.addView(bluetooth.view, lp(220, TUTTO))
            radice.addView(alto, lp(TUTTO, 262))
            radice.addView(centro, lp(TUTTO, 472))
            radice.addView(basso, lp(TUTTO, 232))
            radice.addView(sistema, lp(TUTTO, 120))
            Ui.senzaRitaglio(radice, alto, centro, basso, sistema)
            return radice
        }

        private fun servizio(numero: String, icona: Int, nome: String, dimensione: Int, descrizione: String, azione: () -> Unit): View {
            val cella = Ui.colonna(s, 8, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(14, 0, 14, 0)
                addView(Ui.testo(s, 40, ORO, limelight, 44, numero).apply { gravity = Gravity.CENTER_HORIZONTAL })
                addView(FilettoServizio(s), lp(64, 9))
                addView(Ui.icona(s, icona, ORO), lp(52, 52))
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
                    lpf(60, 60).apply {
                        leftMargin = 12
                        topMargin = 12
                    },
                )
                addView(
                    View(s).apply {
                        background = Ui.bordo(FILETTO_MEDIO, 2)
                        rotation = 45f
                    },
                    lpf(46, 46).apply {
                        leftMargin = 19
                        topMargin = 19
                    },
                )
                addView(Ui.testo(s, 38, ORO, limelight, 38, app.sigla).apply { gravity = Gravity.CENTER }, lpf(84, 84))
            }
            Ui.senzaRitaglio(monogramma)
            val nome = Ui.adatta(Ui.testo(s, 28, AVORIO, serif, 34, app.nome), 18, 28).apply { gravity = Gravity.CENTER }
            val cella = Ui.colonna(s, 8, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(10, 0, 10, 0)
                addView(monogramma, lp(84, 84))
                addView(nome, lp(TUTTO, 40))
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
            return Ui.colonna(s, 12, Gravity.CENTER).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, true)
                addView(ora, lp(TUTTO, 128).apply { topMargin = -22; bottomMargin = 22 })
                addView(riga)
                addView(fregio, lp(328, 28))
            }
        }

        private fun boxMeteo(): View {
            val sinistra = Ui.colonna(s, 6, Gravity.CENTER).apply {
                addView(iconaMeteo, lp(48, 48))
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
                addView(gradi, lp(200, 108))
                addView(rombo(s, 16))
                addView(destra, lp(140, ADATTA))
            }
            val piede = Ui.riga(s, 28, Gravity.CENTER).apply {
                addView(rigaPioggia)
                addView(rigaDomani)
            }
            return Ui.colonna(s, 8, Gravity.CENTER).apply {
                background = Cornice(ORO, FILETTO_MEDIO, 12, true)
                addView(rigaLuogo, lp(ADATTA, 36))
                addView(riga, lp(ADATTA, ADATTA))
                addView(asta, lp(360, 18))
                addView(piede, lp(ADATTA, 36))
            }
        }

        private fun dettaglio(descrizione: String, azione: () -> Unit): View {
            val cella = FrameLayout(s).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                addView(Ui.testo(s, 64, ORO, serif, 64, "›").apply { gravity = Gravity.CENTER }, lpf(TUTTO, TUTTO))
            }
            return Ui.premibile(cella, ONDA, descrizione, azione)
        }

        private fun comandoTema(): View {
            val contenuto = Ui.riga(s, 14, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                addView(Ui.icona(s, R.drawable.ic_tavolozza, ORO), lp(44, 44))
                addView(Ui.testo(s, 32, AVORIO, serif, 38, "Tema"))
            }
            return Ui.premibile(contenuto, ONDA, "Cambia tema") { azioni.temaSuccessivo() }
        }

        private fun rigaPlaylist(): View {
            val riga = Ui.riga(s, 20)
            playlist.forEach { p ->
                val cella = Ui.colonna(s, 0, Gravity.CENTER).apply {
                    background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                    setPadding(16, 0, 16, 0)
                    addView(Ui.testo(s, 28, AVORIO, corsivo, 32, p.nome, righe = 3).apply { gravity = Gravity.CENTER }, lp(TUTTO, ADATTA))
                }
                riga.addView(Ui.premibile(cella, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) }, lp(0, 120, 1f))
            }
            return riga
        }

        private fun aggiungiPlaylist(colonna: LinearLayout) {
            if (playlist.isEmpty()) return
            colonna.addView(FilettoRombo(s), lp(420, 18).apply { topMargin = 16; bottomMargin = 16 })
            colonna.addView(rigaPlaylist(), lp(TUTTO, 120))
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
                        addView(Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }, lpf(204, 204))
                    },
                    lpf(ADATTA, ADATTA),
                )
            }
            val info = Ui.colonna(s, 0).apply {
                addView(titolo)
                addView(artista)
            }
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            val comandi = Ui.riga(s)
            comandi.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_precedente, ORO), 44, false, "Brano precedente") { azioni.precedente() },
                lp(120, 120),
            )
            comandi.addView(View(s), lp(0, 0, 1f))
            pulsantePlay = tasto(s, iconaPlay, 48, true, "Riproduci o metti in pausa") { azioni.playPausa() }
            comandi.addView(pulsantePlay, lp(120, 120))
            comandi.addView(View(s), lp(0, 0, 1f))
            comandi.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_successivo, ORO), 44, false, "Brano successivo") { azioni.successivo() },
                lp(120, 120),
            )
            val destra = Ui.colonna(s).apply {
                addView(info, lp(TUTTO, ADATTA))
                addView(View(s), lp(0, 0, 1f))
                addView(comandi, lp(TUTTO, 120))
            }
            val principale = Ui.riga(s, 24, Gravity.NO_GRAVITY).apply {
                addView(cornice, lp(ADATTA, ADATTA))
                addView(destra, lp(0, TUTTO, 1f))
            }
            inAscolto.setPadding(26, 26, 26, 26)
            inAscolto.addView(principale, lp(TUTTO, 232))
            aggiungiPlaylist(inAscolto)

            val titoloFermo = Ui.riga(s, 28).apply {
                addView(rombo(s, 16))
                addView(Ui.testo(s, 80, AVORIO, limelight, 88, "Spotify"))
                addView(rombo(s, 16))
            }
            val azioniFermo = Ui.riga(s, 32)
            azioniFermo.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_play, NERO), 48, true, "Riproduci") { azioni.playPausa() },
                lp(120, 120),
            )
            val apri = Ui.testo(s, 34, AVORIO, corsivo, 40, "Apri Spotify").apply {
                gravity = Gravity.CENTER
                background = pillola(ORO, 2)
                setPadding(48, 0, 48, 0)
            }
            azioniFermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 120))
            fermo.setPadding(26, 26, 26, 26)
            fermo.addView(titoloFermo, lp(ADATTA, ADATTA))
            fermo.addView(azioniFermo, lp(ADATTA, ADATTA).apply { topMargin = 20 })
            aggiungiPlaylist(fermo)

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
            val testoPioggia = FonteMeteo.testoPioggia(meteo?.pioggiaTraMinuti)
            rigaPioggia.visibility = if (testoPioggia == null) View.GONE else View.VISIBLE
            pioggia.text = testoPioggia.orEmpty()
            val domani = meteo?.domani
            rigaDomani.visibility = if (domani == null) View.GONE else View.VISIBLE
            if (domani != null) {
                iconaDomani.setImageResource(Ui.iconaCielo(domani.codice))
                testoDomani.text = "${domani.minima}° / ${domani.massima}°"
            }
            val nome = meteo?.luogo.orEmpty()
            rigaLuogo.visibility = if (nome.isEmpty()) View.GONE else View.VISIBLE
            luogo.text = nome
            puntina.visibility = if (meteo?.gps == true) View.VISIBLE else View.GONE
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

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> wifi.imposta(0, "spento")
                StatoWifi.ACCESO -> wifi.imposta(1, "acceso")
                StatoWifi.CONNESSO -> wifi.imposta(2, "connesso")
            }
            if (stato.bluetooth) bluetooth.imposta(2, "acceso") else bluetooth.imposta(0, "spento")
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiArtDeco(s, azioni)

    private class BenzinaiArtDeco(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val limelight = limelight(s)
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: LinearLayout
        private val etichetta = maiuscoletto(Ui.testo(s, 28, SABBIA, serif, 34, maiuscolo = true), 28, 5)
        private val indirizzo = Ui.testo(s, 52, ORO, limelight, 58, maiuscolo = true)
        private val dettagli = Ui.testo(s, 28, AVORIO, serif, 34)
        private val aggiornato = Ui.testo(s, 28, SABBIA, corsivo, 34)
        private val spesa = Ui.testo(s, 28, SABBIA, corsivo, 32).apply {
            maxWidth = 300
            visibility = View.GONE
        }
        private val stato = StatoBenzinai()
        private val rigaIntorno = Ui.riga(s, 12, Gravity.CENTER)
        private val iconaIntorno = Ui.icona(s, R.drawable.ic_mirino, ORO)
        private val testoIntorno = maiuscoletto(Ui.testo(s, 30, AVORIO, serif, 36, "Intorno a me"), 30, 2)
        private val testoRaggio = Ui.testo(s, 40, ORO, limelight, 44, "${Benzinai.raggio(s).toInt()} km")

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(NERO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(rigaFiltri(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 186))
            Ui.senzaRitaglio(radice, elencoView)
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 28)
            riga.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_indietro, ORO), 48, false, "Indietro") { azioni.indietro() },
                lp(120, 120),
            )
            val titolo = Ui.testo(s, 72, ORO, limelight, 80, "Benzinai")
            Ui.adatta(titolo, 48, 72)
            riga.addView(titolo, lp(0, 88, 1f))
            val targa = Ui.riga(s, 16).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(30, 0, 30, 0)
                addView(Ui.icona(s, R.drawable.ic_rifornimenti, ORO), lp(44, 44))
                addView(
                    Ui.colonna(s, 0, Gravity.CENTER_VERTICAL).apply {
                        addView(Ui.testo(s, 34, AVORIO, serif, 40, "Rifornimenti").apply { maxWidth = 300 })
                        addView(spesa)
                    },
                    lp(ADATTA, ADATTA),
                )
            }
            val mappa = Ui.riga(s, 14, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(28, 0, 28, 0)
                addView(Ui.icona(s, R.drawable.ic_mappa, ORO), lp(44, 44))
                addView(Ui.testo(s, 34, AVORIO, serif, 40, "Mappa"))
            }
            riga.addView(Ui.premibile(mappa, ONDA, "Mappa dei distributori") { azioni.mappa() }, lp(ADATTA, 120))
            riga.addView(Ui.premibile(targa, ONDA, "Rifornimenti") { azioni.rifornimenti(stato.selezionato) }, lp(ADATTA, 120))
            return riga
        }

        override fun mostraSpesa(testo: String?) {
            spesa.text = testo.orEmpty()
            spesa.visibility = if (testo == null) View.GONE else View.VISIBLE
        }

        private fun rigaFiltri(): View {
            val riga = Ui.riga(s, 20)
            rigaIntorno.apply {
                setPadding(12, 0, 12, 0)
                addView(iconaIntorno, lp(40, 40))
                addView(testoIntorno)
            }
            evidenziaIntorno(false)
            riga.addView(Ui.premibile(rigaIntorno, ONDA, "Intorno a me") { azioni.intornoAMe() }, lp(300, TUTTO))
            val raggio = Ui.colonna(s, 0, Gravity.CENTER).apply {
                background = Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                addView(maiuscoletto(Ui.testo(s, 28, SABBIA, serif, 32, "RAGGIO"), 28, 4).apply { gravity = Gravity.CENTER })
                addView(testoRaggio.apply { gravity = Gravity.CENTER })
            }
            riga.addView(Ui.premibile(raggio, ONDA, "Raggio di ricerca") { azioni.raggio() }, lp(170, TUTTO))
            riga.addView(rombo(s, 12), lp(12, 12).apply { leftMargin = 4; rightMargin = 4 })
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = maiuscoletto(Ui.testo(s, 30, AVORIO, serif, 36, nome), 30, 2).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                riga.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(0, TUTTO, 1f))
            }
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaIntorno(attivo: Boolean) {
            rigaIntorno.background = if (attivo) Cornice(ORO, NERO, 6, false, ORO) else Cornice(FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
            testoIntorno.setTextColor(if (attivo) NERO else AVORIO)
            iconaIntorno.imageTintList = ColorStateList.valueOf(if (attivo) NERO else ORO)
            rigaIntorno.isSelected = attivo
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
                setPadding(36, 0, 36, 0)
                visibility = View.GONE
            }
            Ui.adatta(indirizzo, 30, 52)
            val testi = Ui.colonna(s, 0)
            testi.addView(etichetta)
            testi.addView(indirizzo, lp(TUTTO, 70))
            val riga = Ui.riga(s)
            riga.addView(dettagli)
            riga.addView(aggiornato)
            testi.addView(riga)
            scheda.addView(testi, lp(0, ADATTA, 1f))
            scheda.addView(tasto(s, Ui.icona(s, R.drawable.ic_mappa, ORO), 48, false, "Visualizza sulla mappa") { stato.selezionato?.let { azioni.visualizzaSullaMappa(it) } }, lp(120, 120))
            scheda.addView(
                tasto(s, Ui.icona(s, R.drawable.ic_chiudi, ORO), 48, false, "Chiudi scheda") { seleziona(null) },
                lp(120, 120),
            )
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
                    evidenziaIntorno(e.intornoAMe)
                    testoRaggio.text = "${e.raggioKm.toInt()} km"
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) {
                        val titoloVicini = if (e.intornoAMe) "SOLO INTORNO A ME · ${e.raggioKm.toInt()} KM" else "DINTORNI + COMUNI PREFERITI"
                        sezione(titoloVicini, e.vicini, false, if (e.intornoAMe) ORO else AVORIO, 36)
                    }
                    if (tutti.isEmpty()) messaggio("Nessun distributore entro ${e.raggioKm.toInt()} km")
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

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean, colore: Int = AVORIO, px: Int = 40) {
            val blocco = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 0, Gravity.CENTER).apply {
                addView(View(s).apply { setBackgroundColor(FILETTO_MEDIO) }, lp(0, 2, 1f))
                addView(rombo(s, 16), lp(16, 16).apply { leftMargin = 22; rightMargin = 22 })
                addView(maiuscoletto(Ui.testo(s, px, colore, serif, 48, titolo), px, px / 5))
                addView(rombo(s, 16), lp(16, 16).apply { leftMargin = 22; rightMargin = 22 })
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
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = Cornice(if (scelto) ORO else FILETTO_ESTERNO, FILETTO_INTERNO, 8, false)
                setPadding(28, 0, 32, 0)
            }
            corpo.addView(Ui.testo(s, 40, ORO, limelight, 44, romano(posizione)).apply { gravity = Gravity.CENTER }, lp(56, ADATTA))
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 40, AVORIO, serif, 48, if (luogo) d.comune else d.nome))
            sinistra.addView(Ui.testo(s, 28, SABBIA, corsivo, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            val destra = Ui.colonna(s, 0, Gravity.END)
            val rigaPrezzo = Ui.riga(s, 16)
            DatiMimit.andamento(d)?.let { andamento ->
                val variazione = Ui.riga(s, 6)
                variazione.addView(Ui.icona(s, if (andamento.salito) R.drawable.ic_su else R.drawable.ic_giu, AVORIO), lp(20, 20))
                variazione.addView(Ui.testo(s, 28, AVORIO, serif, 32, andamento.differenza))
                variazione.contentDescription = "Prezzo ${if (andamento.salito) "salito" else "sceso"} di ${andamento.differenza}"
                rigaPrezzo.addView(variazione)
            }
            val prezzo = Ui.riga(s, 0, Gravity.BOTTOM)
            val cifre = Benzinai.prezzo(d.prezzo)
            val testoPrezzo = SpannableString(cifre).apply {
                if (cifre.length > 1) setSpan(RelativeSizeSpan(40f / 58f), cifre.length - 1, cifre.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            prezzo.addView(Ui.testo(s, 58, ORO, limelight, 60).apply { text = testoPrezzo })
            prezzo.addView(Ui.testo(s, 28, SABBIA, serif, 34, " €/l"))
            rigaPrezzo.addView(prezzo)
            destra.addView(rigaPrezzo, lp(ADATTA, ADATTA).apply { gravity = Gravity.END })
            destra.addView(Ui.testo(s, 28, SABBIA, serif, 32, Benzinai.distanza(d.distanzaKm)), lp(ADATTA, ADATTA).apply { gravity = Gravity.END })
            corpo.addView(destra)
            return Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
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
