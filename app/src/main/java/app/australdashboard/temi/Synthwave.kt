package app.australdashboard.temi

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.util.TypedValue
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
import kotlin.math.min

object Synthwave : Tema {
    override val id = "synthwave"
    override val nome = "Synthwave"
    override val idea = "Tramonto anni '80, orizzonte con il sole a righe"

    private const val NOTTE = 0xFF0D0221.toInt()
    private const val SUPERFICIE = 0xFF1A0B33.toInt()
    private const val CONTORNO = 0xFF3B1F66.toInt()
    private const val TESTO = 0xFFFDF0FF.toInt()
    private const val SECONDARIO = 0xFFC7A8E8.toInt()
    private const val ROSA = 0xFFFF6EC4.toInt()
    private const val ARANCIO = 0xFFFFB36B.toInt()
    private const val ANELLO = 0xFF7B55B8.toInt()
    private const val ONDA = 0x33FF6EC4
    private const val ONDA_SCURA = 0x330D0221

    private fun audiowide(context: Context): Typeface = Ui.font(context, R.font.audiowide)

    private fun corpo(peso: Int): Typeface =
        Typeface.create(Typeface.create("sans-serif-condensed", Typeface.NORMAL), peso, true)

    private val tipoDritto: Typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    private val tipoDrittoGrassetto: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    private val normale: Typeface by lazy { corpo(400) }
    private val medio: Typeface by lazy { corpo(500) }
    private val grassetto: Typeface by lazy { corpo(700) }

    override fun palette(context: Context) =
        Palette(NOTTE, TESTO, SECONDARIO, ROSA, NOTTE, CONTORNO, audiowide(context), normale)

    private class TestoSfumato(context: Context) : TextView(context) {
        private var larghezza = -1f

        override fun onDraw(canvas: Canvas) {
            val l = paint.measureText(text, 0, text.length).coerceAtLeast(1f)
            if (l != larghezza) {
                larghezza = l
                paint.shader = LinearGradient(0f, 0f, l, 0f, ROSA, ARANCIO, Shader.TileMode.CLAMP)
            }
            super.onDraw(canvas)
        }
    }

    private fun sfumato(context: Context, px: Int, font: Typeface, interlinea: Int = 0, contenuto: String = ""): TextView =
        TestoSfumato(context).apply {
            text = contenuto
            setTextSize(TypedValue.COMPLEX_UNIT_PX, px.toFloat())
            setTextColor(TESTO)
            typeface = font
            includeFontPadding = false
            maxLines = 1
            isSingleLine = true
            if (interlinea > 0) lineHeight = interlinea
        }

