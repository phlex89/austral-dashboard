---
milestone: M05
stato: in-corso
data: 2026-11-22
lettori: entrambi
---

# M05 · Plancia rinnovata e schermate nuove

## In una pagina

Il volume dalla Home non funziona sul box nemmeno dopo le prove con la Diagnostica: si toglie da tutti i temi. Già che si rifanno i layout, entrano insieme tutte le novità che toccano la Home e il pannello Benzinai, così gli otto temi si toccano una volta sola. Scelte dell'utente del 2026-11-22 in `DOMANDE.md`.

| Dove | Cosa cambia |
|---|---|
| Home | Via il volume. Wi‑Fi e Bluetooth: tocco = azione rapida (pannello Wi‑Fi, Bluetooth acceso/spento), «›» accanto = impostazioni complete. Tasto «tema successivo» |
| Home, meteo | Il nome del luogo che si sta guardando, e un blocco «Domani» con cielo, minima e massima |
| Benzinai | «Intorno a me» (riprende il GPS, solo i vicini), «Raggio» che a ogni tocco passa 5 → 10 → 20 → 30 km, tasto «Mappa» |
| Mappa (nuova) | Mappa OpenStreetMap con i distributori e il prezzo, spostamento col dito, + e − |
| Rifornimenti | Segno «pieno completo»; riepilogo con totali, medie, migliore e peggiore, mese per mese e grafico del consumo; prezzo già scritto se si arriva da un distributore; conferma prima di «Togli» |
| Impostazioni → Meteo | Luoghi salvati: «La mia posizione» (GPS) o un luogo fisso; «Aggiungi dove sono»; la città di riserva diventa un luogo salvato |
| Schermate interne | Grafica scura e moderna comune a tutte, accento dal tema; Impostazioni divise in sezioni |

Rimandato: il percorso da A a B con il distributore più conveniente. Arriverà dentro la schermata Mappa, senza toccare di nuovo i temi.

Tre fasi:

1. **Logica e schermate interne**, senza toccare i temi. Si fa subito, mentre l'utente prepara le tavole.
2. **Tavole** da Claude Design (prompt in `FASE-2.md`), fatte dall'utente.
3. **Otto temi**: le tavole portate nella Home e nel pannello Benzinai.

Il piano cambia il manifest (un permesso per il Bluetooth, schermate nuove). Prima di pubblicare si prova sull'emulatore che la versione nuova riceva ancora l'aggiornamento successivo.

Si chiude quando la versione è pubblicata e il box l'ha ricevuta.

## Per chi esegue

**Fase 1** (nessun file in `temi/` tranne `Ui.kt` se serve; il contratto `Tema.kt` non cambia ancora, così i temi compilano).

