# Austral Dashboard: riferimento

## Per cominciare

Progetto Gradle (wrapper 9.9.1, AGP 9.5.1 con Kotlin integrato), un modulo `app`, package `app.australdashboard`. `minSdk` = `targetSdk` = `compileSdk` = 32. Nessuna dipendenza di runtime; nei test JUnit 4 e `org.json` (che su Android è nella piattaforma). Il controllo lint `ExpiredTargetSdkVersion` è disattivato perché riguarda solo il Play Store. Release con R8 e riduzione delle risorse: l'APK è intorno ai 670 KB con i font VT323, Orbitron, Limelight, Rajdhani (pesi 600 e 700, la parte più pesante) e Audiowide.

JDK: quello di Android Studio, `/Applications/Android Studio.app/Contents/jbr/Contents/Home` (esportare `JAVA_HOME`). SDK in `~/Library/Android/sdk`, indicato in `local.properties` (ignorato da git).

| Comando | Cosa fa |
|---|---|
| `./gradlew :app:testDebugUnitTest` | Unit test |
| `./gradlew :app:assembleRelease` | APK firmato in `app/build/outputs/apk/release/` (serve `keystore.properties`) |
| `./release.sh [major\|minor\|patch]` | Pubblica una versione (vedi «Aggiornare») |

Versione in `version.properties` (`versionCode`, `versionName`), letta da `app/build.gradle.kts`.

