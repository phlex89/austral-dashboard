package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
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
import android.text.style.StyleSpan
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
        Palette(SFONDO, TESTO, CIANO, LINEA_FORTE, orbitron(context), condensato)

    private fun orb(
        context: Context,
        px: Int,
        colore: Int,
        contenuto: String = "",
        peso: Int = 500,
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

    private fun tastoContorno(
        context: Context,
        icona: Int,
        lato: Int,
        descrizione: String,
        sfondo: Int = Color.TRANSPARENT,
        azione: () -> Unit,
    ): View {
        val cornice = FrameLayout(context).apply {
            background = Ui.bordo(LINEA, 2, sfondo)
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

    private fun tastoDettaglio(context: Context, descrizione: String, azione: () -> Unit): View {
        val cornice = FrameLayout(context).apply {
            background = Ui.bordo(LINEA, 2, PANNELLO)
            addView(cond(context, 56, CIANO, "›", true, 0, 60).apply { gravity = Gravity.CENTER }, lpf(TUTTO, TUTTO))
        }
        return Ui.premibile(cornice, ONDA, descrizione, azione).also { angoli(it, LINEA_FORTE, 14f) }
    }

    private fun coppia(context: Context, comando: View, dettaglio: View, ancoraDestra: Boolean): View {
        val riga = Ui.riga(context, 0, Gravity.NO_GRAVITY)
        if (ancoraDestra) {
            riga.addView(dettaglio, lp(80, TUTTO))
            riga.addView(comando, lp(120, TUTTO))
        } else {
            riga.addView(comando, lp(120, TUTTO))
            riga.addView(dettaglio, lp(80, TUTTO))
        }
        return riga
    }

    private class Interruttore(
        private val s: Schermata,
        etichetta: String,
        private val nome: String,
        icona0: Int = R.drawable.ic_wifi,
        azione: () -> Unit,
    ) {
        private val icona = Ui.icona(s, icona0, TESTO)
        private val scritta = cond(s, 28, TESTO, etichetta, true, 0, 30).apply { gravity = Gravity.CENTER }
        val view = FrameLayout(s)
        private val angoliAccesi: Squadrette

        init {
            val colonna = Ui.colonna(s, 4, Gravity.CENTER)
            colonna.addView(icona, lp(48, 48))
            colonna.addView(scritta, lp(ADATTA, ADATTA))
            view.addView(colonna, lpf(TUTTO, TUTTO))
            Ui.premibile(view, ONDA, nome) { azione() }
            angoliAccesi = angoli(view, LINEA_FORTE, 14f)
        }

        fun mostra(livello: Int, risorsa: Int, stato: String) {
            val colore = when (livello) {
                0 -> SECONDARIO
                1 -> TESTO
                else -> SFONDO
            }
            view.background = when (livello) {
                0 -> Ui.bordo(LINEA, 2, PANNELLO)
                1 -> Ui.bordo(LINEA_FORTE, 2, PANNELLO)
                else -> Ui.pieno(CIANO)
            }
            angoliAccesi.acceso = livello == 1
            angoliAccesi.invalidateSelf()
            icona.setImageResource(risorsa)
            icona.imageTintList = ColorStateList.valueOf(colore)
            scritta.setTextColor(colore)
            view.contentDescription = "$nome, $stato"
        }
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeHud(s, azioni)

    private class HomeHud(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val quadrante = Quadrante(s)
        private val ora = orb(s, 140, TESTO, "", 700, 0, 140)
        private val data = cond(s, 34, SECONDARIO, "", false, 0, 40)
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, CIANO)
        private val cielo = cond(s, 30, TESTO, "In attesa", true, 0, 34, true, 2)
        private val gradi = Ui.adatta(orb(s, 88, TESTO, "--°", 700, 0, 92), 52, 88)
        private val rigaPioggia = Ui.riga(s, 10)
        private val testoPioggia = Ui.adatta(cond(s, 28, TESTO, "", true, 0, 32, true, 2), 18, 28)
        private val termometro = Termometro(s)
        private val lettereGiorno = ArrayList<TextView>()
        private val filettiGiorno = ArrayList<View>()
        private var giornoCorrente = -1
        private val interruttoreWifi = Interruttore(s, "WI-FI", "Wi-Fi") { azioni.wifi() }
        private val interruttoreBluetooth = Interruttore(s, "BT", "Bluetooth") { azioni.bluetooth() }
        private val interruttoreTema = Interruttore(s, "TEMA", "Tema", R.drawable.ic_tavolozza) { azioni.temaSuccessivo() }
        private val puntina = Ui.icona(s, R.drawable.ic_puntina, CIANO)
        private val luogo = Ui.adatta(cond(s, 28, TESTO, "", true, 0, 30), 13, 28)
        private val rigaLuogo = Ui.riga(s, 6)
        private val iconaDomani = Ui.icona(s, R.drawable.ic_nuvoloso, CIANO)
        private val gradiDomani = Ui.adatta(cond(s, 26, TESTO, "", true, 0, 30), 14, 26)
        private val rigaDomani = Ui.riga(s, 8)
        private val inAscolto = Ui.riga(s, 24)
        private val fermo = Ui.riga(s, 28)
        private val copertina = ImageView(s).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(LINEA)
        }
        private val statoMusica = orb(s, 28, CIANO, "IN RIPRODUZIONE", 500, 3, 34)
        private val titolo = cond(s, 50, TESTO, "", true, 0, 56)
        private val artista = cond(s, 32, SECONDARIO, "", false, 0, 38)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, SFONDO)
        private val playlist = Playlists.elenco(s)

        override val view: View = costruisci()

        private fun costruisci(): View {
            interruttoreTema.mostra(1, R.drawable.ic_tavolozza, "")
            interruttoreTema.view.contentDescription = "Tema"
            val radice = Ui.colonna(s).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            val primaRiga = Ui.riga(s, 28, Gravity.NO_GRAVITY)
            primaRiga.addView(boxMeteo(), lp(250, TUTTO))
            primaRiga.addView(boxOra(), lp(640, TUTTO))
            primaRiga.addView(servizi(), lp(250, TUTTO))
            radice.addView(primaRiga, lp(TUTTO, 640))
            if (playlist.isEmpty()) {
                radice.addView(boxMusica(), lp(TUTTO, 0, 240f).apply { topMargin = 24 })
                radice.addView(boxApp(), lp(TUTTO, 0, 222f).apply { topMargin = 20 })
            } else {
                radice.addView(boxMusica(), lp(TUTTO, 170).apply { topMargin = 24 })
                radice.addView(rigaPlaylist(), lp(TUTTO, 120).apply { topMargin = 20 })
                radice.addView(boxApp(), lp(TUTTO, 0, 1f).apply { topMargin = 20 })
            }
            return radice
        }

        private fun boxMeteo(): View {
            val colonna = Ui.colonna(s, 8).apply { setPadding(24, 24, 24, 24) }
            Ui.senzaRitaglio(colonna)
            pannello(colonna)
            colonna.addView(orb(s, 28, SECONDARIO, "METEO", 500, 3, 34))
            rigaLuogo.addView(puntina, lp(26, 26))
            rigaLuogo.addView(luogo.apply { gravity = Gravity.CENTER_VERTICAL }, lp(0, 34, 1f))
            rigaLuogo.visibility = View.GONE
            puntina.visibility = View.GONE
            colonna.addView(rigaLuogo, lp(TUTTO, 34))
            val rigaCielo = Ui.riga(s, 12)
            rigaCielo.addView(iconaMeteo, lp(52, 52))
            rigaCielo.addView(cielo, lp(0, ADATTA, 1f))
            colonna.addView(rigaCielo, lp(TUTTO, ADATTA))
            colonna.addView(gradi, lp(202, ADATTA))
            rigaDomani.addView(cond(s, 24, SECONDARIO, "DOMANI", true, 1, 28, true), lp(ADATTA, ADATTA))
            rigaDomani.addView(iconaDomani, lp(32, 32))
            rigaDomani.addView(gradiDomani.apply { gravity = Gravity.CENTER_VERTICAL }, lp(0, 36, 1f))
            rigaDomani.visibility = View.GONE
            colonna.addView(rigaDomani, lp(TUTTO, 36))
            rigaPioggia.addView(Ui.icona(s, R.drawable.hud_pioggia, CIANO), lp(40, 40))
            testoPioggia.gravity = Gravity.CENTER_VERTICAL
            rigaPioggia.addView(testoPioggia, lp(0, 64, 1f))
            rigaPioggia.visibility = View.GONE
            colonna.addView(rigaPioggia, lp(TUTTO, ADATTA))
            colonna.addView(View(s), lp(TUTTO, 0, 1f))
            colonna.addView(termometro, lp(202, 188).apply { topMargin = -12 })
            return colonna
        }

        private fun boxOra(): View {
            val testi = Ui.colonna(s, 8, Gravity.CENTER)
            testi.addView(orb(s, 28, SECONDARIO, "ORA LOCALE", 500, 4, 34).apply { gravity = Gravity.CENTER_HORIZONTAL }, lp(TUTTO, 34))
            testi.addView(ora.apply { gravity = Gravity.CENTER_HORIZONTAL; translationY = -20f }, lp(TUTTO, 140))
            testi.addView(data.apply { gravity = Gravity.CENTER_HORIZONTAL }, lp(TUTTO, 40))
            testi.addView(striscia(), lp(ADATTA, 36).apply { gravity = Gravity.CENTER_HORIZONTAL })
            return FrameLayout(s).apply {
                addView(quadrante, lpf(TUTTO, TUTTO))
                addView(testi, lpf(TUTTO, TUTTO))
                addView(
                    coppia(s, interruttoreWifi.view, tastoDettaglio(s, "Dettagli Wi-Fi") { azioni.wifiDettaglio() }, false),
                    lpf(200, 120, Gravity.TOP or Gravity.START),
                )
                addView(
                    coppia(s, interruttoreBluetooth.view, tastoDettaglio(s, "Dettagli Bluetooth") { azioni.bluetoothDettaglio() }, true),
                    lpf(200, 120, Gravity.TOP or Gravity.END),
                )
                addView(interruttoreTema.view, lpf(120, 120, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))
            }
        }

        private fun striscia(): View {
            val riga = Ui.riga(s, 6, Gravity.NO_GRAVITY)
            riga.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            for (sigla in "LMMGVSD") {
                val cella = FrameLayout(s)
                val lettera = orb(s, 28, SECONDARIO, sigla.toString(), 500, 0, 28).apply { gravity = Gravity.CENTER }
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
                        leftMargin = 22
                        topMargin = 20
                    },
                )
                cella.addView(
                    orb(s, 28, SECONDARIO, numero, 500, 0, 30),
                    lpf(ADATTA, ADATTA, Gravity.TOP or Gravity.END).apply {
                        rightMargin = 20
                        topMargin = 18
                    },
                )
                cella.addView(
                    cond(s, 30, TESTO, nome, true, 1, 32, true, 2),
                    lpf(TUTTO, ADATTA, Gravity.BOTTOM).apply {
                        leftMargin = 22
                        rightMargin = 22
                        bottomMargin = 20
                    },
                )
                colonna.addView(Ui.premibile(cella, ONDA, descrizione) { azione() }.also { angoli(it, CIANO) }, lp(TUTTO, 0, 1f))
            }
            return colonna
        }

        private fun boxMusica(): View {
            val box = FrameLayout(s)
            pannello(box)

            inAscolto.setPadding(16, 0, 24, 0)
            val cornice = FrameLayout(s).apply {
                setPadding(8, 8, 8, 8)
                foreground = Squadrette(TESTO, 20f, 3f)
            }
            cornice.addView(Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }, lpf(130, 130))
            inAscolto.addView(cornice, lp(146, 146).apply { rightMargin = -8 })
            val info = Ui.colonna(s)
            val rigaStato = Ui.riga(s, 12)
            rigaStato.addView(View(s).apply { setBackgroundColor(CIANO) }, lp(14, 14))
            rigaStato.addView(statoMusica)
            info.addView(rigaStato)
            info.addView(titolo, lp(TUTTO, ADATTA).apply { topMargin = 4 })
            info.addView(artista)
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            inAscolto.addView(info, lp(0, ADATTA, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tastoContorno(s, R.drawable.ic_precedente, 52, "Brano precedente") { azioni.precedente() }, lp(130, 130))
            val play = FrameLayout(s).apply {
                background = Smusso(CIANO, 0, 0f, 20f)
                addView(iconaPlay, lpf(60, 60, Gravity.CENTER))
            }
            smussa(play, 20)
            comandi.addView(Ui.premibile(play, ONDA_SCURA, "Riproduci o metti in pausa") { azioni.playPausa() }, lp(170, 130))
            comandi.addView(tastoContorno(s, R.drawable.ic_successivo, 52, "Brano successivo") { azioni.successivo() }, lp(130, 130))
            inAscolto.addView(comandi)

            fermo.setPadding(40, 0, 24, 0)
            val testi = Ui.colonna(s)
            testi.addView(orb(s, 28, SECONDARIO, "SORGENTE AUDIO", 500, 3, 34))
            testi.addView(orb(s, 64, TESTO, "SPOTIFY", 700, 0, 72))
            fermo.addView(testi, lp(0, ADATTA, 1f))
            val apri = cond(s, 34, TESTO, "Apri Spotify", true, 1, 40, true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(LINEA, 2)
                setPadding(40, 0, 40, 0)
            }
            val cornicaApri = FrameLayout(s).apply { addView(apri, lpf(ADATTA, TUTTO)) }
            fermo.addView(Ui.premibile(cornicaApri, ONDA, null) { azioni.apriMusica() }.also { angoli(it, CIANO) }, lp(ADATTA, 130))
            fermo.addView(tastoPieno(s, R.drawable.ic_play, 60, "Riproduci") { azioni.playPausa() }, lp(170, 130))

            box.addView(inAscolto, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
            box.addView(fermo, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
            mostraMusica(null)
            return box
        }

        private fun rigaPlaylist(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            for (p in playlist) {
                val corpo = Ui.riga(s, 14).apply {
                    background = Ui.bordo(LINEA, 2, PANNELLO)
                    setPadding(20, 0, 20, 0)
                }
                corpo.addView(Ui.icona(s, R.drawable.hud_playlist, CIANO), lp(40, 40))
                corpo.addView(cond(s, 30, TESTO, p.nome, true, 0, 34, false, 3), lp(0, ADATTA, 1f))
                angoli(Ui.premibile(corpo, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) }, LINEA_FORTE, 14f)
                riga.addView(corpo, lp(0, TUTTO, 1f))
            }
            return riga
        }

        private fun boxApp(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            AppHome.elenco(s).forEach { app ->
                val cella = FrameLayout(s).apply { background = Ui.bordo(LINEA, 2, PANNELLO) }
                val lettera = FrameLayout(s).apply {
                    background = Smusso(0, CIANO, 2f, 14f)
                    addView(orb(s, 44, CIANO, app.sigla, 700, 0, 44), lpf(ADATTA, ADATTA, Gravity.CENTER))
                }
                smussa(lettera, 14)
                val centro = Ui.colonna(s, 10, Gravity.CENTER).apply { setPadding(6, 0, 6, 0) }
                centro.addView(lettera, lp(80, 80))
                centro.addView(
                    Ui.adatta(cond(s, 28, TESTO, app.nome, true, 0, 32, true), 16, 28).apply { gravity = Gravity.CENTER },
                    lp(TUTTO, 32),
                )
                cella.addView(centro, lpf(TUTTO, TUTTO))
                val installata = app.intent(s) != null
                if (!installata) cella.alpha = 0.35f
                angoli(Ui.premibile(cella, ONDA, app.nome) { if (installata) azioni.apri(app) }, LINEA_FORTE, 14f)
                riga.addView(cella, lp(0, TUTTO, 1f))
            }
            return riga
        }

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> interruttoreWifi.mostra(0, R.drawable.ic_wifi_spento, "spento")
                StatoWifi.ACCESO -> interruttoreWifi.mostra(1, R.drawable.ic_wifi, "acceso, non connesso")
                StatoWifi.CONNESSO -> interruttoreWifi.mostra(2, R.drawable.ic_wifi, "connesso")
            }
            if (stato.bluetooth) {
                interruttoreBluetooth.mostra(2, R.drawable.ic_bluetooth, "acceso")
            } else {
                interruttoreBluetooth.mostra(0, R.drawable.ic_bluetooth_spento, "spento")
            }
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
                    lettera.typeface = orbitron(s, if (oggi) 700 else 500)
                    filettiGiorno[i].setBackgroundColor(if (oggi) CIANO else LINEA)
                }
            }
        }

        override fun mostraMeteo(meteo: Meteo?) {
            termometro.mostra(meteo)
            val pioggia = FonteMeteo.testoPioggia(meteo?.pioggiaTraMinuti)
            testoPioggia.text = pioggia ?: ""
            rigaPioggia.visibility = if (pioggia != null) View.VISIBLE else View.GONE
            val nomeLuogo = meteo?.luogo.orEmpty().take(30)
            luogo.text = nomeLuogo
            rigaLuogo.visibility = if (nomeLuogo.isEmpty()) View.GONE else View.VISIBLE
            puntina.visibility = if (meteo?.gps == true) View.VISIBLE else View.GONE
            val domani = meteo?.domani
            if (domani != null) {
                iconaDomani.setImageResource(Ui.iconaCielo(domani.codice))
                gradiDomani.text = "${domani.minima}° / ${domani.massima}°"
                rigaDomani.contentDescription = "Domani ${FonteMeteo.descrizione(domani.codice)}, minima ${domani.minima}, massima ${domani.massima} gradi"
            }
            rigaDomani.visibility = if (domani != null) View.VISIBLE else View.GONE
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "In attesa"
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
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
        private val etichetta = orb(s, 28, SECONDARIO, "", 500, 3, 34)
        private val indirizzo = Ui.adatta(cond(s, 64, TESTO, "", true, 1, 66, true), 34, 64)
        private val dettaglioScheda = cond(s, 30, TESTO, "", false, 0, 36)
        private val spesa = cond(s, 28, SECONDARIO, "", false, 0, 32).apply { visibility = View.GONE }
        private val stato = StatoBenzinai()
        private val corpoIntorno = Ui.riga(s, 14, Gravity.CENTER)
        private val iconaIntorno = Ui.icona(s, R.drawable.ic_mirino, TESTO)
        private val testoIntorno = cond(s, 32, TESTO, "Intorno a me", true, 2, 36, true)
        private val testoRaggio = cond(s, 32, TESTO, "Raggio ${Benzinai.raggio(s).toInt()} km", true, 2, 36, true)
        private lateinit var angoliIntorno: Squadrette

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(rigaOrdini(), lp(TUTTO, 120))
            radice.addView(rigaFiltri(), lp(TUTTO, 108))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(boxScheda(), lp(TUTTO, 186))
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(tastoContorno(s, R.drawable.ic_indietro, 52, "Indietro") { azioni.indietro() }, lp(120, 120))
            riga.addView(orb(s, 56, TESTO, "BENZINAI", 700, 0, 62), lp(0, ADATTA, 1f))
            val rifornimenti = Ui.riga(s, 16).apply {
                background = Ui.bordo(LINEA, 2, PANNELLO)
                setPadding(24, 0, 24, 0)
            }
            rifornimenti.addView(Ui.icona(s, R.drawable.ic_rifornimenti, CIANO), lp(48, 48))
            val testi = Ui.colonna(s, 2)
            testi.addView(cond(s, 32, TESTO, "Rifornimenti", true, 2, 36, true))
            testi.addView(spesa)
            rifornimenti.addView(testi, lp(ADATTA, ADATTA))
            angoli(Ui.premibile(rifornimenti, ONDA, "Rifornimenti") { azioni.rifornimenti(stato.selezionato) }, CIANO)
            riga.addView(rifornimenti, lp(ADATTA, TUTTO))
            return riga
        }

        private fun rigaOrdini(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(orb(s, 28, SECONDARIO, "ORDINA PER", 500, 3, 34), lp(ADATTA, ADATTA))
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = cond(s, 32, TESTO, nome, true, 2, 36, true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                val cornice = FrameLayout(s).apply { addView(tasto, lpf(TUTTO, TUTTO)) }
                val premibile = Ui.premibile(cornice, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }
                angoliOrdine[ordine] = angoli(premibile, CIANO)
                riga.addView(premibile, lp(0, TUTTO, 1f))
            }
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun rigaFiltri(): View {
            val riga = Ui.riga(s, 20)
            corpoIntorno.addView(iconaIntorno, lp(40, 40))
            corpoIntorno.addView(testoIntorno, lp(ADATTA, ADATTA))
            val intorno = FrameLayout(s).apply { addView(corpoIntorno, lpf(TUTTO, TUTTO)) }
            angoliIntorno = angoli(Ui.premibile(intorno, ONDA, "Intorno a me") { azioni.intornoAMe() }, CIANO)
            riga.addView(intorno, lp(0, TUTTO, 1.4f))
            val raggio = FrameLayout(s).apply {
                background = Ui.bordo(LINEA, 2)
                addView(testoRaggio.apply { gravity = Gravity.CENTER }, lpf(TUTTO, TUTTO))
            }
            angoli(Ui.premibile(raggio, ONDA, "Cambia il raggio") { azioni.raggio() }, CIANO)
            riga.addView(raggio, lp(0, TUTTO, 1.2f))
            val corpoMappa = Ui.riga(s, 14, Gravity.CENTER).apply { background = Ui.bordo(LINEA, 2) }
            corpoMappa.addView(Ui.icona(s, R.drawable.ic_mappa, TESTO), lp(40, 40))
            corpoMappa.addView(cond(s, 32, TESTO, "Mappa", true, 2, 36, true), lp(ADATTA, ADATTA))
            angoli(Ui.premibile(corpoMappa, ONDA, "Mappa dei distributori") { azioni.mappa() }, CIANO)
            riga.addView(corpoMappa, lp(0, TUTTO, 1f))
            evidenziaIntorno(false)
            return riga
        }

        private fun evidenziaIntorno(acceso: Boolean) {
            corpoIntorno.background = if (acceso) Smusso(CIANO, 0, 0f, 18f) else Ui.bordo(LINEA, 2)
            if (acceso) smussa(corpoIntorno, 18) else corpoIntorno.clipToOutline = false
            angoliIntorno.apply { this.acceso = !acceso; invalidateSelf() }
            iconaIntorno.imageTintList = ColorStateList.valueOf(if (acceso) SFONDO else TESTO)
            testoIntorno.setTextColor(if (acceso) SFONDO else TESTO)
            corpoIntorno.isSelected = acceso
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
            val riga = Ui.riga(s, 28).apply { setPadding(28, 0, 28, 0) }
            val testi = Ui.colonna(s, 4)
            testi.addView(etichetta)
            testi.addView(indirizzo, lp(TUTTO, 66))
            testi.addView(dettaglioScheda)
            riga.addView(testi, lp(0, ADATTA, 1f))
            riga.addView(tastoContorno(s, R.drawable.ic_chiudi, 52, "Chiudi scheda") { seleziona(null) }, lp(120, 120))
            scheda.addView(riga, lpf(TUTTO, TUTTO, Gravity.CENTER_VERTICAL))
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
                is ElencoBenzinai.Caricamento -> messaggio("BENZINAI", "Caricamento dei prezzi…")
                is ElencoBenzinai.SenzaPosizione -> messaggio("POSIZIONE", Ui.POSIZIONE_NON_DISPONIBILE)
                is ElencoBenzinai.Errore -> messaggio("ERRORE", e.messaggio)
                is ElencoBenzinai.Pronto -> {
                    evidenziaOrdine(e.ordine)
                    evidenziaIntorno(e.intornoAMe)
                    testoRaggio.text = "Raggio ${e.raggioKm.toInt()} km"
                    val tutti = e.preferiti + e.vicini
                    stato.riallinea(tutti)
                    if (e.preferiti.isNotEmpty()) sezione("PREFERITI", "%02d".format(e.preferiti.size), e.preferiti, true, true)
                    if (e.vicini.isNotEmpty()) sezione("VICINI", "ORDINE: ${e.ordine.name}", e.vicini, false, false)
                    if (tutti.isEmpty()) messaggio("BENZINAI", "Nessun distributore entro ${e.raggioKm.toInt()} km")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(titolo: String, testo: String) {
            val colonna = Ui.colonna(s, 12).apply { setPadding(32, 26, 32, 26) }
            pannello(colonna)
            colonna.addView(orb(s, 28, SECONDARIO, titolo, 500, 3, 34))
            colonna.addView(cond(s, 44, TESTO, testo, false, 0, 50, false, 3), lp(TUTTO, ADATTA))
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun sezione(titolo: String, contatore: String, distributori: List<Distributore>, luogo: Boolean, stella: Boolean) {
            val colonna = Ui.colonna(s, 12)
            val testata = Ui.riga(s, 16)
            if (stella) testata.addView(Ui.icona(s, R.drawable.hud_stella, CIANO), lp(36, 36))
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
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = if (scelto) Ui.bordo(CIANO, 3, PANNELLO) else Ui.bordo(LINEA, 2, PANNELLO)
                setPadding(24, 0, 28, 0)
            }
            corpo.addView(orb(s, 28, SECONDARIO, "%02d".format(posizione), 500, 0, 32), lp(52, ADATTA))
            val sinistra = Ui.colonna(s, 2)
            sinistra.addView(cond(s, 40, TESTO, if (luogo) d.comune else d.nome, true, 1, 46, true))
            sinistra.addView(cond(s, 28, SECONDARIO, "${d.marchio} · aggiornato ${Benzinai.quando(d.aggiornato, adesso)}", false, 0, 34))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            val destra = Ui.colonna(s, 4, Gravity.END)
            val rigaPrezzo = Ui.riga(s, 14)
            DatiMimit.andamento(d)?.let { rigaPrezzo.addView(andamento(it), lp(ADATTA, ADATTA)) }
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
            rigaPrezzo.addView(prezzo)
            destra.addView(rigaPrezzo)
            val rigaDistanza = Ui.riga(s, 14)
            val barra = FrameLayout(s).apply { setBackgroundColor(LINEA) }
            val riempimento = (110 * (d.distanzaKm / 5.0).coerceIn(0.0, 1.0)).roundToInt()
            barra.addView(View(s).apply { setBackgroundColor(TESTO) }, lpf(riempimento, 8, Gravity.START))
            rigaDistanza.addView(barra, lp(110, 8))
            rigaDistanza.addView(orb(s, 28, TESTO, Benzinai.distanza(d.distanzaKm), 500, 0, 32))
            destra.addView(rigaDistanza)
            corpo.addView(destra)
            return Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }
        }

        private fun andamento(a: DatiMimit.Andamento): View =
            Ui.riga(s, 6).apply {
                contentDescription = "Prezzo ${if (a.salito) "salito" else "sceso"} di ${a.differenza}"
                addView(Ui.icona(s, if (a.salito) R.drawable.ic_su else R.drawable.ic_giu, TESTO), lp(20, 20))
                addView(orb(s, 28, TESTO, a.differenza, 500, 0, 32))
            }

        private fun aggiornaScheda() {
            val d = stato.selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            val italiano = Locale.ITALIAN
            etichetta.text = "INDIRIZZO · ${d.nome.uppercase(italiano)} · ${d.marchio.uppercase(italiano)}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}"
            val prezzo = "${Benzinai.prezzo(d.prezzo)} €/l self"
            val aggiornato = "aggiornato ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
            val testo = "$prezzo · ${Benzinai.distanza(d.distanzaKm)} · $aggiornato"
            dettaglioScheda.text = SpannableString(testo).apply {
                setSpan(ForegroundColorSpan(CIANO), 0, prezzo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(Typeface.BOLD), 0, prezzo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(SECONDARIO), testo.length - aggiornato.length, testo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
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

private class Quadrante(context: Context) : View(context) {
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
    private val arcoOre = RectF(60f, 60f, 580f, 580f)
    private val cerchio = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0xFF1B3A55.toInt()
    }
    private val segmento = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.BUTT
    }
    private val tacca = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    init {
        for (i in 0 until 60) {
            val angolo = Math.toRadians(i * 6.0 - 90.0)
            val interno = if (i % 5 == 0) 270.0 else 274.0
            tacche[i * 4] = (320 + interno * cos(angolo)).toFloat()
            tacche[i * 4 + 1] = (320 + interno * sin(angolo)).toFloat()
            tacche[i * 4 + 2] = (320 + 282.0 * cos(angolo)).toFloat()
            tacche[i * 4 + 3] = (320 + 282.0 * sin(angolo)).toFloat()
            taccheCorrenti[i * 4] = (320 + 266.0 * cos(angolo)).toFloat()
            taccheCorrenti[i * 4 + 1] = (320 + 266.0 * sin(angolo)).toFloat()
            taccheCorrenti[i * 4 + 2] = (320 + 284.0 * cos(angolo)).toFloat()
            taccheCorrenti[i * 4 + 3] = (320 + 284.0 * sin(angolo)).toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val scala = width / 640f
        canvas.scale(scala, scala)
        for (i in 0 until 24) {
            segmento.color = when {
                i < ore -> 0xFF8FB3CF.toInt()
                i == ore -> 0xFF3FD5FF.toInt()
                else -> 0xFF1B3A55.toInt()
            }
            canvas.drawArc(arcoOre, -88.5f + 15f * i, 12f, false, segmento)
        }
        canvas.drawCircle(320f, 320f, 250f, cerchio)
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

    fun mostra(meteo: Meteo?) {
        this.meteo = meteo
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val scala = width / 202f
        canvas.scale(scala, scala)
        asse.strokeWidth = 3f
        canvas.drawLine(46f, 16f, 46f, 172f, asse)
        for (i in 0..16) {
            val y = 172f - 9.75f * i
            val lunga = i % 8 == 0
            asse.strokeWidth = if (lunga) 3f else 2f
            canvas.drawLine(if (lunga) 34f else 39f, y, 46f, y, asse)
        }
        val m = meteo ?: return
        val passo = maxOf(1, ceil((m.massima - m.minima + 2) / 16.0).toInt())
        val base = ((m.minima + m.massima) / 2.0 / (4 * passo)).roundToInt() * 4 * passo - 8 * passo
        val pxPerGrado = 156f / (16 * passo)
        fun y(gradi: Int): Float = (172f - (gradi - base) * pxPerGrado).coerceIn(16f, 172f)
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
