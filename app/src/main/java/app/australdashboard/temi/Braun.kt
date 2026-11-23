package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
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

object Braun : Tema {
    override val id = "braun"
    override val nome = "Braun"
    override val idea = "Apparecchio alla Dieter Rams, tasti tondi"

    private const val CORPO = 0xFF1C1C1C.toInt()
    private const val MODULO = 0xFF242424.toInt()
    private const val TASTO = 0xFF2E2E2E.toInt()
    private const val ANELLO = 0xFF3D3D3D.toInt()
    private const val DISPLAY = 0xFF0E100E.toInt()
    private const val FILETTO = 0xFF2A2D2A.toInt()
    private const val GRIGIO_CHIARO = 0xFFD9D9D4.toInt()
    private const val TESTO = 0xFFEDEDED.toInt()
    private const val SECONDARIO = 0xFFA8A8A8.toInt()
    private const val ARANCIO = 0xFFFF7A1A.toInt()
    private const val ONDA = 0x33FFFFFF
    private const val ONDA_SCURA = 0x33000000
    private const val TACCA = 0xFF5A5A5A.toInt()
    private const val CONCA_SCURA_ALTO = 0xFF242424.toInt()
    private const val CONCA_SCURA_BASSO = 0xFF363636.toInt()
    private const val CONCA_CHIARA_ALTO = 0xFFC6C6C1.toInt()
    private const val CONCA_CHIARA_BASSO = 0xFFE8E8E3.toInt()
    private const val CONCA_PLAY_ALTO = 0xFFE96A0A.toInt()
    private const val CONCA_PLAY_BASSO = 0xFFFF9340.toInt()

    private val leggero: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private val normale: Typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    private val medio: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    override fun palette(context: Context) =
        Palette(CORPO, TESTO, ARANCIO, ANELLO, leggero, normale)

    private fun ovale(riempimento: Int, anello: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(riempimento)
            if (anello != 0) setStroke(3, anello)
        }

    private fun contorno(colore: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(2, colore)
        }

    private fun arrotondato(riempimento: Int, raggio: Int, bordo: Int = 0, spessore: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(riempimento)
            cornerRadius = raggio.toFloat()
            if (spessore > 0) setStroke(spessore, bordo)
        }

    private enum class Tipo(val esterno: Int, val alto: Int, val basso: Int) {
        SCURO(TASTO, CONCA_SCURA_ALTO, CONCA_SCURA_BASSO),
        CHIARO(GRIGIO_CHIARO, CONCA_CHIARA_ALTO, CONCA_CHIARA_BASSO),
        PLAY(ARANCIO, CONCA_PLAY_ALTO, CONCA_PLAY_BASSO),
    }

