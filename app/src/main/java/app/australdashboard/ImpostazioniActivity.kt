package app.australdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import app.australdashboard.aggiornamenti.Aggiornatore
import app.australdashboard.funzioni.Benzinai
import app.australdashboard.funzioni.Lettore
import app.australdashboard.funzioni.Playlists
import app.australdashboard.funzioni.Luoghi
import app.australdashboard.temi.Temi

class ImpostazioniActivity : Schermata() {
    private lateinit var tema: Riga
    private lateinit var luogo: Riga
    private lateinit var comuni: Riga
    private lateinit var permessoMusica: Riga
    private lateinit var playlist: Riga
    private lateinit var traduzioni: Riga
    private lateinit var aggiornamenti: Riga

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val radice = pagina("Impostazioni")
        radice.addView(sezione("Aspetto"))
        tema = riga("Tema", icona = R.drawable.ic_tavolozza) { vai(TemiActivity::class.java) }
        radice.addView(scheda().apply { addView(tema.view) })
        radice.addView(sezione("Home"))
        radice.addView(scheda().apply {
            addView(riga("App della Home", icona = R.drawable.ic_casa) { vai(AppHomeActivity::class.java) }.view)
        })
        radice.addView(sezione("Meteo"))
        luogo = riga("Luogo del meteo", icona = R.drawable.ic_puntina) { vai(LuoghiActivity::class.java) }
        radice.addView(scheda().apply { addView(luogo.view) })
        radice.addView(sezione("Benzinai"))
        comuni = riga("Comuni preferiti", icona = R.drawable.ic_benzinaio) { vai(ComuniActivity::class.java) }
        radice.addView(scheda().apply { addView(comuni.view) })
        radice.addView(sezione("Musica"))
        permessoMusica = riga("Consenti la lettura della musica", icona = R.drawable.ic_musica) { Lettore.apriPermesso(this) }
        playlist = riga("Playlist preferite", icona = R.drawable.ic_play) { vai(PlaylistActivity::class.java) }
        traduzioni = riga("Traduzione dei testi", icona = R.drawable.ic_testo) { vai(TraduzioniActivity::class.java) }
        radice.addView(scheda().apply {
            addView(permessoMusica.view)
            addView(playlist.view)
            addView(traduzioni.view)
        })
        radice.addView(sezione("Sistema e aggiornamenti"))
        aggiornamenti = riga("Controlla aggiornamenti", icona = R.drawable.ic_aggiorna, freccia = false) { controllaOra() }
        radice.addView(scheda().apply {
            addView(aggiornamenti.view)
            addView(riga("Diagnostica", icona = R.drawable.ic_info) { vai(DiagnosticaActivity::class.java) }.view)
            addView(riga("Impostazioni di sistema", icona = R.drawable.ic_ingranaggio) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }.view)
            addView(riga("Cambia launcher predefinito", icona = R.drawable.ic_griglia) {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }.view)
        })
        val versione = packageManager.getPackageInfo(packageName, 0).versionName
        radice.addView(secondario("Austral Dashboard $versione").apply { setPadding(dp(8), dp(28), 0, 0) })
    }

    override fun onResume() {
        super.onResume()
        mostra(tema, Temi.scelto(this).nome)
        mostra(luogo, Luoghi.attivo(this)?.first ?: Luoghi.NOME_GPS)
        mostra(comuni, Benzinai.preferiti(this).joinToString(", ").ifEmpty { "nessuno" })
        permessoMusica.view.visibility = if (Lettore.permessoConcesso(this)) View.GONE else View.VISIBLE
        mostra(playlist, Playlists.elenco(this).joinToString(", ") { it.nome }.ifEmpty { "nessuna" })
        val email = getSharedPreferences(TestoActivity.PREFERENZE, Context.MODE_PRIVATE).getString(TestoActivity.CHIAVE_EMAIL, "").orEmpty()
        mostra(traduzioni, email.ifEmpty { "senza email, circa 3 brani al giorno" })
        mostra(aggiornamenti, Stato.esito(this))
    }

    private fun mostra(riga: Riga, valore: String) {
        riga.valore.text = valore
        riga.valore.visibility = if (valore.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun vai(destinazione: Class<*>) = startActivity(Intent(this, destinazione))

    private fun controllaOra() {
        mostra(aggiornamenti, "Controllo in corso…")
        Thread {
            val occupato = Aggiornatore.controlla(applicationContext) == Aggiornatore.Esito.OCCUPATO
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                mostra(aggiornamenti, if (occupato) "Un controllo è già in corso" else Stato.esito(this))
            }
        }.start()
    }
}
