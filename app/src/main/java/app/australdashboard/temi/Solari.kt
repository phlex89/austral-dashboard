package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
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
        Palette(SFONDO, CARATTERI, AMBRA, PANNELLO, grassetto, medio)

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

    private fun accendi(spia: View, accesa: Boolean, contorno: Int = SPENTA) {
        spia.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (accesa) setColor(AMBRA) else setStroke(2, contorno)
        }
    }

    private class ChiaveSistema(
        val vista: View,
        val spia: View,
        val icona: ImageView,
        val nome: TextView,
        val stato: TextView,
    )

    private fun chiaveSistema(context: Context, nome: String, descrizione: String, azione: () -> Unit): ChiaveSistema {
        val spia = View(context)
        val icona = ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        val titolo = Ui.testo(context, 30, CARATTERI, grassetto, 34, nome)
        val stato = Ui.testo(context, 28, SECONDARIO, medio, 32)
        val testi = Ui.colonna(context, 8)
        testi.addView(titolo, lp(TUTTO, ADATTA))
        testi.addView(stato, lp(TUTTO, ADATTA))
        val riga = Ui.riga(context, 10).apply { setPadding(14, 0, 14, 0) }
        riga.addView(icona, lp(40, 40))
        riga.addView(testi, lp(0, ADATTA, 1f))
        val corpo = FrameLayout(context).apply { background = paletta(6, 2) }
        corpo.addView(riga, lpf(TUTTO, TUTTO))
        corpo.addView(spia, lpf(14, 14, Gravity.TOP or Gravity.END).apply {
            topMargin = 12
            rightMargin = 14
        })
        accendi(spia, false)
        return ChiaveSistema(Ui.premibile(corpo, ONDA, descrizione, azione), spia, icona, titolo, stato)
    }

    private fun aggiornaChiave(chiave: ChiaveSistema, icona: Int, acceso: Boolean, testo: String, colore: Int, spiaPiena: Boolean, contorno: Int) {
        val principale = if (acceso) CARATTERI else SECONDARIO
        chiave.icona.setImageResource(icona)
        chiave.icona.imageTintList = ColorStateList.valueOf(principale)
        chiave.nome.setTextColor(principale)
        chiave.stato.text = testo
        chiave.stato.setTextColor(colore)
        accendi(chiave.spia, spiaPiena, contorno)
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
        private val playlist = Playlists.elenco(s)
        private val cifreOra = List(4) { piastra(s, "0", 150, perni = true) }
        private val giorniSettimana = "LMMGVSD".map { piastra(s, it.toString(), 28, SECONDARIO, 3) }
        private val giorno = piastra(s, "", 34, margine = 14, maiuscolo = true)
        private val numero = piastra(s, "", 34, margine = 14)
        private val mese = piastra(s, "", 34, margine = 14, maiuscolo = true)
        private val gradi = Ui.riga(s, 8)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, AMBRA)
        private val cielo = piastra(s, "IN ATTESA", 34, margine = 14, maiuscolo = true).apply { maxWidth = 330 }
        private val massima = piastra(s, "", 30, margine = 14, maiuscolo = true)
        private val minima = piastra(s, "", 30, margine = 14, maiuscolo = true)
        private val pioggia = piastra(s, "", 28, AMBRA, margine = 14, maiuscolo = true).apply {
            typeface = grassetto
            maxWidth = 340
            visibility = View.GONE
        }
        private val estremi = Ui.riga(s, 8)
        private val spiaOnda = View(s)
        private val inAscolto = Ui.riga(s, 24).apply { setPadding(18, 18, 18, 18) }
        private val fermo = Ui.riga(s, 24).apply { setPadding(30, 18, 18, 18) }
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = pieno(0xFF2A2A2A.toInt(), 4)
            clipToOutline = true
        }
        private val stato = etichetta(s, "IN ONDA")
        private val titolo = Ui.adatta(Ui.testo(s, 44, CARATTERI, medio, 44, maiuscolo = true).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = paletta(4)
            setPadding(14, 0, 14, 0)
        }, 24, 44)
        private val artista = piastra(s, "", 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 420 }
        private val play = tasto(s, R.drawable.ic_pausa, 60, "Pausa", ambra = true) { azioni.playPausa() }
        private val wifi = chiaveSistema(s, "WI-FI", "Wi-Fi") { azioni.wifi() }
        private val bluetooth = chiaveSistema(s, "BLUETOOTH", "Bluetooth") { azioni.bluetooth() }
        private val puntina = Ui.icona(s, R.drawable.ic_puntina, AMBRA).apply { visibility = View.GONE }
        private val luogo = Ui.adatta(piastra(s, "", 28, margine = 14, maiuscolo = true), 18, 28)
        private val coda = Ui.riga(s, 8).apply { visibility = View.GONE }
        private val iconaDomani = Ui.icona(s, R.drawable.ic_nuvoloso, AMBRA)
        private val temperatureDomani = piastra(s, "", 28, margine = 14)
        private val domani = Ui.colonna(s, 8).apply { visibility = View.GONE }

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 23).apply {
                setBackgroundColor(SFONDO)
                setPadding(28, 28, 28, 28)
            }
            val primaRiga = Ui.riga(s, 40, Gravity.NO_GRAVITY)
            primaRiga.addView(boxOra(), lp(540, TUTTO))
            primaRiga.addView(boxMeteo(), lp(0, TUTTO, 1f))
            radice.addView(primaRiga, lp(TUTTO, 286))
            radice.addView(boxMusica(), lp(TUTTO, 200))
            if (playlist.isNotEmpty()) radice.addView(rigaPlaylist(), lp(TUTTO, 120))
            val ultima = Ui.riga(s, 24, Gravity.NO_GRAVITY)
            ultima.addView(boxApp(), lp(0, TUTTO, 1f))
            ultima.addView(servizi(), lp(360, TUTTO))
            radice.addView(ultima, lp(TUTTO, 0, 1f))
            radice.addView(rigaSistema(), lp(TUTTO, 120))
            return radice
        }

        private fun boxOra(): View {
            val colonna = Ui.colonna(s, 8)
            val mini = Ui.riga(s, 4)
            giorniSettimana.forEach { mini.addView(it, lp(32, 36)) }
            colonna.addView(intestazioneSezione(s, "ORA", coda = mini), lp(TUTTO, 36))
            val orologio = Ui.riga(s, 12)
            val sinistra = Ui.riga(s, 8)
            sinistra.addView(cifreOra[0], lp(114, 170))
            sinistra.addView(cifreOra[1], lp(114, 170))
            val destra = Ui.riga(s, 8)
            destra.addView(cifreOra[2], lp(114, 170))
            destra.addView(cifreOra[3], lp(114, 170))
            orologio.addView(sinistra)
            orologio.addView(Ui.testo(s, 120, CARATTERI, medio, 120, ":").apply { gravity = Gravity.CENTER }, lp(44, 170))
            orologio.addView(destra)
            colonna.addView(orologio, lp(540, 170))
            val data = Ui.riga(s, 8)
            data.addView(giorno, lp(ADATTA, 56))
            data.addView(numero, lp(ADATTA, 56))
            data.addView(mese, lp(ADATTA, 56))
            colonna.addView(data, lp(ADATTA, ADATTA).apply { topMargin = 8 })
            return colonna
        }

        private fun boxMeteo(): View {
            val colonna = Ui.colonna(s, 8)
            coda.addView(puntina, lp(28, 28))
            coda.addView(luogo, lp(330, 36))
            colonna.addView(intestazioneSezione(s, "METEO", coda = coda), lp(TUTTO, 36))
            val riga = Ui.riga(s, 20)
            riempi(s, gradi, "-°", 96, 124, 100)
            riga.addView(gradi)
            val lato = Ui.colonna(s, 12)
            lato.addView(iconaMeteo, lp(56, 56))
            lato.addView(cielo, lp(ADATTA, 56))
            riga.addView(lato, lp(0, ADATTA, 1f))
            domani.addView(etichetta(s, "DOMANI"), lp(ADATTA, ADATTA))
            domani.addView(iconaDomani, lp(40, 40))
            domani.addView(temperatureDomani, lp(ADATTA, 48))
            riga.addView(domani, lp(ADATTA, ADATTA))
            colonna.addView(riga, lp(TUTTO, ADATTA))
            estremi.addView(massima, lp(ADATTA, 56))
            estremi.addView(minima, lp(ADATTA, 56))
            estremi.addView(pioggia, lp(ADATTA, 56))
            estremi.visibility = View.INVISIBLE
            colonna.addView(estremi, lp(ADATTA, ADATTA))
            return colonna
        }

        private fun boxMusica(): View {
            val box = FrameLayout(s).apply { background = pieno(PANNELLO, 8, 2, BORDO) }

            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            inAscolto.addView(copertina, lp(164, 164))
            val info = Ui.colonna(s, 8, Gravity.CENTER_VERTICAL)
            info.addView(
                Ui.riga(s, 14).apply {
                    addView(spiaOnda, lp(14, 14))
                    addView(stato)
                    addView(View(s).apply { setBackgroundColor(FILETTO) }, lp(0, 2, 1f))
                },
                lp(TUTTO, 36),
            )
            info.addView(titolo, lp(TUTTO, 64))
            info.addView(artista, lp(ADATTA, 48))
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, TUTTO, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tasto(s, R.drawable.ic_precedente, 52, "Brano precedente") { azioni.precedente() }.vista, lp(140, 140))
            comandi.addView(play.vista, lp(140, 140))
            comandi.addView(tasto(s, R.drawable.ic_successivo, 52, "Brano successivo") { azioni.successivo() }.vista, lp(140, 140))
            comandi.addView(tasto(s, R.drawable.ic_testo, 52, "Testo della canzone") { azioni.testo() }.vista, lp(140, 140))
            inAscolto.addView(comandi)

            val testi = Ui.colonna(s, 8, Gravity.CENTER_VERTICAL)
            val spiaSorgente = View(s)
            accendi(spiaSorgente, false)
            testi.addView(intestazioneSezione(s, "SORGENTE", segnale = spiaSorgente), lp(TUTTO, 36))
            testi.addView(cifre(s, "SPOTIFY", 60, 88, 68, 6), lp(ADATTA, 88))
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

        private fun rigaPlaylist(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            for (p in playlist) {
                val pannello = Ui.riga(s, 14).apply {
                    background = pieno(PANNELLO, 8, 2, BORDO)
                    setPadding(16, 0, 16, 0)
                    addView(Ui.icona(s, R.drawable.ic_play, AMBRA), lp(36, 36))
                    addView(
                        Ui.testo(s, 28, CARATTERI, grassetto, 30, p.nome, maiuscolo = true, righe = 3).apply {
                            letterSpacing = 1 / 28f
                        },
                        lp(0, ADATTA, 1f),
                    )
                }
                riga.addView(Ui.premibile(pannello, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) }, lp(0, TUTTO, 1f))
            }
            return riga
        }

        private fun boxApp(): View {
            val griglia = Ui.colonna(s, 20)
            AppHome.elenco(s).chunked(3).forEachIndexed { r, gruppo ->
                val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
                gruppo.forEachIndexed { i, app -> riga.addView(cella(app, r * 3 + i + 1), lp(0, TUTTO, 1f)) }
                griglia.addView(riga, lp(TUTTO, 0, 1f))
            }
            return griglia
        }

        private fun cella(app: AppHome, numero: Int): View {
            val centro = Ui.colonna(s, 10, Gravity.CENTER)
            centro.addView(piastra(s, app.sigla.uppercase(Locale.ITALIAN), 76, perni = true), lp(72, 90))
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
                        topMargin = 10
                        leftMargin = 14
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
                    val cella = Ui.colonna(s, 10, Gravity.CENTER).apply {
                        background = pieno(PANNELLO, 8, 2, BORDO)
                        setPadding(10, 0, 10, 0)
                        addView(Ui.icona(s, icona, AMBRA), lp(48, 48))
                        val lunga = nome.length > 12 && nome.contains(' ')
                        addView(
                            Ui.adatta(Ui.testo(s, 28, CARATTERI, grassetto, 32, nome, righe = if (lunga) 2 else 1).apply {
                                gravity = Gravity.CENTER_HORIZONTAL
                            }, 22, 28),
                            lp(TUTTO, if (lunga) 64 else 36),
                        )
                    }
                    val descrizione = if (r * 2 + i == 3) "Impostazioni del launcher" else nome
                    riga.addView(Ui.premibile(cella, ONDA, descrizione) { azione() }, lp(0, TUTTO, 1f))
                }
                griglia.addView(riga, lp(TUTTO, 0, 1f))
            }
            return griglia
        }

        private fun rigaSistema(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(wifi.vista, lp(260, 120))
            riga.addView(dettaglio("Dettagli Wi-Fi") { azioni.wifiDettaglio() }, lp(100, 120))
            riga.addView(bluetooth.vista, lp(260, 120))
            riga.addView(dettaglio("Dettagli Bluetooth") { azioni.bluetoothDettaglio() }, lp(100, 120))
            riga.addView(comandoTema(), lp(0, 120, 1f))
            return riga
        }

        private fun dettaglio(descrizione: String, azione: () -> Unit): View {
            val segno = Ui.testo(s, 48, AMBRA, grassetto, 48, "›").apply {
                gravity = Gravity.CENTER
                background = paletta(6, 2)
            }
            return Ui.premibile(segno, ONDA, descrizione, azione)
        }

        private fun comandoTema(): View {
            val riga = Ui.riga(s, 10, Gravity.CENTER).apply {
                background = paletta(6, 2)
                setPadding(14, 0, 14, 0)
                addView(Ui.icona(s, R.drawable.ic_tavolozza, AMBRA), lp(44, 44))
                addView(Ui.testo(s, 30, CARATTERI, grassetto, 34, "TEMA"))
            }
            return Ui.premibile(riga, ONDA, "Tema") { azioni.temaSuccessivo() }
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
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                riempi(s, gradi, "-°", 96, 124, 100)
                cielo.text = "IN ATTESA"
                massima.text = ""
                minima.text = ""
                pioggia.visibility = View.GONE
                estremi.visibility = View.INVISIBLE
                coda.visibility = View.GONE
                domani.visibility = View.GONE
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            val testo = "${meteo.gradi}°"
            if (testo.length > 2) riempi(s, gradi, testo, 80, 124, 100) else riempi(s, gradi, testo, 96, 124, 100)
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            massima.text = "max ${meteo.massima}°"
            minima.text = "min ${meteo.minima}°"
            val avviso = FonteMeteo.testoPioggia(meteo.pioggiaTraMinuti)
            pioggia.text = avviso ?: ""
            pioggia.visibility = if (avviso != null) View.VISIBLE else View.GONE
            estremi.visibility = View.VISIBLE
            luogo.text = meteo.luogo
            puntina.visibility = if (meteo.gps) View.VISIBLE else View.GONE
            coda.visibility = if (meteo.luogo.isNotEmpty()) View.VISIBLE else View.GONE
            val previsione = meteo.domani
            if (previsione != null) {
                iconaDomani.setImageResource(Ui.iconaCielo(previsione.codice))
                temperatureDomani.text = "${previsione.minima}° / ${previsione.massima}°"
            }
            domani.visibility = if (previsione != null) View.VISIBLE else View.GONE
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

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> aggiornaChiave(wifi, R.drawable.ic_wifi_spento, false, "SPENTO", SECONDARIO, false, SPENTA)
                StatoWifi.ACCESO -> aggiornaChiave(wifi, R.drawable.ic_wifi, true, "ACCESO", SECONDARIO, false, AMBRA)
                StatoWifi.CONNESSO -> aggiornaChiave(wifi, R.drawable.ic_wifi, true, "CONNESSO", AMBRA, true, AMBRA)
            }
            if (stato.bluetooth) {
                aggiornaChiave(bluetooth, R.drawable.ic_bluetooth, true, "ACCESO", AMBRA, true, AMBRA)
            } else {
                aggiornaChiave(bluetooth, R.drawable.ic_bluetooth_spento, false, "SPENTO", SECONDARIO, false, SPENTA)
            }
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiSolari(s, azioni)

    private class BenzinaiSolari(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 18)
        private lateinit var scheda: View
        private val indirizzo = Ui.riga(s, 4)
        private val spesa = Ui.testo(s, 28, AMBRA, medio, 32, maiuscolo = true).apply { visibility = View.GONE }
        private val nomeScheda = piastra(s, "", 28, margine = 14, maiuscolo = true).apply { maxWidth = 440 }
        private val prezzoScheda = piastra(s, "", 28, AMBRA, margine = 14, maiuscolo = true)
        private val distanzaScheda = piastra(s, "", 28, margine = 14, maiuscolo = true).apply { maxWidth = 150 }
        private val quandoScheda = piastra(s, "", 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 220 }
        private val stato = StatoBenzinai()
        private val intornoIcona = Ui.icona(s, R.drawable.ic_mirino, AMBRA)
        private val intornoTesto = Ui.testo(s, 32, CARATTERI, grassetto, 36, "INTORNO A ME").apply { letterSpacing = 1 / 32f }
        private val raggioTesto = Ui.testo(s, 32, CARATTERI, grassetto, 60, raggioEtichetta(Benzinai.raggio(s))).apply { gravity = Gravity.CENTER }
        private lateinit var intornoVista: View

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 18).apply {
                setBackgroundColor(SFONDO)
                setPadding(28, 28, 28, 28)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(rigaComandi(), lp(TUTTO, 120))
            radice.addView(colonne(), lp(TUTTO, 34))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 158))
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(tasto(s, R.drawable.ic_indietro, 52, "Indietro") { azioni.indietro() }.vista, lp(120, 120))
            riga.addView(cifre(s, "BENZINAI", 46, 72, 52, 6), lp(0, ADATTA, 1f))
            riga.addView(
                pulsante(Ui.icona(s, R.drawable.ic_mappa, AMBRA), Ui.testo(s, 32, CARATTERI, grassetto, 36, "MAPPA").apply { letterSpacing = 1 / 32f }, "Mappa dei distributori") { azioni.mappa() },
                lp(ADATTA, 120),
            )
            val testi = Ui.colonna(s, 14)
            testi.addView(Ui.testo(s, 32, CARATTERI, grassetto, 32, "RIFORNIMENTI").apply { letterSpacing = 1 / 32f }, lp(ADATTA, ADATTA))
            testi.addView(spesa, lp(ADATTA, ADATTA))
            val corpo = Ui.riga(s, 14).apply {
                background = paletta(6, 2)
                setPadding(22, 0, 22, 0)
                addView(Ui.icona(s, R.drawable.ic_rifornimenti, AMBRA), lp(44, 44))
                addView(testi)
            }
            riga.addView(Ui.premibile(corpo, ONDA, "Rifornimenti") { azioni.rifornimenti(stato.selezionato) }, lp(ADATTA, 120))
            return riga
        }

        private fun raggioEtichetta(km: Double): String = "${km.toInt()} KM"

        private fun pulsante(icona: View?, testo: View, descrizione: String, azione: () -> Unit): View {
            val corpo = Ui.riga(s, 12, Gravity.CENTER).apply {
                background = paletta(6, 2)
                setPadding(14, 0, 14, 0)
                if (icona != null) addView(icona, lp(40, 40))
                addView(testo)
            }
            return Ui.premibile(corpo, ONDA, descrizione, azione)
        }

        private fun rigaComandi(): View {
            val riga = Ui.riga(s, 20)
            intornoVista = pulsante(intornoIcona, intornoTesto, "Intorno a me") { azioni.intornoAMe() }
            riga.addView(intornoVista, lp(310, 120))
            val raggio = Ui.colonna(s).apply { background = paletta(6, 2) }
            raggio.addView(Ui.testo(s, 28, AMBRA, grassetto, 60, "RAGGIO").apply {
                gravity = Gravity.CENTER
                letterSpacing = 1 / 28f
            }, lp(TUTTO, 60))
            raggio.addView(raggioTesto, lp(TUTTO, 60))
            riga.addView(Ui.premibile(raggio, ONDA, "Raggio di ricerca") { azioni.raggio() }, lp(170, 120))
            riga.addView(View(s).apply { setBackgroundColor(FILETTO) }, lp(2, 70))
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = Ui.testo(s, 32, CARATTERI, grassetto, 36, nome, maiuscolo = true).apply {
                    gravity = Gravity.CENTER
                    letterSpacing = 1 / 32f
                }
                ordini[ordine] = tasto
                riga.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(0, 120, 1f))
            }
            evidenziaOrdine(Ordine.CONVIENE)
            evidenziaIntorno(false)
            return riga
        }

        private fun statoVicini(acceso: Boolean, km: Double): View {
            val spia = View(s)
            accendi(spia, acceso)
            val testo = if (acceso) "SOLO INTORNO A ME · ${km.toInt()} KM" else "+ COMUNI PREFERITI · ${km.toInt()} KM"
            return Ui.riga(s, 10).apply {
                addView(spia, lp(14, 14))
                addView(Ui.testo(s, 28, if (acceso) AMBRA else SECONDARIO, grassetto, 34, testo).apply { letterSpacing = 1 / 28f })
            }
        }

        private fun evidenziaIntorno(acceso: Boolean) {
            intornoVista.background = if (acceso) pieno(AMBRA, 6) else paletta(6, 2)
            intornoTesto.setTextColor(if (acceso) SFONDO else CARATTERI)
            intornoIcona.imageTintList = ColorStateList.valueOf(if (acceso) SFONDO else AMBRA)
            intornoVista.isSelected = acceso
        }

        private fun colonne(): View {
            val riga = Ui.riga(s, 12).apply { setPadding(16, 0, 0, 0) }
            val voci = listOf("DISTRIBUTORE · MARCHIO" to 326, "SELF €/L" to 216, "VAR." to 100, "DISTANZA" to 134, "AGGIORNATO" to 0)
            for ((testo, larghezza) in voci) {
                val peso = if (larghezza == 0) 1f else 0f
                riga.addView(etichetta(s, testo, 28, 1), lp(larghezza, ADATTA, peso))
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
            val corpo = Ui.riga(s, 20).apply {
                background = pieno(PANNELLO, 8, 3, AMBRA)
                setPadding(20, 0, 14, 0)
            }
            val sinistra = Ui.colonna(s, 8, Gravity.CENTER_VERTICAL)
            sinistra.addView(indirizzo, lp(TUTTO, 52))
            val riga = Ui.riga(s, 8)
            riga.addView(nomeScheda, lp(ADATTA, 48))
            riga.addView(prezzoScheda, lp(ADATTA, 48))
            riga.addView(distanzaScheda, lp(ADATTA, 48))
            riga.addView(quandoScheda, lp(ADATTA, 48))
            sinistra.addView(riga, lp(TUTTO, 48))
            corpo.addView(sinistra, lp(0, TUTTO, 1f))
            corpo.addView(tasto(s, R.drawable.ic_mappa, 52, "Visualizza sulla mappa") { stato.selezionato?.let { azioni.visualizzaSullaMappa(it) } }.vista, lp(120, 120))
            corpo.addView(tasto(s, R.drawable.ic_chiudi, 52, "Chiudi scheda") { seleziona(null) }.vista, lp(120, 120))
            scheda = corpo
            scheda.visibility = View.GONE
            return scheda
        }

        override fun mostra(elenco: ElencoBenzinai) {
            stato.ultimo = elenco
            disegna()
        }

        override fun mostraSpesa(testo: String?) {
            spesa.text = testo ?: ""
            spesa.visibility = if (testo != null) View.VISIBLE else View.GONE
        }

        private fun seleziona(d: Distributore?) {
            stato.alterna(d)
            disegna()
        }

        private fun disegna() {
            elencoView.removeAllViews()
            when (val e = stato.ultimo) {
                is ElencoBenzinai.Caricamento -> messaggio("AGGIORNAMENTO", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("POSIZIONE", Ui.POSIZIONE_NON_DISPONIBILE)
                is ElencoBenzinai.Errore -> messaggio("ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    evidenziaIntorno(e.intornoAMe)
                    raggioTesto.text = raggioEtichetta(e.raggioKm)
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione("VICINI", e.vicini, false, statoVicini(e.intornoAMe, e.raggioKm))
                    if (tutti.isEmpty()) messaggio("RISULTATI", "Nessun distributore entro ${e.raggioKm.toInt()} km")
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

        private fun sezione(titolo: String, distributori: List<Distributore>, luogo: Boolean, coda: View? = null) {
            val colonna = Ui.colonna(s, 8)
            colonna.addView(intestazioneSezione(s, titolo, 40, 3, coda = coda), lp(TUTTO, 40))
            val righe = Ui.colonna(s, 20)
            distributori.forEach { righe.addView(riga(it, luogo), lp(TUTTO, 120)) }
            colonna.addView(righe, lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean): View {
            val adesso = System.currentTimeMillis()
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 12).apply {
                background = pieno(PANNELLO, 6, 3, if (scelto) AMBRA else PANNELLO)
                setPadding(16, 0, 16, 0)
            }
            val segnale = View(s)
            accendi(segnale, scelto)
            corpo.addView(segnale, lp(14, 14))
            val nome = Ui.colonna(s, 6, Gravity.CENTER_VERTICAL)
            nome.addView(piastra(s, if (luogo) d.comune else d.nome, 30, margine = 14, maiuscolo = true).apply { maxWidth = 300 }, lp(ADATTA, 50))
            nome.addView(piastra(s, d.marchio, 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 300 }, lp(ADATTA, 40))
            corpo.addView(nome, lp(300, TUTTO))
            corpo.addView(cifre(s, Benzinai.prezzo(d.prezzo), 40, 72, 52, 4), lp(216, 72))
            val variazione = Ui.riga(s, 6)
            DatiMimit.andamento(d)?.let {
                variazione.addView(Ui.icona(s, if (it.salito) R.drawable.ic_su else R.drawable.ic_giu, AMBRA), lp(18, 18))
                variazione.addView(Ui.testo(s, 28, CARATTERI, grassetto, 32, it.differenza))
            }
            corpo.addView(variazione, lp(100, TUTTO))
            val distanza = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            distanza.addView(piastra(s, Benzinai.distanza(d.distanzaKm), 30, margine = 14, maiuscolo = true).apply { maxWidth = 134 }, lp(ADATTA, 56))
            corpo.addView(distanza, lp(134, TUTTO))
            val quando = Ui.colonna(s, 0, Gravity.CENTER_VERTICAL)
            quando.addView(piastra(s, Benzinai.quando(d.aggiornato, adesso), 28, SECONDARIO, margine = 14, maiuscolo = true).apply { maxWidth = 300 }, lp(ADATTA, 50))
            corpo.addView(quando, lp(0, TUTTO, 1f))
            return Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
        }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            val testo = "${d.indirizzo} - ${d.comune}".uppercase(Locale.ITALIAN)
            val ingombro = testo.sumOf { if (it == ' ') 21 else 38 }
            if (ingombro <= 960) {
                riempi(s, indirizzo, testo, 34, 52, 40)
            } else {
                indirizzo.removeAllViews()
                indirizzo.addView(
                    Ui.adatta(Ui.testo(s, 40, CARATTERI, medio, 40, testo).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        background = paletta(4)
                        setPadding(14, 0, 14, 0)
                    }, 22, 40),
                    lp(TUTTO, 52),
                )
            }
            nomeScheda.text = "${d.nome} · ${d.marchio}"
            prezzoScheda.text = "${Benzinai.prezzo(d.prezzo)} €/l"
            distanzaScheda.text = Benzinai.distanza(d.distanzaKm)
            quandoScheda.text = Benzinai.quando(d.aggiornato, System.currentTimeMillis())
        }
    }
}
