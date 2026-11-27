package app.australdashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.australdashboard.funzioni.Archivio
import app.australdashboard.funzioni.FonteTesti
import app.australdashboard.funzioni.FonteTraduzioni
import app.australdashboard.funzioni.LimiteTraduzioni
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.TestoBrano
import java.util.Locale
import kotlin.math.abs

class TestoActivity : Schermata() {
    private val principale = Handler(Looper.getMainLooper())
    private lateinit var brano: TextView
    private lateinit var nota: TextView
    private lateinit var tempi: LinearLayout
    private lateinit var etichettaSpostamento: TextView
    private lateinit var archivio: Archivio
    private lateinit var traduci: TextView
    private lateinit var avviso: TextView
    private lateinit var scorrimento: ScrollView
    private lateinit var colonnaRighe: LinearLayout
    private lateinit var segui: Button
    private var lettore: Lettore? = null
    private var chiave: String? = null
    private var caricato: TestoBrano? = null
    private var righe: List<TextView> = emptyList()
    private var tradotte: List<TextView> = emptyList()
    private var traduzioneAccesa = false
    private var traduzione: List<String>? = null
    private var traducendo = false
    private var spostamento = 0L
    private var corrente = -1
    private var seguendo = true
    private var daCentrare = false
    private var dimensione = DIMENSIONE_PREDEFINITA

