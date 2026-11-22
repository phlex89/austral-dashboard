package app.australdashboard

import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import app.australdashboard.funzioni.Rifornimenti
import app.australdashboard.funzioni.Rifornimento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RifornimentiActivity : Schermata() {
    private lateinit var euro: EditText
    private lateinit var litri: EditText
    private lateinit var km: EditText
    private lateinit var messaggio: TextView
    private lateinit var riepilogo: TextView
    private lateinit var voci: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = colonna()
        radice.addView(testo("Rifornimenti", 40f))
        euro = campo("Euro", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        litri = campo("Litri", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        km = campo("Contachilometri", InputType.TYPE_CLASS_NUMBER)
        radice.addView(euro)
        radice.addView(litri)
        radice.addView(km)
        radice.addView(pulsante("Salva") { salva() })
        messaggio = testo("")
        radice.addView(messaggio)
        riepilogo = testo("")
        radice.addView(riepilogo)
        voci = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        radice.addView(voci)
        setContentView(scorrevole(radice))
        aggiorna()
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
        Rifornimenti.aggiungi(this, Rifornimento(System.currentTimeMillis(), valoreEuro!!, valoreLitri!!, valoreKm!!))
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
        val adesso = Calendar.getInstance()
        val spesa = Rifornimenti.spesaMese(tutti, adesso.get(Calendar.YEAR), adesso.get(Calendar.MONTH) + 1)
        val consumo = Rifornimenti.consumo(tutti)
        val costo = Rifornimenti.costoCentoKm(tutti)
        riepilogo.text = "Spesa di questo mese: " + String.format(Locale.ITALY, "%.2f €", spesa) + "\n" +
            if (consumo == null || costo == null) {
                "Servono almeno due pieni"
            } else {
                String.format(Locale.ITALY, "Consumo medio: %.1f km/l\nCosto: %.2f € ogni 100 km", consumo, costo)
            }
        km.hint = tutti.maxByOrNull { it.km }?.let { "Contachilometri (ultimo: ${it.km})" } ?: "Contachilometri"
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        voci.removeAllViews()
        tutti.sortedByDescending { it.quando }.take(ULTIMI).forEach { r ->
            voci.addView(testo(
                formato.format(Date(r.quando)) + "  " + String.format(Locale.ITALY, "%.2f €  %.2f l  ", r.euro, r.litri) +
                    "${r.km} km"
            ))
            voci.addView(pulsante("Togli") {
                Rifornimenti.togli(this, r.quando)
                aggiorna()
            })
        }
    }

    private companion object {
        const val ULTIMI = 20
    }
}
