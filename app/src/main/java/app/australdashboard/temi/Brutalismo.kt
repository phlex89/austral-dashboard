package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.view.Gravity
import android.view.View
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

object Brutalismo : Tema {
    override val id = "brutalismo"
    override val nome = "Brutalismo"
    override val idea = "Blocchi grezzi, tipografia enorme"

    private const val SFONDO = 0xFF161616.toInt()
    private const val TESTO = 0xFFF0F0F0.toInt()
    private const val SECONDARIO = 0xFFA3A3A3.toInt()
    private const val ROSSO = 0xFFFF3D2E.toInt()
    private const val GRIGIO = 0xFF4A4A4A.toInt()
    private const val ONDA = 0x33F0F0F0
    private const val ONDA_SCURA = 0x33161616
    private const val BORDO = 4

    private val nero: Typeface by lazy {
        Typeface.create(Typeface.create("sans-serif-condensed", Typeface.NORMAL), 900, false)
    }
    private val condensato: Typeface by lazy {
        Typeface.create(Typeface.create("sans-serif-condensed", Typeface.NORMAL), 700, false)
    }
    private val mono: Typeface get() = Ui.monoGrassetto

    override fun palette(context: Context) = Palette(SFONDO, TESTO, ROSSO, TESTO, nero, mono)

    private enum class Livello { SPENTO, ACCESO, PIENO }

    private class TastoSistema(context: Context, nome: String) {
        private val icona = ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        private val etichetta = Ui.testo(context, 28, TESTO, Ui.monoGrassetto, 30, nome)
        val cornice = FrameLayout(context).apply {
            val colonna = Ui.colonna(context, 4, Gravity.CENTER)
            colonna.addView(icona, lp(52, 52))
            colonna.addView(etichetta, lp(ADATTA, 30))
            addView(colonna, lpf(TUTTO, TUTTO))
        }

        fun imposta(risorsa: Int, livello: Livello) {
            val colore = when (livello) {
                Livello.SPENTO -> SECONDARIO
                Livello.ACCESO -> TESTO
                Livello.PIENO -> SFONDO
            }
            cornice.background = when (livello) {
                Livello.SPENTO -> Ui.bordo(GRIGIO, BORDO)
                Livello.ACCESO -> Ui.bordo(TESTO, BORDO)
                Livello.PIENO -> Ui.pieno(TESTO)
            }
            icona.setImageResource(risorsa)
            icona.imageTintList = ColorStateList.valueOf(colore)
            etichetta.setTextColor(colore)
        }
    }

    private fun tasto(
        context: Context,
        icona: Int,
        coloreIcona: Int,
        lato: Int,
        descrizione: String,
        coloreBordo: Int = coloreIcona,
        onda: Int = ONDA,
        azione: () -> Unit,
    ): View {
        val cornice = FrameLayout(context).apply {
            background = Ui.bordo(coloreBordo, BORDO)
            addView(Ui.icona(context, icona, coloreIcona), lpf(lato, lato, Gravity.CENTER))
        }
        return Ui.premibile(cornice, onda, descrizione, azione)
    }

    private fun tastoPieno(context: Context, icona: Int, lato: Int, descrizione: String, azione: () -> Unit): View {
        val cornice = FrameLayout(context).apply {
            setBackgroundColor(ROSSO)
            addView(Ui.icona(context, icona, SFONDO), lpf(lato, lato, Gravity.CENTER))
        }
        return Ui.premibile(cornice, ONDA_SCURA, descrizione, azione)
    }

    private fun etichetta(context: Context, contenuto: String): TextView =
        Ui.testo(context, 28, SFONDO, mono, 32, contenuto).apply {
            setBackgroundColor(TESTO)
            setPadding(8, 2, 8, 2)
        }

    private class BarraMeteo(context: Context) : View(context) {
        private val pennello = Paint()
        var frazione: Float? = null
            set(valore) {
                field = valore
                invalidate()
            }

        override fun onDraw(canvas: Canvas) {
            val l = width.toFloat()
            pennello.color = GRIGIO
            canvas.drawRect(0f, 6f, l, 18f, pennello)
            val f = frazione ?: return
            val x = (f * l).coerceIn(5f, l - 5f)
            pennello.color = TESTO
            canvas.drawRect(0f, 6f, x, 18f, pennello)
            pennello.color = ROSSO
            canvas.drawRect(x - 5f, 0f, x + 5f, 24f, pennello)
        }
    }