- *Sistema.* `Sistema.alternaBluetooth(activity)`: `BluetoothAdapter.enable()`/`disable()`, che su API 32 con `targetSdk` 32 chiedono `BLUETOOTH_CONNECT` (permesso a runtime, da aggiungere al manifest); senza permesso lo chiede, se negato apre `Settings.ACTION_BLUETOOTH_SETTINGS`. `Sistema.dettaglioWifi` (`Settings.ACTION_WIFI_SETTINGS`); `apriBluetooth` resta come dettaglio. `volume`, i campi del volume di `StatoSistema` e `VOLUME_CHANGED_ACTION` in `osserva` si tolgono in fase 3, insieme al contratto. Dalla Diagnostica si toglie subito la sezione Audio.
- *Luoghi.* `funzioni/Luoghi.kt`: `SharedPreferences` `luoghi`, chiave `elenco` (nome, lat, lon separati da tabulazione, uno per riga) e chiave `attivo` (vuota = «La mia posizione», altrimenti il nome). Alla prima lettura la città di riserva di `Posizione` (`citta_nome`, `citta_lat`, `citta_lon`) entra in cima all'elenco e le sue chiavi si cancellano. Riserva quando manca il GPS: il primo luogo salvato. `Posizione.cittaDiRiserva` legge da `Luoghi`, e i Benzinai continuano a usarla così. Il luogo attivo vale solo per il meteo.
- *Nome del luogo.* Se il luogo è fisso, il suo nome. Con il GPS: `Geocoder.getFromLocation` (sincrono, su un thread) → `locality`, altrimenti `subAdminArea`. Se il geocoder manca o fallisce, il comune del distributore più vicino in `benzinai.txt`, se il file c'è; altrimenti «Posizione attuale». Il nome si salva con il meteo.
- *Meteo.* La richiesta passa a `forecast_days=2` e aggiunge `weather_code` a `daily`. In `Meteo` entrano `luogo: String` e `domani: Previsione?` (codice, minima, massima), salvati in `SharedPreferences` `meteo` (chiavi nuove), con `daJson` testato. Se il luogo attivo cambia, il meteo è da riscaricare subito: si salva una chiave del luogo (nome o coordinate arrotondate) e `daAggiornare` la confronta. `HomeActivity.aggiornaMeteo`: con un luogo fisso usa le sue coordinate senza chiedere il GPS. Finché i temi non lo mostrano (fase 3), luogo e domani si calcolano e basta.
- *LuoghiActivity* (sostituisce `CittaActivity`, da togliere dal manifest): righe «La mia posizione» e luoghi salvati, ✓ su quello attivo e un tocco per sceglierlo; «Togli» su ogni luogo; «Aggiungi dove sono» (`Posizione.aggiorna`, poi il nome come sopra, poi salva); campo «Cerca un luogo» con `Citta.cerca` e un tocco su un risultato per aggiungerlo.
- *Benzinai.* `DatiMimit.filtra` riceve `raggioKm` e `intornoAMe` (con `intornoAMe` niente preferiti). `RAGGIO_KM` diventa la scelta salvata in `SharedPreferences` `benzinai`, chiave `raggio` (5, 10, 20, 30; predefinito 10, al posto dei 15 fissi di oggi). `MASSIMO_VICINI` resta 30. `ElencoBenzinai.Pronto` porta `raggioKm` e `intornoAMe`, così il tema li mostra. `intornoAMe` non si salva: vale finché il pannello è aperto. Test in `BenzinaiTest`.
- *Mappa.* `MappaActivity` con grafica comune; nel manifest, non esportata. View disegnata in codice: tessere `https://tile.openstreetmap.org/{z}/{x}/{y}.png`, scaricate su un thread con `User-Agent: AustralDashboard/<versionName>` e tenute in `cacheDir/tessere/` (al massimo 30 MB, le più vecchie si cancellano). In memoria una `LruCache` piccola. Nessun prefetch, attribuzione «© OpenStreetMap» sempre visibile. Trascinamento con un dito, + e − (zoom 11–17), tasto «centra su di me». Distributori dall'ultimo elenco del pannello, passato in `StatoBenzinai` o rifatto con `Benzinai.elenco`. Ogni distributore è un'etichetta col prezzo; un tocco apre la scheda (nome, indirizzo, distanza, «Rifornimenti»). Conversione lat/lon ↔ tessera in una funzione pura con test. In fase 1 si apre con `adb shell am start -n` (con `adb root`); il tasto nel pannello arriva in fase 3.
- *Rifornimenti.* Quinto campo `pieno` (1/0). `daRiga` accetta 4 campi (pieno = sì) o 5, `riga` ne scrive 5. Tratte tra due pieni completi consecutivi: km tra i due, litri ed euro di tutti i rifornimenti dopo il primo fino al secondo compreso. Medie dal primo all'ultimo pieno completo; ultimo, migliore e peggiore sulle tratte. Totali: km (ultimo − primo contachilometri), euro, litri, €/l medio (euro / litri). Mese per mese, i più recenti prima: spesa del mese; km del mese = ultimo contachilometri del mese − ultimo del mese precedente con dati (per il primo mese, − il primo del mese). Grafico: `View` disegnata in codice, una barra di km/l per tratta, le ultime 12. Prezzo: `BenzinaiActivity` passa `prezzo` e `nome` del distributore selezionato come extra. In `RifornimentiActivity` compare «Prezzo 1,789 €/l da <nome>» e, scritti gli euro, i litri si calcolano (modificabili). «Togli»: il primo tocco cambia il testo in «Tocca ancora per togliere», che torna com'era dopo 3 s. Test in `RifornimentiTest` (righe vecchie, tratte con un parziale, mesi).
- *Schermate interne.* Mattoni nuovi in `Schermata.kt`, usati da tutte: `intestazione(titolo)` con il tasto indietro (`ic_indietro`), `sezione(titolo)`, `scheda()` (sfondo grigio scuro, angoli da 16 dp), `riga(icona, titolo, valore, azione)` alta almeno 88 dp con «›», `pulsante` rifatto (pieno, accento). Accento: `Temi.scelto(this).palette(this).accento`. Le schermate da rifare: Impostazioni, Temi, Luoghi, Comuni, App della Home, Playlist, Rifornimenti, Diagnostica, Mappa (solo intestazione e tasti). Tutte le app tiene la sua griglia. Impostazioni in sezioni: Aspetto (Tema); Home (App della Home); Meteo (Luogo del meteo); Benzinai (Comuni preferiti); Musica (permesso, se manca; Playlist preferite); Sistema e aggiornamenti (Controlla aggiornamenti con l'esito, Diagnostica, Impostazioni di sistema, Cambia launcher). Le icone nuove vanno in `res/drawable/ic_*.xml`, viewport 48, bianche.

**Fase 3** (dopo le tavole).

- Contratto in `temi/Tema.kt`. `AzioniHome`: via `volume`; entrano `wifiDettaglio`, `bluetoothDettaglio`, `temaSuccessivo`. `bluetooth` ora accende e spegne. `StatoSistema` senza volume; `mostraMeteo` riceve il `Meteo` con `luogo` e `domani` (e se il luogo segue il GPS, per la spia). `AzioniBenzinai`: `intornoAMe`, `raggio`, `mappa`, e `rifornimenti(selezionato: Distributore?)`: il distributore aperto vive nello `StatoBenzinai` privato di ogni tema, quindi il prezzo precompilato dal pannello passa da qui (dalla Mappa arriva già). `BenzinaiActivity` passa `prezzo` e `distributore` come fa `MappaActivity`. `HomeActivity.bluetooth` chiama `Sistema.alternaBluetooth` e gestisce `CODICE_BLUETOOTH` in `onRequestPermissionsResult`; poi si tolgono `Sistema.volume`, i campi del volume e `VOLUME_CHANGED_ACTION`.
- `HomeActivity.temaSuccessivo`: salva il tema dopo quello attuale in `Temi.elenco` (dopo l'ultimo il primo) e chiama `costruisci()` subito.
- Un agente per tema, ognuno solo sul suo file, come in M04. Poi il confronto con le tavole sull'emulatore `box`: Home piena, Home vuota, Benzinai normale, Benzinai «Intorno a me». Infine le misure: avvio a freddo, PSS, frame in ritardo, APK.

**Passi.**

- [x] Proposte, scelte dell'utente, prompt per Claude Design.
- [x] Fase 1: logica, Luoghi, Mappa, Rifornimenti, schermate interne, test, prove sull'emulatore.
- [ ] Tavole aggiornate da Claude Design (utente): da rivedere con la versione già pubblicata.
- [x] Fase 3, prima versione senza tavole (richiesta dell'utente): contratto, otto temi, prove sull'emulatore, misure.
- [ ] Fase 3 con le tavole: confronto e ritocchi.
- [x] Prova della catena di aggiornamento sull'emulatore (manifest cambiato).
- [x] Guida, riferimento, decisioni (volume tolto e perché; luoghi; mappa e tessere OSM).
- [x] Rilascio con `./release.sh minor`: v0.5.0 (prima versione dei temi, senza tavole).
- [ ] Prove sul box (Prove manuali).

**Rischi.**
- Il geocoder di Android potrebbe mancare sul box: c'è il ripiego sul comune dei distributori.
- Le regole delle tessere OSM chiedono un `User-Agent` che identifichi l'app e vietano l'uso pesante: un dispositivo che guarda la mappa ogni tanto va bene. Se un giorno le tessere venissero rifiutate, la mappa resta vuota con l'attribuzione e i puntini.
- `BluetoothAdapter.enable()` è deprecato da API 33, ma il box resta su API 32.

## Prove manuali

| Prova | Esito |
|---|---|
| Il box riceve la 0.5.0 senza conferma | da provare |
| Bluetooth: il tocco accende e spegne (dopo il permesso); «›» apre le impostazioni | da provare |
| Wi‑Fi: il tocco apre il pannello, «›» le impostazioni complete | da provare |
| Il meteo mostra il nome del comune con il GPS (geocoder del box) | da provare |
| Mappa: le tessere si caricano con la rete del box, il trascinamento risponde via CarPlay | da provare |
| Tema successivo: un tocco cambia tema senza ritardi visibili | da provare |

## Diario

- 2026-11-22: niente da fare per il volume, nemmeno con la Diagnostica della 0.4.1 (prove dell'utente). Piano scritto dalle scelte dell'utente (D-1…D-8).
- 2026-11-22: fase 1 fatta, quattro agenti in parallelo (Rifornimenti; Benzinai e Mappa; Luoghi e meteo; schermate interne e Bluetooth) sopra i mattoni comuni di `Schermata.kt`, poi unione e revisione. Correzioni dopo la revisione: «Aggiungi dove sono» non salva più la posizione di ripiego (`Posizione.aggiorna(ripiego = false)`), il comune più vicino è passato in `DatiMimit`. Provate sull'emulatore `box` tutte le schermate interne. Rifornimenti con sette pieni di prova, uno parziale: tratte, grafico, mesi. Mappa su Milano: 36 tessere scaricate, trascinamento, scheda e «Rifornimenti» con 50 € → 28,77 l a 1,738 €/l. Dopo la prima prova la mappa è stata scurita (tessere chiare in mezzo all'interfaccia scura) e le etichette sovrapposte diventano puntini. Meteo con il GPS: salvati «Milano» e domani. Il prezzo precompilato dal pannello Benzinai passa alla fase 3 (serve il contratto dei temi). APK release 537 KB.
- 2026-11-22: l'utente chiede di non aspettare le tavole e di pubblicare: prima versione della fase 3 con la disposizione decisa tema per tema, un agente per tema, poi confronto a vista sull'emulatore `box` (Home con meteo completo e Benzinai, otto temi). Corretti dopo le foto: pioggia tagliata in Matrix, ora tagliata in alto in Art Déco (riga alta da 262 a 240 px: ora da 160 a 140 px), testo di «Intorno a me» non centrato in Synthwave; «Intorno a me» filtra subito invece di aspettare il GPS (fino a 30 s). Provati «Intorno a me», il giro del raggio, «Tema» (da Synthwave a Matrix). Misure in release (Matrix): avvio a freddo 122–135 ms, PSS 25,6 MB, 0 frame in ritardo, APK 562 KB. Catena: questa versione con `versionCode` 7, installata come installatrice di sé stessa, ha ricevuto da sola la 0.4.1 pubblicata senza conferma.
- 2026-11-22: pubblicata la v0.5.0 (codice 9, 562 KB); l'emulatore `box` l'ha ricevuta da Impostazioni → Controlla aggiornamenti. Restano le tavole da rivedere con l'utente e le prove sul box.