    private fun conca(tipo: Tipo): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(tipo.alto, tipo.basso)).apply {
            shape = GradientDrawable.OVAL
        }

    private fun cornice(context: Context, tipo: Tipo, interno: Int, contenuto: View): FrameLayout =
        FrameLayout(context).apply {
            background = ovale(tipo.esterno, if (tipo == Tipo.SCURO) ANELLO else 0)
            clipToOutline = true
            addView(contenuto.apply { background = conca(tipo) }, lpf(interno, interno, Gravity.CENTER))
        }

    private class Tasto(val view: View, val icona: ImageView)

    private class Pulsante(val view: View, val icona: ImageView?, val testo: TextView)

    private fun tasto(
        context: Context,
        lato: Int,
        interno: Int,
        icona: Int,
        tipo: Tipo,
        tinta: Int,
        descrizione: String?,
        azione: () -> Unit,
    ): Tasto {
        val glifo = Ui.icona(context, icona, tinta)
        val contenuto = FrameLayout(context).apply { addView(glifo, lpf(lato, lato, Gravity.CENTER)) }
        val cornice = cornice(context, tipo, interno, contenuto)
        Ui.premibile(cornice, if (tipo == Tipo.SCURO) ONDA else ONDA_SCURA, descrizione, azione)
        return Tasto(cornice, glifo)
    }

    private class ScalaOre(context: Context) : View(context) {
        private val pennello = Paint()
        var ore = 0f
            set(valore) {
                field = valore
                invalidate()
            }

        override fun onDraw(canvas: Canvas) {
            val passo = (width - 2f) / 24f
            for (i in 0..24) {
                val lunga = i % 6 == 0
                pennello.color = if (lunga) SECONDARIO else TACCA
                val x = i * passo
                canvas.drawRect(x, if (lunga) 0f else 10f, x + 2f, 18f, pennello)
            }
            pennello.color = ARANCIO
            val x = ore / 24f * (width - 2f) - 1.5f
            canvas.drawRect(x, 0f, x + 5f, 18f, pennello)
        }
    }

    private class Termometro(context: Context) : View(context) {
        private val pennello = Paint()
        private var gradi = 0
        private var minima = 0
        private var massima = 0
        private var presente = false

        fun imposta(meteo: Meteo?) {
            presente = meteo != null
            if (meteo != null) {
                minima = minOf(meteo.minima, meteo.gradi)
                massima = maxOf(meteo.massima, meteo.gradi)
                gradi = meteo.gradi
            }
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.scale(width / 28f, height / 112f)
            pennello.color = TACCA
            canvas.drawRect(11f, 6f, 13f, 106f, pennello)
            if (!presente) return
            val escursione = massima - minima
            if (escursione > 0) {
                val gradiPerTacca = (escursione + 15) / 16
                var g = minima
                while (g <= massima) {
                    val estremo = g == minima || g == massima
                    pennello.color = if (estremo) SECONDARIO else TACCA
                    val y = 105f - (g - minima) * 100f / escursione
                    val mezza = if (estremo) 8f else 3f
                    canvas.drawRect(12f - mezza, y, 12f + mezza, y + 2f, pennello)
                    g += gradiPerTacca
                }
            }
            val y = if (escursione > 0) 106f - (gradi - minima) * 100f / escursione else 56f
            pennello.color = ARANCIO
            canvas.drawCircle(12f, y, 8f, pennello)
        }
    }

    private class Interruttore(context: Context, nome: String, descrizione: String, azione: () -> Unit) {
        private val spia = View(context)
        private val icona = ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        private val stato = Ui.adatta(Ui.testo(context, 28, SECONDARIO, normale, 32), 20, 28)
        val view: View

        init {
            val tondo = cornice(context, Tipo.SCURO, 80, FrameLayout(context).apply { addView(icona, lpf(44, 44, Gravity.CENTER)) })
            val etichetta = Ui.riga(context, 8).apply {
                addView(spia, lp(12, 12))
                addView(Ui.adatta(Ui.testo(context, 30, TESTO, normale, 34, nome), 20, 30), lp(0, 38, 1f))
            }
            val testi = Ui.colonna(context, 0, Gravity.CENTER_VERTICAL).apply {
                addView(etichetta, lp(TUTTO, 38))
                addView(stato, lp(TUTTO, 36))
            }
            val riga = Ui.riga(context, 14).apply {
                addView(tondo, lp(104, 104))
                addView(testi, lp(0, ADATTA, 1f))
            }
            view = Ui.premibile(FrameLayout(context).apply { addView(riga, lpf(TUTTO, TUTTO)) }, ONDA, descrizione, azione)
        }

        fun imposta(spiaNuova: Drawable, risorsa: Int, tinta: Int, testo: String, colore: Int) {
            spia.background = spiaNuova
            icona.setImageResource(risorsa)
            icona.imageTintList = ColorStateList.valueOf(tinta)
            stato.text = testo
            stato.setTextColor(colore)
        }
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeBraun(s, azioni)

    private class HomeBraun(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, TESTO)
        private val cielo = Ui.adatta(Ui.testo(s, 32, TESTO, normale, 38, "In attesa"), 20, 32)
        private val gradi = Ui.testo(s, 104, TESTO, leggero, 0, "--°").apply { letterSpacing = -0.0288f }
        private val estremi = Ui.adatta(Ui.testo(s, 28, SECONDARIO, normale, 34, " "), 20, 28)
        private val pioggia = Ui.adatta(Ui.testo(s, 28, ARANCIO, normale, 34), 20, 28)
        private val rigaPioggia = Ui.riga(s, 10)
        private val scala = ScalaOre(s)
        private val termometro = Termometro(s)
        private val ora = Ui.testo(s, 186, TESTO, leggero).apply {
            letterSpacing = -0.043f
            gravity = Gravity.END
        }
        private val data = Ui.testo(s, 32, SECONDARIO, normale, 38).apply { gravity = Gravity.END }
        private val inAscolto = Ui.riga(s, 24).apply { setPadding(20, 15, 28, 15) }
        private val fermo = Ui.riga(s, 24).apply { setPadding(20, 15, 28, 15) }
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = arrotondato(TASTO, 12)
            clipToOutline = true
        }
        private val cornicecopertina = FrameLayout(s).apply {
            background = arrotondato(0, 18, ANELLO, 2)
            setPadding(6, 6, 6, 6)
        }
        private val spia = Ui.riga(s, 12).apply {
            addView(View(s).apply { background = ovale(ARANCIO) }, lp(12, 12))
            addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "in riproduzione"))
        }
        private val titolo = Ui.testo(s, 44, TESTO, normale, 50)
        private val artista = Ui.testo(s, 28, SECONDARIO, normale, 34)
        private lateinit var play: Tasto
        private val wifi = Interruttore(s, "Wi-Fi", "Wi-Fi") { azioni.wifi() }
        private val bluetooth = Interruttore(s, "Bluetooth", "Bluetooth") { azioni.bluetooth() }
        private val luogo = Ui.adatta(Ui.testo(s, 28, TESTO, normale, 34), 20, 28)
        private val puntina = Ui.icona(s, R.drawable.ic_puntina, ARANCIO)
        private val rigaLuogo = Ui.riga(s, 10)
        private val iconaDomani = Ui.icona(s, R.drawable.ic_nuvoloso, TESTO)
        private val previsioneDomani = Ui.adatta(Ui.testo(s, 28, TESTO, normale, 34), 20, 28)
        private val rigaDomani = Ui.riga(s, 12)

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s).apply {
                setBackgroundColor(CORPO)
                setPadding(32, 32, 32, 32)
            }
            radice.addView(display(), lp(TUTTO, 290))
            radice.addView(modulo(), lp(TUTTO, 160).apply { topMargin = 12 })
            val playlist = Playlists.elenco(s)
            if (playlist.isNotEmpty()) radice.addView(preselezioni(playlist), lp(TUTTO, 112).apply { topMargin = 12 })
            radice.addView(tastierino(), lp(TUTTO, 0, 1f).apply { topMargin = 16 })
            radice.addView(sistema(), lp(TUTTO, 112).apply { topMargin = 16 })
            return radice
        }

        private fun margineInterlinea(testo: TextView, linea: Int): Int =
            -(testo.paint.getFontMetricsInt(null) - linea) / 2

        private fun display(): View {
            val riquadro = Ui.colonna(s).apply {
                background = arrotondato(DISPLAY, 28, FILETTO, 2)
                setPadding(42, 20, 42, 16)
            }
            val contenuto = Ui.riga(s, 0, Gravity.NO_GRAVITY)
            val sinistra = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            val rigaMeteo = Ui.riga(s, 24)
            val rigaGradi = Ui.riga(s, 20)
            val margineGradi = margineInterlinea(gradi, 104)
            rigaGradi.addView(gradi, lp(ADATTA, ADATTA).apply {
                topMargin = margineGradi
                bottomMargin = margineGradi
            })
            rigaGradi.addView(termometro, lp(25, 100))
            rigaMeteo.addView(rigaGradi, lp(ADATTA, ADATTA))
            val colonnaMeteo = Ui.colonna(s, 2, Gravity.CENTER_VERTICAL)
            puntina.visibility = View.GONE
            rigaLuogo.addView(puntina, lp(24, 24))
            rigaLuogo.addView(luogo, lp(0, 36, 1f))
            rigaLuogo.visibility = View.GONE
            colonnaMeteo.addView(rigaLuogo, lp(TUTTO, 36))
            val rigaCielo = Ui.riga(s, 10)
            rigaCielo.addView(iconaMeteo, lp(40, 40))
            rigaCielo.addView(cielo, lp(0, 42, 1f))
            colonnaMeteo.addView(rigaCielo, lp(TUTTO, 42))
            colonnaMeteo.addView(estremi, lp(TUTTO, 36))
            rigaPioggia.addView(View(s).apply { background = ovale(ARANCIO) }, lp(12, 12))
            rigaPioggia.addView(pioggia, lp(0, 36, 1f))
            rigaPioggia.visibility = View.GONE
            colonnaMeteo.addView(rigaPioggia, lp(TUTTO, 36))
            rigaDomani.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "Domani"), lp(ADATTA, ADATTA))
            rigaDomani.addView(iconaDomani, lp(28, 28))
            rigaDomani.addView(previsioneDomani, lp(0, 36, 1f))
            rigaDomani.visibility = View.GONE
            colonnaMeteo.addView(rigaDomani, lp(TUTTO, 36))
            rigaMeteo.addView(colonnaMeteo, lp(0, ADATTA, 1f))
            sinistra.addView(rigaMeteo, lp(TUTTO, ADATTA))
            contenuto.addView(sinistra, lp(480, TUTTO))
            contenuto.addView(View(s).apply { setBackgroundColor(FILETTO) }, lp(2, TUTTO).apply {
                leftMargin = 40
                rightMargin = 40
            })
            val destra = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            val margineOra = margineInterlinea(ora, 146)
            destra.addView(ora, lp(TUTTO, ADATTA).apply {
                topMargin = margineOra
                bottomMargin = margineOra
            })
            destra.addView(data, lp(TUTTO, ADATTA))
            contenuto.addView(destra, lp(0, TUTTO, 1f))
            Ui.senzaRitaglio(riquadro, contenuto, destra, sinistra, rigaGradi, rigaMeteo, colonnaMeteo, rigaLuogo, rigaDomani)
            riquadro.addView(contenuto, lp(TUTTO, 0, 1f))
            riquadro.addView(scala, lp(TUTTO, 18).apply { topMargin = 4 })
            return riquadro
        }

        private fun modulo(): View {
            val sezione = FrameLayout(s).apply { background = arrotondato(MODULO, 28) }

            inAscolto.gravity = Gravity.CENTER_VERTICAL
            cornicecopertina.addView(copertina, lpf(118, 118))
            Ui.premibile(cornicecopertina, ONDA, "Apri Spotify") { azioni.apriMusica() }
            inAscolto.addView(cornicecopertina, lp(130, 130))
            val info = Ui.colonna(s, 2, Gravity.CENTER_VERTICAL)
            info.addView(spia)
            info.addView(titolo, lp(TUTTO, ADATTA).apply { topMargin = 2 })
            info.addView(artista)
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, TUTTO, 1f))
            val comandi = Ui.riga(s, 22)
            comandi.addView(tasto(s, 40, 92, R.drawable.ic_precedente, Tipo.SCURO, TESTO, "Brano precedente") { azioni.precedente() }.view, lp(120, 120))
            play = tasto(s, 48, 100, R.drawable.ic_pausa, Tipo.PLAY, CORPO, "Pausa") { azioni.playPausa() }
            comandi.addView(play.view, lp(130, 130))
            comandi.addView(tasto(s, 40, 92, R.drawable.ic_successivo, Tipo.SCURO, TESTO, "Brano successivo") { azioni.successivo() }.view, lp(120, 120))
            inAscolto.addView(comandi)

            fermo.gravity = Gravity.CENTER_VERTICAL
            fermo.addView(Ui.testo(s, 56, TESTO, leggero, 64, "Spotify").apply { setPadding(20, 0, 0, 0) }, lp(0, ADATTA, 1f))
            val apri = Ui.testo(s, 32, TESTO, normale, 38, "Apri Spotify").apply {
                gravity = Gravity.CENTER
                background = arrotondato(TASTO, 60, ANELLO, 3)
                clipToOutline = true
                setPadding(48, 0, 48, 0)
            }
            fermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 120))
            fermo.addView(tasto(s, 48, 100, R.drawable.ic_play, Tipo.PLAY, CORPO, "Riproduci") { azioni.playPausa() }.view, lp(130, 130))

            sezione.addView(inAscolto, lpf(TUTTO, TUTTO))
            sezione.addView(fermo, lpf(TUTTO, TUTTO))
            mostraMusica(null)
            return sezione
        }

        private fun preselezioni(playlist: List<app.australdashboard.funzioni.Playlist>): View {
            val riga = Ui.riga(s, 20)
            playlist.forEachIndexed { i, p ->
                val corpo = Ui.riga(s, 16).apply {
                    background = arrotondato(TASTO, 24, ANELLO, 3)
                    clipToOutline = true
                    setPadding(16, 0, 20, 0)
                }
                corpo.addView(Ui.testo(s, 32, TESTO, leggero, 0, "${i + 1}").apply {
                    gravity = Gravity.CENTER
                    background = conca(Tipo.SCURO)
                }, lp(56, 56))
                corpo.addView(
                    Ui.adatta(Ui.testo(s, 28, TESTO, normale, 32, p.nome, righe = 3).apply { gravity = Gravity.CENTER_VERTICAL }, 18, 28),
                    lp(0, 104, 1f),
                )
                riga.addView(Ui.premibile(corpo, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) }, lp(0, TUTTO, 1f))
            }
            return riga
        }

        private fun dettaglio(descrizione: String, azione: () -> Unit): View {
            val glifo = Ui.testo(s, 56, TESTO, leggero, 0, "›").apply { gravity = Gravity.CENTER }
            val tondo = cornice(s, Tipo.SCURO, 80, glifo)
            return Ui.premibile(FrameLayout(s).apply { addView(tondo, lpf(100, 100, Gravity.CENTER)) }, ONDA, descrizione, azione)
        }

        private fun comandoTema(): View {
            val icona = Ui.icona(s, R.drawable.ic_tavolozza, CORPO)
            val tondo = cornice(s, Tipo.CHIARO, 80, FrameLayout(s).apply { addView(icona, lpf(44, 44, Gravity.CENTER)) })
            val riga = Ui.riga(s, 14).apply {
                addView(tondo, lp(104, 104))
                addView(Ui.adatta(Ui.testo(s, 30, TESTO, normale, 34, "Tema"), 20, 30), lp(0, 38, 1f))
            }
            return Ui.premibile(FrameLayout(s).apply { addView(riga, lpf(TUTTO, TUTTO)) }, ONDA, "Tema") { azioni.temaSuccessivo() }
        }

        private fun sistema(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(wifi.view, lp(276, TUTTO))
            riga.addView(dettaglio("Dettagli Wi-Fi") { azioni.wifiDettaglio() }, lp(100, TUTTO))
            riga.addView(bluetooth.view, lp(276, TUTTO))
            riga.addView(dettaglio("Dettagli Bluetooth") { azioni.bluetoothDettaglio() }, lp(100, TUTTO))
            riga.addView(View(s).apply { setBackgroundColor(ANELLO) }, lp(2, 56))
            riga.addView(comandoTema(), lp(0, TUTTO, 1f))
            return riga
        }

        private fun tastierino(): View {
            val app = AppHome.elenco(s)
            val celle = listOf(
                cellaApp(app[0]),
                cellaApp(app[1]),
                cellaApp(app[2]),
                cellaServizio(R.drawable.ic_griglia, 48, "Tutte le app", null) { azioni.tutteLeApp() },
                cellaServizio(R.drawable.ic_benzinaio, 48, "Benzinai", null) { azioni.benzinai() },
                cellaApp(app[3]),
                cellaApp(app[4]),
                cellaApp(app[5]),
                cellaServizio(R.drawable.ic_cursori, 48, "Impostazioni Android", null) { azioni.impostazioniAndroid() },
                cellaServizio(R.drawable.ic_ingranaggio, 52, "Launcher", "Impostazioni del launcher") { azioni.impostazioniLauncher() },
            )
            val colonna = Ui.colonna(s, 20)
            for (r in 0 until 2) {
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                for (c in 0 until 5) riga.addView(celle[r * 5 + c], lp(220, TUTTO))
                colonna.addView(riga, lp(TUTTO, 0, 1f))
            }
            val filetto = View(s).apply { setBackgroundColor(ANELLO) }
            return FrameLayout(s).apply {
                addView(colonna, lpf(TUTTO, TUTTO))
                addView(filetto, lpf(2, TUTTO).apply {
                    leftMargin = 709
                    topMargin = 10
                    bottomMargin = 10
                })
            }
        }

        private fun cella(tondo: View, nome: String): LinearLayout {
            val colonna = Ui.colonna(s, 6, Gravity.CENTER)
            colonna.addView(tondo, lp(120, 120))
            colonna.addView(
                Ui.testo(s, 28, TESTO, normale, 32, nome, righe = 2).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL },
                lp(TUTTO, 64),
            )
            colonna.setOnClickListener { tondo.performClick() }
            return colonna
        }

        private fun cellaApp(app: AppHome): View {
            val tondo = cornice(s, Tipo.SCURO, 92, Ui.testo(s, 46, TESTO, leggero, 0, app.sigla).apply { gravity = Gravity.CENTER })
            val installata = app.intent(s) != null
            Ui.premibile(tondo, ONDA, app.nome) { if (installata) azioni.apri(app) }
            val cella = cella(tondo, app.nome)
            if (!installata) cella.alpha = 0.35f
            return cella
        }

        private fun cellaServizio(icona: Int, lato: Int, nome: String, descrizione: String?, azione: () -> Unit): View {
            val t = tasto(s, lato, 92, icona, Tipo.CHIARO, CORPO, descrizione ?: nome, azione)
            return cella(t.view, nome)
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
            val calendario = Calendar.getInstance().apply { time = adesso }
            scala.ore = calendario.get(Calendar.HOUR_OF_DAY) + calendario.get(Calendar.MINUTE) / 60f
        }

        override fun mostraMeteo(meteo: Meteo?) {
            termometro.imposta(meteo)
            val testoPioggia = FonteMeteo.testoPioggia(meteo?.pioggiaTraMinuti)
            rigaPioggia.visibility = if (testoPioggia != null) View.VISIBLE else View.GONE
            pioggia.text = testoPioggia.orEmpty()
            val nomeLuogo = meteo?.luogo.orEmpty().take(30)
            luogo.text = nomeLuogo
            rigaLuogo.visibility = if (nomeLuogo.isNotEmpty()) View.VISIBLE else View.GONE
            puntina.visibility = if (meteo?.gps == true) View.VISIBLE else View.GONE
            val domani = meteo?.domani
            rigaDomani.visibility = if (domani != null) View.VISIBLE else View.GONE
            if (domani != null) {
                iconaDomani.setImageResource(Ui.iconaCielo(domani.codice))
                previsioneDomani.text = "${domani.minima}° / ${domani.massima}°"
            }
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                cielo.text = "In attesa"
                gradi.text = "--°"
                estremi.text = " "
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            gradi.text = "${meteo.gradi}°"
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            titolo.text = musica.titolo
            artista.text = musica.artista
            copertina.contentDescription = "Apri ${musica.app}"
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            play.icona.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
            play.view.contentDescription = if (musica.inRiproduzione) "Pausa" else "Riproduci"
        }

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> wifi.imposta(contorno(TACCA), R.drawable.ic_wifi_spento, SECONDARIO, "spento", SECONDARIO)
                StatoWifi.ACCESO -> wifi.imposta(contorno(ARANCIO), R.drawable.ic_wifi, TESTO, "acceso", SECONDARIO)
                StatoWifi.CONNESSO -> wifi.imposta(ovale(ARANCIO), R.drawable.ic_wifi, TESTO, "connesso", ARANCIO)
            }
            if (stato.bluetooth) {
                bluetooth.imposta(ovale(ARANCIO), R.drawable.ic_bluetooth, TESTO, "acceso", ARANCIO)
            } else {
                bluetooth.imposta(contorno(TACCA), R.drawable.ic_bluetooth_spento, SECONDARIO, "spento", SECONDARIO)
            }
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiBraun(s, azioni)

    private class BenzinaiBraun(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private lateinit var intorno: Pulsante
        private val raggio = Ui.testo(s, 36, TESTO, leggero, 40, "${Benzinai.raggio(s).toInt()} km").apply { gravity = Gravity.CENTER }
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: View
        private val spesa = Ui.testo(s, 28, SECONDARIO, normale, 32).apply { visibility = View.GONE }
        private val nomeScheda = Ui.testo(s, 28, SECONDARIO, normale, 34)
        private val indirizzo = Ui.adatta(
            Ui.testo(s, 56, TESTO, leggero, 62, maiuscolo = true).apply { letterSpacing = 0.018f },
            28,
            56,
        )
        private val dettagli = Ui.testo(s, 28, TESTO, normale, 34)
        private val stato = StatoBenzinai()

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(CORPO)
                setPadding(32, 32, 32, 32)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(filtri(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 182))
            return radice
        }

        override fun mostraSpesa(testo: String?) {
            spesa.text = testo.orEmpty()
            spesa.visibility = if (testo != null) View.VISIBLE else View.GONE
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(tasto(s, 44, 92, R.drawable.ic_indietro, Tipo.SCURO, TESTO, "Indietro") { azioni.indietro() }.view, lp(120, 120))
            riga.addView(Ui.testo(s, 64, TESTO, leggero, 72, "Benzinai"), lp(0, ADATTA, 1f).apply { leftMargin = 8 })
            riga.addView(pulsante(R.drawable.ic_mappa, "Mappa") { azioni.mappa() }.view, lp(215, 120))
            val pillola = Ui.riga(s, 16).apply {
                background = arrotondato(TASTO, 60, ANELLO, 3)
                clipToOutline = true
                setPadding(30, 0, 40, 0)
            }
            pillola.addView(Ui.icona(s, R.drawable.ic_rifornimenti, TESTO), lp(44, 44))
            val testi = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            testi.addView(Ui.testo(s, 32, TESTO, normale, 38, "Rifornimenti"))
            testi.addView(spesa)
            pillola.addView(testi)
            riga.addView(Ui.premibile(pillola, ONDA, "Rifornimenti") { azioni.rifornimenti(stato.selezionato) }, lp(ADATTA, 120))
            return riga
        }

        private fun filtri(): View {
            val riga = Ui.riga(s, 20)
            intorno = pulsante(R.drawable.ic_mirino, "Intorno a me") { azioni.intornoAMe() }
            riga.addView(intorno.view, lp(300, TUTTO))
            val corpoRaggio = Ui.colonna(s, 0, Gravity.CENTER).apply {
                background = arrotondato(TASTO, 60, ANELLO, 3)
                clipToOutline = true
                addView(Ui.testo(s, 28, SECONDARIO, normale, 32, "Raggio").apply { gravity = Gravity.CENTER })
                addView(raggio)
            }
            riga.addView(Ui.premibile(corpoRaggio, ONDA, "Raggio") { azioni.raggio() }, lp(170, TUTTO))
            riga.addView(View(s).apply { setBackgroundColor(ANELLO) }, lp(2, 56))
            for ((ordine, nome) in Ui.ORDINI) {
                val pulsante = Ui.testo(s, 32, TESTO, normale, 38, nome).apply {
                    gravity = Gravity.CENTER
                    clipToOutline = true
                }
                ordini[ordine] = pulsante
                riga.addView(Ui.premibile(pulsante, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(0, TUTTO, 1f))
            }
            evidenziaOrdine(Ordine.CONVIENE)
            evidenziaIntorno(false)
            return riga
        }

        private fun pulsante(icona: Int?, nome: String, azione: () -> Unit): Pulsante {
            val glifo = icona?.let { Ui.icona(s, it, TESTO) }
            val testo = Ui.adatta(Ui.testo(s, 32, TESTO, normale, 38, nome), 22, 32).apply { gravity = Gravity.CENTER }
            val corpo = Ui.riga(s, 14).apply {
                gravity = Gravity.CENTER
                background = arrotondato(TASTO, 60, ANELLO, 3)
                clipToOutline = true
                setPadding(24, 0, 24, 0)
                if (glifo != null) addView(glifo, lp(44, 44))
                addView(testo, lp(0, 44, 1f))
            }
            return Pulsante(Ui.premibile(corpo, ONDA, nome, azione), glifo, testo)
        }

        private fun evidenziaIntorno(acceso: Boolean) {
            intorno.view.background = if (acceso) arrotondato(ARANCIO, 60) else arrotondato(TASTO, 60, ANELLO, 3)
            intorno.testo.setTextColor(if (acceso) CORPO else TESTO)
            intorno.testo.typeface = if (acceso) medio else normale
            intorno.icona?.imageTintList = ColorStateList.valueOf(if (acceso) CORPO else TESTO)
            intorno.view.isSelected = acceso
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, pulsante) in ordini) {
                val scelto = ordine == attivo
                pulsante.background = if (scelto) arrotondato(ARANCIO, 60) else arrotondato(TASTO, 60, ANELLO, 3)
                pulsante.setTextColor(if (scelto) CORPO else TESTO)
                pulsante.typeface = if (scelto) medio else normale
                pulsante.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            val riquadro = Ui.riga(s, 28).apply {
                background = arrotondato(DISPLAY, 28, FILETTO, 2)
                setPadding(36, 0, 30, 0)
            }
            val testi = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            val rigaNome = Ui.riga(s, 12)
            rigaNome.addView(View(s).apply { background = ovale(ARANCIO) }, lp(12, 12))
            rigaNome.addView(nomeScheda, lp(0, ADATTA, 1f))
            testi.addView(rigaNome, lp(TUTTO, ADATTA))
            testi.addView(indirizzo, lp(TUTTO, 62))
            testi.addView(dettagli)
            riquadro.addView(testi, lp(0, ADATTA, 1f))
            riquadro.addView(tasto(s, 44, 92, R.drawable.ic_chiudi, Tipo.SCURO, TESTO, "Chiudi scheda") { seleziona(null) }.view, lp(120, 120))
            riquadro.visibility = View.GONE
            scheda = riquadro
            return riquadro
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
                    raggio.text = "${e.raggioKm.toInt()} km"
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("Preferiti", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("Vicini", e.vicini, false, e)
                    if (tutti.isEmpty()) messaggio("Nessun distributore entro ${e.raggioKm.toInt()} km")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(testo: String) {
            val card = Ui.colonna(s).apply {
                background = arrotondato(MODULO, 28)
                setPadding(32, 28, 32, 28)
            }
            card.addView(Ui.testo(s, 40, TESTO, normale, 48, testo, righe = 3))
            elencoView.addView(card, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean, pronto: ElencoBenzinai.Pronto? = null) {
            val colonna = Ui.colonna(s, 10)
            val intestazioneSezione = Ui.riga(s, 20)
            intestazioneSezione.addView(Ui.testo(s, 40, TESTO, normale, 44, titolo))
            intestazioneSezione.addView(View(s).apply { setBackgroundColor(ANELLO) }, lp(0, 2, 1f))
            if (pronto != null) {
                val km = pronto.raggioKm.toInt()
                val acceso = pronto.intornoAMe
                val stato = Ui.riga(s, 10)
                stato.addView(View(s).apply { background = if (acceso) ovale(ARANCIO) else contorno(TACCA) }, lp(12, 12))
                stato.addView(Ui.testo(s, 28, if (acceso) ARANCIO else SECONDARIO, normale, 34, if (acceso) "solo intorno a me · $km km" else "+ comuni preferiti · $km km"))
                intestazioneSezione.addView(stato)
            }
            intestazioneSezione.setPadding(12, 0, 0, 0)
            colonna.addView(intestazioneSezione, lp(TUTTO, ADATTA))
            val righe = Ui.colonna(s, 20)
            distributori.forEach { righe.addView(riga(it, luogo), lp(TUTTO, 120)) }
            colonna.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean): View {
            val adesso = System.currentTimeMillis()
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = arrotondato(MODULO, 28, if (scelto) ARANCIO else MODULO, 3)
                clipToOutline = true
                setPadding(32, 0, 32, 0)
            }
            val sinistra = Ui.colonna(s, 2)
            sinistra.addView(Ui.testo(s, 40, TESTO, normale, 46, if (luogo) d.comune else d.nome))
            sinistra.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(View(s).apply { setBackgroundColor(ANELLO) }, lp(2, 56))
            corpo.addView(
                Ui.adatta(Ui.testo(s, 30, SECONDARIO, normale, 36, Benzinai.distanza(d.distanzaKm)).apply { gravity = Gravity.END }, 22, 30),
                lp(96, 42),
            )
            corpo.addView(View(s).apply { setBackgroundColor(ANELLO) }, lp(2, 56))
            val blocco = Ui.riga(s, 14)
            val andamento = DatiMimit.andamento(d)
            if (andamento != null) {
                val variazione = Ui.riga(s, 6)
                variazione.addView(Ui.icona(s, if (andamento.salito) R.drawable.ic_su else R.drawable.ic_giu, SECONDARIO), lp(18, 18))
                variazione.addView(Ui.testo(s, 28, TESTO, normale, 32, andamento.differenza))
                variazione.contentDescription = "Prezzo ${if (andamento.salito) "salito" else "sceso"} di ${andamento.differenza}"
                blocco.addView(variazione)
            }
            val prezzo = Ui.riga(s, 0, Gravity.TOP)
            val testoPrezzo = SpannableStringBuilder(Benzinai.prezzo(d.prezzo)).apply {
                if (isNotEmpty()) setSpan(RelativeSizeSpan(42f / 64f), length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            prezzo.addView(Ui.testo(s, 64, TESTO, leggero, 64).apply {
                letterSpacing = -0.031f
                text = testoPrezzo
            })
            prezzo.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, " €/l"))
            blocco.addView(prezzo)
            corpo.addView(blocco)
            return Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
        }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            nomeScheda.text = "${d.nome} · ${d.marchio}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            val prezzo = "${Benzinai.prezzo(d.prezzo)} €/l self"
            val centro = " · ${Benzinai.distanza(d.distanzaKm)} · "
            val aggiornato = "aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
            dettagli.text = SpannableStringBuilder(prezzo + centro + aggiornato).apply {
                setSpan(ForegroundColorSpan(ARANCIO), 0, prezzo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(SECONDARIO), prezzo.length + centro.length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