    private fun prezzoConMillesimo(valore: Double): SpannableString {
        val testo = Benzinai.prezzo(valore)
        return SpannableString(testo).apply {
            setSpan(AbsoluteSizeSpan(56), testo.length - 1, testo.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeBrutalismo(s, azioni)

    private class HomeBrutalismo(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val ora = Ui.testo(s, 208, SFONDO, nero).apply { letterSpacing = -0.038f }
        private val data = Ui.testo(s, 28, SFONDO, mono, 34, maiuscolo = true)
        private val gradi = Ui.testo(s, 156, TESTO, nero, 140, "--°").apply { letterSpacing = -0.038f }
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, TESTO)
        private val cielo = Ui.adatta(Ui.testo(s, 32, TESTO, mono, 38, "IN ATTESA", maiuscolo = true), 20, 32)
        private val estremi = Ui.adatta(Ui.testo(s, 28, SECONDARIO, mono, 34, " "), 20, 28)
        private val pioggia = Ui.adatta(Ui.testo(s, 28, SFONDO, mono, 34), 20, 28).apply { visibility = View.GONE }
        private val inAscolto = Ui.colonna(s)
        private val fermo = Ui.colonna(s)
        private val copertina = ImageView(s).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val statoMusica = Ui.testo(s, 28, SECONDARIO, mono, 34, "IN ONDA /")
        private val titolo = Ui.testo(s, 54, TESTO, nero, 50, maiuscolo = true)
        private val artista = Ui.testo(s, 28, TESTO, mono, 34, maiuscolo = true)
        private val giorni = "LMMGVSD".map { lettera ->
            Ui.testo(s, 28, SFONDO, mono, 28, lettera.toString()).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(SFONDO, 3)
            }
        }
        private val avanzamento = View(s).apply {
            setBackgroundColor(SFONDO)
            pivotX = 0f
            scaleX = 0f
        }
        private val barraMeteo = BarraMeteo(s)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, SFONDO)
        private val wifi = TastoSistema(s, "WIFI")
        private val bluetooth = TastoSistema(s, "BT")
        private val volume = Ui.adatta(Ui.testo(s, 52, TESTO, nero, 52, "-").apply { gravity = Gravity.CENTER }, 30, 52)
        private val volumeMassimo = Ui.adatta(Ui.testo(s, 28, SECONDARIO, mono, 30, "/-").apply { gravity = Gravity.CENTER }, 16, 28)

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.riga(s, 20, Gravity.NO_GRAVITY).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            val sinistra = Ui.colonna(s, 20)
            sinistra.addView(bloccoOra(), lp(TUTTO, 268))
            sinistra.addView(bloccoMeteo(), lp(TUTTO, 204))
            sinistra.addView(bloccoMusica(), lp(TUTTO, 0, 1f))
            val playlist = rigaPlaylist()
            if (playlist != null) sinistra.addView(playlist, lp(TUTTO, 126))
            sinistra.addView(rigaSistema(), lp(TUTTO, 120))
            val destra = Ui.colonna(s, 20)
            destra.addView(servizi(), lp(TUTTO, 306))
            destra.addView(app(), lp(TUTTO, 0, 1f))
            radice.addView(sinistra, lp(616, TUTTO))
            radice.addView(destra, lp(0, TUTTO, 1f))
            Ui.senzaRitaglio(radice, sinistra)
            return radice
        }

        private fun cella(lettera: String, nome: String, icona: Int, lato: Int, azione: () -> Unit): View {
            val colonna = Ui.colonna(s).apply {
                background = Ui.bordo(TESTO, BORDO)
                setPadding(16, 14, 16, 14)
            }
            val alto = Ui.riga(s)
            alto.addView(etichetta(s, lettera))
            alto.addView(View(s), lp(0, 1, 1f))
            alto.addView(Ui.icona(s, icona, TESTO), lp(lato, lato))
            colonna.addView(alto, lp(TUTTO, 44))
            colonna.addView(View(s), lp(1, 0, 1f))
            colonna.addView(Ui.testo(s, 28, TESTO, mono, 32, nome, maiuscolo = true, righe = 2), lp(TUTTO, ADATTA))
            return Ui.premibile(colonna, ONDA, nome) { azione() }
        }

