package app.australdashboard

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.Rifornimenti
import app.australdashboard.funzioni.Rifornimento
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class RifornimentiActivity : Schermata() {
    private lateinit var euro: EditText
    private lateinit var litri: EditText
    private lateinit var km: EditText
    private lateinit var messaggio: TextView
    private lateinit var corpo: LinearLayout
    private lateinit var pieno: Riga
    private var pienoCompleto = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contenuto = pagina("Rifornimenti")
        val decimale = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        euro = campo("Euro", decimale)
        litri = campo("Litri", decimale)
        km = campo("Contachilometri", InputType.TYPE_CLASS_NUMBER)
        val campi = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        val prezzo = intent.getDoubleExtra("prezzo", 0.0)
        val distributore = intent.getStringExtra("distributore")
        if (prezzo > 0 && distributore != null) {
            campi.addView(secondario(String.format(Locale.ITALY, "Prezzo %.3f €/l da %s", prezzo, distributore)).apply {
                setPadding(dp(8), dp(8), dp(8), 0)
            })
            euro.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    val valore = Rifornimenti.numero(s.toString()) ?: return
                    litri.setText(String.format(Locale.ITALY, "%.2f", valore / prezzo))
                }
            })
        }
        campi.addView(euro)
        campi.addView(litri)
        campi.addView(km)
        pieno = riga("Pieno completo", "Sì", freccia = false) { alternaPieno() }
        val nuovo = scheda().apply {
            addView(campi)
            addView(pieno.view)
        }
        messaggio = secondario("")
        corpo = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        contenuto.addView(sezione("Nuovo rifornimento"))
        contenuto.addView(nuovo)
        contenuto.addView(pulsante("Salva") { salva() })
        contenuto.addView(messaggio)
        contenuto.addView(corpo)
        aggiorna()
    }

    private fun alternaPieno() {
        pienoCompleto = !pienoCompleto
        pieno.valore.text = if (pienoCompleto) "Sì" else "No"
    }

    private fun salva() {
        val valoreEuro = Rifornimenti.numero(euro.text.toString())
        val valoreLitri = Rifornimenti.numero(litri.text.toString())
        val valoreKm = Rifornimenti.numero(km.text.toString())?.toInt()?.takeIf { it > 0 }
        val errore = when {
            valoreEuro == null -> "Controlla gli euro"
            valoreLitri == null -> "Controlla i litri"
            valoreKm == null -> "Controlla il contachilometri"
            else -> null
        }
        if (errore != null) {
            messaggio.text = errore
            return
        }
        Rifornimenti.aggiungi(this, Rifornimento(System.currentTimeMillis(), valoreEuro!!, valoreLitri!!, valoreKm!!, pienoCompleto))
        euro.text.clear()
        litri.text.clear()
        km.text.clear()
        messaggio.text = "Salvato"
        getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(km.windowToken, 0)
        km.clearFocus()
        aggiorna()
    }

    private fun aggiorna() {
        val tutti = Rifornimenti.elenco(this)
        km.hint = tutti.maxByOrNull { it.km }?.let { "Contachilometri (ultimo: ${it.km})" } ?: "Contachilometri"
        corpo.removeAllViews()
        corpo.addView(sezione("Riepilogo"))
        corpo.addView(riepilogo(tutti))
        val tratte = Rifornimenti.tratte(tutti)
        if (tratte.isNotEmpty()) {
            corpo.addView(sezione("Consumo (km/l)"))
            corpo.addView(scheda().apply {
                setPadding(dp(16), dp(16), dp(16), dp(16))
                addView(GraficoConsumo(this@RifornimentiActivity, tratte.takeLast(GRAFICO).map { it.consumo }, accento))
            })
        }
        val mesi = Rifornimenti.mesi(tutti)
        if (mesi.isNotEmpty()) {
            corpo.addView(sezione("Mese per mese"))
            corpo.addView(scheda().apply {
                mesi.forEach { m ->
                    val nome = Month.of(m.mese).getDisplayName(TextStyle.FULL, Locale.ITALIAN)
                    val valore = String.format(Locale.ITALY, "%.2f €  ·  %,d km", m.spesa, m.km)
                    addView(riga("$nome ${m.anno}", valore).view)
                }
            })
        }
        if (tutti.isNotEmpty()) {
            corpo.addView(sezione("Ultimi rifornimenti"))
            corpo.addView(scheda().apply {
                tutti.sortedByDescending { it.quando }.take(ULTIMI).forEach { addView(voce(it)) }
            })
        }
    }

    private fun riepilogo(tutti: List<Rifornimento>): LinearLayout {
        val carta = scheda()
        val totali = Rifornimenti.totali(tutti)
        if (totali == null) {
            carta.addView(riga("Nessun rifornimento", "Aggiungi il primo qui sopra", freccia = false).view)
            return carta
        }
        carta.addView(riga("Percorsi", String.format(Locale.ITALY, "%,d km", totali.km)).view)
        carta.addView(riga("Speso", String.format(Locale.ITALY, "%.2f € per %.2f l", totali.euro, totali.litri)).view)
        carta.addView(riga("Prezzo medio", String.format(Locale.ITALY, "%.3f €/l", totali.euroAlLitro)).view)
        val consumo = Rifornimenti.consumo(tutti)
        val costo = Rifornimenti.costoCentoKm(tutti)
        if (consumo == null || costo == null) {
            carta.addView(riga("Servono almeno due pieni completi", freccia = false).view)
            return carta
        }
        val consumi = Rifornimenti.tratte(tutti).map { it.consumo }
        carta.addView(riga("Consumo medio", String.format(Locale.ITALY, "%.1f km/l", consumo)).view)
        carta.addView(riga("Costo medio", String.format(Locale.ITALY, "%.2f € ogni 100 km", costo)).view)
        carta.addView(riga("Ultimo", String.format(Locale.ITALY, "%.1f km/l", consumi.last())).view)
        carta.addView(riga("Migliore", String.format(Locale.ITALY, "%.1f km/l", consumi.max())).view)
        carta.addView(riga("Peggiore", String.format(Locale.ITALY, "%.1f km/l", consumi.min())).view)
        return carta
    }

    private fun voce(r: Rifornimento): View {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        val dettaglio = String.format(Locale.ITALY, "%.2f €  ·  %.2f l  ·  %,d km", r.euro, r.litri, r.km) +
            if (r.pieno) "" else "  ·  parziale"
        val riga = riga(formato.format(Date(r.quando)), dettaglio)
        val togli = TextView(this).apply {
            text = TOGLI
            textSize = 20f
            setTextColor(accento)
            gravity = Gravity.CENTER
            minHeight = dp(64)
            setPadding(dp(16), 0, dp(8), 0)
        }
        val ripristina = Runnable { togli.text = TOGLI }
        togli.setOnClickListener {
            if (togli.text == TOGLI) {
                togli.text = CONFERMA
                togli.postDelayed(ripristina, 3000)
            } else {
                togli.removeCallbacks(ripristina)
                Rifornimenti.togli(this, r.quando)
                aggiorna()
            }
        }
        riga.view.addView(togli)
        return riga.view
    }

    private companion object {
        const val ULTIMI = 20
        const val GRAFICO = 12
        const val TOGLI = "Togli"
        const val CONFERMA = "Tocca ancora per togliere"
    }
}
