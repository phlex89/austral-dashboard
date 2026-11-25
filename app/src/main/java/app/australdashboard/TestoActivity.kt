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
import app.australdashboard.funzioni.FonteTesti
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Musica
import app.australdashboard.funzioni.TestoBrano
import kotlin.math.abs

class TestoActivity : Schermata() {
    private val principale = Handler(Looper.getMainLooper())
    private lateinit var brano: TextView
    private lateinit var avviso: TextView
    private lateinit var scorrimento: ScrollView
    private lateinit var colonnaRighe: LinearLayout
    private lateinit var segui: Button
    private var lettore: Lettore? = null
    private var chiave: String? = null
    private var caricato: TestoBrano? = null
    private var righe: List<TextView> = emptyList()
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
        dimensione = getSharedPreferences(PREFERENZE, Context.MODE_PRIVATE).getFloat(CHIAVE_DIMENSIONE, DIMENSIONE_PREDEFINITA)
        val dimensioni = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(tastoDimensione("A−", "Testo più piccolo", -PASSO), LinearLayout.LayoutParams(dp(96), dp(80)))
            addView(tastoDimensione("A+", "Testo più grande", PASSO), LinearLayout.LayoutParams(dp(96), dp(80)).apply { marginStart = dp(12) })
        }
        brano = secondario("", 22f).apply {
            isSingleLine = true
            setPadding(dp(40), 0, dp(40), dp(8))
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
            addView(brano)
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
            val esito = runCatching { FonteTesti.scarica(musica.titolo, musica.artista, musica.durataMs) }
            runOnUiThread {
                if (isDestroyed || chiave != nuova) return@runOnUiThread
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
        avviso.visibility = View.GONE
        scorrimento.visibility = View.VISIBLE
        segui.visibility = View.GONE
        colonnaRighe.removeAllViews()
        righe = nuovo.righe.map { riga ->
            TextView(this).apply {
                text = riga.testo.ifEmpty { "♪" }
                textSize = dimensione
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (nuovo.sincronizzato) SPENTA else Color.WHITE)
                setPadding(0, dp(10), 0, dp(10))
                if (nuovo.sincronizzato) {
                    setOnClickListener {
                        lettore?.vaiA(riga.ms)
                        seguendo = true
                        segui.visibility = View.GONE
                    }
                }
            }.also { colonnaRighe.addView(it) }
        }
        scorrimento.scrollTo(0, 0)
        daCentrare = true
        aggiornaRiga()
    }

    private fun aggiornaRiga() {
        val attuale = caricato ?: return
        if (!attuale.sincronizzato) return
        val posizione = lettore?.posizioneMs() ?: return
        val indice = FonteTesti.rigaCorrente(attuale.righe, posizione)
        if (daCentrare && seguendo && indice == corrente && righe.firstOrNull()?.isLaidOut == true) {
            daCentrare = false
            centra(false)
        }
        if (indice == corrente) return
        righe.getOrNull(corrente)?.setTextColor(SPENTA)
        righe.getOrNull(indice)?.setTextColor(Color.WHITE)
        corrente = indice
        if (seguendo) centra(true)
    }

    private fun centra(animato: Boolean) {
        val riga = righe.getOrNull(corrente.coerceAtLeast(0)) ?: return
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
        scorrimento.post { centra(false) }
    }

    private companion object {
        const val PREFERENZE = "testi"
        const val CHIAVE_DIMENSIONE = "dimensione"
        const val DIMENSIONE_PREDEFINITA = 34f
        const val DIMENSIONE_MINIMA = 22f
        const val DIMENSIONE_MASSIMA = 58f
        const val PASSO = 4f
        const val INTERVALLO_MS = 200L
        const val SPENTA = 0xFF5E636C.toInt()
    }
}