        private fun servizi(): View {
            val griglia = Ui.colonna(s, 20)
            val prima = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            prima.addView(cella("A", "Benzinai", R.drawable.ic_benzinaio, 44, azioni::benzinai), lp(0, TUTTO, 1f))
            prima.addView(cella("B", "Tutte le app", R.drawable.ic_griglia, 44, azioni::tutteLeApp), lp(0, TUTTO, 1f))
            val seconda = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            seconda.addView(cella("C", "Impostazioni Android", R.drawable.ic_cursori, 44, azioni::impostazioniAndroid), lp(0, TUTTO, 1f))
            seconda.addView(cella("D", "Launcher", R.drawable.ic_ingranaggio, 48, azioni::impostazioniLauncher), lp(0, TUTTO, 1f))
            griglia.addView(prima, lp(TUTTO, 0, 1f))
            griglia.addView(seconda, lp(TUTTO, 0, 1f))
            return griglia
        }

        private fun bloccoOra(): View {
            val blocco = Ui.colonna(s).apply { setPadding(24, 18, 24, 18) }
            val intestazione = Ui.riga(s)
            intestazione.addView(Ui.testo(s, 28, SFONDO, mono, 34, "ORA /"), lp(0, ADATTA, 1f))
            val caselle = Ui.riga(s, 6)
            giorni.forEach { caselle.addView(it, lp(34, 34)) }
            intestazione.addView(caselle, lp(ADATTA, 34))
            blocco.addView(intestazione, lp(TUTTO, 34))
            blocco.addView(ora, lp(TUTTO, ADATTA).apply {
                leftMargin = -6
                topMargin = -40
                bottomMargin = -40
            })
            blocco.addView(View(s), lp(1, 0, 1f))
            blocco.addView(data)
            Ui.senzaRitaglio(blocco)
            return FrameLayout(s).apply {
                setBackgroundColor(TESTO)
                addView(blocco, lpf(TUTTO, TUTTO))
                addView(avanzamento, lpf(TUTTO, 10, Gravity.BOTTOM))
                Ui.senzaRitaglio(this)
            }
        }

        private fun bloccoMeteo(): View {
            val blocco = Ui.riga(s, 22).apply {
                background = Ui.bordo(TESTO, BORDO)
                setPadding(20, 14, 20, 14)
            }
            blocco.addView(gradi)
            val colonna = Ui.colonna(s)
            val rigaCielo = Ui.riga(s, 12)
            rigaCielo.addView(iconaMeteo, lp(44, 44))
            rigaCielo.addView(cielo, lp(0, 38, 1f))
            colonna.addView(rigaCielo, lp(TUTTO, 44))
            colonna.addView(estremi, lp(TUTTO, 34))
            colonna.addView(barraMeteo, lp(TUTTO, 24).apply { topMargin = 4 })
            colonna.addView(pioggia, lp(TUTTO, 34).apply { topMargin = 8 })
            blocco.addView(colonna, lp(0, ADATTA, 1f))
            Ui.senzaRitaglio(blocco)
            return blocco
        }

