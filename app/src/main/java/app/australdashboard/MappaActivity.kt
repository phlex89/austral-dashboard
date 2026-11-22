package app.australdashboard

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.ArchivioTessere
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.Distributore
import app.australdashboard.funzioni.ElencoBenzinai
import app.australdashboard.funzioni.Luogo
import app.australdashboard.funzioni.Mercatore
import app.australdashboard.funzioni.Ordine
import app.australdashboard.funzioni.Posizione

class MappaActivity : Schermata() {
    private lateinit var mappa: VistaMappa
    private lateinit var messaggio: TextView
    private lateinit var scheda: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intornoAMe = intent.getBooleanExtra("intornoAMe", false)
        mappa = VistaMappa(this)
        messaggio = TextView(this).apply {
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
            text = "Cerco i distributori…"
        }
        scheda = scheda().apply {
            setPadding(dp(24), dp(16), dp(24), dp(16))
            visibility = View.GONE
        }
        mappa.alSelezionare = { mostraScheda(it) }

        val comandi = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(comando("+") { mappa.zoom(1) })
            addView(comando("−") { mappa.zoom(-1) })
            addView(comando("Centra") { mappa.centra() })
        }
        val area = FrameLayout(this).apply {
            addView(mappa, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(comandi, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END or Gravity.TOP)
                .apply { setMargins(0, dp(16), dp(16), 0) })
            addView(scheda, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
                .apply { setMargins(dp(16), 0, dp(16), dp(40)) })
            addView(messaggio, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(SFONDO)
            addView(intestazione("Mappa dei benzinai"))
            addView(area, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        })
        carica(intornoAMe)
    }

    private fun comando(etichetta: String, azione: () -> Unit) = pulsante(etichetta, true, azione).apply {
        minWidth = dp(88)
        setPadding(dp(8), dp(8), dp(8), dp(8))
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(88)).apply { setMargins(0, dp(6), 0, dp(6)) }
    }

    private fun carica(intornoAMe: Boolean) {
        val contesto = applicationContext
        Thread {
            val luogo = Posizione.attuale(contesto)
            val elenco = if (luogo == null) null else Benzinai.elenco(contesto, luogo, Ordine.PREZZO, intornoAMe)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                when {
                    luogo == null || elenco is ElencoBenzinai.SenzaPosizione -> messaggio.text = "Posizione non disponibile."
                    elenco is ElencoBenzinai.Errore -> messaggio.text = elenco.messaggio
                    elenco is ElencoBenzinai.Pronto -> {
                        messaggio.visibility = View.GONE
                        mappa.imposta(luogo, elenco.raggioKm, elenco.preferiti + elenco.vicini)
                    }
                    else -> messaggio.text = "Prezzi non disponibili."
                }
            }
        }.start()
    }

    private fun mostraScheda(d: Distributore?) {
        scheda.removeAllViews()
        if (d == null) {
            scheda.visibility = View.GONE
            return
        }
        val titolo = if (d.marchio.isBlank()) d.nome else "${d.nome} · ${d.marchio}"
        scheda.addView(testo(titolo, 24f).apply { typeface = Typeface.DEFAULT_BOLD })
        scheda.addView(secondario(d.indirizzo))
        scheda.addView(secondario("${d.comune} · ${Benzinai.distanza(d.distanzaKm)}"))
        scheda.addView(testo("${Benzinai.prezzo(d.prezzo)} €/l", 30f).apply { setTextColor(accento) })
        scheda.addView(pulsante("Rifornimenti") {
            startActivity(
                Intent(this, RifornimentiActivity::class.java)
                    .putExtra("prezzo", d.prezzo)
                    .putExtra("distributore", d.nome.ifBlank { d.marchio }),
            )
        })
        scheda.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        mappa.chiudi()
        super.onDestroy()
    }

    private inner class VistaMappa(context: Context) : View(context) {
        private val archivio = ArchivioTessere(context) { postInvalidate() }
        private var utente: Luogo? = null
        private var raggioKm = 10.0
        private var distributori = emptyList<Distributore>()
        private var economico: Distributore? = null
        private var selezionato: Distributore? = null
        private var zoom = 0
        private var centroX = 0.0
        private var centroY = 0.0
        private var inquadrato = false
        private var inizioX = 0f
        private var inizioY = 0f
        private var ultimoX = 0f
        private var ultimoY = 0f
        private var trascinato = false
        private val soglia = ViewConfiguration.get(context).scaledTouchSlop
        private val etichette = ArrayList<Pair<RectF, Distributore>>()
        var alSelezionare: (Distributore?) -> Unit = {}

        private val pennello = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pennelloTessere = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix(floatArrayOf(-1f, 0f, 0f, 0f, 255f, 0f, -1f, 0f, 0f, 255f, 0f, 0f, -1f, 0f, 255f, 0f, 0f, 0f, 1f, 0f)).apply {
                    postConcat(
                        ColorMatrix(
                            floatArrayOf(
                                -0.574f, 1.430f, 0.144f, 0f, 0f,
                                0.426f, 0.430f, 0.144f, 0f, 0f,
                                0.426f, 1.430f, -0.856f, 0f, 0f,
                                0f, 0f, 0f, 1f, 0f,
                            ),
                        ),
                    )
                    postConcat(ColorMatrix().apply { setScale(0.8f, 0.8f, 0.85f, 1f) })
                },
            )
        }
        private val testoEtichetta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = dp(22).toFloat()
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        private val testoAttribuzione = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = dp(14).toFloat()
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
        }

        fun imposta(posizione: Luogo, raggio: Double, elenco: List<Distributore>) {
            utente = posizione
            raggioKm = raggio
            distributori = elenco
            economico = elenco.minByOrNull { it.prezzo }
            inquadrato = false
            inquadra()
            invalidate()
        }

        fun zoom(passo: Int) {
            if (zoom == 0) return
            val nuovo = (zoom + passo).coerceIn(Mercatore.ZOOM_MIN, Mercatore.ZOOM_MAX)
            if (nuovo == zoom) return
            val lat = Mercatore.lat(centroY, zoom)
            val lon = Mercatore.lon(centroX, zoom)
            zoom = nuovo
            centroX = Mercatore.pixelX(lon, zoom)
            centroY = Mercatore.pixelY(lat, zoom)
            invalidate()
        }

        fun centra() {
            val posizione = utente ?: return
            if (zoom == 0) return
            centroX = Mercatore.pixelX(posizione.lon, zoom)
            centroY = Mercatore.pixelY(posizione.lat, zoom)
            invalidate()
        }

        fun chiudi() = archivio.chiudi()

        private fun inquadra() {
            val posizione = utente ?: return
            if (inquadrato || width == 0) return
            inquadrato = true
            zoom = Mercatore.zoomPer(raggioKm, posizione.lat, minOf(width, height))
            centra()
        }

        override fun onSizeChanged(w: Int, h: Int, vecchioW: Int, vecchioH: Int) {
            super.onSizeChanged(w, h, vecchioW, vecchioH)
            inquadra()
        }

        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(0xFF15171B.toInt())
            if (zoom == 0) return
            disegnaTessere(canvas)
            etichette.clear()
            utente?.let { disegnaUtente(canvas, it) }
            val puntini = ArrayList<Distributore>()
            distributori.sortedWith(compareBy<Distributore> { it.id != selezionato?.id }.thenBy { it.prezzo }).forEach { d ->
                val rettangolo = rettangoloDi(d) ?: return@forEach
                if (etichette.any { RectF.intersects(it.first, rettangolo) }) puntini.add(d) else etichette.add(rettangolo to d)
            }
            puntini.forEach { disegnaPuntino(canvas, it) }
            etichette.forEach { (rettangolo, d) -> disegnaEtichetta(canvas, rettangolo, d) }
            val testo = "© OpenStreetMap"
            val larghezza = testoAttribuzione.measureText(testo)
            pennello.color = 0xAA000000.toInt()
            pennello.style = Paint.Style.FILL
            canvas.drawRect(width - larghezza - dp(16), height - dp(28).toFloat(), width.toFloat(), height.toFloat(), pennello)
            canvas.drawText(testo, width - dp(8).toFloat(), height - dp(8).toFloat(), testoAttribuzione)
        }

        private fun disegnaTessere(canvas: Canvas) {
            val sinistra = centroX - width / 2.0
            val alto = centroY - height / 2.0
            val massimo = (1 shl zoom) - 1
            val primoX = Math.floor(sinistra / Mercatore.LATO).toInt()
            val ultimoX = Math.floor((sinistra + width) / Mercatore.LATO).toInt()
            val primoY = Math.floor(alto / Mercatore.LATO).toInt().coerceAtLeast(0)
            val ultimoY = Math.floor((alto + height) / Mercatore.LATO).toInt().coerceAtMost(massimo)
            for (ty in primoY..ultimoY) {
                for (tx in primoX..ultimoX) {
                    val x = (tx * Mercatore.LATO - sinistra).toFloat()
                    val y = (ty * Mercatore.LATO - alto).toFloat()
                    val bitmap = archivio.cerca(zoom, Math.floorMod(tx, massimo + 1), ty)
                    if (bitmap != null) {
                        canvas.drawBitmap(bitmap, x, y, pennelloTessere)
                    } else {
                        pennello.style = Paint.Style.FILL
                        pennello.color = 0xFF1B1D22.toInt()
                        canvas.drawRect(x, y, x + Mercatore.LATO, y + Mercatore.LATO, pennello)
                        pennello.style = Paint.Style.STROKE
                        pennello.strokeWidth = 2f
                        pennello.color = 0xFF2A2D34.toInt()
                        canvas.drawRect(x, y, x + Mercatore.LATO, y + Mercatore.LATO, pennello)
                    }
                }
            }
        }

        private fun schermoX(lon: Double) = (Mercatore.pixelX(lon, zoom) - centroX + width / 2.0).toFloat()

        private fun schermoY(lat: Double) = (Mercatore.pixelY(lat, zoom) - centroY + height / 2.0).toFloat()

        private fun disegnaUtente(canvas: Canvas, posizione: Luogo) {
            val x = schermoX(posizione.lon)
            val y = schermoY(posizione.lat)
            pennello.style = Paint.Style.FILL
            pennello.color = Color.WHITE
            canvas.drawCircle(x, y, dp(14).toFloat(), pennello)
            pennello.color = 0xFF2A7FFF.toInt()
            canvas.drawCircle(x, y, dp(10).toFloat(), pennello)
        }

        private fun rettangoloDi(d: Distributore): RectF? {
            val x = schermoX(d.luogo.lon)
            val y = schermoY(d.luogo.lat)
            if (x < -dp(100) || x > width + dp(100) || y < -dp(100) || y > height + dp(100)) return null
            val mezza = testoEtichetta.measureText(Benzinai.prezzo(d.prezzo)) / 2 + dp(12)
            return RectF(x - mezza, y - dp(44).toFloat(), x + mezza, y - dp(8).toFloat())
        }

        private fun disegnaPuntino(canvas: Canvas, d: Distributore) {
            val x = schermoX(d.luogo.lon)
            val y = schermoY(d.luogo.lat) - dp(4)
            pennello.style = Paint.Style.FILL
            pennello.color = accento
            canvas.drawCircle(x, y, dp(7).toFloat(), pennello)
            pennello.style = Paint.Style.STROKE
            pennello.strokeWidth = dp(2).toFloat()
            pennello.color = Color.BLACK
            canvas.drawCircle(x, y, dp(7).toFloat(), pennello)
        }

        private fun disegnaEtichetta(canvas: Canvas, rettangolo: RectF, d: Distributore) {
            val x = schermoX(d.luogo.lon)
            val y = schermoY(d.luogo.lat)
            val testo = Benzinai.prezzo(d.prezzo)
            val migliore = d.id == economico?.id
            val scelto = d.id == selezionato?.id
            pennello.style = Paint.Style.FILL
            pennello.color = if (migliore) accento else SCHEDA
            canvas.drawRoundRect(rettangolo, dp(10).toFloat(), dp(10).toFloat(), pennello)
            pennello.style = Paint.Style.STROKE
            pennello.strokeWidth = dp(if (scelto) 4 else 2).toFloat()
            pennello.color = if (scelto) Color.WHITE else accento
            canvas.drawRoundRect(rettangolo, dp(10).toFloat(), dp(10).toFloat(), pennello)
            pennello.style = Paint.Style.FILL
            pennello.color = if (migliore) Color.BLACK else accento
            canvas.drawCircle(x, y - dp(4), dp(6).toFloat(), pennello)
            testoEtichetta.color = if (migliore) Color.BLACK else Color.WHITE
            canvas.drawText(testo, rettangolo.centerX(), rettangolo.centerY() + testoEtichetta.textSize / 3, testoEtichetta)
        }

        override fun onTouchEvent(evento: MotionEvent): Boolean {
            when (evento.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    inizioX = evento.x
                    inizioY = evento.y
                    ultimoX = evento.x
                    ultimoY = evento.y
                    trascinato = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!trascinato && Math.hypot((evento.x - inizioX).toDouble(), (evento.y - inizioY).toDouble()) > soglia) {
                        trascinato = true
                    }
                    if (trascinato && zoom != 0) {
                        centroX -= evento.x - ultimoX
                        centroY -= evento.y - ultimoY
                        invalidate()
                    }
                    ultimoX = evento.x
                    ultimoY = evento.y
                }
                MotionEvent.ACTION_UP -> if (!trascinato) tocca(evento.x, evento.y)
            }
            return true
        }

        private fun tocca(x: Float, y: Float) {
            val colpita = etichette.lastOrNull { it.first.contains(x, y) }?.second
            selezionato = colpita
            alSelezionare(colpita)
            invalidate()
        }
    }
}
