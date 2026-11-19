---
milestone: M03
stato: in-corso
data: 2026-11-19
lettori: entrambi
---

# M03 · Catena robusta e rifiniture

## In una pagina

Dopo una revisione del codice contro i requisiti di `CLAUDE.md`, si rinforza la catena di aggiornamento e si sistemano difetti e debiti trovati.

| Cosa | Perché |
|---|---|
| Aggiornatore in un processo suo | Una versione che si chiude all'avvio bloccava anche l'aggiornamento che l'avrebbe corretta |
| Permesso per aggiornare senza conferma | Senza, Android 12 chiede «Aggiornare?» a ogni versione |
| Controllo dopo 60 secondi e poi ogni 6 ore | Nei primi 30 secondi Android non installa da solo; e il box potrebbe risvegliarsi senza riavviarsi |
| Meteo che si spegne dopo 3 ore | La Home mostrava la temperatura di ieri come attuale |
| Ultimo crash in Diagnostica | Il box non è raggiungibile dal Mac |
| Benzinai più leggeri | Ogni cambio di ordinamento rileggeva tutto il file e ritentava il download |
| App della Home e comuni preferiti in Impostazioni | Erano fissi nel codice, e i comuni sono un dato personale in un repository pubblico |
| Font Rajdhani ridotti | APK da 690 a 454 KB |
| Guardia sulla firma in `release.sh` | Un APK firmato con un'altra chiave verrebbe pubblicato e rifiutato dal box |

Scartati dall'utente: interruttori per meteo e musica, «portami qui» nei Benzinai, azioni all'accensione.

Si chiude quando la versione è pubblicata, il box l'ha ricevuta e l'aggiornamento successivo è arrivato senza conferma.

## Per chi esegue

**Catena di aggiornamento** (`aggiornamenti/`, manifest, `release.sh`: prova della catena obbligatoria).

- Manifest: `UPDATE_PACKAGES_WITHOUT_USER_ACTION`; `android:process=":aggiornamenti"` su `AggiornamentoJob` e `AvvioReceiver`; `EsitoInstallazioneReceiver` resta nel processo principale.
- `AggiornamentoJob.programma`: job 1 con `setMinimumLatency` di 60 secondi, ripetuto se il controllo dà errore; job 2 periodico, 6 ore con un'ora di margine.
- `Aggiornatore.controlla` restituisce `Esito`; download dell'APK limitato a 50 MB.
- `EsitoInstallazioneReceiver`: la conferma che non si può mostrare resta in memoria e `Schermata.onStart` la riapre.
- `Stato`: esito in `SharedPreferences` `aggiornamenti` con `MODE_MULTI_PROCESS` e `commit`.
- `HomeActivity.onCreate`: `AggiornamentoJob.programma` prima di costruire la vista.
- `release.sh`: alza la versione se esiste la release su GitHub (non il tag); controlla l'impronta del certificato con `apksigner`.

**Funzioni.**

- `FonteMeteo.ultimo`: `null` oltre le 3 ore; `HomeActivity` rilegge il dato a ogni giro del timer; `mostraMeteo(null)` riporta tutti i temi allo stato senza dato.
- `Lettore`: metadati letti al cambio di sessione e in `onMetadataChanged`; copertina riusata se uguale (`Bitmap.sameAs`).
- `Benzinai`: elenco in memoria con `libera`, pausa di 10 minuti dopo un download fallito, regola delle 08:30 (`DatiMimit.daAggiornare`), preferiti in `SharedPreferences`, ricerca dei comuni (`DatiMimit.comuniContenenti`).
- `AppHome.elenco(context)`, `scegli`, `firma`; `HomeActivity` ricostruisce se la firma cambia.
- `AppHomeActivity`, `ComuniActivity`, voci in `ImpostazioniActivity`.
- `Schermata`: gestore delle eccezioni che salva l'ultimo crash; `DiagnosticaActivity` lo mostra.

**Pulizia.** `StatoBenzinai`, `Ui.ORDINI`, `Ui.POSIZIONE_NON_DISPONIBILE`, `Ui.frazione` al posto delle otto copie; `Palette` senza i due campi non letti; descrizione delle ore in Cyberpunk; `scripts/verifica.sh` esegue gli unit test; font Rajdhani ridotti con `pyftsubset`.