        private fun bloccoMusica(): View {
            val sezione = FrameLayout(s).apply { background = Ui.bordo(TESTO, BORDO) }

            val alto = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            copertina.setBackgroundColor(SECONDARIO)
            alto.addView(copertina, lp(140, 140))
            val info = Ui.colonna(s)
            info.addView(statoMusica)
            info.addView(titolo, lp(TUTTO, ADATTA).apply { topMargin = 6 })
            info.addView(View(s), lp(1, 0, 1f))
            info.addView(artista)
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            alto.addView(info, lp(0, 140, 1f))
            inAscolto.addView(alto, lp(TUTTO, 140))
            inAscolto.addView(View(s), lp(1, 0, 1f))
            inAscolto.addView(filetto(), lp(TUTTO, 4).apply { leftMargin = -18; rightMargin = -18 })
            inAscolto.addView(View(s), lp(1, 0, 1f))
            val comandi = Ui.riga(s, 20, Gravity.TOP)
            comandi.addView(tasto(s, R.drawable.ic_precedente, TESTO, 56, "Brano precedente") { azioni.precedente() }, lp(0, 130, 1f))
            val play = FrameLayout(s).apply {
                setBackgroundColor(ROSSO)
                addView(iconaPlay, lpf(64, 64, Gravity.CENTER))
            }
            comandi.addView(
                rilievo(Ui.premibile(play, ONDA, "Riproduci o metti in pausa") { azioni.playPausa() }),
                lp(0, 130, 1f).apply { rightMargin = 8 },
            )
            comandi.addView(tasto(s, R.drawable.ic_successivo, TESTO, 56, "Brano successivo") { azioni.successivo() }, lp(0, 130, 1f))
            Ui.senzaRitaglio(comandi, inAscolto)
            inAscolto.addView(comandi, lp(TUTTO, 138))

            val intestazione = Ui.colonna(s)
            intestazione.addView(Ui.testo(s, 28, SECONDARIO, mono, 34, "MUSICA /"))
            intestazione.addView(
                Ui.adatta(Ui.testo(s, 120, TESTO, nero, 110, "Spotify", maiuscolo = true).apply { letterSpacing = -0.033f }, 60, 120),
                lp(TUTTO, ADATTA).apply { topMargin = -15; bottomMargin = -15 },
            )
            Ui.senzaRitaglio(intestazione)
            fermo.addView(intestazione)
            fermo.addView(View(s), lp(1, 0, 1f))
            fermo.addView(filetto(), lp(TUTTO, 4).apply { leftMargin = -18; rightMargin = -18 })
            fermo.addView(View(s), lp(1, 0, 1f))
            val basso = Ui.riga(s, 20, Gravity.TOP)
            val apri = Ui.testo(s, 30, TESTO, mono, 34, "Apri Spotify", maiuscolo = true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(TESTO, BORDO)
            }
            basso.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(0, 130, 2f))
            basso.addView(rilievo(tastoPieno(s, R.drawable.ic_play, 64, "Riproduci") { azioni.playPausa() }), lp(0, 130, 1f).apply { rightMargin = 8 })
            Ui.senzaRitaglio(basso, fermo)
            fermo.addView(basso, lp(TUTTO, 138))

