package app.australdashboard.temi

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
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
    private const val ONDA = 0x33F0F0F0
    private const val ONDA_SCURA = 0x33161616
    private const val BORDO = 4

    private val nero: Typeface by lazy {
        Typeface.create(Typeface.create("sans-serif-condensed", Typeface.NORMAL), 900, false)
    }
    private val mono: Typeface get() = Ui.monoGrassetto

    override fun palette(context: Context) = Palette(SFONDO, TESTO, SECONDARIO, ROSSO, SFONDO, TESTO, nero, mono)

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

    override fun home(s: Schermata, azioni: AzioniHome): VistaHome = HomeBrutalismo(s, azioni)

    private class HomeBrutalismo(private val s: Schermata, private val azioni: AzioniHome) : VistaHome {
        private val ora = Ui.adatta(Ui.testo(s, 208, SFONDO, nero, 178).apply { letterSpacing = -0.038f }, 120, 208)
        private val data = Ui.testo(s, 28, SFONDO, mono, 34, maiuscolo = true)
        private val gradi = Ui.testo(s, 156, TESTO, nero, 140, "--°").apply { letterSpacing = -0.038f }
        private val iconaMeteo = Ui.icona(s, R.drawable.ic_nuvoloso, TESTO)
        private val cielo = Ui.adatta(Ui.testo(s, 32, TESTO, mono, 38, "IN ATTESA", maiuscolo = true), 20, 32)
        private val estremi = Ui.adatta(Ui.testo(s, 28, SECONDARIO, mono, 34, " "), 20, 28)
        private val inAscolto = Ui.colonna(s)
        private val fermo = Ui.colonna(s)
        private val copertina = ImageView(s).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        private val statoMusica = Ui.testo(s, 28, SECONDARIO, mono, 34, "IN ONDA /")
        private val titolo = Ui.testo(s, 54, TESTO, nero, 50, maiuscolo = true, righe = 3)
        private val artista = Ui.testo(s, 28, TESTO, mono, 34, maiuscolo = true, righe = 2)
        private val iconaPlay = Ui.icona(s, R.drawable.ic_pausa, SFONDO)

        override val view: View = costruisci()

        private fun costruisci(): View {
            val radice = Ui.colonna(s, 20).apply {
                setBackgroundColor(SFONDO)
                setPadding(24, 24, 24, 24)
            }
            radice.addView(servizi(), lp(TUTTO, 120))
            val corpo = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val sinistra = Ui.colonna(s, 20)
            sinistra.addView(bloccoOra(), lp(TUTTO, 290))
            sinistra.addView(bloccoMeteo(), lp(TUTTO, 196))
            sinistra.addView(bloccoMusica(), lp(TUTTO, 0, 1f))
            corpo.addView(sinistra, lp(520, TUTTO))
            corpo.addView(app(), lp(0, TUTTO, 1f))
            radice.addView(corpo, lp(TUTTO, 0, 1f))
            Ui.senzaRitaglio(radice, corpo, sinistra)
            return radice
        }

        private fun servizi(): View {
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val voci = listOf(
                Triple("A", "Benzinai", azioni::benzinai),
                Triple("B", "Tutte le app", azioni::tutteLeApp),
                Triple("C", "Impostazioni Android", azioni::impostazioniAndroid),
            )
            val pesi = listOf(1f, 1.1f, 1.5f)
            voci.forEachIndexed { i, (lettera, nome, azione) ->
                val cella = Ui.riga(s, 16).apply {
                    background = Ui.bordo(TESTO, BORDO)
                    setPadding(20, 0, 20, 0)
                    addView(etichetta(s, lettera))
                    addView(Ui.testo(s, 28, TESTO, mono, 32, nome, maiuscolo = true, righe = 2), lp(0, ADATTA, 1f))
                }
                riga.addView(Ui.premibile(cella, ONDA, nome) { azione() }, lp(0, TUTTO, pesi[i]))
            }
            riga.addView(
                tasto(s, R.drawable.ic_ingranaggio, TESTO, 60, "Impostazioni del launcher") { azioni.impostazioniLauncher() },
                lp(120, TUTTO),
            )
            return riga
        }

        private fun bloccoOra(): View {
            val blocco = Ui.colonna(s).apply {
                setBackgroundColor(TESTO)
                setPadding(24, 18, 24, 18)
            }
            blocco.addView(Ui.testo(s, 28, SFONDO, mono, 34, "ORA LOCALE /"))
            blocco.addView(ora, lp(TUTTO, 178).apply { leftMargin = -6 })
            blocco.addView(View(s), lp(1, 0, 1f))
            blocco.addView(data)
            Ui.senzaRitaglio(blocco)
            return blocco
        }

        private fun bloccoMeteo(): View {
            val blocco = Ui.riga(s, 22).apply {
                background = Ui.bordo(TESTO, BORDO)
                setPadding(20, 14, 20, 14)
            }
            blocco.addView(gradi)
            val colonna = Ui.colonna(s, 2)
            colonna.addView(iconaMeteo, lp(52, 52))
            colonna.addView(cielo, lp(TUTTO, 38))
            colonna.addView(estremi, lp(TUTTO, 34))
            blocco.addView(colonna, lp(0, ADATTA, 1f))
            Ui.senzaRitaglio(blocco)
            return blocco
        }

        private fun bloccoMusica(): View {
            val sezione = FrameLayout(s).apply { background = Ui.bordo(TESTO, BORDO) }

            val alto = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            Ui.premibile(copertina, ONDA, "Apri l'app della musica") { azioni.apriMusica() }
            copertina.setBackgroundColor(SECONDARIO)
            alto.addView(copertina, lp(236, 236))
            val info = Ui.colonna(s)
            info.addView(statoMusica)
            info.addView(titolo, lp(TUTTO, ADATTA).apply { topMargin = 6 })
            info.addView(View(s), lp(1, 0, 1f))
            info.addView(artista)
            Ui.premibile(info, ONDA, null) { azioni.apriMusica() }
            alto.addView(info, lp(0, 236, 1f))
            inAscolto.addView(alto, lp(TUTTO, 236))
            inAscolto.addView(View(s), lp(1, 0, 1f))
            val comandi = Ui.riga(s, 20)
            comandi.addView(tasto(s, R.drawable.ic_precedente, TESTO, 56, "Brano precedente") { azioni.precedente() }, lp(0, 140, 1f))
            val play = FrameLayout(s).apply {
                setBackgroundColor(ROSSO)
                addView(iconaPlay, lpf(64, 64, Gravity.CENTER))
            }
            comandi.addView(Ui.premibile(play, ONDA_SCURA, "Riproduci o metti in pausa") { azioni.playPausa() }, lp(0, 140, 1f))
            comandi.addView(tasto(s, R.drawable.ic_successivo, TESTO, 56, "Brano successivo") { azioni.successivo() }, lp(0, 140, 1f))
            inAscolto.addView(comandi, lp(TUTTO, 140))

            val intestazione = Ui.colonna(s)
            intestazione.addView(Ui.testo(s, 28, SECONDARIO, mono, 34, "MUSICA /"))
            intestazione.addView(
                Ui.adatta(Ui.testo(s, 120, TESTO, nero, 110, "Spotify", maiuscolo = true).apply { letterSpacing = -0.033f }, 60, 120),
                lp(TUTTO, 110),
            )
            fermo.addView(intestazione)
            fermo.addView(View(s), lp(1, 0, 1f))
            val basso = Ui.riga(s, 20)
            val apri = Ui.testo(s, 30, TESTO, mono, 34, "Apri Spotify", maiuscolo = true).apply {
                gravity = Gravity.CENTER
                background = Ui.bordo(TESTO, BORDO)
            }
            basso.addView(Ui.premibile(apri, ONDA, null) { azioni.apriMusica() }, lp(0, 140, 2f))
            basso.addView(tastoPieno(s, R.drawable.ic_play, 64, "Riproduci") { azioni.playPausa() }, lp(0, 140, 1f))
            fermo.addView(basso, lp(TUTTO, 140))

            sezione.addView(inAscolto, lpf(TUTTO, TUTTO).apply { setMargins(18, 18, 18, 18) })
            sezione.addView(fermo, lpf(TUTTO, TUTTO).apply { setMargins(18, 18, 18, 18) })
            mostraMusica(null)
            return sezione
        }

        private fun app(): View {
            val colonna = Ui.colonna(s, 20)
            AppHome.elenco.forEachIndexed { i, app ->
                val riga = Ui.riga(s, 22).apply {
                    background = Ui.bordo(TESTO, BORDO)
                    setPadding(16, 0, 24, 0)
                }
                riga.addView(
                    Ui.testo(s, 84, SFONDO, nero, 84, app.sigla).apply {
                        setBackgroundColor(TESTO)
                        gravity = Gravity.CENTER
                    },
                    lp(110, 110),
                )
                riga.addView(
                    Ui.adatta(Ui.testo(s, 68, TESTO, nero, 64, app.nome, maiuscolo = true).apply { letterSpacing = -0.015f }, 36, 68),
                    lp(0, 70, 1f),
                )
                riga.addView(Ui.testo(s, 28, SECONDARIO, mono, 32, "%02d".format(i + 1)))
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
        }

        override fun mostraMeteo(meteo: Meteo?) {
            if (meteo == null) {
                iconaMeteo.setImageResource(R.drawable.ic_nuvoloso)
                gradi.text = "--°"
                cielo.text = "IN ATTESA"
                estremi.text = " "
                return
            }
            iconaMeteo.setImageResource(Ui.iconaCielo(meteo.codice))
            gradi.text = "${meteo.gradi}°"
            cielo.text = FonteMeteo.descrizione(meteo.codice)
            estremi.text = "MAX ${meteo.massima}° / MIN ${meteo.minima}°"
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
    }

    override fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai = BenzinaiBrutalismo(s, azioni)

    private class BenzinaiBrutalismo(private val s: Schermata, private val azioni: AzioniBenzinai) : VistaBenzinai {
        private val ordini = LinkedHashMap<Ordine, TextView>()
        private val elencoView = Ui.colonna(s, 20)
        private val scheda = Ui.riga(s, 24).apply {
            setBackgroundColor(TESTO)
            setPadding(24, 20, 24, 20)
            visibility = View.GONE
        }
        private val schedaEtichetta = Ui.testo(s, 28, SFONDO, mono, 34, maiuscolo = true)
        private val indirizzo = Ui.adatta(
            Ui.testo(s, 92, SFONDO, nero, contenuto = "", righe = 2).apply { ellipsize = null; letterSpacing = -0.02f },
            44,
            92,
        )
        private val dettagli = Ui.testo(s, 30, SFONDO, mono, 36, maiuscolo = true)
        private val stellaScheda = Ui.icona(s, R.drawable.ic_stella, SFONDO)
        private val stellaTasto = FrameLayout(s).apply {
            background = Ui.bordo(SFONDO, BORDO)
            addView(stellaScheda, lpf(60, 60, Gravity.CENTER))
        }
        private var ultimo: ElencoBenzinai = ElencoBenzinai.Caricamento
        private var preferiti: Set<Long> = emptySet()
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
            radice.addView(costruisciScheda(), lp(TUTTO, 300))
            Ui.senzaRitaglio(radice, elencoView)
            return radice
        }

        private fun intestazione(): View {
            val riga = Ui.riga(s, 24)
            riga.addView(tasto(s, R.drawable.ic_indietro, TESTO, 60, "Indietro") { azioni.indietro() }, lp(120, 120))
            riga.addView(
                Ui.adatta(Ui.testo(s, 112, TESTO, nero, 100, "BENZINAI").apply { letterSpacing = -0.036f }, 56, 112),
                lp(0, 100, 1f),
            )
            val gruppo = Ui.riga(s, 20)
            for ((ordine, nome) in listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")) {
                val tasto = Ui.testo(s, 30, TESTO, mono, 34, nome, maiuscolo = true).apply { gravity = Gravity.CENTER }
                ordini[ordine] = tasto
                gruppo.addView(Ui.premibile(tasto, ONDA, "Ordina per $nome") { azioni.ordina(ordine) }, lp(200, 120))
            }
            riga.addView(gruppo, lp(ADATTA, TUTTO))
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
            testi.addView(indirizzo, lp(TUTTO, 0, 1f).apply { topMargin = 6 })
            testi.addView(dettagli)
            scheda.addView(testi, lp(0, TUTTO, 1f))
            val comandi = Ui.colonna(s, 20)
            comandi.addView(Ui.premibile(stellaTasto, ONDA_SCURA, "Salva tra i preferiti") { selezionato?.let(azioni::cambiaPreferito) }, lp(120, 120))
            comandi.addView(tasto(s, R.drawable.ic_chiudi, SFONDO, 56, "Chiudi scheda", onda = ONDA_SCURA) { seleziona(null) }, lp(120, 120))
            scheda.addView(comandi, lp(ADATTA, TUTTO))
            return scheda
        }

        override fun mostra(elenco: ElencoBenzinai, preferiti: Set<Long>) {
            ultimo = elenco
            this.preferiti = preferiti
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
                    if (e.preferiti.isNotEmpty()) sezione("★ ${e.preferiti.size}", "Preferiti", e.preferiti)
                    val per = when (e.ordine) {
                        Ordine.CONVIENE -> "PER CONVENIENZA"
                        Ordine.PREZZO -> "PER PREZZO"
                        Ordine.DISTANZA -> "PER DISTANZA"
                    }
                    if (e.vicini.isNotEmpty()) sezione(per, "Vicini", e.vicini)
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

        private fun sezione(pillola: String, titolo: String, distributori: List<Distributore>) {
            val colonna = Ui.colonna(s, 12)
            val intestazione = Ui.riga(s, 16)
            intestazione.addView(etichetta(s, pillola))
            intestazione.addView(Ui.testo(s, 44, TESTO, nero, 48, titolo, maiuscolo = true))
            colonna.addView(intestazione)
            val righe = Ui.colonna(s, 20)
            distributori.forEach { righe.addView(riga(it), lp(TUTTO, 120)) }
            colonna.addView(righe)
            elencoView.addView(colonna, lp(TUTTO, ADATTA))
        }

        private fun riga(d: Distributore): View {
            val adesso = System.currentTimeMillis()
            val riga = Ui.riga(s, 20, Gravity.NO_GRAVITY)
            val scelto = d.id == selezionato?.id
            val corpo = Ui.riga(s, 24).apply {
                background = if (scelto) Ui.bordo(ROSSO, 6) else Ui.bordo(TESTO, BORDO)
                setPadding(24, 0, 24, 0)
            }
            val sinistra = Ui.colonna(s)
            sinistra.addView(Ui.testo(s, 48, TESTO, nero, 50, d.nome, maiuscolo = true))
            sinistra.addView(Ui.testo(s, 28, SECONDARIO, mono, 34, "${d.marchio} / AGG. ${Benzinai.quando(d.aggiornato, adesso)}", maiuscolo = true))
            corpo.addView(sinistra, lp(0, ADATTA, 1f))
            corpo.addView(Ui.testo(s, 32, TESTO, mono, 38, Benzinai.distanza(d.distanzaKm), maiuscolo = true), lp(150, ADATTA))
            corpo.addView(Ui.testo(s, 88, TESTO, nero, 80, Benzinai.prezzo(d.prezzo)).apply { letterSpacing = -0.034f })
            riga.addView(Ui.premibile(corpo, ONDA, d.nome) { seleziona(d) }, lp(0, TUTTO, 1f))
            val preferito = d.id in preferiti
            riga.addView(
                tasto(
                    s,
                    if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella,
                    if (preferito) ROSSO else TESTO,
                    60,
                    if (preferito) "Rimuovi dai preferiti" else "Salva tra i preferiti",
                    coloreBordo = TESTO,
                ) { azioni.cambiaPreferito(d) },
                lp(120, 120),
            )
            return riga
        }

        private fun aggiornaScheda() {
            val d = selezionato
            if (d == null) {
                scheda.visibility = View.GONE
                return
            }
            scheda.visibility = View.VISIBLE
            schedaEtichetta.text = "INDIRIZZO / ${d.nome} / ${d.marchio}"
            indirizzo.text = "${d.indirizzo} - ${d.comune}".uppercase(Locale.ITALIAN)
            dettagli.text = "SELF ${Benzinai.prezzo(d.prezzo)} €/L / ${Benzinai.distanza(d.distanzaKm)} / AGG. ${Benzinai.quando(d.aggiornato, System.currentTimeMillis())}"
            val preferito = d.id in preferiti
            stellaScheda.setImageResource(if (preferito) R.drawable.ic_stella_piena else R.drawable.ic_stella)
            stellaTasto.contentDescription = if (preferito) "Rimuovi dai preferiti" else "Salva tra i preferiti"
        }
    }
}
