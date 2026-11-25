# Austral Dashboard: decisioni

Perché il progetto è fatto così. Una voce `##` per decisione, con la più recente in fondo.

## Indice

<!-- indice -->
- [Kotlin e View native, senza dipendenze](#kotlin-e-view-native-senza-dipendenze)
- [Sviluppo e test solo sul Mac](#sviluppo-e-test-solo-sul-mac)
- [Aggiornamenti dalle GitHub Releases](#aggiornamenti-dalle-github-releases)
- [Temi intercambiabili per la Home](#temi-intercambiabili-per-la-home)
- [Fonti di meteo e prezzi](#fonti-di-meteo-e-prezzi)
- [Dettagli dei temi legati ai dati](#dettagli-dei-temi-legati-ai-dati)
- [Aggiornatore in un processo suo, senza conferma](#aggiornatore-in-un-processo-suo-senza-conferma)
- [App della Home e comuni preferiti scelti in Impostazioni](#app-della-home-e-comuni-preferiti-scelti-in-impostazioni)
- [Niente interruttori per meteo e musica](#niente-interruttori-per-meteo-e-musica)
- [La Home non blocca l'orientamento](#la-home-non-blocca-l-orientamento)
- [«Aggiorna app» apre il Play Store](#aggiorna-app-apre-il-play-store)
- [Funzioni di bordo: come funzionano e perché](#funzioni-di-bordo-come-funzionano-e-perche)
- [Luoghi del meteo e mappa dei benzinai](#luoghi-del-meteo-e-mappa-dei-benzinai)
- [Niente volume dal launcher, Bluetooth diretto](#niente-volume-dal-launcher-bluetooth-diretto)
- [Il testo delle canzoni nel launcher, da LRCLIB](#il-testo-delle-canzoni-nel-launcher-da-lrclib)
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
- I preferiti sono tre comuni scelti dall'utente, fissi nel codice (Torino, Cellamare, Valenzano), non distributori: un comune ha molti distributori e quello più economico cambia di giorno in giorno. Di ognuno si mostra il più economico, anche se è lontano (D-6 del piano M02).

## Dettagli dei temi legati ai dati

**In breve.** Ogni dettaglio grafico dei temi porta un'informazione: giorno della settimana, ore passate, temperatura tra minima e massima, musica in riproduzione, posizione nell'elenco dei Benzinai. Nel launcher ogni dettaglio legge il dato vero e si aggiorna con l'ora o con il meteo; senza il dato resta spento, mai con un valore inventato.

**Dettagli.**

- Decisione dell'utente (2026-11-18): le tavole dei temi si arricchiscono di dettagli solo se ognuno porta un'informazione; la scheda `t<n>spec` di ogni tema li elenca nella sezione DETTAGLI.
- Gli indicatori si ridisegnano in `mostraOra` (a ogni minuto) e `mostraMeteo`, senza timer propri, per non pesare sul box.
- Le scale della temperatura vanno dalla minima alla massima del giorno; con minima uguale alla massima, o senza meteo, restano spente.
- Le ★ delle tavole restano fuori: i preferiti sono comuni fissi (D-6), non distributori da salvare.
- Costo misurato in release: 2–3 MB di PSS in più per tema, avvio a freddo invariato.

## Aggiornatore in un processo suo, senza conferma

**In breve.** Il controllo degli aggiornamenti gira in un processo separato dalla Home (`:aggiornamenti`), parte un minuto dopo l'accensione e poi ogni sei ore, e il manifest dichiara il permesso `UPDATE_PACKAGES_WITHOUT_USER_ACTION`. Così una versione che si chiude all'avvio si corregge da sola e, dopo il primo aggiornamento confermato a mano, Android non chiede più niente.

**Dettagli.**

- Prova del 2026-11-19 sull'emulatore, con una build che lancia un'eccezione in `HomeActivity.onCreate`: con l'aggiornatore nel processo della Home, Android segna il processo come guasto e `BOOT_COMPLETED` non arriva più, quindi l'aggiornamento non parte. Nel processo separato parte e installa la versione pubblicata.
- Senza il permesso nel manifest Android 12 chiede sempre «Aggiornare?», anche quando l'app è l'installatore di sé stessa: le prove precedenti lo attribuivano all'installazione con `adb`.
- Nei primi 30 secondi dall'accensione Android non installa senza conferma: per questo il controllo di avvio aspetta 60 secondi.
- Il controllo periodico copre il caso, non ancora verificato sul box, in cui il box si risveglia invece di riavviarsi.
- Costo: un secondo processo, vivo solo durante un controllo. L'esito dell'ultimo controllo sta in un file di preferenze letto da tutti e due i processi.
- Il pulsante «Controlla aggiornamenti» resta nel processo della Home: due controlli contemporanei nei due processi sono possibili e innocui (la seconda installazione sostituisce la prima).

## App della Home e comuni preferiti scelti in Impostazioni

**In breve.** Le sei app della Home e i comuni preferiti dei Benzinai non sono più fissi nel codice: si scelgono in Impostazioni, con soli tocchi. Le app partono dalle sei di prima; i comuni partono vuoti, perché sono un dato personale e il repository è pubblico.

**Dettagli.**

- Sostituisce le due scelte precedenti («le sei app della Home sono fisse», «i preferiti sono tre comuni fissi nel codice»). Resta vero che i preferiti sono comuni e non distributori (D-6 del piano M02), e che sono al massimo tre.
- I comuni si cercano nel file dei prezzi già scaricato, non in rete: i nomi sono quelli del MIMIT, quindi il confronto con gli impianti è esatto.
- La Home si ricostruisce quando cambia l'elenco o quando un'app viene installata o tolta: prima un'app installata dopo l'avvio restava spenta.

## Niente interruttori per meteo e musica

**In breve.** Valutato e scartato il 2026-11-19: un sistema di moduli attivabili dalle Impostazioni. Meteo e musica restano sempre attivi, perché l'utente li vuole sempre e il codice è già pigro: tutto si ferma quando la Home non è visibile e i Benzinai lavorano solo a pannello aperto.

**Dettagli.**

- Scartati nello stesso giro, per scelta dell'utente: il tasto «portami qui» nella scheda del distributore (il navigatore non si usa dal box) e le azioni automatiche all'accensione.

## La Home non blocca l'orientamento

**In breve.** Nel manifest `HomeActivity` non dichiara più `screenOrientation="nosensor"`. Sul box quel blocco faceva rimpicciolire la Home da Android, con due bande grigie ai lati; senza, la Home occupa tutta la larghezza. Il box non ruota lo schermo, quindi il blocco non serviva.

**Dettagli.**

- Visto sul box il 2026-11-19, in tutti i temi. Riprodotto sull'emulatore `box` con un display verticale ruotato in orizzontale (`wm size 1194x1244`, `cmd window user-rotation lock 1`) e `cmd window set-ignore-orientation-request true`: con `nosensor` Android chiede l'orientamento naturale del display e mette la Home in una finestra di 1146×1194 con le bande ai lati (`areBoundsLetterboxed=true`); senza, la finestra è 1244×1194.
- È un'ipotesi su come è configurato il display del box, confermata solo dal fatto che il sintomo è lo stesso: la prova vera è la versione successiva sul box.
- Le altre activity non avevano il blocco.

## «Aggiorna app» apre il Play Store

**In breve.** Un'app normale non può aggiornare le altre app: solo il Play Store può farlo. Il tasto «Aggiorna app», in Tutte le app, apre quindi il Play Store sulla pagina delle app installate, dove basta toccare «Aggiorna tutto». Sta in Tutte le app e non sulla Home per non ridisegnare le otto tavole.

**Dettagli.**

- La pagina si apre con l'intent `com.google.android.finsky.VIEW_MY_DOWNLOADS`, non documentato ma dichiarato dal Play Store (verificato nella versione 50.7 su un emulatore con Play Store). Se un giorno sparisce, il tasto apre il Play Store e basta.
- Se il Play Store non c'è, il tasto non compare.

## Funzioni di bordo: come funzionano e perché

**In breve.** Le funzioni di M04 restano senza permessi speciali e senza dati nuovi da scaricare. Wi‑Fi e Bluetooth aprono i pannelli di Android, perché da Android 10 un'app non può accenderli o spegnerli da sola. Le playlist si aggiungono condividendole da Spotify, perché scrivere un link sullo schermo dell'auto è scomodo. La freccia del prezzo confronta con il download precedente, che il launcher già fa. Il registro dei pieni e la pioggia usano solo dati salvati sul box o la stessa richiesta del meteo.

**Dettagli.**

- Scelte dell'utente del 2026-11-19 tra le proposte: registro dei rifornimenti, andamento dei prezzi, scorciatoie Wi‑Fi, Bluetooth e volume, playlist preferite, pioggia in arrivo. Scartate: dati consumati, destinazioni preferite (il navigatore non si usa dal box), app recenti, tema giorno e notte. Le scadenze dell'auto restano un'idea (`doc/idee/scadenze-auto.md`).
- Playlist: un link `open.spotify.com/<tipo>/<id>` si apre come `spotify:<tipo>:<id>:play`. Il suffisso `:play` non è documentato da Spotify; senza, la playlist si apre ma non parte. Se sul box non funziona, resta un tocco su play.
- Volume: cambia `STREAM_MUSIC` del box, senza la barra di sistema, perché il livello è già sulla Home. Non è provato che il volume del box cambi l'audio che arriva all'auto via CarPlay.
- Andamento: il confronto è con il penultimo download (`benzinai-prima.txt`), non con «ieri»: i prezzi si scaricano solo quando si apre Benzinai, al massimo una volta al giorno. Molti distributori comunicano il prezzo di rado, quindi la freccia compare su pochi.
- Rifornimenti: consumo da pieno a pieno, perché senza il carburante nel serbatoio è l'unico calcolo onesto; il file resta sul box, mai nel repository.
- Pioggia: `minutely_15` di Open‑Meteo nella stessa richiesta del meteo, 12 passi da 15 minuti; da 0,1 mm in un passo conta come pioggia. La serie salvata permette di aggiornare «tra N minuti» senza riscaricare.
- Grafica: le tavole degli otto temi sono state aggiornate da Claude Design con il prompt in `doc/piani/M04-funzioni-di-bordo/FASE-2.md`; le ★ dei preferiti per distributore restano fuori come in M02.

## Luoghi del meteo e mappa dei benzinai

**In breve.** Il meteo segue il GPS oppure un luogo salvato, scelto in Impostazioni; il primo luogo salvato fa da riserva quando manca il GPS, e la vecchia città di riserva è diventata quel primo luogo. La mappa dei benzinai è disegnata dall'app con le tessere di OpenStreetMap, perché il progetto non usa librerie; è una schermata con la grafica comune, così il percorso da A a B potrà arrivarci senza ridisegnare gli otto temi.

**Dettagli.** Scelte dell'utente del 2026-11-22 (D-3 e D-6 di M05). Il nome del luogo che segue il GPS viene dal geocoder di Android; se manca, dal comune del distributore più vicino nel file dei prezzi, che è già sul box. Le tessere OSM sono gratuite per un uso leggero, con un `User-Agent` che identifica l'app (solo nome e versione, nessun dato dell'utente) e l'attribuzione visibile; la cache sul box (al massimo 30 MB, sette giorni) evita di riscaricarle. Le tessere erano scurite con un filtro di colore, per non abbagliare; il 2026-11-23, dopo la prova in auto, l'utente le ha volute nello stile stradale originale, perché scurite sembravano un satellite e si leggevano male. Sempre dopo la prova: la mappa segue la posizione e può ruotare con la direzione di marcia (tasto bussola), perché a nord fisso, andando verso sud, era capovolta rispetto alla strada. Nelle zone fitte un'etichetta che coprirebbe un'altra già disegnata diventa un puntino, e le più economiche si disegnano per prime.

## Niente volume dal launcher, Bluetooth diretto

**In breve.** Il volume è stato tolto dalla Home: sul box nessun modo di cambiarlo dal launcher ha effetto, e l'audio verso l'auto lo regola solo l'auto. Il Bluetooth invece ora si accende e si spegne con un tocco, perché su Android 12 un'app può farlo con il permesso `BLUETOOTH_CONNECT`; il Wi‑Fi no, e resta il pannello di Android. Accanto a entrambi un «›» apre le impostazioni complete.

**Dettagli.** Prove in auto dell'utente con la Diagnostica della 0.4.1 (2026-11-22): né `adjustStreamVolume`, né `adjustSuggestedStreamVolume`, né il tasto volume simulato cambiavano l'audio. La sezione Audio della Diagnostica serviva solo a questo ed è stata tolta. Lo spazio liberato ospita i «›» e il tasto Tema (D-2 di M05). `BluetoothAdapter.enable()` è deprecato da API 33, ma il box resta su API 32. Questa voce aggiorna «Funzioni di bordo» per il Bluetooth.

## Il testo delle canzoni nel launcher, da LRCLIB

**In breve.** Deciso il 2026-11-25: il tasto Testo apre una schermata del launcher, non la vista del testo di Spotify. Spotify non ha un link pubblico per aprirla, e premere il suo tasto con l'accessibilità si romperebbe a ogni aggiornamento. I testi arrivano da LRCLIB: gratuito, senza chiave né account, spesso con i tempi di ogni riga, e funziona con qualsiasi app musicale.

**Dettagli.**

- `spotify:track:<id>` apre la pagina del brano, non il testo; nella sessione media non c'è nessun riferimento al testo.
- LRCLIB si interroga con titolo, artista e durata, che la sessione media dà già; la posizione per seguire il testo viene da `PlaybackState`, quindi niente permessi nuovi.
- Limite: LRCLIB non ha tutti i brani, soprattutto quelli italiani meno noti; in quel caso la schermata lo dice.
- Seguire il testo si ferma appena l'utente scorre a mano, e riparte solo col tasto: un aggancio a tempo farebbe saltare la pagina mentre si legge.
