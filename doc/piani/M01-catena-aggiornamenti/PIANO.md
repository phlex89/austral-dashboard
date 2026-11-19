---
milestone: M01
stato: in-corso
data: 2026-11-18
lettori: entrambi
---

# M01 · La catena di aggiornamento

## In una pagina

Prima delle funzioni del launcher, si verifica che tutto il percorso funzioni: sviluppo sul Mac, prova sull'emulatore, pubblicazione con un comando, installazione e aggiornamento in macchina.

| Pezzo | Cosa fa |
|---|---|
| Home provvisoria | Si dichiara launcher, mostra versione e ora, apre Impostazioni e Diagnostica |
| Aggiornatore | All'accensione e dal tasto «Controlla aggiornamenti»: scarica, verifica, installa |
| Diagnostica | Avvio a freddo, memoria, frame persi, esito dell'ultimo controllo |
| `release.sh` | Sul Mac: numero di versione, build firmata, pubblicazione su GitHub |
| Emulatori | «box» (come il dispositivo) e «box-lento» (caso peggiore) |

Si chiude con il primo test reale:

1. Si pubblica la v1 e la si installa a mano sul box dal browser.
2. Si pubblica una v2 con una differenza visibile.
3. Il box la trova, la scarica e la installa.

Qui si verificano anche le cose rimandate: aggiornamento senza conferma, origini sconosciute, barra Ottocast, GPS, taratura delle prestazioni.

## Per chi esegue

**File e struttura.**

- Progetto Gradle con Kotlin DSL, un solo modulo `app`, package `app.australdashboard`.
- `minSdk` = `targetSdk` = 32. R8 attivo in release (`isMinifyEnabled`, `isShrinkResources`). Nessuna dipendenza di runtime oltre alla libreria standard di Kotlin; JUnit solo per i test.
- `HomeActivity`: intent-filter `MAIN` + `HOME` + `DEFAULT`, `launchMode="singleTask"`, tema scuro senza barra del titolo.
- `ImpostazioniActivity`: tasto «Controlla aggiornamenti» ed esito.
- `DiagnosticaActivity`: avvio a freddo (da `Process.getStartUptimeMillis()` al primo frame), memoria (`Debug.MemoryInfo`), frame persi (`Window.addOnFrameMetricsAvailableListener`), versione, esito dell'ultimo controllo.
- Aggiornatore separato dall'interfaccia:
  - un `BroadcastReceiver` su `BOOT_COMPLETED` programma un job `JobScheduler` con rete richiesta;
  - lo stesso job parte dal tasto e dall'avvio della Home.
- Permessi: `INTERNET`, `REQUEST_INSTALL_PACKAGES`, `RECEIVE_BOOT_COMPLETED`.

**Protocollo di aggiornamento.**

- `https://github.com/phlex89/austral-dashboard/releases/latest/download/versione.json`, con `{ "versionCode": n, "versionName": "x.y.z", "apk": "<url>", "sha256": "<hex>" }`.
- Se `versionCode` è maggiore di quello installato: scarica l'APK in `cacheDir` con `HttpURLConnection` e verifica lo SHA-256. Se l'impronta non corrisponde, scarta il file e lo segna nell'esito.
- Installa con una sessione `PackageInstaller` e `setRequireUserAction(USER_ACTION_NOT_REQUIRED)`. Se arriva `STATUS_PENDING_USER_ACTION`, mostra la conferma: subito se la Home è in primo piano, altrimenti con una notifica.

**Firma e pubblicazione.**

- Keystore fuori dal repository, in `~/.android-keys/austral-dashboard.jks`, con copia di backup; password in `keystore.properties`, ignorato da git.
- `release.sh`:
  1. aumenta `versionCode` e `versionName` in `version.properties`;
  2. esegue `./gradlew assembleRelease` e calcola lo SHA-256 dell'APK;
  3. scrive `versione.json` e crea la release con `gh release create v<versionName>` allegando `austral-dashboard.apk` e `versione.json`;
  4. fa il commit e il tag della versione.

**Emulatori.**

- Immagine `system-images;android-32;google_apis;arm64-v8a`.
- AVD «box»: 1244×1194, `hw.lcd.density=200`, `hw.mainKeys=yes`, 8 core, 8 GB.
- AVD «box-lento»: come «box» ma con 2 core e 2 GB.

**Test.**

- Unit test per il confronto delle versioni, la lettura di `versione.json` e la verifica dello SHA-256 (impronta giusta, sbagliata, file troncato).

**Passi.**

- [x] Strumenti: Android Studio installato dall'utente, SDK e immagine Android 12 arm64, AVD «box» e «box-lento».
- [x] Scheletro Gradle e `HomeActivity` impostabile come launcher sull'emulatore.
- [x] Diagnostica.
- [x] Aggiornatore con test.
- [x] Keystore e `release.sh`.
- [x] Prova completa sull'emulatore: v1 installata, v2 pubblicata, aggiornamento ricevuto.
- [ ] Test reale sul box (Prove manuali), previsto il 2026-11-19.

## Prove manuali

| Prova | Esito |
|---|---|
| v1 installata a mano dal browser del box e impostata come launcher | fatto il 2026-11-19 con la 0.3.0 |
| v2 trovata all'accensione e installata | da provare |
| v2 trovata dal tasto «Controlla aggiornamenti» | da provare |
| L'aggiornamento parte senza conferma (altrimenti con un tocco) | da provare |
| Taratura: avvio a freddo e memoria, box contro emulatore | da provare |
| Barra Ottocast in basso: si può nascondere? | non serve: l'utente preferisce tenerla visibile (2026-11-18) |
| Barra di stato di Android: compare sopra il launcher? | da provare |
| GPS del box: c'è una posizione senza il telefono? | da provare |

## Diario

- 2026-11-18: piano scritto.
- 2026-11-18: strumenti, emulatori, scheletro, diagnostica, aggiornatore (9 unit test), chiave e `release.sh` fatti. Sull'emulatore `box`: APK 41 KB, PSS 20–24 MB, avvio a freddo 180–330 ms; il controllo aggiornamenti risponde HTTP 404 finché non c'è una release. Scostamenti dal piano: `compileSdk` 32; la taskbar di Android 12L dell'emulatore si toglie disattivando il launcher di sistema; i frame in ritardo si contano contro `FrameMetrics.DEADLINE`.
- 2026-11-18: pubblicate v0.1.0 e v0.1.1. Sull'emulatore `box` la 0.1.0 scaricata da GitHub, al riavvio, trova la 0.1.1, la scarica e chiede l'installazione. La prima volta Android chiede il permesso «installa app sconosciute» per Austral Dashboard; dopo, conferma con un tocco («Update») e la 0.1.1 si installa. Da quel momento l'app risulta installata da sé stessa (`installerPackageName=app.australdashboard`): gli aggiornamenti successivi potrebbero non chiedere conferma, da verificare. Il link `releases/latest/download` di GitHub punta alla nuova versione solo 1–2 minuti dopo la pubblicazione.
- 2026-11-18: pubblicata la v0.2.0 (codice 3, la Home a temi di M02). Sull'emulatore `box` la 0.1.1 l'ha trovata con «Controlla aggiornamenti» e installata, e l'app risulta installata da sé stessa. Le prove sul box sono rimandate al 2026-11-19: l'utente installa a mano la 0.2.0 dal browser del box, poi si pubblica una versione successiva per provare l'aggiornamento all'accensione e dal tasto. La barra Ottocast resta visibile per scelta dell'utente, quindi la prova per nasconderla non serve.