| File | Ruolo |
|---|---|
| `Schermata.kt` | Base delle activity: conta le schermate visibili in `Misure.schermateVisibili`, crea colonne, testi, pulsanti grandi e contenitori a scorrimento in codice (nessun layout XML) |
| `HomeActivity.kt` | Launcher (`HOME` + `DEFAULT`, `singleTask`): costruisce la Home del tema scelto e la ricostruisce in `onStart` se il tema è cambiato; implementa `AzioniHome`; ora da `ACTION_TIME_TICK`, meteo ogni 30 minuti mentre è visibile, musica da `Lettore`; chiede la posizione una volta per processo; programma il controllo aggiornamenti in `onCreate`; misura il primo frame e i frame in ritardo |
| `BenzinaiActivity.kt` | Pannello Benzinai del tema scelto; posizione da `Posizione`, elenco da `Benzinai.elenco` su un thread; scarta le risposte superate |
| `TutteLeAppActivity.kt` | Griglia a 5 colonne delle app `CATEGORY_LAUNCHER`, nei colori di `Tema.palette`; icone ed etichette caricate su un thread |
| `TemiActivity.kt` | Elenco dei temi; un tocco salva e chiude |
| `CittaActivity.kt` | Città di riserva: ricerca con `Citta.cerca`, un tocco salva |
| `ImpostazioniActivity.kt` | Tema, permesso musica (nascosto se concesso), città di riserva, controllo manuale aggiornamenti, Diagnostica, impostazioni di sistema, scelta del launcher |
| `temi/Tema.kt` | Contratto dei temi: `Tema` (`home`, `benzinai`, `palette`), `VistaHome`, `VistaBenzinai`, `AzioniHome`, `AzioniBenzinai`, le sei `AppHome` con i nomi dei pacchetti |
| `temi/Temi.kt` | Elenco dei temi e scelta, in `SharedPreferences` `impostazioni`, chiave `tema` (predefinito il primo) |
| `temi/Ui.kt` | Costruzione delle view in px: testi, icone colorate, bordi, righe e colonne con spaziatura, `premibile` (ripple), icona del cielo |
| `temi/Matrix.kt` | Tema 1: misure e colori della tavola `T1Home` e `T1Benzinai`; nei servizi niente icone, che non ci stavano accanto alle etichette |
| `temi/Hud.kt` | Tema 2: quadrante a 60 tacche (i minuti trascorsi in ciano), termometro con fascia minima–massima, squadrette e smussi a 45° disegnati in codice; Orbitron variabile con `fontVariationSettings` |
| `temi/Brutalismo.kt` | Tema 3: blocchi con bordi da 4 px, indice delle app a sei righe, servizi in alto (il quarto è solo l'ingranaggio); `sans-serif-condensed` peso 900 dove esiste |
| `temi/ArtDeco.kt` | Tema 4: trittico simmetrico, cornici doppie con rombi d'oro (`Drawable` privato), comandi tondi; Limelight e `serif`; i nomi dei distributori in maiuscolo, perché nei dati del MIMIT sono un po' maiuscoli e un po' no |
| `temi/Solari.kt` | Tema 5: ogni cifra e parola su una paletta (`GradientDrawable` con la giunzione scura a metà); orologio largo 650 px fissi come nella tavola; `sans-serif-condensed` |
| `temi/Cyberpunk.kt` | Tema 6: smussi a 45° con `Drawable` e contorno privati, così anche il ripple segue lo smusso; servizi con larghezze fisse dai pesi della tavola; Rajdhani 600 e 700 |
| `temi/Braun.kt` | Tema 7: display incassato, tastierino 5×2 di tasti tondi (app scure, servizi chiari; tutta la cella è toccabile); `sans-serif-light` |
| `temi/Synthwave.kt` | Tema 8: un solo gradiente magenta→arancio su ora, orizzonte, cornice della copertina, anelli e play (testi a gradiente con `LinearGradient`); Audiowide per titoli e cifre, `sans-serif-condensed` corsivo per il testo |
| `funzioni/` | Dati condivisi dai temi: vedi «Funzioni» |
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

## Temi

- Le tavole sono nell'artifact Design `FB5G7dpEA4q1GcwEvhxoXL`: `project/T<n>Home.dc.html`, `project/T<n>Benzinai.dc.html`, e in `project/canvas.json` le note `t<n>spec` con palette, font e misure.
- Le misure delle tavole sono in px a 1244×1194 e i temi le usano così come sono (`TypedValue.COMPLEX_UNIT_PX`), senza convertire in dp.
- Tutte le activity usano `Theme.DeviceDefault.NoActionBar.Fullscreen`: la barra di stato di Android non toglie spazio alla Home.
- Un riquadro con l'etichetta sul bordo esce dai propri limiti: il genitore deve avere `clipChildren=false` (`Ui.senzaRitaglio`). Mai su un genitore diretto di uno `ScrollView`, che altrimenti disegna fuori dai suoi limiti: lo `ScrollView` va dentro un `FrameLayout`.
- `Ui.testo` imposta `isAllCaps` dopo `isSingleLine`, che altrimenti lo annulla.
- Un testo che deve rimpicciolirsi per stare nello spazio si costruisce con `Ui.adatta`, con una larghezza data (non `ADATTA`). L'adattamento automatico di Android non scatta se il testo ha l'ellissi, né su una riga sola con lo scorrimento orizzontale che `isSingleLine` accende; con un'interlinea fissa le lettere rimpicciolite si tagliano in basso. `Ui.adatta` toglie tutte e tre le cose. `setTextSize` su un testo adattato non ha effetto.
- Un testo più alto del suo riquadro non si centra: Android lo allinea in alto e taglia il fondo. Per imitare il `line-height` della tavola (più basso dell'altezza del font) si lascia l'altezza naturale e si mettono margini negativi sopra e sotto, metà della differenza ciascuno (ora di Braun).
- Una parola sola in un testo a due righe si spezza a metà («LAUNCH/ER»): le etichette di una parola vanno su una riga.
- Una riga a pesi che non ci sta sposta e taglia i figli: quando lo spazio è stretto, larghezze fisse calcolate sulla tavola.
- Font esterni in `res/font/`, letti con `resources.getFont`; licenze in `licenze/`. I font di sistema: `Typeface.MONOSPACE` al posto di Roboto Mono.
- Icone: `res/drawable/ic_*.xml`, convertite dalle SVG delle tavole (viewport 48), bianche, colorate con `imageTintList`.

## Funzioni

| File | Cosa fa |
|---|---|
| `funzioni/Dati.kt` | Tipi condivisi: `Meteo`, `Cielo`, `Musica`, `Luogo`, `Distributore`, `Ordine`, `ElencoBenzinai` |
| `funzioni/Lettore.kt` | Sessioni media con `MediaSessionManager.getActiveSessions`: sceglie la sessione in riproduzione, altrimenti la più recente con un titolo; titolo, artista, copertina (lato massimo 600 px), app; comandi con `transportControls`. Senza sessione, play invia `KEYCODE_MEDIA_PLAY` e dopo 1,5 s, se niente suona, apre Spotify |
| `funzioni/AscoltoNotifiche.kt` | `NotificationListenerService` vuoto: dà il diritto di leggere le sessioni media |
| `funzioni/Posizione.kt` | `LocationManager`: ultima posizione nota di fused, gps, network; poi l'ultima salvata; poi la città di riserva. `aggiorna` chiede una posizione fresca con `getCurrentLocation`. `SharedPreferences` `posizione` |
| `funzioni/Meteo.kt` | `FonteMeteo`: Open-Meteo `v1/forecast` (attuale, minima, massima, codice WMO), salvato in `SharedPreferences` `meteo`, vecchio dopo 30 minuti; descrizione italiana e `Cielo` dal codice WMO |
| `funzioni/Citta.kt` | Open-Meteo Geocoding, 6 risultati in italiano, etichetta «nome, admin2» |
| `funzioni/Benzinai.kt` | Scarica i due CSV del MIMIT al massimo una volta al giorno, solo quando si apre il pannello; salva `filesDir/benzinai.txt` (tutti gli impianti con benzina self, separati da tabulazione, prima riga la data); se il download fallisce usa il file vecchio. Preferiti in `SharedPreferences` `benzinai` |
| `funzioni/DatiMimit.kt` | Logica pura dei Benzinai: lettura dei CSV, file compatto, filtro entro 15 km più i preferiti, al massimo 30 vicini, ordinamenti, formati di prezzo, distanza e data |

CSV del MIMIT (`https://www.mimit.gov.it/images/exportCSV/`), verificati il 2026-11-18:

- Separatore `|`, nessuna virgoletta di quoting; prima riga `Estrazione del <data>`, intestazione sulla seconda.
- `anagrafica_impianti_attivi.csv` (circa 3,6 MB, 24.000 righe): `idImpianto|Gestore|Bandiera|Tipo Impianto|Nome Impianto|Indirizzo|Comune|Provincia|Latitudine|Longitudine`. Circa 115 righe hanno un `|` dentro nome o gestore: si leggono ancorandosi alla fine della riga e al campo Tipo.
- `prezzo_alle_8.csv` (circa 3,9 MB, 93.000 righe): `idImpianto|descCarburante|prezzo|isSelf|dtComu`, data `dd/MM/yyyy HH:mm:ss`. Si tiene solo `Benzina` esatta con `isSelf` = 1.
- Molti impianti comunicano il prezzo di rado: la data di aggiornamento può essere di settimane prima.

Permessi aggiunti: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`; `<queries>` per le app `CATEGORY_LAUNCHER` (Tutte le app, nomi delle app musicali, app della Home).

## Aggiornare

- Il box legge `https://github.com/phlex89/austral-dashboard/releases/latest/download/versione.json`: `{"versionCode", "versionName", "apk", "sha256"}`. `InfoVersione.daJson` rifiuta un `apk` non https e un'impronta che non sia di 64 cifre esadecimali.
- `Aggiornatore.controlla` è protetto da un `AtomicBoolean` (un controllo alla volta) e scrive sempre l'esito in `Stato`. Se `versionCode` è maggiore di quello installato: scarica in `cacheDir/aggiornamento.apk`, verifica lo SHA-256 con `Impronta`, installa con una sessione `PackageInstaller` con `USER_ACTION_NOT_REQUIRED`, poi cancella il file.
- `EsitoInstallazioneReceiver` riceve l'esito. Con `STATUS_PENDING_USER_ACTION` apre la conferma di Android se una schermata dell'app è visibile, altrimenti pubblica una notifica nel canale `aggiornamenti`.
- Conferma dell'installazione: la prima volta Android chiede il permesso «installa app sconosciute» per l'app; poi chiede «Aggiornare?» finché l'app non risulta installata da sé stessa (`installerPackageName=app.australdashboard`, vero dopo il primo aggiornamento fatto dall'app). Con l'app come proprio installer, `USER_ACTION_NOT_REQUIRED` dovrebbe evitare la conferma: da verificare.
- GitHub aggiorna il rinvio di `releases/latest/download` 1–2 minuti dopo la pubblicazione: un controllo fatto subito dopo `release.sh` vede ancora la versione precedente.
- Chi avvia il controllo: `AvvioReceiver` (`BOOT_COMPLETED`) e `HomeActivity.onCreate` programmano `AggiornamentoJob` (`JobScheduler`, id 1, rete richiesta; non riprogramma se è già in attesa o in corso); il pulsante in Impostazioni chiama `Aggiornatore.controlla` su un thread.
- `release.sh`: rifiuta se manca `keystore.properties`, se ci sono modifiche non committate o se il branch non è `main`. Se il tag `v<versionName>` esiste già alza la versione (default `patch`) e `versionCode` di 1, altrimenti pubblica la versione del file così com'è. Esegue test e build, scrive `build/release/austral-dashboard.apk` e `versione.json`, committa `version.properties` se è cambiato, crea il tag, fa il push e crea la release con `gh`.
- Prova della catena senza pubblicare (regola di `CLAUDE.md`): compilare la versione nuova con `versionCode` più basso di quello dell'ultima release (modificare `version.properties` solo per la build e rimetterlo subito com'era), installarla sull'emulatore con `adb install -r -d`, poi Impostazioni → Controlla aggiornamenti: deve scaricare, verificare e installare la release pubblicata. Installata con `adb`, l'app chiede la conferma «Update»; dopo, `adb shell pm list packages -i app.australdashboard` deve dire `installer=app.australdashboard`. Infine `adb install -r` della build normale sopra la release pubblicata conferma firma e `applicationId` (`apksigner verify --print-certs` sui due APK dà lo stesso SHA-256).
- Chiave: `~/.android-keys/austral-dashboard.jks` (PKCS12, alias `austral-dashboard`, RSA 4096); `keystore.properties` nella radice del progetto ha `storeFile`, `storePassword`, `keyAlias`, `keyPassword` ed è ignorato da git.

