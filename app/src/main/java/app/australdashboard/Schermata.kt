package app.australdashboard

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.australdashboard.aggiornamenti.EsitoInstallazioneReceiver
import app.australdashboard.temi.Temi

abstract class Schermata : Activity() {
    class Riga(val view: LinearLayout, val titolo: TextView, val valore: TextView)

    protected val accento: Int by lazy { Temi.scelto(this).palette(this).accento }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (gestoreInstallato) return
        gestoreInstallato = true
        val contesto = applicationContext
        val precedente = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { filo, errore ->
            runCatching {
                val versione = contesto.packageManager.getPackageInfo(contesto.packageName, 0).versionName
                Stato.salvaCrash(contesto, versione, Log.getStackTraceString(errore))
            }
            precedente?.uncaughtException(filo, errore)
        }
    }

    override fun onStart() {
        super.onStart()
        Misure.schermateVisibili++
        EsitoInstallazioneReceiver.riprendi(this)
    }

    override fun onStop() {
        Misure.schermateVisibili--
        super.onStop()
    }

    protected fun dp(valore: Int): Int = (valore * resources.displayMetrics.density).toInt()

    protected fun pagina(titolo: String, destra: View? = null): LinearLayout {
        val contenuto = colonna()
        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(SFONDO)
            addView(intestazione(titolo, destra))
            addView(scorrevole(contenuto), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        setContentView(radice)
        return contenuto
    }

    protected fun intestazione(titolo: String, destra: View? = null): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), dp(12), dp(24), dp(12))
        setBackgroundColor(SFONDO)
        addView(ImageView(this@Schermata).apply {
            setImageResource(R.drawable.ic_indietro)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = ripple(null, dp(36).toFloat())
            contentDescription = "Indietro"
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(80), dp(80)))
        addView(TextView(this@Schermata).apply {
            text = titolo
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            isSingleLine = true
            setPadding(dp(8), 0, dp(8), 0)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (destra != null) addView(destra)
    }

    protected fun colonna(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(SFONDO)
        setPadding(dp(32), dp(8), dp(32), dp(32))
    }

    protected fun scorrevole(contenuto: LinearLayout): ScrollView = ScrollView(this).apply {
        setBackgroundColor(SFONDO)
        isFillViewport = true
        addView(contenuto)
    }

    protected fun sezione(titolo: String): TextView = TextView(this).apply {
        text = titolo.uppercase()
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.12f
        setTextColor(accento)
        setPadding(dp(8), dp(28), dp(8), dp(12))
    }

    protected fun scheda(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply {
            setColor(SCHEDA)
            cornerRadius = dp(16).toFloat()
        }
        clipToOutline = true
        dividerDrawable = GradientDrawable().apply {
            setColor(LINEA)
            setSize(1, dp(1).coerceAtLeast(1))
        }
        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    }

    protected fun riga(
        titolo: String,
        valore: String? = null,
        icona: Int? = null,
        freccia: Boolean = true,
        azione: (() -> Unit)? = null,
    ): Riga {
        val testoTitolo = TextView(this).apply {
            text = titolo
            textSize = 24f
            setTextColor(Color.WHITE)
        }
        val testoValore = TextView(this).apply {
            text = valore.orEmpty()
            textSize = 18f
            setTextColor(SECONDARIO)
            visibility = if (valore.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        val vista = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(88)
            setPadding(dp(20), dp(12), dp(20), dp(12))
            if (icona != null) {
                addView(ImageView(this@Schermata).apply {
                    setImageResource(icona)
                    imageTintList = ColorStateList.valueOf(accento)
                }, LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(20) })
            }
            addView(LinearLayout(this@Schermata).apply {
                orientation = LinearLayout.VERTICAL
                addView(testoTitolo)
                addView(testoValore)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            if (azione != null && freccia) {
                addView(TextView(this@Schermata).apply {
                    text = "›"
                    textSize = 36f
                    setTextColor(SECONDARIO)
                    setPadding(dp(16), 0, 0, dp(4))
                })
            }
            if (azione != null) {
                background = ripple(null, 0f)
                setOnClickListener { azione() }
            }
        }
        return Riga(vista, testoTitolo, testoValore)
    }

    protected fun rigaTogli(titolo: String, valore: String? = null, azione: () -> Unit): Riga {
        var armata = false
        lateinit var riga: Riga
        val ripristina = Runnable {
            armata = false
            riga.titolo.text = titolo
            riga.titolo.setTextColor(Color.WHITE)
        }
        riga = riga(titolo, valore, R.drawable.ic_chiudi, false) {
            if (armata) {
                riga.view.removeCallbacks(ripristina)
                azione()
            } else {
                armata = true
                riga.titolo.text = "Tocca ancora per togliere"
                riga.titolo.setTextColor(accento)
                riga.view.postDelayed(ripristina, 3000)
            }
        }
        return riga
    }

    protected fun testo(contenuto: String, dimensione: Float = 24f): TextView = TextView(this).apply {
        text = contenuto
        textSize = dimensione
        setTextColor(Color.WHITE)
        setPadding(0, dp(8), 0, dp(8))
    }

    protected fun secondario(contenuto: String, dimensione: Float = 18f): TextView =
        testo(contenuto, dimensione).apply { setTextColor(SECONDARIO) }

    protected fun pulsante(etichetta: String, secondario: Boolean = false, azione: () -> Unit): Button = Button(this).apply {
        text = etichetta
        textSize = 26f
        isAllCaps = false
        stateListAnimator = null
        minHeight = dp(88)
        setPadding(dp(24), dp(12), dp(24), dp(12))
        val raggio = dp(14).toFloat()
        val fondo = GradientDrawable().apply {
            cornerRadius = raggio
            if (secondario) {
                setColor(SCHEDA)
                setStroke(dp(2), accento)
            } else {
                setColor(accento)
            }
        }
        background = ripple(fondo, raggio)
        setTextColor(if (secondario) Color.WHITE else testoSu(accento))
        setOnClickListener { azione() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { setMargins(0, dp(8), 0, dp(8)) }
    }

    protected fun campo(suggerimento: String, tipo: Int = InputType.TYPE_CLASS_TEXT): EditText = EditText(this).apply {
        hint = suggerimento
        textSize = 26f
        setTextColor(Color.WHITE)
        setHintTextColor(SECONDARIO)
        setSingleLine()
        inputType = tipo
        if (tipo and InputType.TYPE_NUMBER_FLAG_DECIMAL != 0) {
            keyListener = DigitsKeyListener.getInstance("0123456789.,")
            setRawInputType(tipo)
        }
        minHeight = dp(88)
        setPadding(dp(20), dp(12), dp(20), dp(12))
        background = GradientDrawable().apply {
            setColor(SCHEDA)
            cornerRadius = dp(14).toFloat()
            setStroke(dp(1).coerceAtLeast(1), LINEA)
        }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { setMargins(0, dp(8), 0, dp(8)) }
    }

    private fun ripple(contenuto: Drawable?, raggio: Float): RippleDrawable {
        val maschera = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = raggio
        }
        return RippleDrawable(ColorStateList.valueOf(0x33FFFFFF), contenuto, if (contenuto == null && raggio == 0f) ColorDrawable(Color.WHITE) else maschera)
    }

    private fun testoSu(sfondo: Int): Int {
        val luce = (0.299 * Color.red(sfondo) + 0.587 * Color.green(sfondo) + 0.114 * Color.blue(sfondo)) / 255
        return if (luce > 0.6) Color.BLACK else Color.WHITE
    }

    protected companion object {
        const val SFONDO = 0xFF0E0F12.toInt()
        const val SCHEDA = 0xFF1B1D22.toInt()
        const val LINEA = 0xFF2A2D34.toInt()
        const val SECONDARIO = 0xFF9AA0AA.toInt()
        private var gestoreInstallato = false
    }
}
