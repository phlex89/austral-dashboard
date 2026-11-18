package app.australdashboard.temi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Solari : Tema {
    override val id = "solari"
    override val nome = "Solari"
    override val idea = "Tabellone partenze a palette"

    private const val SFONDO = 0xFF0D0D0D.toInt()
    private const val PANNELLO = 0xFF151515.toInt()
    private const val CARATTERI = 0xFFF5F5F5.toInt()
    private const val SECONDARIO = 0xFFBDBDBD.toInt()
    private const val AMBRA = 0xFFFFB300.toInt()
    private const val ONDA = 0x55FFB300
    private const val ONDA_SCURA = 0x550D0D0D
    private const val BORDO = 0xFF242424.toInt()
    private const val FILETTO = 0xFF2A2A2A.toInt()
    private const val SPENTA = 0xFF5A5A5A.toInt()
    private const val PERNI = 0xFF050505.toInt()

    private val COLORI_PALETTA = intArrayOf(0xFF2A2A2A.toInt(), 0xFF2A2A2A.toInt(), 0xFF050505.toInt(), 0xFF050505.toInt(), 0xFF202020.toInt(), 0xFF202020.toInt())
    private val STACCHI_PALETTA = floatArrayOf(0f, 0.49f, 0.49f, 0.51f, 0.51f, 1f)

    private val medio: Typeface = Typeface.create(Typeface.create("sans-serif-condensed", Typeface.NORMAL), 500, false)
    private val grassetto: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    override fun palette(context: Context) =
        Palette(SFONDO, CARATTERI, SECONDARIO, AMBRA, SFONDO, PANNELLO, grassetto, medio)

    private class Paletta(raggio: Int, private val perni: Int) : GradientDrawable() {
        private val pennello = Paint().apply { color = PERNI }

        init {
            orientation = Orientation.TOP_BOTTOM
            setColors(COLORI_PALETTA, STACCHI_PALETTA)
            cornerRadius = raggio.toFloat()
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            if (perni == 0) return
            val h = bounds.height()
            val larghezza: Float
            val altezza: Float
            if (perni == 2) {
                larghezza = 6f
                altezza = 14f
            } else if (h >= 200) {
                larghezza = 6f
                altezza = 19f
            } else if (h >= 150) {
                larghezza = 5f
                altezza = 14f
            } else if (h >= 110) {
                larghezza = 4f
                altezza = 11f
            } else {
                larghezza = 3f
                altezza = 8f
            }
            val y = bounds.exactCenterY()
            canvas.drawRect(bounds.left.toFloat(), y - altezza / 2, bounds.left + larghezza, y + altezza / 2, pennello)
            canvas.drawRect(bounds.right - larghezza, y - altezza / 2, bounds.right.toFloat(), y + altezza / 2, pennello)
        }
    }

    private fun paletta(raggio: Int, perni: Int = 0): GradientDrawable = Paletta(raggio, perni)

    private fun pieno(colore: Int, raggio: Int, spessore: Int = 0, bordo: Int = 0): GradientDrawable = GradientDrawable().apply {
        setColor(colore)
        cornerRadius = raggio.toFloat()
        if (spessore > 0) setStroke(spessore, bordo)
    }

    private fun piastra(
        context: Context,
        testo: String,
        px: Int,
        colore: Int = CARATTERI,
        raggio: Int = 4,
        margine: Int = 0,
        maiuscolo: Boolean = false,
        perni: Boolean = false,
    ): TextView = Ui.testo(context, px, colore, medio, px, testo, maiuscolo).apply {
        gravity = Gravity.CENTER
        background = paletta(raggio, if (perni) 1 else 0)
        if (margine > 0) setPadding(margine, 0, margine, 0)
    }

    private fun etichetta(context: Context, testo: String, px: Int = 28, spazio: Int = 3): TextView =
        Ui.testo(context, px, AMBRA, grassetto, px + 6, testo).apply { letterSpacing = spazio / px.toFloat() }

    private fun spia(context: Context): View = View(context)

    private fun accendi(spia: View, accesa: Boolean) {
        spia.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (accesa) setColor(AMBRA) else setStroke(2, SPENTA)
        }
    }

    private fun intestazioneSezione(
        context: Context,
        testo: String,
        px: Int = 28,
        spazio: Int = 3,
        segnale: View? = null,
        coda: View? = null,
    ): LinearLayout = Ui.riga(context, 14).apply {
        if (segnale != null) addView(segnale, lp(14, 14))
        addView(etichetta(context, testo, px, spazio))
        addView(View(context).apply { setBackgroundColor(FILETTO) }, lp(0, 2, 1f))
        if (coda != null) addView(coda)
    }

    private fun riempi(context: Context, riga: LinearLayout, testo: String, larghezza: Int, altezza: Int, px: Int) {
        riga.removeAllViews()
        for (c in testo) {
            if (c == ' ') riga.addView(View(context), lp(larghezza / 2, altezza))
            else riga.addView(piastra(context, c.toString(), px, perni = true), lp(larghezza, altezza))
        }
    }

    private fun cifre(context: Context, testo: String, larghezza: Int, altezza: Int, px: Int, spazio: Int): LinearLayout =
        Ui.riga(context, spazio).also { riempi(context, it, testo, larghezza, altezza, px) }

    private class Tasto(val vista: View, val icona: ImageView)

    private fun tasto(
        context: Context,
        res: Int,
        lato: Int,
        descrizione: String?,
        ambra: Boolean = false,
        colore: Int = CARATTERI,
        azione: () -> Unit,
    ): Tasto {
        val icona = Ui.icona(context, res, if (ambra) SFONDO else colore)
        val cornice = FrameLayout(context).apply {
            background = if (ambra) pieno(AMBRA, 6) else paletta(6, 2)
            addView(icona, lpf(lato, lato, Gravity.CENTER))
        }
        return Tasto(Ui.premibile(cornice, if (ambra) ONDA_SCURA else ONDA, descrizione, azione), icona)
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeSolari(s, azioni)

    private class HomeSolari(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val cifreOra = List(4) { piastra(s, "0", 180, perni = true) }
        private val giorniSettimana = "LMMGVSD".map { piastra(s, it.toString(), 28, SECONDARIO, 3) }
        private val giorno = piastra(s, "", 36, margine = 14, maiuscolo = true)
        private val numero = piastra(s, "", 36, margine = 14)
        private val mese = piastra(s, "", 36, margine = 14, maiuscolo = true)
        private val gradi = Ui.riga(s, 8)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, AMBRA)
        private val cielo = Ui.adatta(piastra(s, "IN ATTESA", 36, margine = 14, maiuscolo = true), 20, 36)
        private val massima = piastra(s, "", 32, margine = 14, maiuscolo = true)
        private val minima = piastra(s, "", 32, margine = 14, maiuscolo = true)
        private val estremi = Ui.riga(s, 8)
        private val spiaOnda = spia(s)
        private val inAscolto = Ui.riga(s, 24).apply { setPadding(20, 20, 20, 20) }
        private val fermo = Ui.riga(s, 24).apply { setPadding(32, 20, 20, 20) }
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = pieno(0xFF2A2A2A.toInt(), 4)
            clipToOutline = true
        }
        private val stato = etichetta(s, "IN ONDA")
        private val titolo = Ui.adatta(Ui.testo(s, 48, CARATTERI, medio, 48, maiuscolo = true).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = paletta(4)
            setPadding(14, 0, 14, 0)
        }, 26, 48)
        private val artista = piastra(s, "", 30, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 420 }
        private val play = tasto(s, R.drawable.ic_pausa, 60, "Pausa", ambra = true) { azioni.playPausa() }

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 24).apply {
                setBackgroundColor(SFONDO)
                setPadding(28, 28, 28, 28)
            }
            val primaRiga = Ui.riga(s, 40, Gravity.NO_GRAVITY)
            primaRiga.addView(boxOra(), lp(650, TUTTO))
            primaRiga.addView(boxMeteo(), lp(0, TUTTO, 1f))
            radice.addView(primaRiga, lp(TUTTO, 334))
            radice.addView(boxMusica(), lp(TUTTO, 260))
            val ultima = Ui.riga(s, 24, Gravity.NO_GRAVITY)
            ultima.addView(boxApp(), lp(0, TUTTO, 1f))
            ultima.addView(servizi(), lp(360, TUTTO))
            radice.addView(ultima, lp(TUTTO, 0, 1f))
            return radice
        }

        private fun boxOra(): View {
            val colonna = Ui.colonna(s, 8)
            val mini = Ui.riga(s, 4)
            giorniSettimana.forEach { mini.addView(it, lp(32, 36)) }
            colonna.addView(intestazioneSezione(s, "ORA", coda = mini), lp(TUTTO, 36))
            val orologio = Ui.riga(s, 12)
            val sinistra = Ui.riga(s, 8)
            sinistra.addView(cifreOra[0], lp(140, 210))
            sinistra.addView(cifreOra[1], lp(140, 210))
            val destra = Ui.riga(s, 8)
            destra.addView(cifreOra[2], lp(140, 210))
            destra.addView(cifreOra[3], lp(140, 210))
            orologio.addView(sinistra)
            orologio.addView(Ui.testo(s, 150, CARATTERI, medio, 150, ":").apply { gravity = Gravity.CENTER }, lp(50, 210))
            orologio.addView(destra)
            colonna.addView(orologio, lp(650, 210))
            val data = Ui.riga(s, 8)
            data.addView(giorno, lp(ADATTA, 60))
            data.addView(numero, lp(ADATTA, 60))
            data.addView(mese, lp(ADATTA, 60))
            colonna.addView(data, lp(ADATTA, ADATTA).apply { topMargin = 8 })
            return colonna
        }

        private fun boxMeteo(): View {
            val colonna = Ui.colonna(s, 8)
            colonna.addView(intestazioneSezione(s, "METEO"), lp(TUTTO, 36))
            val riga = Ui.riga(s, 20)
            riempi(s, gradi, "-°", 110, 160, 130)
            riga.addView(gradi)
            val lato = Ui.colonna(s, 12)
            lato.addView(iconaMeteo, lp(60, 60))
            lato.addView(cielo, lp(TUTTO, 60))
            riga.addView(lato, lp(0, ADATTA, 1f))
            colonna.addView(riga, lp(TUTTO, ADATTA))
            estremi.addView(massima, lp(ADATTA, 56))
            estremi.addView(minima, lp(ADATTA, 56))
            estremi.visibility = View.INVISIBLE
            colonna.addView(estremi, lp(ADATTA, ADATTA).apply { topMargin = 16 })
            return colonna
        }

        private fun boxMusica(): View {
            val box = FrameLayout(s).apply { background = pieno(PANNELLO, 8, 2, BORDO) }

            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            inAscolto.addView(copertina, lp(220, 220))
            val info = Ui.colonna(s, 10, Gravity.CENTER_VERTICAL)
            info.addView(
                Ui.riga(s, 14).apply {
                    addView(spiaOnda, lp(14, 14))
                    addView(stato)
                    addView(View(s).apply { setBackgroundColor(FILETTO) }, lp(0, 2, 1f))
                },
                lp(TUTTO, 36),
            )
            info.addView(titolo, lp(TUTTO, 72))
            info.addView(artista, lp(ADATTA, 52))
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, TUTTO, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tasto(s, R.drawable.ic_precedente, 52, "Brano precedente") { azioni.precedente() }.vista, lp(140, 140))
            comandi.addView(play.vista, lp(140, 140))
            comandi.addView(tasto(s, R.drawable.ic_successivo, 52, "Brano successivo") { azioni.successivo() }.vista, lp(140, 140))
            inAscolto.addView(comandi)

            val testi = Ui.colonna(s, 10, Gravity.CENTER_VERTICAL)
            val spiaSorgente = spia(s)
            accendi(spiaSorgente, false)
            testi.addView(intestazioneSezione(s, "SORGENTE", segnale = spiaSorgente), lp(TUTTO, 36))
            testi.addView(cifre(s, "SPOTIFY", 64, 96, 72, 6), lp(ADATTA, 96))
            fermo.addView(testi, lp(0, TUTTO, 1f))
            val apri = Ui.testo(s, 34, CARATTERI, grassetto, 40, "APRI SPOTIFY", maiuscolo = true).apply {
                gravity = Gravity.CENTER
                background = paletta(6, 2)
                setPadding(36, 0, 36, 0)
                letterSpacing = 1 / 34f
            }
            fermo.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(ADATTA, 140))
            fermo.addView(tasto(s, R.drawable.ic_play, 60, "Riproduci", ambra = true) { azioni.playPausa() }.vista, lp(140, 140))

            box.addView(inAscolto, lpf(TUTTO, TUTTO))
            box.addView(fermo, lpf(TUTTO, TUTTO))
            mostraMusica(null)
            return box
        }

        private fun boxApp(): View {
            val griglia = Ui.colonna(s, 20)
            AppHome.elenco.chunked(3).forEachIndexed { r, gruppo ->
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                gruppo.forEachIndexed { i, app -> riga.addView(cella(app, r * 3 + i + 1), lp(0, TUTTO, 1f)) }
                griglia.addView(riga, lp(TUTTO, 0, 1f))
            }
            return griglia
        }

        private fun cella(app: AppHome, numero: Int): View {
            val centro = Ui.colonna(s, 12, Gravity.CENTER)
            centro.addView(piastra(s, app.sigla.uppercase(Locale.ITALIAN), 100, perni = true), lp(96, 128))
            centro.addView(Ui.testo(s, 30, CARATTERI, grassetto, 34, app.nome, maiuscolo = true).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                letterSpacing = 1 / 30f
            }, lp(TUTTO, ADATTA))
            val cella = FrameLayout(s).apply {
                background = pieno(PANNELLO, 8, 2, BORDO)
                addView(centro, lpf(TUTTO, TUTTO))
                addView(
                    Ui.testo(s, 28, AMBRA, grassetto, 30, String.format(Locale.ITALIAN, "%02d", numero)),
                    lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.START).apply {
                        topMargin = 14
                        leftMargin = 18
                    },
                )
            }
            val installata = app.intent(s) != null
            if (!installata) cella.alpha = 0.35f
            return Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }
        }

        private fun servizi(): View {
            val voci = listOf(
                Triple(R.drawable.ic_benzinaio, "Benzinai", azioni::benzinai),
                Triple(R.drawable.ic_griglia, "Tutte le app", azioni::tutteLeApp),
                Triple(R.drawable.ic_cursori, "Impostazioni Android", azioni::impostazioniAndroid),
                Triple(R.drawable.ic_ingranaggio, "Launcher", azioni::impostazioniLauncher),
            )
            val griglia = Ui.colonna(s, 20)
            voci.chunked(2).forEachIndexed { r, gruppo ->
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                gruppo.forEachIndexed { i, (icona, nome, azione) ->
                    val cella = Ui.colonna(s, 14, Gravity.CENTER).apply {
                        background = pieno(PANNELLO, 8, 2, BORDO)
                        setPadding(10, 0, 10, 0)
                        addView(Ui.icona(s, icona, AMBRA), lp(56, 56))
                        addView(
                            Ui.adatta(Ui.testo(s, 28, CARATTERI, grassetto, 32, nome, righe = 2).apply {
                                gravity = Gravity.CENTER_HORIZONTAL
                            }, 22, 28),
                            lp(TUTTO, 64),
                        )
                    }
                    val descrizione = if (r * 2 + i == 3) "Impostazioni del launcher" else nome
                    riga.addView(Ui.premibile(cella, ONDA, descrizione) { azione() }, lp(0, TUTTO, 1f))
                }
                griglia.addView(riga, lp(TUTTO, 0, 1f))
            }
            return griglia
        }

        override fun mostraOra(adesso: Date) {
            val ore = SimpleDateFormat("HHmm", Locale.ITALIAN).format(adesso)
            cifreOra.forEachIndexed { i, p -> p.text = ore[i].toString() }
            giorno.text = SimpleDateFormat("EEEE", Locale.ITALIAN).format(adesso)
            numero.text = SimpleDateFormat("d", Locale.ITALIAN).format(adesso)
            mese.text = SimpleDateFormat("MMMM", Locale.ITALIAN).format(adesso)
            val oggi = (Calendar.getInstance().apply { time = adesso }.get(Calendar.DAY_OF_WEEK) + 5) % 7
            giorniSettimana.forEachIndexed { i, p ->
                p.setTextColor(if (i == oggi) AMBRA else SECONDARIO)
                p.typeface = if (i == oggi) grassetto else medio
            }
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) return
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            val testo = "${meteo.gradi}°"
            if (testo.length > 2) riempi(s, gradi, testo, 76, 160, 100) else riempi(s, gradi, testo, 110, 160, 130)
            val descrizione = FonteMeteo.descrizione(meteo.codice)
            cielo.text = descrizione
            massima.text = "max ${meteo.massima}°"
            minima.text = "min ${meteo.minima}°"
            estremi.visibility = View.VISIBLE
        }

        init {
            accendi(spiaOnda, false)
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            val quando = if (musica.inRiproduzione) "IN ONDA" else "IN PAUSA"
            stato.text = "$quando · ${musica.app.uppercase(Locale.ITALIAN)}"
            accendi(spiaOnda, musica.inRiproduzione)
            titolo.text = musica.titolo
            artista.text = musica.artista
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            play.icona.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
            play.vista.contentDescription = if (musica.inRiproduzione) "Pausa" else "Riproduci"
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiSolari(s, azioni)

    private class BenzinaiSolari(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private lateinit var scheda: View
        private val indirizzo = Ui.riga(s, 4)
        private val spiaScheda = spia(s)
        private val nomeScheda = piastra(s, "", 30, margine = 14, maiuscolo = true)
        private val prezzoScheda = piastra(s, "", 30, AMBRA, margine = 14, maiuscolo = true)
        private val distanzaScheda = piastra(s, "", 30, margine = 14, maiuscolo = true)
        private val quandoScheda = piastra(s, "", 30, SECONDARIO, margine = 14, maiuscolo = true)
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
        private var selezionato: Distributore? = null

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(28, 28, 28, 28)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(colonne(), lp(TUTTO, 34))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 260))
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(tasto(s, R.drawable.ic_indietro, 52, "Indietro") { azioni.indietro() }.vista, lp(120, 120))
            riga.addView(cifre(s, "BENZINAI", 50, 80, 56, 6), lp(0, ADATTA, 1f))
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val tasto = Ui.testo(s, 32, CARATTERI, grassetto, 36, nome, maiuscolo = true).apply {
                    gravity = Gravity.CENTER
                    letterSpacing = 1 / 32f
                }
                ordini[ordine] = tasto
                gruppo.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(170, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun colonne(): View {
            val riga = Ui.riga(s, 12).apply { setPadding(16, 0, 0, 0) }
            for ((testo, larghezza) in listOf("DISTRIBUTORE · MARCHIO" to 382, "SELF €/L" to 246, "DISTANZA" to 150, "AGGIORNATO" to 190)) {
                riga.addView(etichetta(s, testo, 28, 2), lp(larghezza, ADATTA))
            }
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, tasto) in ordini) {
                val scelto = ordine == attivo
                tasto.background = if (scelto) pieno(AMBRA, 6) else paletta(6, 2)
                tasto.setTextColor(if (scelto) SFONDO else CARATTERI)
                tasto.isSelected = scelto
            }
        }

        private fun boxScheda(): View {
            val corpo = Ui.colonna(s).apply {
                background = pieno(PANNELLO, 8, 3, AMBRA)
                setPadding(23, 15, 23, 19)
            }
            accendi(spiaScheda, true)
            corpo.addView(intestazioneSezione(s, "INDIRIZZO", segnale = spiaScheda), lp(TUTTO, 36))
            corpo.addView(View(s), lp(TUTTO, 0, 1f))
            corpo.addView(indirizzo, lp(TUTTO, 58))
            corpo.addView(View(s), lp(TUTTO, 0, 1f))
            val riga = Ui.riga(s, 10)
            val nome = FrameLayout(s).apply {
                addView(nomeScheda.apply { maxWidth = 560 }, lpf(ADATTA, 56, Gravity.START or Gravity.CENTER_VERTICAL))
            }
            riga.addView(nome, lp(0, 56, 1f))
            riga.addView(prezzoScheda, lp(ADATTA, 56))
            riga.addView(distanzaScheda, lp(ADATTA, 56))
            riga.addView(quandoScheda, lp(ADATTA, 56))
            riga.addView(tasto(s, R.drawable.ic_chiudi, 52, "Chiudi scheda") { seleziona(null) }.vista, lp(120, 120).apply { leftMargin = 10 })
            corpo.addView(riga, lp(TUTTO, 120))
            scheda = corpo
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
                is ElencoBenzinai.Caricamento -> messaggio("AGGIORNAMENTO", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("POSIZIONE", "Posizione non disponibile. Scegli una città di riserva nelle Impostazioni del launcher.")
                is ElencoBenzinai.Errore -> messaggio("ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    val tutti = e.preferiti + e.vicini
                    selezionato = selezionato?.let { sel -> tutti.firstOrNull { it.id == sel.id } }
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("VICINI", e.vicini, false)
                    if (tutti.isEmpty()) messaggio("RISULTATI", "Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(titolo: String, testo: String) {
            val pannello = Ui.colonna(s, 12).apply {
                background = pieno(PANNELLO, 8)
                setPadding(24, 20, 24, 24)
            }
            pannello.addView(etichetta(s, titolo))
            pannello.addView(Ui.testo(s, 40, CARATTERI, medio, 48, testo, righe = 4), lp(TUTTO, ADATTA))
            elencoView.addView(pannello, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean) {
            val colonna = Ui.colonna(s, 12)
            colonna.addView(intestazioneSezione(s, titolo, 40, 3), lp(TUTTO, 48))
            val righe = Ui.colonna(s, 20)
            distributori.forEach { righe.addView(riga(it, luogo), lp(TUTTO, 120)) }
            colonna.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpo = Ui.riga(s, 12).apply {
                background = pieno(PANNELLO, 6, 3, if (scelto) AMBRA else PANNELLO)
                setPadding(16, 0, 16, 0)
            }
            val segnale = spia(s)
            accendi(segnale, scelto)
            corpo.addView(segnale, lp(14, 14))
            val nome = Ui.colonna(s, 6, Gravity.CENTER_VERTICAL)
            nome.addView(piastra(s, if (luogo) d.comune else d.nome, 32, margine = 14, maiuscolo = true).apply { maxWidth = 356 }, lp(ADATTA, 50))
            nome.addView(piastra(s, d.marchio, 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 356 }, lp(ADATTA, 40))
            corpo.addView(nome, lp(356, TUTTO))
            corpo.addView(cifre(s, Benzinai.prezzo(d.prezzo), 46, 72, 56, 4), lp(ADATTA, 72))
            val distanza = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            distanza.addView(piastra(s, Benzinai.distanza(d.distanzaKm), 32, margine = 14, maiuscolo = true).apply { maxWidth = 150 }, lp(ADATTA, 56))
            corpo.addView(distanza, lp(150, TUTTO))
            val quando = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            quando.addView(piastra(s, Benzinai.quando(d.aggiornato, adesso), 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 190 }, lp(ADATTA, 50))
            corpo.addView(quando, lp(190, TUTTO))
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
            val testo = "${d.indirizzo} - ${d.comune}".uppercase(Locale.ITALIAN)
            val ingombro = testo.sumOf { if (it == ' ') 23 else 42 }
            if (ingombro <= 1100) {
                riempi(s, indirizzo, testo, 38, 58, 44)
            } else {
                indirizzo.removeAllViews()
                indirizzo.addView(
                    Ui.adatta(Ui.testo(s, 44, CARATTERI, medio, 44, testo).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        background = paletta(4)
                        setPadding(14, 0, 14, 0)
                    }, 24, 44),
                    lp(TUTTO, 58),
                )
            }
            nomeScheda.text = "${d.nome} · ${d.marchio}"
            prezzoScheda.text = "${Benzinai.prezzo(d.prezzo)} €/l"
            distanzaScheda.text = Benzinai.distanza(d.distanzaKm)
            quandoScheda.text = Benzinai.quando(d.aggiornato, System.currentTimeMillis())
        }
    }
}
