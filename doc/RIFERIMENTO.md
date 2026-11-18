# Austral Dashboard: riferimento

## Per cominciare

Progetto Gradle (wrapper 9.9.1, AGP 9.5.1 con Kotlin integrato), un modulo `app`, package `app.australdashboard`. `minSdk` = `targetSdk` = `compileSdk` = 32. Nessuna dipendenza di runtime; nei test JUnit 4 e `org.json` (che su Android è nella piattaforma). Il controllo lint `ExpiredTargetSdkVersion` è disattivato perché riguarda solo il Play Store. Release con R8 e riduzione delle risorse: l'APK è intorno ai 41 KB.

JDK: quello di Android Studio, `/Applications/Android Studio.app/Contents/jbr/Contents/Home` (esportare `JAVA_HOME`). SDK in `~/Library/Android/sdk`, indicato in `local.properties` (ignorato da git).

| Comando | Cosa fa |
|---|---|
| `./gradlew :app:testDebugUnitTest` | Unit test |
| `./gradlew :app:assembleRelease` | APK firmato in `app/build/outputs/apk/release/` (serve `keystore.properties`) |
| `./release.sh [major\|minor\|patch]` | Pubblica una versione (vedi «Aggiornare») |

Versione in `version.properties` (`versionCode`, `versionName`), letta da `app/build.gradle.kts`.

| File | Ruolo |
|---|---|
| `Schermata.kt` | Base delle activity: conta le schermate visibili in `Misure.schermateVisibili`, crea colonne, testi e pulsanti grandi in codice (nessun layout XML) |
| `HomeActivity.kt` | Launcher (`HOME` + `DEFAULT`, `singleTask`): ora, versione, Impostazioni; programma il controllo aggiornamenti in `onCreate`; misura il primo frame e i frame in ritardo |
| `ImpostazioniActivity.kt` | Controllo manuale aggiornamenti, Diagnostica, impostazioni di sistema, scelta del launcher |
| `DiagnosticaActivity.kt` | Mostra versione, dimensione APK, PSS, tempi del primo frame, frame in ritardo, esito aggiornamenti |
| `Misure.kt` | Misure in memoria del processo |
| `Stato.kt` | Esito dell'ultimo controllo, in `SharedPreferences` `stato` |
| `aggiornamenti/` | Vedi «Aggiornare» |

## Il dispositivo

Fatti letti dalle schermate del box (Car Launcher e Impostazioni di Android) il 2026-11-18:

| Voce | Valore |
|---|---|
| Box | Ottocast OttoAibox P3, nome dispositivo `OttoAibox-P3` |
| Sistema | Android 12, SDK 32, firmware `SLM920.A12.1610.202411051537_EAU`, regione firmware `KZ` |
| Processore | Qualcomm (modulo Quectel SLM920, probabilmente QCM6125), 8 GB di RAM (circa 4,8 GB liberi), 128 GB di memoria |
| Auto | Renault Austral, unità `Renault_AIVI2 FULL_LG Electronics`, collegata via CarPlay cablato |
| Schermo | Risoluzione dell'unità 1244×1304; area per le app 1244×1194 px, densità 1,25 (200 dpi), circa 995×955 dp |
| Rete | Connesso a internet (rete mobile o hotspot), non raggiungibile dal Mac |

Vincoli che valgono per ogni funzione:

- L'immagine arriva all'auto come video CarPlay: un piccolo ritardo tra tocco e risposta c'è comunque e non dipende dal launcher.
- Tocco via CarPlay: progettare per tap e swipe semplici, bersagli grandi; niente multitouch.
- Il box non legge i dati dell'auto (velocità, carburante, clima): parla con l'auto solo via CarPlay. Ora, rete, media e app sì.
- La barra in basso (casa, indietro, ora, regolazioni) è di Ottocast ed esiste anche fuori dal launcher. Car Launcher, nelle foto, non mostrava la barra di stato di Android.
- Il box si riaccende a ogni partenza: l'avvio a freddo del launcher conta.
- Un aggiornamento del firmware Ottocast potrebbe reimpostare il launcher predefinito.