**Passi.**

- [x] Prova del crash all'avvio sull'emulatore, prima e dopo.
- [x] Catena di aggiornamento.
- [x] Funzioni, schermate nuove, pulizia dei temi.
- [x] Prove sull'emulatore `box` e documentazione.
- [x] Commit e rilascio con `./release.sh minor`.
- [ ] Prove sul box (Prove manuali).

## Prove manuali

| Prova | Esito |
|---|---|
| Il box riceve questa versione dalla 0.2.1, con la conferma «Aggiorna» | da provare |
| L'aggiornamento successivo arriva senza nessuna conferma | da provare |
| Il box, all'accensione dell'auto, si riavvia o si risveglia? | da provare |
| Comuni preferiti e app della Home scelti dal box, con la tastiera del box | da provare |
| Diagnostica mostra «Ultimo crash: nessuno» dopo qualche giorno d'uso | da provare |

## Diario

- 2026-11-19: revisione del codice contro `CLAUDE.md`; l'utente approva le proposte 1–3 e 6–11 e chiede di sistemare tutti i difetti trovati.
- 2026-11-19: prova del crash all'avvio sull'emulatore `box`, quattro build di prova con `versionCode` 1 installate con `adb install -r -d -i app.australdashboard`. (a) Codice della 0.2.1 più un'eccezione in `HomeActivity.onCreate`: dopo due crash Android segna il processo come guasto, `BOOT_COMPLETED` va in timeout, nessun aggiornamento in due minuti. (b) Con job e receiver di avvio in `:aggiornamenti` e il permesso nuovo: il download parte, ma Android risponde `STATUS_PENDING_USER_ACTION`. (c) Lo stesso controllo rilanciato a emulatore acceso da più di 30 secondi: la 0.2.1 si installa senza conferma. (d) Codice della 0.2.1 senza crash e senza il permesso, app installatore di sé stessa: Android apre la conferma «Update». Quindi servono tutti e tre: processo separato, permesso, attesa dopo l'avvio.
- 2026-11-19: prova finale della catena con il codice di M03 più l'eccezione di prova: riavvio alle 02:32:57, crash, job dopo 60 secondi, alle 02:34:08 l'app è la 0.2.1 pubblicata, senza conferma, e la Home è ripartita. Poi la build normale di M03 installata con `adb install -r` sopra la 0.2.1 pubblicata: stessa firma (SHA-256 `7412986f…9e36`) e stesso `applicationId`.
- 2026-11-19: prove sull'emulatore `box` con la build di release: otto temi (Home e Benzinai) a vista, Diagnostica con il crash di prova, scelta di Chrome nella posizione 2 e Home ricostruita, ricerca «tori» tra i comuni con Torino in cima, Torino tra i preferiti dei Benzinai, brano finto in Chrome con copertina. Misure: APK 454 KB, avvio a freddo 139–163 ms, PSS 23 MB (Synthwave), 0 frame in ritardo; con il brano in riproduzione 0 frame in 30 secondi (prima della correzione circa uno al secondo). 45 unit test.
- 2026-11-19: meteo vecchio provato su Matrix e HUD: emulatore in modalità aereo con l'orologio avanti di 5 ore, la Home mostra «--°» e «In attesa» con le scale spente.
- 2026-11-19: non provati: `mostraMeteo(null)` negli altri quattro temi cambiati (Art Déco, Solari, Cyberpunk, Braun: solo letti nel codice), la conferma riaperta al ritorno sulla Home, il job periodico (visto solo in `dumpsys jobscheduler`: 6 ore, margine di un'ora), `release.sh` con una chiave sbagliata, i tasti precedente e successivo dopo la modifica al lettore.
- 2026-11-19: pubblicata la v0.3.0 (codice 5, 454 KB). Il file scaricato da GitHub ha lo SHA-256 di `versione.json` ed è firmato con la chiave di sempre. Sull'emulatore `box` l'app installata (codice di M03 con `versionCode` 4, installatore di sé stessa) l'ha ricevuta da sola 85 secondi dopo l'accensione, senza conferma, con la Home in primo piano. Restano le prove sul box.
