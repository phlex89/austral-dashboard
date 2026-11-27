package app.australdashboard.funzioni

import android.content.Context
import java.io.File
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

data class Rifornimento(val quando: Long, val euro: Double, val litri: Double, val km: Int, val pieno: Boolean = true)

data class Tratta(val km: Int, val litri: Double, val euro: Double) {
    val consumo: Double get() = km / litri
    val costoCentoKm: Double get() = euro / km * 100
}

data class Totali(val km: Int, val euro: Double, val litri: Double) {
    val euroAlLitro: Double get() = euro / litri
}

data class Mese(val anno: Int, val mese: Int, val spesa: Double, val km: Int)

data class Pieno(val litri: Double = 40.0, val litriAlKm: Double = 0.06)

object Rifornimenti {
    private const val FILE = "rifornimenti.txt"
    private const val PIENI_PER_MEDIA = 5

    fun elenco(context: Context): List<Rifornimento> {
        val file = File(context.filesDir, FILE)
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { daRiga(it) }
    }

    @Synchronized
    fun aggiungi(context: Context, r: Rifornimento) = salva(context, elenco(context) + r)

    @Synchronized
    fun togli(context: Context, quando: Long) = salva(context, elenco(context).filter { it.quando != quando })

    private fun salva(context: Context, voci: List<Rifornimento>) {
        val file = File(context.filesDir, FILE)
        val temporaneo = File(context.filesDir, "$FILE.tmp")
        temporaneo.writeText(voci.joinToString("") { riga(it) + "\n" })
        if (!temporaneo.renameTo(file)) error("Salvataggio non riuscito")
    }

    fun riga(r: Rifornimento): String =
        String.format(Locale.US, "%d\t%.2f\t%.2f\t%d\t%d", r.quando, r.euro, r.litri, r.km, if (r.pieno) 1 else 0)

    fun daRiga(riga: String): Rifornimento? {
        val campi = riga.split('\t')
        if (campi.size != 4 && campi.size != 5) return null
        val pieno = if (campi.size == 5) when (campi[4]) {
            "1" -> true
            "0" -> false
            else -> return null
        } else true
        return Rifornimento(
            campi[0].toLongOrNull() ?: return null,
            campi[1].toDoubleOrNull() ?: return null,
            campi[2].toDoubleOrNull() ?: return null,
            campi[3].toIntOrNull() ?: return null,
            pieno,
        )
    }

    fun numero(testo: String): Double? {
        val valore = testo.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return if (valore > 0 && valore.isFinite()) valore else null
    }

    fun spesaMese(elenco: List<Rifornimento>, anno: Int, mese: Int): Double =
        elenco.filter {
            val data = Calendar.getInstance().apply { timeInMillis = it.quando }
            data.get(Calendar.YEAR) == anno && data.get(Calendar.MONTH) + 1 == mese
        }.sumOf { it.euro }

    fun spesaDelMese(elenco: List<Rifornimento>, anno: Int, mese: Int): String? {
        val spesa = spesaMese(elenco, anno, mese)
        if (spesa <= 0) return null
        val nome = Month.of(mese).getDisplayName(TextStyle.FULL, Locale.ITALIAN)
        return String.format(Locale.ITALY, "%.0f € a %s", spesa, nome)
    }

    fun tratte(elenco: List<Rifornimento>): List<Tratta> {
        val ordinati = elenco.sortedBy { it.km }
        val pieni = ordinati.filter { it.pieno }
        return pieni.zipWithNext().mapNotNull { (da, a) ->
            val km = a.km - da.km
            if (km <= 0) return@mapNotNull null
            val dentro = ordinati.filter { it.km > da.km && it.km <= a.km }
            Tratta(km, dentro.sumOf { it.litri }, dentro.sumOf { it.euro })
        }
    }

    fun consumo(elenco: List<Rifornimento>): Double? {
        val tratte = tratte(elenco).ifEmpty { return null }
        return tratte.sumOf { it.km } / tratte.sumOf { it.litri }
    }

    fun pieno(elenco: List<Rifornimento>): Pieno {
        val ultimi = elenco.filter { it.pieno && it.litri > 0 }.sortedBy { it.km }.takeLast(PIENI_PER_MEDIA)
        val predefinito = Pieno()
        return Pieno(
            litri = if (ultimi.isEmpty()) predefinito.litri else ultimi.sumOf { it.litri } / ultimi.size,
            litriAlKm = consumo(elenco)?.let { 1 / it } ?: predefinito.litriAlKm,
        )
    }

    fun costoCentoKm(elenco: List<Rifornimento>): Double? {
        val tratte = tratte(elenco).ifEmpty { return null }
        return tratte.sumOf { it.euro } / tratte.sumOf { it.km } * 100
    }

    fun totali(elenco: List<Rifornimento>): Totali? {
        if (elenco.isEmpty()) return null
        return Totali(
            elenco.maxOf { it.km } - elenco.minOf { it.km },
            elenco.sumOf { it.euro },
            elenco.sumOf { it.litri },
        )
    }

    fun mesi(elenco: List<Rifornimento>): List<Mese> {
        val gruppi = elenco.groupBy {
            val data = Calendar.getInstance().apply { timeInMillis = it.quando }
            data.get(Calendar.YEAR) * 12 + data.get(Calendar.MONTH)
        }.toSortedMap()
        var precedente: Int? = null
        val risultato = gruppi.map { (chiave, voci) ->
            val ultimo = voci.maxOf { it.km }
            val km = ultimo - (precedente ?: voci.minOf { it.km })
            precedente = ultimo
            Mese(chiave / 12, chiave % 12 + 1, voci.sumOf { it.euro }, km)
        }
        return risultato.reversed()
    }
}