Da verificare sul box (prove manuali di M01): se la barra Ottocast si può nascondere, se c'è la barra di stato di Android, se il box ha un GPS suo, se l'aggiornamento avviene senza conferma, il fattore tra i tempi del box e quelli dell'emulatore.

## Aggiornare

- Il box legge `https://github.com/phlex89/austral-dashboard/releases/latest/download/versione.json`: `{"versionCode", "versionName", "apk", "sha256"}`. `InfoVersione.daJson` rifiuta un `apk` non https e un'impronta che non sia di 64 cifre esadecimali.
- `Aggiornatore.controlla` è protetto da un `AtomicBoolean` (un controllo alla volta) e scrive sempre l'esito in `Stato`. Se `versionCode` è maggiore di quello installato: scarica in `cacheDir/aggiornamento.apk`, verifica lo SHA-256 con `Impronta`, installa con una sessione `PackageInstaller` con `USER_ACTION_NOT_REQUIRED`, poi cancella il file.
- `EsitoInstallazioneReceiver` riceve l'esito. Con `STATUS_PENDING_USER_ACTION` apre la conferma di Android se una schermata dell'app è visibile, altrimenti pubblica una notifica nel canale `aggiornamenti`.
- Conferma dell'installazione: la prima volta Android chiede il permesso «installa app sconosciute» per l'app; poi chiede «Aggiornare?» finché l'app non risulta installata da sé stessa (`installerPackageName=app.australdashboard`, vero dopo il primo aggiornamento fatto dall'app). Con l'app come proprio installer, `USER_ACTION_NOT_REQUIRED` dovrebbe evitare la conferma: da verificare.
- GitHub aggiorna il rinvio di `releases/latest/download` 1–2 minuti dopo la pubblicazione: un controllo fatto subito dopo `release.sh` vede ancora la versione precedente.
- Chi avvia il controllo: `AvvioReceiver` (`BOOT_COMPLETED`) e `HomeActivity.onCreate` programmano `AggiornamentoJob` (`JobScheduler`, id 1, rete richiesta; non riprogramma se è già in attesa o in corso); il pulsante in Impostazioni chiama `Aggiornatore.controlla` su un thread.
- `release.sh`: rifiuta se manca `keystore.properties`, se ci sono modifiche non committate o se il branch non è `main`. Se il tag `v<versionName>` esiste già alza la versione (default `patch`) e `versionCode` di 1, altrimenti pubblica la versione del file così com'è. Esegue test e build, scrive `build/release/austral-dashboard.apk` e `versione.json`, committa `version.properties` se è cambiato, crea il tag, fa il push e crea la release con `gh`.
- Chiave: `~/.android-keys/austral-dashboard.jks` (PKCS12, alias `austral-dashboard`, RSA 4096); `keystore.properties` nella radice del progetto ha `storeFile`, `storePassword`, `keyAlias`, `keyPassword` ed è ignorato da git.

## Emulatori

Immagine `system-images;android-32;google_apis;arm64-v8a`. AVD in `~/.android/avd/`:

| AVD | Schermo | Core | RAM |
|---|---|---|---|
| `box` | 1244×1194, 200 dpi, `hw.mainKeys=yes` | 8 | 8 GB |
| `box-lento` | uguale | 2 | 2 GB |

- Avvio senza finestra: `~/Library/Android/sdk/emulator/emulator -avd box -no-window -no-audio -no-boot-anim -no-snapshot-save`; con finestra si apre da Android Studio (Device Manager).
- Su un AVD nuovo, per somigliare al box (che non ha la taskbar di Android 12L): `adb shell pm disable-user --user 0 com.google.android.apps.nexuslauncher`, poi `adb shell cmd package set-home-activity app.australdashboard/.HomeActivity`.
- Avvio a freddo: `adb shell am start -W -n app.australdashboard/.HomeActivity` (`TotalTime`). Memoria: `adb shell dumpsys meminfo app.australdashboard`. Frame: `adb shell dumpsys gfxinfo app.australdashboard`.

Misure sull'emulatore `box` (build 0.1.0, release): APK 41 KB, PSS 20–24 MB, avvio a freddo `TotalTime` 180–330 ms, primo frame dalla creazione della Home 85–125 ms, 0 frame in ritardo secondo `gfxinfo`.
