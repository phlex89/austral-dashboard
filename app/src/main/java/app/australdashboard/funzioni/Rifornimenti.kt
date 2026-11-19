package app.australdashboard.funzioni

import android.content.Context
import java.io.File
import java.util.Calendar
import java.util.Locale

data class Rifornimento(val quando: Long, val euro: Double, val litri: Double, val km: Int)

object Rifornimenti {
    private const val FILE = "rifornimenti.txt"

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
        String.format(Locale.US, "%d\t%.2f\t%.2f\t%d", r.quando, r.euro, r.litri, r.km)

    fun daRiga(riga: String): Rifornimento? {
        val campi = riga.split('\t')
        if (campi.size != 4) return null
        return Rifornimento(
            campi[0].toLongOrNull() ?: return null,
            campi[1].toDoubleOrNull() ?: return null,
            campi[2].toDoubleOrNull() ?: return null,
            campi[3].toIntOrNull() ?: return null,
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

    fun consumo(elenco: List<Rifornimento>): Double? {
        val (distanza, ordinati) = distanzaEResto(elenco) ?: return null
        return distanza / ordinati.sumOf { it.litri }
    }

    fun costoCentoKm(elenco: List<Rifornimento>): Double? {
        val (distanza, ordinati) = distanzaEResto(elenco) ?: return null
        return ordinati.sumOf { it.euro } / distanza * 100
    }

    private fun distanzaEResto(elenco: List<Rifornimento>): Pair<Double, List<Rifornimento>>? {
        if (elenco.size < 2) return null
        val ordinati = elenco.sortedBy { it.km }
        val distanza = (ordinati.last().km - ordinati.first().km).toDouble()
        if (distanza <= 0) return null
        return distanza to ordinati.drop(1)
    }
}