    private val aggiorna = object : Runnable {
        override fun run() {
            aggiornaRiga()
            principale.postDelayed(this, INTERVALLO_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        archivio = Archivio.di(this)
        val preferenze = getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE)
        dimensione = preferenze.getFloat(CHIAVE_DIMENSIONE, DIMENSIONE_PREDEFINITA)
        traduzioneAccesa = preferenze.getBoolean(CHIAVE_TRADUZIONE, false)
        traduci = tastoDimensione("Traduci", "Traduzione in italiano", 0f).apply {
            textSize = 24f
            setOnClickListener { cambiaTraduzione() }
            visibility = View.GONE
        }
        val dimensioni = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(traduci, LinearLayout.LayoutParams(dp(150), dp(80)).apply { marginEnd = dp(12) })
            addView(tastoDimensione("A−", "Testo più piccolo", -PASSO), LinearLayout.LayoutParams(dp(96), dp(80)))
            addView(tastoDimensione("A+", "Testo più grande", PASSO), LinearLayout.LayoutParams(dp(96), dp(80)).apply { marginStart = dp(12) })
        }
        brano = secondario("", 22f).apply { isSingleLine = true }
        etichettaSpostamento = secondario("", 22f).apply {
            gravity = Gravity.CENTER
            contentDescription = "Spostamento del testo"
        }
        tempi = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            addView(tastoDimensione("−0,5 s", "Testo prima", 0f).apply {
                textSize = 22f
                setOnClickListener { cambiaSpostamento(-PASSO_SPOSTAMENTO_MS) }
            }, LinearLayout.LayoutParams(dp(120), dp(64)))
            addView(etichettaSpostamento, LinearLayout.LayoutParams(dp(96), ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(tastoDimensione("+0,5 s", "Testo dopo", 0f).apply {
                textSize = 22f
                setOnClickListener { cambiaSpostamento(PASSO_SPOSTAMENTO_MS) }
            }, LinearLayout.LayoutParams(dp(120), dp(64)))
        }
        val rigaBrano = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(40), 0, dp(32), dp(8))
            addView(brano, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(tempi)
        }
        nota = secondario("", 20f).apply {
            setPadding(dp(40), 0, dp(40), dp(8))
            visibility = View.GONE
        }
        avviso = testo("", 28f).apply {
            gravity = Gravity.CENTER
            setTextColor(SECONDARIO)
            setPadding(dp(40), dp(40), dp(40), dp(40))
            visibility = View.GONE
        }
        colonnaRighe = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(40), 0, dp(40), 0)
        }
        scorrimento = ScrollView(this).apply {
            setBackgroundColor(SFONDO)
            isVerticalScrollBarEnabled = false
            addView(colonnaRighe)
            addOnLayoutChangeListener { _, _, alto, _, basso, _, altoPrima, _, bassoPrima ->
                if (basso - alto != bassoPrima - altoPrima) {
                    val meta = (basso - alto) / 2
                    colonnaRighe.setPadding(dp(40), meta, dp(40), meta)
                    post { centra(false) }
                }
            }
        }
        sorvegliaTocco()
        segui = pulsante("Segui il testo") {
            seguendo = true
            segui.visibility = View.GONE
            centra(true)
        }.apply { visibility = View.GONE }
        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(SFONDO)
            addView(intestazione("Testo", dimensioni))
            addView(rigaBrano)
            addView(nota)
            addView(avviso, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(scorrimento, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(segui, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(32), dp(8), dp(32), dp(16))
            })
        }
        setContentView(radice)
    }

    override fun onStart() {
        super.onStart()
        if (!Lettore.permessoConcesso(this)) {
            mostraAvviso("Per leggere il brano in ascolto serve il permesso della musica: Impostazioni → Musica")
            return
        }
        lettore = Lettore(this) { mostraBrano(it) }.also { it.avvia() }
        principale.post(aggiorna)
    }

    override fun onStop() {
        principale.removeCallbacks(aggiorna)
        lettore?.ferma()
        lettore = null
        super.onStop()
    }

    private fun mostraBrano(musica: Musica?) {
        if (musica == null || musica.titolo.isEmpty()) {
            chiave = null
            brano.text = ""
            mostraAvviso("Niente in riproduzione")
            return
        }
        val nuova = "${musica.titolo}|${musica.artista}"
        if (nuova == chiave) return
        chiave = nuova
        brano.text = if (musica.artista.isEmpty()) musica.titolo else "${musica.titolo} · ${musica.artista}"
        mostraAvviso("Cerco il testo…")
        Thread {
            val esito = runCatching { FonteTesti.scarica(musica.titolo, musica.artista, musica.durataMs, archivio) }
            val salvato = FonteTesti.spostamento(archivio, nuova)
            runOnUiThread {
                if (isDestroyed || chiave != nuova) return@runOnUiThread
                spostamento = salvato
                val trovato = esito.getOrNull()
                when {
                    esito.isFailure -> mostraAvviso("Testo non raggiungibile: controlla la connessione")
                    trovato == null || (trovato.righe.isEmpty() && !trovato.strumentale) -> mostraAvviso("Testo non disponibile per questo brano")
                    trovato.strumentale -> mostraAvviso("Brano strumentale")
                    else -> mostraTesto(trovato)
                }
            }
        }.start()
    }

    private fun mostraAvviso(messaggio: String) {
        caricato = null
        righe = emptyList()
        tradotte = emptyList()
        traduzione = null
        traduci.visibility = View.GONE
        tempi.visibility = View.GONE
        mostraNota("")
        corrente = -1
        colonnaRighe.removeAllViews()
        avviso.text = messaggio
        avviso.visibility = View.VISIBLE
        scorrimento.visibility = View.GONE
        segui.visibility = View.GONE
    }

    private fun mostraTesto(nuovo: TestoBrano) {
        caricato = nuovo
        corrente = -1
        seguendo = nuovo.sincronizzato
        traduzione = null
        avviso.visibility = View.GONE
        scorrimento.visibility = View.VISIBLE
        segui.visibility = View.GONE
        colonnaRighe.removeAllViews()
        val colore = if (nuovo.sincronizzato) SPENTA else Color.WHITE
        val originali = mutableListOf<TextView>()
        val traduzioni = mutableListOf<TextView>()
        nuovo.righe.forEach { riga ->
            val originale = rigaTesto(riga.testo.ifEmpty { "♪" }, colore, Typeface.DEFAULT_BOLD)
            val tradotta = rigaTesto("", colore, Typeface.DEFAULT).apply {
                setPadding(dp(24), dp(10), 0, dp(10))
                visibility = View.GONE
            }
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(originale, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(tradotta, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                if (nuovo.sincronizzato) {
                    setOnClickListener {
                        lettore?.vaiA((riga.ms + spostamento).coerceAtLeast(0))
                        seguendo = true
                        segui.visibility = View.GONE
                    }
                }
            }.also { colonnaRighe.addView(it, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
            originali.add(originale)
            traduzioni.add(tradotta)
        }
        righe = originali
        tradotte = traduzioni
        traduci.visibility = View.VISIBLE
        tempi.visibility = if (nuovo.sincronizzato) View.VISIBLE else View.GONE
        mostraSpostamento()
        aggiornaTasto()
        mostraNota("")
        if (traduzioneAccesa) caricaTraduzione()
        scorrimento.scrollTo(0, 0)
        daCentrare = true
        aggiornaRiga()
    }

    private fun rigaTesto(contenuto: String, colore: Int, carattere: Typeface): TextView = TextView(this).apply {
        text = contenuto
        textSize = dimensione
        typeface = carattere
        setTextColor(colore)
        setPadding(0, dp(10), 0, dp(10))
    }

    private fun cambiaTraduzione() {
        traduzioneAccesa = !traduzioneAccesa
        getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit().putBoolean(CHIAVE_TRADUZIONE, traduzioneAccesa).apply()
        aggiornaTasto()
        if (traduzioneAccesa) caricaTraduzione() else mostraTraduzione()
    }

    private fun aggiornaTasto() {
        traduci.setTextColor(if (traduzioneAccesa) accento else Color.WHITE)
        (traduci.background as GradientDrawable).setStroke(if (traduzioneAccesa) dp(2) else 0, accento)
    }

    private fun caricaTraduzione() {
        val attuale = caricato ?: return
        val perBrano = chiave ?: return
        if (traduzione != null) {
            mostraTraduzione()
            return
        }
        if (traducendo) return
        traducendo = true
        mostraNota("Traduco…")
        val email = getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getString(CHIAVE_EMAIL, "").orEmpty()
        Thread {
            val esito = runCatching { FonteTraduzioni.traduci(perBrano, attuale.righe.map { it.testo }, email, archivio) }
            runOnUiThread {
                traducendo = false
                if (isDestroyed || chiave != perBrano || caricato !== attuale) return@runOnUiThread
                val errore = esito.exceptionOrNull()
                when {
                    errore is LimiteTraduzioni -> mostraNota(
                        if (email.isBlank()) "Traduzioni di oggi finite: con un'email in Impostazioni → Musica se ne hanno di più"
                        else "Traduzioni di oggi finite",
                    )
                    errore != null -> mostraNota("Traduzione non raggiungibile: controlla la connessione")
                    esito.getOrNull() == null -> {
                        traduci.visibility = View.GONE
                        mostraNota("")
                    }
                    else -> {
                        traduzione = esito.getOrNull()
                        mostraNota("")
                        mostraTraduzione()
                    }
                }
            }
        }.start()
    }

    private fun mostraTraduzione() {
        val testi = traduzione
        val visibile = traduzioneAccesa && testi != null
        tradotte.forEachIndexed { i, vista ->
            vista.text = testi?.getOrNull(i).orEmpty()
            vista.visibility = if (visibile) View.VISIBLE else View.GONE
        }
        if (!traduzioneAccesa) mostraNota("")
        scorrimento.post { centra(false) }
    }

    private fun cambiaSpostamento(passo: Long) {
        val perBrano = chiave ?: return
        spostamento = (spostamento + passo).coerceIn(-SPOSTAMENTO_MASSIMO_MS, SPOSTAMENTO_MASSIMO_MS)
        mostraSpostamento()
        aggiornaRiga()
        val valore = spostamento
        Thread { FonteTesti.salvaSpostamento(archivio, perBrano, valore) }.start()
    }

    private fun mostraSpostamento() {
        etichettaSpostamento.text = if (spostamento == 0L) "0 s" else String.format(Locale.ITALY, "%+.1f s", spostamento / 1000.0)
    }

    private fun mostraNota(messaggio: String) {
        nota.text = messaggio
        nota.visibility = if (messaggio.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun aggiornaRiga() {
        val attuale = caricato ?: return
        if (!attuale.sincronizzato) return
        val posizione = lettore?.posizioneMs() ?: return
        val indice = FonteTesti.rigaCorrente(attuale.righe, posizione - spostamento)
        if (daCentrare && seguendo && indice == corrente && righe.firstOrNull()?.isLaidOut == true) {
            daCentrare = false
            centra(false)
        }
        if (indice == corrente) return
        righe.getOrNull(corrente)?.setTextColor(SPENTA)
        tradotte.getOrNull(corrente)?.setTextColor(SPENTA)
        righe.getOrNull(indice)?.setTextColor(Color.WHITE)
        tradotte.getOrNull(indice)?.setTextColor(Color.WHITE)
        corrente = indice
        if (seguendo) centra(true)
    }

    private fun centra(animato: Boolean) {
        val riga = righe.getOrNull(corrente.coerceAtLeast(0))?.parent as? View ?: return
        if (caricato?.sincronizzato != true) return
        val meta = scorrimento.height / 2
        val destinazione = (riga.top + riga.height / 2 - meta).coerceAtLeast(0)
        if (animato) scorrimento.smoothScrollTo(0, destinazione) else scorrimento.scrollTo(0, destinazione)
    }

    private fun sorvegliaTocco() {
        val soglia = ViewConfiguration.get(this).scaledTouchSlop
        var inizio = 0f
        scorrimento.setOnTouchListener { _, evento ->
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> inizio = evento.y
                MotionEvent.ACTION_MOVE -> if (seguendo && abs(evento.y - inizio) > soglia && caricato?.sincronizzato == true) {
                    seguendo = false
                    segui.visibility = View.VISIBLE
                }
            }
            false
        }
    }

    private fun tastoDimensione(etichetta: String, descrizione: String, passo: Float): TextView = TextView(this).apply {
        text = etichetta
        textSize = 30f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        contentDescription = descrizione
        background = GradientDrawable().apply {
            setColor(SCHEDA)
            cornerRadius = dp(14).toFloat()
        }
        setOnClickListener { cambiaDimensione(passo) }
    }

    private fun cambiaDimensione(passo: Float) {
        val nuova = (dimensione + passo).coerceIn(DIMENSIONE_MINIMA, DIMENSIONE_MASSIMA)
        if (nuova == dimensione) return
        dimensione = nuova
        getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).edit().putFloat(CHIAVE_DIMENSIONE, nuova).apply()
        righe.forEach { it.textSize = nuova }
        tradotte.forEach { it.textSize = nuova }
        scorrimento.post { centra(false) }
    }

    companion object {
        const val PREFERENZE = "testi"
        const val CHIAVE_EMAIL = "email"
        private const val CHIAVE_DIMENSIONE = "dimensione"
        private const val CHIAVE_TRADUZIONE = "traduzione"
        private const val DIMENSIONE_PREDEFINITA = 34f
        private const val DIMENSIONE_MINIMA = 22f
        private const val DIMENSIONE_MASSIMA = 58f
        private const val PASSO = 4f
        private const val PASSO_SPOSTAMENTO_MS = 500L
        private const val SPOSTAMENTO_MASSIMO_MS = 10_000L
        private const val INTERVALLO_MS = 200L
        private const val SPENTA = 0xFF5E636C.toInt()
    }
}
