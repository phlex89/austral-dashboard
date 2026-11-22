package app.australdashboard.temi

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.R
import app.australdashboard.funzioni.Ordine

object Ui {
    private val font = HashMap<Int, Typeface>()

    fun font(context: Context, id: Int): Typeface = font.getOrPut(id) { context.resources.getFont(id) }

    const val POSIZIONE_NON_DISPONIBILE =
        "Posizione non disponibile. Salva un luogo in Impostazioni → Luogo del meteo: il primo fa da riserva."

    val ORDINI = listOf(Ordine.CONVIENE to "Conviene", Ordine.PREZZO to "Prezzo", Ordine.DISTANZA to "Distanza")

    fun frazione(gradi: Int, minima: Int, massima: Int): Float =
        ((gradi - minima).toFloat() / (massima - minima)).coerceIn(0f, 1f)

    val mono: Typeface = Typeface.MONOSPACE
    val monoGrassetto: Typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

    fun bordo(colore: Int, spessore: Int, sfondo: Int = Color.TRANSPARENT): GradientDrawable =
        GradientDrawable().apply {
            setColor(sfondo)
            if (spessore > 0) setStroke(spessore, colore)
        }

    fun pieno(colore: Int): Drawable = ColorDrawable(colore)

    fun testo(
        context: Context,
        px: Int,
        colore: Int,
        font: Typeface,
        interlinea: Int = 0,
        contenuto: String = "",
        maiuscolo: Boolean = false,
        righe: Int = 1,
    ): TextView = TextView(context).apply {
        text = contenuto
        setTextSize(TypedValue.COMPLEX_UNIT_PX, px.toFloat())
        setTextColor(colore)
        typeface = font
        includeFontPadding = false
        maxLines = righe
        if (righe == 1) isSingleLine = true
        isAllCaps = maiuscolo
        ellipsize = TextUtils.TruncateAt.END
        if (interlinea > 0) lineHeight = interlinea
    }

    fun adatta(testo: TextView, minimo: Int, massimo: Int): TextView {
        testo.ellipsize = null
        testo.setHorizontallyScrolling(false)
        val naturale = testo.paint.getFontMetricsInt(null)
        if (naturale > 0) testo.setLineSpacing(0f, testo.lineHeight / naturale.toFloat())
        testo.setAutoSizeTextTypeUniformWithConfiguration(minimo, massimo, 2, TypedValue.COMPLEX_UNIT_PX)
        return testo
    }

    fun icona(context: Context, res: Int, colore: Int): ImageView = ImageView(context).apply {
        setImageResource(res)
        imageTintList = ColorStateList.valueOf(colore)
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    fun premibile(view: View, colore: Int, descrizione: String? = null, azione: () -> Unit): View {
        view.isClickable = true
        view.isFocusable = true
        view.foreground = RippleDrawable(ColorStateList.valueOf(colore), null, ColorDrawable(Color.WHITE))
        if (descrizione != null) view.contentDescription = descrizione
        view.setOnClickListener { azione() }
        return view
    }

    fun riga(context: Context, spazio: Int = 0, gravita: Int = Gravity.CENTER_VERTICAL): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = gravita
            if (spazio > 0) spaziatura(this, spazio)
        }

    fun colonna(context: Context, spazio: Int = 0, gravita: Int = Gravity.NO_GRAVITY): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = gravita
            if (spazio > 0) spaziatura(this, spazio)
        }

    private fun spaziatura(layout: LinearLayout, spazio: Int) {
        layout.dividerDrawable = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setSize(spazio, spazio)
        }
        layout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    }

    fun lp(larghezza: Int, altezza: Int, peso: Float = 0f): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(larghezza, altezza, peso)

    fun lpf(larghezza: Int, altezza: Int, gravita: Int = Gravity.NO_GRAVITY): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(larghezza, altezza, gravita)

    const val TUTTO = ViewGroup.LayoutParams.MATCH_PARENT
    const val ADATTA = ViewGroup.LayoutParams.WRAP_CONTENT

    fun senzaRitaglio(vararg gruppi: ViewGroup) {
        for (g in gruppi) {
            g.clipChildren = false
            g.clipToPadding = false
        }
    }

    fun iconaCielo(codice: Int): Int = when (app.australdashboard.funzioni.FonteMeteo.cielo(codice)) {
        app.australdashboard.funzioni.Cielo.SERENO -> R.drawable.ic_sereno
        app.australdashboard.funzioni.Cielo.POCO_NUVOLOSO -> R.drawable.ic_poco_nuvoloso
        app.australdashboard.funzioni.Cielo.NUVOLOSO -> R.drawable.ic_nuvoloso
        app.australdashboard.funzioni.Cielo.NEBBIA -> R.drawable.ic_nebbia
        app.australdashboard.funzioni.Cielo.PIOGGIA -> R.drawable.ic_pioggia
        app.australdashboard.funzioni.Cielo.NEVE -> R.drawable.ic_neve
        app.australdashboard.funzioni.Cielo.TEMPORALE -> R.drawable.ic_temporale
    }
}