            sezione.addView(inAscolto, lpf(TUTTO, TUTTO).apply { setMargins(18, 18, 18, 18) })
            sezione.addView(fermo, lpf(TUTTO, TUTTO).apply { setMargins(18, 18, 18, 18) })
            Ui.senzaRitaglio(sezione)
            mostraMusica(null)
            return sezione
        }

        private fun filetto(): View = View(s).apply { setBackgroundColor(TESTO) }

        private fun rilievo(tasto: View): View = FrameLayout(s).apply {
            addView(View(s).apply {
                setBackgroundColor(TESTO)
                translationX = 8f
                translationY = 8f
            }, lpf(TUTTO, TUTTO))
            addView(tasto, lpf(TUTTO, TUTTO))
            Ui.senzaRitaglio(this)
        }

        private fun rigaPlaylist(): View? {
            val elenco = Playlists.elenco(s)
            if (elenco.isEmpty()) return null
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            elenco.forEach { p ->
                val colonna = Ui.colonna(s, 4, Gravity.CENTER_VERTICAL).apply {
                    background = Ui.bordo(TESTO, BORDO)
                    setPadding(12, 0, 12, 0)
                }
                colonna.addView(Ui.icona(s, R.drawable.ic_play, ROSSO), lp(22, 22))
                colonna.addView(
                    Ui.testo(s, 28, TESTO, condensato, 30, p.nome, maiuscolo = true, righe = 3),
                    lp(TUTTO, ADATTA),
                )
                riga.addView(
                    Ui.premibile(colonna, ONDA, "Riproduci la playlist ${p.nome}") { azioni.playlist(p) },
                    lp(0, TUTTO, 1f),
                )
            }
            return riga
        }

        private fun rigaSistema(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            riga.addView(Ui.premibile(wifi.cornice, ONDA, "Wi-Fi") { azioni.wifi() }, lp(120, 120))
            riga.addView(Ui.premibile(bluetooth.cornice, ONDA, "Bluetooth") { azioni.bluetooth() }, lp(120, 120))
            riga.addView(tasto(s, R.drawable.ic_meno, TESTO, 60, "Abbassa il volume") { azioni.volume(false) }, lp(120, 120))
            val livello = Ui.colonna(s, 0, Gravity.CENTER)
            livello.addView(volume, lp(TUTTO, 52))
            livello.addView(volumeMassimo, lp(TUTTO, 30))
            riga.addView(livello, lp(56, TUTTO))
            riga.addView(tasto(s, R.drawable.ic_piu, TESTO, 60, "Alza il volume") { azioni.volume(true) }, lp(120, 120))
            return riga
        }

        private fun app(): View {
            val colonna = Ui.colonna(s, 20)
            AppHome.elenco(s).forEachIndexed { i, app ->
                val riga = Ui.riga(s, 18).apply {
                    clipChildren = false
                    background = Ui.bordo(TESTO, BORDO)
                    setPadding(10, 0, 16, 0)
                }
                riga.addView(
                    Ui.testo(s, 72, SFONDO, nero, 72, app.sigla).apply {
                        setBackgroundColor(TESTO)
                        gravity = Gravity.CENTER
                    },
                    lp(92, 92),
                )
                riga.addView(
                    Ui.adatta(Ui.testo(s, 56, TESTO, nero, 56, app.nome, maiuscolo = true).apply { letterSpacing = -0.018f }, 32, 56),
                    lp(0, ADATTA, 1f).apply { topMargin = -5; bottomMargin = -5 },
                )
                riga.addView(Ui.testo(s, 28, SECONDARIO, mono, 32, "%02d".format(i + 1)), lp(ADATTA, ADATTA).apply { rightMargin = -4 })
                riga.addView(Ui.icona(s, R.drawable.brutalismo_freccia, TESTO), lp(32, 32))
                val installata = app.intent(s) != null
                if (!installata) riga.alpha = 0.35f
                Ui.premibile(riga, ONDA, app.nome) { if (installata) azioni.apri(app) }
                colonna.addView(riga, lp(TUTTO, 0, 1f))
            }
            return colonna
        }

        override fun mostraOra(adesso: Date) {
            ora.text = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(adesso)
            data.text = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN).format(adesso).uppercase(Locale.ITALIAN)
            val calendario = Calendar.getInstance().apply { time = adesso }
            val oggi = (calendario.get(Calendar.DAY_OF_WEEK) + 5) % 7
            giorni.forEachIndexed { i, casella ->
                val attivo = i == oggi
                casella.background = if (attivo) Ui.pieno(SFONDO) else Ui.bordo(SFONDO, 3)
                casella.setTextColor(if (attivo) TESTO else SFONDO)
            }
            avanzamento.scaleX = (calendario.get(Calendar.HOUR_OF_DAY) * 60 + calendario.get(Calendar.MINUTE)) / 1440f
        }

        override fun mostraMeteo(meteo: Meteo?) {
            val testoPioggia = FonteMeteo.testoPioggia(meteo?.pioggiaTraMinuti)
            if (testoPioggia == null) {
                pioggia.visibility = View.GONE
            } else {
                val t = " ${testoPioggia.uppercase(Locale.ITALIAN)} "
                pioggia.text = SpannableString(t).apply {
                    setSpan(BackgroundColorSpan(TESTO), 0, t.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                pioggia.visibility = View.VISIBLE
            }
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "IN ATTESA"
                estremi.text = " "
                barraMeteo.frazione = null
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "MAX ${meteo.massima}° / MIN ${meteo.minima}°"
            val escursione = meteo.massima - meteo.minima
            barraMeteo.frazione =
                if (escursione > 0) Ui.frazione(meteo.gradi, meteo.minima, meteo.massima) else 0.5f
        }

        override fun mostraMusica(musica: Musica?) {
            inAscolto.visibility = if (musica != null) View.VISIBLE else View.GONE
            fermo.visibility = if (musica == null) View.VISIBLE else View.GONE
            if (musica == null) return
            statoMusica.text = if (musica.inRiproduzione) "IN ONDA /" else "IN PAUSA /"
            copertina.contentDescription = "Apri ${musica.app}"
            titolo.text = musica.titolo
            artista.text = musica.artista
            if (musica.copertina != null) copertina.setImageBitmap(musica.copertina) else copertina.setImageDrawable(null)
            iconaPlay.setImageResource(if (musica.inRiproduzione) R.drawable.ic_pausa else R.drawable.ic_play)
        }

        override fun mostraSistema(stato: StatoSistema) {
            when (stato.wifi) {
                StatoWifi.SPENTO -> wifi.imposta(R.drawable.ic_wifi_spento, Livello.SPENTO)
                StatoWifi.ACCESO -> wifi.imposta(R.drawable.ic_wifi, Livello.ACCESO)
                StatoWifi.CONNESSO -> wifi.imposta(R.drawable.ic_wifi, Livello.PIENO)
            }
            if (stato.bluetooth) bluetooth.imposta(R.drawable.ic_bluetooth, Livello.PIENO)
            else bluetooth.imposta(R.drawable.ic_bluetooth_spento, Livello.SPENTO)
            volume.text = stato.volume.toString()
            volumeMassimo.text = "/${stato.volumeMassimo}"
        }
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiBrutalismo(s, azioni)

    private class BenzinaiBrutalismo(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private val scheda = Ui.riga(s, 24).apply {
            background = LayerDrawable(arrayOf(Ui.pieno(ROSSO), Ui.pieno(TESTO))).apply { setLayerInset(1, 20, 0, 0, 0) }
            setPadding(44, 12, 24, 12)
            visibility = View.GONE
        }
        private val schedaEtichetta = Ui.testo(s, 28, SFONDO, mono, 34, maiuscolo = true)
        private val indirizzo = Ui.adatta(
            Ui.testo(s, 72, SFONDO, nero, 68, contenuto = "").apply { letterSpacing = -0.028f },
            30,
            72,
        )
        private val dettagli = Ui.testo(s, 28, SFONDO, mono, 34, maiuscolo = true)
        private val spesa = Ui.testo(s, 28, SECONDARIO, mono, 32, maiuscolo = true).apply { visibility = View.GONE }
        private val stato = StatoBenzinai()

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(intestazione(), lp(TUTTO, 120))
            radice.addView(ordinamenti(), lp(TUTTO, 120))
            val scorrimento = ScrollView(s).apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(elencoView)
            }
            radice.addView(FrameLayout(s).apply { addView(scorrimento) }, lp(TUTTO, 0, 1f))
            radice.addView(costruisciScheda(), lp(TUTTO, 186))
            Ui.senzaRitaglio(radice, elencoView)
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            Ui.senzaRitaglio(riga)
            riga.addView(tasto(s, R.drawable.ic_indietro, TESTO, 60, "Indietro") { azioni.indietro() }, lp(120, 120))
            riga.addView(
                Ui.adatta(Ui.testo(s, 104, TESTO, nero, 92, "BENZINAI").apply { letterSpacing = -0.038f }, 56, 104),
                lp(0, ADATTA, 1f).apply { topMargin = -15; bottomMargin = -15 },
            )
            val testi = Ui.colonna(s, 2)
            testi.addView(Ui.testo(s, 40, TESTO, nero, 40, "RIFORNIMENTI"))
            testi.addView(spesa)
            val cornice = Ui.riga(s, 16).apply {
                background = Ui.bordo(TESTO, BORDO)
                setPadding(20, 0, 20, 0)
            }
            cornice.addView(Ui.icona(s, R.drawable.ic_rifornimenti, TESTO), lp(52, 52))
            cornice.addView(testi, lp(ADATTA, ADATTA))
            riga.addView(Ui.premibile(cornice, ONDA, "Rifornimenti") { azioni.rifornimenti() }, lp(ADATTA, TUTTO))
            return riga
        }

        private fun ordinamenti(): View {
            val riga = Ui.riga(s, 20)
            riga.addView(etichetta(s, "ORDINA PER"))
            for ((ordine, nome) in Ui.ORDINI) {
                val tasto = Ui.testo(s, 30, TESTO, mono, 34, nome, maiuscolo = true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                riga.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(0, TUTTO, 1f))
            }
            evidenziaOrdine(Ordine.CONVIENE)
            return riga
        }

        private fun evidenziaOrdine(attivo: Ordine) {
            for ((ordine, tasto) in ordini) {
                val scelto = ordine == attivo
                tasto.background = if (scelto) Ui.pieno(TESTO) else Ui.bordo(TESTO, BORDO)
                tasto.setTextColor(if (scelto) SFONDO else TESTO)
                tasto.isSelected = scelto
            }
        }

        private fun costruisciScheda(): View {
            val testi = Ui.colonna(s)
            testi.addView(schedaEtichetta)
            testi.addView(indirizzo, lp(TUTTO, 68))
            testi.addView(dettagli)
            scheda.addView(testi, lp(0, ADATTA, 1f))
            scheda.addView(tasto(s, R.drawable.ic_chiudi, SFONDO, 56, "Chiudi scheda", onda = ONDA_SCURA) { seleziona(null) }, lp(120, 120))
            return scheda
        }

        override fun mostra(elenco: ElencoBenzinai) {
            stato.ultimo = elenco
            disegna()
        }

        override fun mostraSpesa(testo: String?) {
            spesa.text = testo ?: ""
            spesa.visibility = if (testo.isNullOrEmpty()) View.GONE else View.VISIBLE
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
                    if (e.preferiti.isNotEmpty()) sezione("★ ${e.preferiti.size}", "Preferiti", e.preferiti, true)
                    if (e.vicini.isNotEmpty()) sezione(null, "Vicini", e.vicini, false)
                    if (tutti.isEmpty()) messaggio("Nessun distributore entro 15 km.")
                }
            }
            aggiornaScheda()
        }

        private fun messaggio(testo: String) {
            val blocco = Ui.colonna(s, 12).apply {
                background = Ui.bordo(TESTO, BORDO)
                setPadding(24, 20, 24, 20)
            }
            blocco.addView(Ui.testo(s, 28, SECONDARIO, mono, 34, "STATO /"))
            blocco.addView(Ui.testo(s, 54, TESTO, nero, 52, testo, maiuscolo = true, righe = 4))
            elencoView.addView(blocco, lp(TUTTO, ADATTA))
        }

        private fun sezione(pillola: String?, titolo: String, distributori: List<Distributore>, luogo: Boolean) {
            val colonna = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 16)
            if (pillola != null) intestazione.addView(etichetta(s, pillola))
            intestazione.addView(Ui.testo(s, 44, TESTO, nero, 48, titolo, maiuscolo = true))
            intestazione.addView(View(s).apply { setBackgroundColor(TESTO) }, lp(0, 4, 1f))
            if (!luogo) intestazione.addView(Ui.testo(s, 28, SECONDARIO, mono, 32, "€/L SELF"))
            colonna.addView(intestazione, lp(TUTTO, ADATTA))
            val righe = Ui.colonna(s, 20)
            distributori.forEachIndexed { i, d -> righe.addView(riga(d, luogo, i + 1), lp(TUTTO, 120)) }
            colonna.addView(righe)
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore, luogo: Boolean, posizione: Int): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == stato.selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = if (scelto) Ui.bordo(ROSSO, 6) else Ui.bordo(TESTO, BORDO)
                setPadding(18, 0, 24, 0)
            }
            corpo.addView(
                Ui.testo(s, 60, SFONDO, nero, 60, posizione.toString()).apply {
                    setBackgroundColor(if (scelto) ROSSO else TESTO)
                    gravity = Gravity.CENTER
                },
                lp(72, 72),
            )
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 48, TESTO, nero, 50, if (luogo) d.comune else d.nome, maiuscolo = true))
            sinistra.addView(Ui.testo(s, 28, SECONDARIO, mono, 34, "${d.marchio} / AGG. ${Benzinai.quando(d.aggiornato, adesso)}", maiuscolo = true))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(Ui.testo(s, 32, TESTO, mono, 38, Benzinai.distanza(d.distanzaKm), maiuscolo = true), lp(ADATTA, ADATTA))
            DatiMimit.andamento(d)?.let { a ->
                val variazione = Ui.riga(s, 6).apply {
                    contentDescription = "Prezzo ${if (a.salito) "salito" else "sceso"} di ${a.differenza}"
                }
                variazione.addView(
                    Ui.icona(s, if (a.salito) R.drawable.ic_su else R.drawable.ic_giu, TESTO),
                    lp(22, 22),
                )
                variazione.addView(Ui.testo(s, 28, TESTO, mono, 34, a.differenza), lp(ADATTA, ADATTA))
                corpo.addView(variazione, lp(ADATTA, ADATTA))
            }
            corpo.addView(Ui.testo(s, 88, TESTO, nero, 80).apply {
                letterSpacing = -0.034f
                text = prezzoConMillesimo(d.prezzo)
            })
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
            schedaEtichetta.text = "INDIRIZZO / ${d.nome} / ${d.marchio}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}".uppercase(Locale.ITALIAN)
            dettagli.text = "SELF ${Benzinai.prezzo(d.prezzo)} €/L / ${Benzinai.distanza(d.distanzaKm)} / AGG. ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
        }
    }
}