    private fun tramonto(raggio: Float, ovale: Boolean = false): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(ROSA, ARANCIO)).apply {
            if (ovale) shape = GradientDrawable.OVAL else cornerRadius = raggio
        }

    private fun superficie(raggio: Float, bordo: Int, spessore: Int = 3, ovale: Boolean = false): GradientDrawable =
        GradientDrawable().apply {
            setColor(SUPERFICIE)
            if (ovale) shape = GradientDrawable.OVAL else cornerRadius = raggio
            setStroke(spessore, bordo)
        }

    private fun arrotonda(view: View, raggio: Float): View {
        view.clipToOutline = true
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, if (raggio < 0) min(v.width, v.height) / 2f else raggio)
            }
        }
        return view
    }

    private fun anello(spessore: Int, colore: Int): GradientDrawable =
        Ui.bordo(colore, spessore).apply { shape = GradientDrawable.OVAL }

    private class Righe(context: Context) : View(context) {
        private val paint = Paint()
        private val altezze = floatArrayOf(4f, 3f, 2f)
        private val tops = floatArrayOf(0f, 9f, 17f)

        override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
            paint.shader = LinearGradient(0f, 0f, w.toFloat().coerceAtLeast(1f), 0f, ROSA, ARANCIO, Shader.TileMode.CLAMP)
        }

        override fun onDraw(canvas: Canvas) {
            for (i in 0..2) canvas.drawRect(0f, tops[i], width.toFloat(), tops[i] + altezze[i], paint)
        }
    }

    private class Orizzonte(context: Context) : View(context) {
        var frazione = -1f
            set(valore) {
                field = valore
                invalidate()
            }
        private val linee = Paint()
        private val tacca = Paint().apply { color = ANELLO }
        private val sole = Paint().apply { shader = LinearGradient(0f, 0f, 0f, 20f, ARANCIO, ROSA, Shader.TileMode.CLAMP) }
        private val fessura = Paint().apply { color = NOTTE }
        private val mezzo = Path().apply { addArc(RectF(-20f, 0f, 20f, 40f), 180f, 180f); close() }

        override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
            linee.shader = LinearGradient(0f, 0f, w.toFloat().coerceAtLeast(1f), 0f, ROSA, ARANCIO, Shader.TileMode.CLAMP)
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            for (i in 0..4) {
                val sinistra = when (i) {
                    0 -> 0f
                    4 -> w - 2f
                    else -> w * i / 4f - 1f
                }
                canvas.drawRect(sinistra, 12f, sinistra + 2f, 20f, tacca)
            }
            if (frazione >= 0f) {
                canvas.save()
                canvas.translate(w * frazione, 0f)
                canvas.clipPath(mezzo)
                canvas.drawPath(mezzo, sole)
                canvas.drawRect(-20f, 14f, 20f, 16f, fessura)
                canvas.drawRect(-20f, 8f, 20f, 10f, fessura)
                canvas.restore()
            }
            canvas.drawRect(0f, 20f, w, 26f, linee)
            canvas.drawRect(0f, 32f, w, 35f, linee)
            canvas.drawRect(0f, 40f, w, 42f, linee)
        }
    }

    private class Tubo(context: Context) : View(context) {
        var frazione = -1f
            set(valore) {
                field = valore
                invalidate()
            }
        private val pista = Paint().apply { color = CONTORNO }
        private val riempimento = Paint()
        private val punto = Paint().apply { color = TESTO; isAntiAlias = true }
        private val riquadro = RectF()

        override fun onMeasure(larghezza: Int, altezza: Int) {
            val w = if (MeasureSpec.getMode(larghezza) == MeasureSpec.EXACTLY) MeasureSpec.getSize(larghezza) else 0
            setMeasuredDimension(w, MeasureSpec.getSize(altezza))
        }

        override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
            riempimento.shader = LinearGradient(0f, 0f, w.toFloat().coerceAtLeast(1f), 0f, ROSA, ARANCIO, Shader.TileMode.CLAMP)
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            riquadro.set(0f, 4f, w, 12f)
            canvas.drawRoundRect(riquadro, 4f, 4f, pista)
            if (frazione < 0f) return
            val x = w * frazione
            riquadro.set(0f, 4f, x.coerceAtLeast(8f), 12f)
            canvas.drawRoundRect(riquadro, 4f, 4f, riempimento)
            canvas.drawCircle(x.coerceIn(8f, w - 8f), 8f, 8f, punto)
        }
    }

    private fun tondo(context: Context, icona: Int, latoIcona: Int, colore: Int, descrizione: String, azione: () -> Unit): View {
        val cerchio = FrameLayout(context).apply {
            background = superficie(0f, CONTORNO, ovale = true)
            addView(View(context).apply { background = anello(2, ANELLO) }, lpf(TUTTO, TUTTO).apply { setMargins(10, 10, 10, 10) })
            addView(Ui.icona(context, icona, colore), lpf(latoIcona, latoIcona, Gravity.CENTER))
        }
        arrotonda(cerchio, -1f)
        return Ui.premibile(cerchio, ONDA, descrizione, azione)
    }

    private fun cerchioPlay(context: Context, icona: ImageView, descrizione: String, azione: () -> Unit): View {
        val interno = FrameLayout(context).apply {
            background = tramonto(0f, ovale = true)
            addView(icona, lpf(52, 52, Gravity.CENTER))
        }
        val cerchio = FrameLayout(context).apply {
            background = anello(2, ROSA)
            addView(interno, lpf(120, 120, Gravity.CENTER))
        }
        arrotonda(cerchio, -1f)
        return Ui.premibile(cerchio, ONDA_SCURA, descrizione, azione)
    }

    private fun cornice(context: Context, raggio: Float, spessore: Int, interno: View): FrameLayout =
        FrameLayout(context).apply {
            background = tramonto(raggio)
            setPadding(spessore, spessore, spessore, spessore)
            addView(interno, lpf(TUTTO, TUTTO))
        }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeSynthwave(s, azioni)

    private class HomeSynthwave(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val aw = audiowide(s)
        private val ora = sfumato(s, 150, aw, 150)
        private val data = Ui.testo(s, 34, TESTO, normale, 40).apply { letterSpacing = 0.06f }
        private val giorni = Ui.riga(s, 4).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES }
        private val lettere = "LMMGVSD".map { lettera ->
            Ui.testo(s, 28, SECONDARIO, tipoDritto, 28, lettera.toString()).apply {
                gravity = Gravity.CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        }
        private val orizzonte = Orizzonte(s)
        private val tubo = Tubo(s).apply { importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES }
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, ROSA)
        private val gradi = Ui.testo(s, 104, TESTO, aw, 104, "--°")
        private val cielo = Ui.testo(s, 36, TESTO, medio, 42, "In attesa")
        private val estremi = Ui.testo(s, 30, SECONDARIO, normale, 36, " ")
        private val inAscolto = Ui.riga(s, 24)
        private val fermo = Ui.colonna(s, 28, Gravity.CENTER_VERTICAL).apply { setPadding(20, 0, 0, 0) }
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0xFFE0874F.toInt(), 0xFF5B2240.toInt()))
        }
        private val etichetta = Ui.adatta(Ui.testo(s, 28, SECONDARIO, normale, maiuscolo = true).apply { letterSpacing = 0.14f }, 18, 28)
        private val titolo = Ui.adatta(Ui.testo(s, 54, TESTO, aw, righe = 2), 28, 54)
        private val artista = Ui.testo(s, 38, SECONDARIO, normale, 44)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, NOTTE)
        private lateinit var playIn: View

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 24).apply {
                setBackgroundColor(NOTTE)
                setPadding(28, 28, 28, 28)
            }
            radice.addView(fasciaOra(), lp(TUTTO, 220))
            radice.addView(orizzonte, lp(TUTTO, 42).apply { topMargin = -20 })
            val centro = Ui.riga(s, 24, Gravity.NO_GRAVITY)
            centro.addView(sezioneMusica(), lp(0, TUTTO, 1f))
            centro.addView(servizi(), lp(260, TUTTO))
            radice.addView(centro, lp(TUTTO, 560))
            radice.addView(app(), lp(TUTTO, 0, 1f))
            return radice
        }

        private fun fasciaOra(): View {
            val riga = Ui.riga(s)
            val sinistra = Ui.colonna(s)
            sinistra.addView(ora, lp(ADATTA, 150))
            val rigaData = Ui.riga(s, 20)
            rigaData.addView(data, lp(ADATTA, ADATTA))
            lettere.forEach { giorni.addView(it, lp(34, 34)) }
            rigaData.addView(giorni, lp(ADATTA, ADATTA))
            sinistra.addView(rigaData, lp(ADATTA, ADATTA).apply { topMargin = 6 })
            riga.addView(sinistra, lp(ADATTA, ADATTA))
            riga.addView(View(s), lp(0, 1, 1f))
            val destra = Ui.riga(s, 22)
            destra.addView(iconaMeteo, lp(76, 76))
            destra.addView(gradi)
            val testi = Ui.colonna(s, 4)
            testi.addView(cielo)
            testi.addView(estremi)
            testi.addView(tubo, lp(TUTTO, 16).apply { topMargin = 6 })
            destra.addView(testi, lp(ADATTA, ADATTA))
            riga.addView(destra, lp(ADATTA, ADATTA))
            return riga
        }

        private fun sezioneMusica(): View {
            val sezione = FrameLayout(s)

            arrotonda(copertina, 22f)
            Ui.premibile(copertina, ONDA_SCURA, "Apri l'app della musica") { azioni.apriMusica() }
            val frame = cornice(s, 28f, 6, copertina)
            val doppia = FrameLayout(s).apply {
                background = Ui.bordo(CONTORNO, 2).apply { cornerRadius = 36f }
                setPadding(8, 8, 8, 8)
                addView(frame, lpf(TUTTO, TUTTO))
            }
            inAscolto.gravity = Gravity.CENTER_VERTICAL
            inAscolto.addView(doppia, lp(468, 468))
            val info = Ui.colonna(s)
            val blocco = Ui.colonna(s)
            blocco.addView(etichetta, lp(TUTTO, 34))
            blocco.addView(titolo, lp(TUTTO, 128).apply { topMargin = 10 })
            blocco.addView(artista, lp(TUTTO, ADATTA).apply { topMargin = 8 })
            Ui.premibile(blocco, ONDA, null) { azioni.apriMusica() }
            info.addView(blocco, lp(TUTTO, ADATTA))
            info.addView(View(s), lp(TUTTO, 0, 1f))
            info.addView(Righe(s), lp(TUTTO, 19))
            info.addView(View(s), lp(TUTTO, 0, 1f))
            val comandi = Ui.riga(s, 16)
            comandi.addView(tondo(s, R.drawable.ic_precedente, 44, TESTO, "Brano precedente") { azioni.precedente() }, lp(120, 120))
            playIn = cerchioPlay(s, iconaPlay, "Pausa") { azioni.playPausa() }
            comandi.addView(playIn, lp(140, 140))
            comandi.addView(tondo(s, R.drawable.ic_successivo, 44, TESTO, "Brano successivo") { azioni.successivo() }, lp(120, 120))
            info.addView(comandi, lp(ADATTA, 140))
            inAscolto.addView(info, lp(0, 468, 1f))

            fermo.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "SORGENTE AUDIO").apply { letterSpacing = 0.14f })
            fermo.addView(Ui.adatta(sfumato(s, 150, aw, contenuto = "Spotify"), 70, 150), lp(TUTTO, 150))
            fermo.addView(Righe(s), lp(520, 19))
            val riga = Ui.riga(s, 24)
            riga.addView(cerchioPlay(s, Ui.icona(s, R.drawable.ic_play, NOTTE), "Riproduci") { azioni.playPausa() }, lp(140, 140))
            val apri = Ui.testo(s, 34, TESTO, medio, 40, "Apri Spotify").apply {
                gravity = Gravity.CENTER
                background = superficie(60f, CONTORNO)
                setPadding(48, 0, 48, 0)
            }
            arrotonda(apri, 60f)
            riga.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 120))
            fermo.addView(riga, lp(ADATTA, ADATTA))

            sezione.addView(inAscolto, lpf(TUTTO, TUTTO))
            sezione.addView(fermo, lpf(TUTTO, TUTTO))
            mostraMusica(null)
            return sezione
        }

        private fun servizi(): View {
            val colonna = Ui.colonna(s, 20)
            val voci = listOf(
                Triple(R.drawable.ic_benzinaio, "Benzinai", "Benzinai") to azioni::benzinai,
                Triple(R.drawable.ic_griglia, "Tutte le app", "Tutte le app") to azioni::tutteLeApp,
                Triple(R.drawable.ic_cursori, "Impostazioni Android", "Impostazioni Android") to azioni::impostazioniAndroid,
                Triple(R.drawable.ic_ingranaggio, "Launcher", "Impostazioni del launcher") to azioni::impostazioniLauncher,
            )
            for ((voce, azione) in voci) {
                val (icona, nome, descrizione) = voce
                val pillola = Ui.riga(s, 14).apply {
                    background = superficie(62f, CONTORNO, 2)
                    setPadding(22, 0, 22, 0)
                    addView(Ui.icona(s, icona, ROSA), lp(40, 40))
                    addView(
                        Ui.adatta(Ui.testo(s, 30, TESTO, medio, contenuto = nome, righe = 2), 20, 30).apply {
                            gravity = Gravity.CENTER_VERTICAL
                        },
                        lp(0, TUTTO, 1f),
                    )
                }
                arrotonda(pillola, 62f)
                colonna.addView(Ui.premibile(pillola, ONDA, descrizione) { azione() }, lp(TUTTO, 0, 1f))
            }
            return colonna
        }

        private fun app(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            for (app in AppHome.elenco) {
                val interno = FrameLayout(s).apply {
                    background = GradientDrawable().apply { setColor(SUPERFICIE); cornerRadius = 30f }
                    addView(Ui.testo(s, 54, TESTO, aw, 54, app.sigla).apply { gravity = Gravity.CENTER }, lpf(TUTTO, ADATTA, Gravity.CENTER))
                }
                val anello = cornice(s, 34f, 4, interno)
                val cella = Ui.colonna(s, 14, Gravity.CENTER)
                cella.addView(anello, lp(130, 130))
                cella.addView(
                    Ui.adatta(Ui.testo(s, 30, TESTO, medio, contenuto = app.nome), 20, 30).apply { gravity = Gravity.CENTER },
                    lp(TUTTO, 34),
                )
                val installata = app.intent(s) != null
                if (!installata) cella.alpha = 0.35f
                Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }
                riga.addView(cella, lp(0, TUTTO, 1f))
            }
            return riga
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso)
            val calendario = Calendar.getInstance().apply { time = adesso }
            orizzonte.frazione = (calendario.get(Calendar.HOUR_OF_DAY) * 60 + calendario.get(Calendar.MINUTE)) / 1440f
            val oggi = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7
            lettere.forEachIndexed { i, lettera ->
                val attivo = i == oggi
                lettera.background = if (attivo) tramonto(0f, ovale = true) else null
                lettera.setTextColor(if (attivo) NOTTE else SECONDARIO)
                lettera.typeface = if (attivo) tipoDrittoGrassetto else tipoDritto
            }
            giorni.contentDescription = "Giorno della settimana: " + SimpleDateFormat("EEEE", Locale.ITALIAN).format(adesso)
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "In attesa"
                estremi.text = " "
                tubo.frazione = -1f
                tubo.contentDescription = null
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "max ${meteo.massima}° · min ${meteo.minima}°"
            if (meteo.massima > meteo.minima) {
                tubo.frazione = ((meteo.gradi - meteo.minima).toFloat() / (meteo.massima - meteo.minima)).coerceIn(0f, 1f)
                tubo.contentDescription = "Temperatura attuale ${meteo.gradi}° tra minima ${meteo.minima}° e massima ${meteo.massima}°"
            } else {
                tubo.frazione = -1f
                tubo.contentDescription = null
            }
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            val stato = if (musica.inRiproduzione) "IN RIPRODUZIONE" else "IN PAUSA"
            etichetta.text = "${musica.app} · $stato"
            titolo.text = musica.titolo
            artista.text = musica.artista
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
            playIn.contentDescription = if (musica.inRiproduzione) "Pausa" else "Riproduci"
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiSynthwave(s, azioni)

    private class BenzinaiSynthwave(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val aw = audiowide(s)
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: View
        private val nomeScheda = Ui.testo(s, 30, SECONDARIO, normale, 36)
        private val indirizzo = Ui.adatta(Ui.testo(s, 76, TESTO, grassetto, maiuscolo = true, righe = 2), 34, 76)
        private val dettagli = Ui.testo(s, 30, TESTO, normale, 36)
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
        private var selezionato: Distributore? = null

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(NOTTE)
                setPadding(28, 28, 28, 28)
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
            val riga = Ui.riga(s, 28)
            riga.addView(tondo(s, R.drawable.ic_indietro, 48, TESTO, "Indietro") { azioni.indietro() }, lp(120, 120))
            riga.addView(Ui.adatta(sfumato(s, 70, aw, contenuto = "Benzinai"), 36, 70), lp(0, TUTTO, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val pillola = Ui.testo(s, 32, TESTO, medio, 38, nome).apply { gravity = Gravity.CENTER }
                arrotonda(pillola, 60f)
                ordini[ordine] = pillola
                gruppo.addView(Ui.premibile(pillola, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(190, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, pillola) in ordini) {
                val scelto = ordine == attivo
                pillola.background = if (scelto) tramonto(60f) else superficie(60f, CONTORNO)
                pillola.setTextColor(if (scelto) NOTTE else TESTO)
                pillola.typeface = if (scelto) grassetto else medio
                pillola.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            val testi = Ui.colonna(s, 8)
            testi.addView(nomeScheda)
            testi.addView(indirizzo, lp(TUTTO, 156))
            testi.addView(Righe(s), lp(360, 19).apply { topMargin = 4; bottomMargin = 4 })
            testi.addView(dettagli)
            val comandi = Ui.riga(s, 20)
            comandi.addView(tondo(s, R.drawable.ic_chiudi, 48, TESTO, "Chiudi scheda") { seleziona(null) }, lp(120, 120))
            val interno = Ui.riga(s, 28).apply {
                background = GradientDrawable().apply { setColor(SUPERFICIE); cornerRadius = 28f }
                setPadding(36, 12, 36, 12)
                addView(testi, lp(0, ADATTA, 1f))
                addView(comandi, lp(ADATTA, ADATTA))
            }
            scheda = cornice(s, 32f, 4, interno)
            scheda.visibility = View.GONE
            return scheda
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
                is ElencoBenzinai.Caricamento -> messaggio("Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("Posizione non disponibile. Scegli una città di riserva nelle Impostazioni del launcher.")
                is ElencoBenzinai.Errore -> messaggio(e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
                    if (e.preferiti.isNotEmpty()) sezione("Preferiti", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("Vicini", e.vicini, false)
                    if (tutti.isEmpty()) messaggio("Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(testo: String) {
            val casella = Ui.colonna(s).apply {
                background = superficie(24f, CONTORNO)
                setPadding(32, 28, 32, 28)
                addView(Ui.testo(s, 42, TESTO, medio, 50, testo, righe = 3))
            }
            elencoView.addView(casella, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean) {
            val colonna = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 24)
            intestazione.addView(Ui.testo(s, 42, TESTO, grassetto, 48, titolo), lp(ADATTA, ADATTA))
            intestazione.addView(Righe(s), lp(0, 19, 1f))
            colonna.addView(intestazione, lp(TUTTO, ADATTA).apply { leftMargin = 12 })
            val righe = Ui.colonna(s, 20)
            distributori.forEachIndexed { i, d -> righe.addView(riga(d, luogo, i + 1), lp(TUTTO, 120)) }
            colonna.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, numero: Int): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpoRiga = Ui.riga(s, 24).apply {
                background = superficie(24f, if (scelto) ROSA else CONTORNO)
                setPadding(28, 0, 32, 0)
            }
            corpoRiga.addView(Ui.testo(s, 28, SECONDARIO, aw, 32, String.format(Locale.ITALY, "%02d", numero)).apply { ellipsize = null; minWidth = 44 }, lp(ADATTA, ADATTA))
            val sinistra = Ui.colonna(s, 2)
            sinistra.addView(Ui.adatta(Ui.testo(s, 42, TESTO, medio, contenuto = if (luogo) d.comune else d.nome, maiuscolo = true), 24, 42), lp(TUTTO, 48))
            sinistra.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}"), lp(TUTTO, 34))
            corpoRiga.addView(sinistra, lp(0, ADATTA, 1f))
            corpoRiga.addView(Ui.testo(s, 32, TESTO, medio, 38, Benzinai.distanza(d.distanzaKm)))
            val prezzo = Ui.riga(s, 8, Gravity.NO_GRAVITY)
            prezzo.addView(sfumato(s, 52, aw, 60).apply { text = millesimo(Benzinai.prezzo(d.prezzo)) })
            prezzo.addView(Ui.testo(s, 28, SECONDARIO, normale, 34, "€/l"))
            corpoRiga.addView(prezzo, lp(ADATTA, ADATTA))
            arrotonda(corpoRiga, 24f)
            riga.addView(Ui.premibile(corpoRiga, ONDA, d.nome) { seleziona(d) }, lp(0, TUTTO, 1f))
            return riga
        }

        private fun millesimo(prezzo: String): CharSequence = SpannableString(prezzo).apply {
            if (prezzo.length > 1) setSpan(RelativeSizeSpan(36f / 52f), prezzo.length - 1, prezzo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        private fun aggiornaScheda() {
            val d = selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            nomeScheda.text = "${d.nome} · ${d.marchio}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            val prezzo = "${Benzinai.prezzo(d.prezzo)} €/l self"
            val testo = SpannableStringBuilder(prezzo).apply {
                setSpan(ForegroundColorSpan(ARANCIO), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(TypefaceSpan(grassetto), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(" · ${Benzinai.distanza(d.distanzaKm)} · ")
                val inizio = length
                append("aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}")
                setSpan(ForegroundColorSpan(SECONDARIO), inizio, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            dettagli.text = testo
        }
    }
}
