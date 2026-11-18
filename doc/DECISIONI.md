# Austral Dashboard: decisioni

Perché il progetto è fatto così. Una voce `##` per decisione, con la più recente in fondo.

## Indice

<!-- indice -->
- [Kotlin e View native, senza dipendenze](#kotlin-e-view-native-senza-dipendenze)
- [Sviluppo e test solo sul Mac](#sviluppo-e-test-solo-sul-mac)
- [Aggiornamenti dalle GitHub Releases](#aggiornamenti-dalle-github-releases)
- [Temi intercambiabili per la Home](#temi-intercambiabili-per-la-home)
- [Fonti di meteo e prezzi](#fonti-di-meteo-e-prezzi)
<!-- /indice -->

## Kotlin e View native, senza dipendenze

**In breve.** L'app è scritta in Kotlin con le View native di Android, senza AndroidX, Compose o altre librerie di runtime. Il dispositivo è uno solo (Android 12), quindi le librerie di compatibilità non servono, e l'obiettivo è il minimo di dimensione, memoria e tempo di avvio.

**Dettagli.**

Ordini di grandezza stimati: APK sotto i 300 KB e 20–30 MB di memoria con le View native, contro 3–6 MB e 40–60 MB con Compose. Flutter e soluzioni web sono state scartate perché ancora più pesanti. L'avvio rapido conta perché il box si riaccende a ogni partenza dell'auto.

## Sviluppo e test solo sul Mac

**In breve.** Si sviluppa e si prova sul Mac, con un emulatore Android 12 configurato come il box. Le prestazioni del box non si possono emulare, quindi si controllano misure che non dipendono dal processore e si tara una volta un fattore di conversione confrontando la stessa build sul box e sull'emulatore.

**Dettagli.**

- Mac: Apple M5 Pro, 24 GB. Box: Qualcomm QCM6125 (modulo Quectel SLM920), molto più lento.
- L'emulatore usa l'immagine arm64 (gira nativa sull'M5), schermo 1244×1194 a 200 dpi, senza barra di navigazione Android. Un secondo profilo «lento» (2 core, meno RAM) fa da caso peggiore.
- Limitare la CPU del processo dell'emulatore è stato scartato: produce scatti irregolari, non un processore più lento.
- Misure indipendenti dal processore: dimensione dell'APK, memoria, oggetti creati all'avvio, lavoro sul thread dell'interfaccia, pixel ridisegnati più volte.
- La schermata diagnostica del launcher mostra avvio a freddo, memoria e frame persi: serve alla taratura sul box.

## Aggiornamenti dalle GitHub Releases

**In breve.** Il launcher si aggiorna da solo scaricando l'APK dalle GitHub Releases di questo repository pubblico. Controlla a ogni accensione e dal tasto in Impostazioni; scarica da solo, verifica l'impronta SHA-256 e installa con un tocco, o senza tocco se Android 12 lo permette sul box.

**Dettagli.**

- Il box è su rete mobile e non è raggiungibile dal Mac: l'aggiornamento deve partire dal box.
- Repository pubblico per non dover mettere un token nell'app. Scartati: repository privato con token di sola lettura, server di casa, storage statico.
- Gli indirizzi `releases/latest/download/<file>` sono stabili e non consumano il limite di chiamate dell'API di GitHub.
- Tutte le versioni si firmano con la stessa chiave, tenuta fuori dal repository e con una copia di backup: con una chiave diversa Android rifiuta l'aggiornamento.
- Il controllo degli aggiornamenti gira anche all'avvio del sistema, separato dall'interfaccia, così una versione che si chiude all'avvio si può ancora correggere. Il launcher Ottocast resta installato come riserva.
- Requisito dell'utente (2026-11-18): dopo la prima installazione in macchina, fatta a mano scaricando l'APK dal browser o da Drive, l'app deve essere del tutto indipendente negli aggiornamenti. Il primo aggiornamento chiede una volta il permesso «installa app sconosciute» e la conferma «Aggiorna», perché l'app risulta installata dal browser; dai successivi l'app è l'installatore di sé stessa e `USER_ACTION_NOT_REQUIRED` dovrebbe evitare ogni conferma (da verificare sul box, prove di M01).

## Temi intercambiabili per la Home

**In breve.** La Home ha otto temi, disegnati nella tavola Design del 2026-11-18, e si sceglie quale usare in Impostazioni. Ogni tema costruisce in codice la sua Home e il suo pannello Benzinai; i dati e le azioni sono gli stessi per tutti. Si costruisce solo il tema scelto, così l'avvio non dipende dal numero dei temi.

**Dettagli.**

- I temi cambiano disposizione oltre ai colori (in alcuni domina l'ora, in altri la musica o le app): per questo ogni tema è una classe con le sue view, non un insieme di colori applicato a un layout unico.
- I cinque font che Android non ha (VT323, Orbitron, Limelight, Rajdhani, Audiowide) vanno nell'APK, licenza OFL: l'APK resta sotto 1 MB.
- Le sei app della Home sono fisse: Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok. Spotify si apre dalla scheda del brano.
- Ordine di lavoro: prima le funzioni e il tema Matrix, poi gli altri a gruppi (piano M02).

## Fonti di meteo e prezzi

**In breve.** Il meteo viene da Open-Meteo e i prezzi dei carburanti dagli open data del Ministero (MIMIT). Nessuno dei due chiede una chiave, quindi niente segreti nel repository pubblico. I prezzi si scaricano solo quando si apre il pannello Benzinai, al massimo una volta al giorno, perché i file coprono tutta Italia e il box usa la rete del telefono.

**Dettagli.**

- I due CSV del MIMIT pesano circa 7,5 MB insieme. Dopo il download se ne tiene un file compatto con i soli impianti che hanno la benzina self, e lo si filtra per posizione a ogni apertura: così ci si può spostare senza riscaricare.
- Se il box non ha una posizione, meteo e Benzinai usano una città di riserva scelta in Impostazioni (Open-Meteo Geocoding).
- «Conviene» ordina per il costo di un pieno da 40 litri più andata e ritorno a 6 l/100 km.
