package app.australdashboard

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Patterns

class TraduzioniActivity : Schermata() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = pagina("Traduzione dei testi")
        val preferenze = getSharedPreferences(TestoActivity.PREFERENZE, Context.MODE_PRIVATE)
        radice.addView(secondario("Le traduzioni arrivano da MyMemory: senza email circa 3 brani al giorno, con l'email circa 30. L'email resta su questo box e va solo a MyMemory."))
        val email = campo("Email (facoltativa)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS).apply {
            setText(preferenze.getString(TestoActivity.CHIAVE_EMAIL, ""))
        }
        radice.addView(email)
        radice.addView(pulsante("Salva") {
            val scritta = email.text.toString().trim()
            if (scritta.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(scritta).matches()) {
                email.error = "Email non valida"
                return@pulsante
            }
            preferenze.edit().putString(TestoActivity.CHIAVE_EMAIL, scritta).apply()
            finish()
        })
    }
}
