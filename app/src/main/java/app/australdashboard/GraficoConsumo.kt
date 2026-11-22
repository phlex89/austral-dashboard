package app.australdashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import java.util.Locale

class GraficoConsumo(context: Context, private val valori: List<Double>, accento: Int) : View(context) {
    private val densita = resources.displayMetrics.density
    private val barra = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accento }
    private val etichetta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 16f * densita
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val rettangolo = RectF()

    override fun onMeasure(larghezza: Int, altezza: Int) {
        setMeasuredDimension(getDefaultSize(0, larghezza), (220 * densita).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        if (valori.isEmpty()) return
        val massimo = valori.max()
        val area = height - 28f * densita
        val passo = width.toFloat() / valori.size
        val spessore = passo * 0.6f
        valori.forEachIndexed { i, valore ->
            val centro = passo * i + passo / 2
            val altezzaBarra = (area * (valore / massimo)).toFloat().coerceAtLeast(2f * densita)
            rettangolo.set(centro - spessore / 2, height - altezzaBarra, centro + spessore / 2, height.toFloat())
            canvas.drawRoundRect(rettangolo, 4f * densita, 4f * densita, barra)
            canvas.drawText(
                String.format(Locale.ITALY, "%.1f", valore),
                centro,
                height - altezzaBarra - 6f * densita,
                etichetta,
            )
        }
    }
}