## Emulatori

Immagine `system-images;android-32;google_apis;arm64-v8a`. AVD in `~/.android/avd/`:

| AVD | Schermo | Core | RAM |
|---|---|---|---|
| `box` | 1244×1194, 200 dpi, `hw.mainKeys=yes` | 8 | 8 GB |
| `box-lento` | uguale | 2 | 2 GB |

- Avvio senza finestra: `~/Library/Android/sdk/emulator/emulator -avd box -no-window -no-audio -no-boot-anim -no-snapshot-save`; con finestra si apre da Android Studio (Device Manager).
- Su un AVD nuovo, per somigliare al box (che non ha la taskbar di Android 12L): `adb shell pm disable-user --user 0 com.google.android.apps.nexuslauncher`, poi `adb shell cmd package set-home-activity app.australdashboard/.HomeActivity`.
- Avvio a freddo: `adb shell am force-stop app.australdashboard`, poi `adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.HOME` (`TotalTime`, `LaunchState: COLD`).
- Posizione finta: `adb shell appops set com.android.shell android:mock_location allow`, poi per `gps` e `fused` `adb shell cmd location providers add-test-provider <p>`, `set-test-provider-enabled <p> true`, `set-test-provider-location <p> --location 41.9028,12.4964`. `adb emu geo fix` da solo non bastava.
- Permesso musica senza toccare: `adb shell cmd notification allow_listener app.australdashboard/app.australdashboard.funzioni.AscoltoNotifiche`.
- Un brano finto per provare la scheda della musica: una pagina servita dal Mac (`python3 -m http.server 8765`, poi `adb reverse tcp:8765 tcp:8765`) con un `<audio>` e `navigator.mediaSession.metadata` (titolo, artista, copertina PNG), aperta in Chrome sull'emulatore; un tocco su play crea una sessione media vera, con il nome dell'app «Chrome». `adb shell am force-stop com.android.chrome` la chiude.
- Le activity diverse dalla Home non sono esportate: ci si arriva dalla Home con `adb shell input tap`. Per toccare un elemento per testo o descrizione: `adb exec-out uiautomator dump /dev/tty`, poi il centro dei suoi `bounds`. Il testo letto è quello originale, senza il maiuscolo applicato dalla vista. Memoria: `adb shell dumpsys meminfo app.australdashboard`. Frame: `adb shell dumpsys gfxinfo app.australdashboard`.

Misure sull'emulatore `box` (build 0.1.0, release): APK 41 KB, PSS 20–24 MB, avvio a freddo `TotalTime` 180–330 ms, primo frame dalla creazione della Home 85–125 ms, 0 frame in ritardo secondo `gfxinfo`.

Misure sull'emulatore `box` con il tema Matrix (M02, release): APK 174 KB, PSS 23 MB, avvio a freddo `TotalTime` 152–196 ms, 0 frame in ritardo. Primo download dei prezzi MIMIT e lettura: circa 4 s.

Misure sull'emulatore `box` con i temi 1–4 (M02, release, APK 291 KB), avvio a freddo dalla riga `Displayed` di logcat dopo `am force-stop` (Android riapre subito la Home, e `am start -W` dà allora `TotalTime: 0`):

| Tema | Avvio a freddo | PSS |
|---|---|---|
| Matrix | 145–184 ms | 24 MB |
| HUD | 164–226 ms | 34 MB (memoria nativa in più, probabilmente il disegno del quadrante) |
| Brutalismo | 141–165 ms, un picco a 1 s non ripetuto | 24 MB |
| Art Déco | 150–226 ms | 24 MB |

Frame in ritardo: 0 per tutti.

Temi 5–8 (M02, release, APK 673 KB), stesso metodo:

| Tema | Avvio a freddo | PSS |
|---|---|---|
| Solari | 153–176 ms | 25 MB |
| Cyberpunk | 142–165 ms | 28–31 MB |
| Braun | 174–297 ms | 23 MB |
| Synthwave | 156–271 ms | 24 MB |

Frame in ritardo: 0 per tutti.
