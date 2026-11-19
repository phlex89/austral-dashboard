---
milestone: M04
stato: in-corso
data: 2026-11-19
lettori: entrambi
---

# M04 · Funzioni di bordo

## In una pagina

Cinque funzioni nuove, scelte dall'utente il 2026-11-19 tra le proposte per l'uso in auto, anche a motore spento.

| Funzione | Dove si vede | Cosa fa |
|---|---|---|
| Registro dei rifornimenti | Benzinai → Rifornimenti | A ogni pieno euro, litri e contachilometri; spesa del mese, consumo medio, € ogni 100 km |
| Andamento dei prezzi | Benzinai, accanto al prezzo | Freccia su o giù con la differenza rispetto al download precedente |
| Scorciatoie di sistema | Home | Wi‑Fi e Bluetooth con il loro stato, volume con «−» e «+» |
| Playlist preferite | Home, vicino alla musica | Fino a tre; un tocco le fa partire in Spotify. Si aggiungono da Spotify con Condividi |
| Pioggia in arrivo | Home, nel meteo | «Piove» o «Pioggia tra 20 min», solo se arriva entro due ore |

Scartate per ora: scadenze dell'auto (tenuta in `doc/idee/scadenze-auto.md`), dati consumati, destinazioni preferite, app recenti, tema giorno e notte.

Due fasi:

1. **Logica e schermate secondarie**, che non dipendono dalla grafica: pioggia, prezzo precedente, registro, playlist, stato del sistema, con i test.
2. **Otto temi**: le tavole aggiornate da Claude Design (prompt in `FASE-2.md`) portate in ogni tema, Home e Benzinai.

Il piano aggiunge al manifest una schermata che riceve le condivisioni e alcuni permessi: prima di pubblicare si prova sull'emulatore che la versione nuova riceva ancora l'aggiornamento successivo.

Si chiude quando la versione è pubblicata e il box l'ha ricevuta.

## Per chi esegue

**Fase 1** (nessun file in `temi/`, nessuna modifica a `HomeActivity` e `BenzinaiActivity`).

- Pioggia: `FonteMeteo.scarica` chiede `minutely_15=precipitation&forecast_minutely_15=12`; serie salvata in `SharedPreferences` `meteo`; `Meteo.pioggiaTraMinuti` (0 = piove ora, N minuti a passi di 5, `null` = niente entro 120 minuti), calcolato in `ultimo` con l'ora attuale. Un passo è pioggia da 0,1 mm.
- Prezzo precedente: `Benzinai.scarica` tiene il file vecchio come `benzinai-prima.txt`; `Distributore.prezzoPrima`; `DatiMimit.prezziDi` legge in streaming solo gli id richiesti; cache in memoria per id, svuotata da `libera` e da un download nuovo.
- Registro: `funzioni/Rifornimenti.kt`, `filesDir/rifornimenti.txt` (una riga per pieno, tabulazioni); consumo da pieno a pieno ordinando per km; `RifornimentiActivity`.
- Playlist: `funzioni/Playlist.kt`, `SharedPreferences` `playlist`, al massimo tre; link `open.spotify.com/<tipo>/<id>` aperto come `spotify:<tipo>:<id>:play`; `PlaylistActivity` esportata con `ACTION_SEND` `text/plain` e, aperta da Impostazioni, l'elenco con «Togli»; `<queries>` per lo schema `spotify`.
- Sistema: `funzioni/Sistema.kt`, `StatoSistema` (Wi‑Fi spento, acceso o connesso; Bluetooth acceso; volume `STREAM_MUSIC`), `apriWifi` (`Settings.Panel.ACTION_WIFI`), `apriBluetooth`, `volume(su)`, `osserva` per i cambi.

**Fase 2** (dopo le tavole aggiornate).

- Contratto in `temi/Tema.kt`: `VistaHome.mostraSistema(StatoSistema)`; le playlist arrivano alla costruzione della Home e cambiano la firma che la fa ricostruire (come `AppHome.firma`); `AzioniHome` con `wifi`, `bluetooth`, `volume(su)`, `playlist(i)`; `AzioniBenzinai.rifornimenti`; il riepilogo della spesa del mese al pannello Benzinai.
- `HomeActivity`: `Sistema.osserva` tra `onStart` e `onStop`. Un agente per tema, ognuno solo sul suo file; poi il confronto a vista con le tavole sull'emulatore `box` e le misure (avvio a freddo, PSS, frame in ritardo).

**Passi.**

- [x] Proposte, scelta dell'utente, idea delle scadenze, prompt per Claude Design.
- [x] Fase 1: logica, schermate secondarie, test.
- [x] Tavole aggiornate da Claude Design (utente).
- [x] Fase 2: otto temi, prove sull'emulatore, misure.
- [x] Prova della catena di aggiornamento sull'emulatore.
- [x] Guida, riferimento, decisioni.
- [x] Rilascio con `./release.sh minor`: v0.4.0.
- [ ] Prove sul box (Prove manuali).

**Rischi.** Il suffisso `:play` per far partire la playlist non è documentato da Spotify e l'emulatore non ha Spotify: si prova solo sul box; se non parte, la playlist si apre e serve un tocco su play. Non si sa se il volume del box cambia davvero l'audio verso l'auto o se conta solo quello dell'auto.

## Prove manuali

| Prova | Esito |
|---|---|
| Il box riceve la 0.4.0 senza conferma | da provare |
| Una playlist condivisa da Spotify si salva e parte da sola con un tocco (`:play`) | da provare |
| Il pannello Wi‑Fi si apre sopra la Home; Bluetooth apre le sue impostazioni | da provare |
| «−» e «+» cambiano il volume che si sente in auto | non funziona (2026-11-19): la Home mostra il volume al massimo e «−»/«+» non cambiano né il numero né l'audio. Da rifare con la Diagnostica della 0.4.1 |
| Diagnostica → Audio: volume fisso, livelli, uscite; prove A, B, C; cambi dalla barra Ottocast e dalla manopola | da provare |
| La pioggia compare nel meteo quando arriva entro due ore | da provare |
| Rifornimenti: tastiera del box con la virgola, consumo dal secondo pieno | da provare |
| Le frecce dei prezzi compaiono nei Benzinai dal secondo giorno di download | da provare |

## Diario

- 2026-11-19: piano scritto dalle scelte dell'utente; avviata la fase 1.
- 2026-11-19: fase 1 fatta. Provate sull'emulatore `box` la condivisione di un link Spotify (nome ricavato dal testo, salvato) e la schermata Rifornimenti (tre pieni: 17,1 km/l, 9,84 € ogni 100 km). Corretti dopo la prova: i campi decimali scartavano la virgola (il tastierino accetta solo il separatore della lingua del sistema) e la tastiera restava aperta sopra il riepilogo dopo «Salva». La pioggia non si è vista sull'emulatore (posizione finta negata dopo `adb root`): controllata la risposta reale di Open‑Meteo, che ha il formato letto dai test. APK 472 KB. Stato di Wi‑Fi, Bluetooth e volume non ancora collegato: arriva con i temi.
- 2026-11-19: fase 2 fatta. Tavole aggiornate da Claude Design per tutti e otto i temi; contratto comune (`mostraSistema`, `mostraSpesa`, azioni per Wi‑Fi, Bluetooth, volume, playlist, rifornimenti), nove icone condivise, poi un agente per tema. Confronto con le tavole sull'emulatore `box` in quattro stati per tema (tutto acceso, stati vuoti, Benzinai, scheda del distributore aperta) e un giro di correzioni per tema: posizioni di ora e titoli, testi rimpiccioliti da `Ui.adatta`, distanze allineate a destra, spie del volume tagliate in Solari. Le ★ dei preferiti restano fuori (D-6). Misure in release: avvio 160–267 ms, PSS 25–29 MB, 0 frame in ritardo, APK 511 KB. Prova della catena: una build di questa versione con `versionCode` 5, installata come installatore di sé stessa, ha ricevuto da sola la 0.3.1 pubblicata senza conferma; firma uguale a quella della release. Durante le prove lo schermo dell'emulatore era tornato verticale dopo il riavvio (rotazione non più bloccata): annotato in `RIFERIMENTO.md`.
- 2026-11-19: pubblicata la v0.4.0 (codice 7, 511 KB); l'emulatore `box` l'ha ricevuta da Impostazioni → Controlla aggiornamenti. Resta la prova sul box.
- 2026-11-19: prova in auto dell'utente: il volume non funziona. La Home mostra il massimo e «−»/«+» non cambiano niente. Ipotesi: il box ha il volume fisso (Android tiene i canali al massimo e il volume lo regola solo l'auto), oppure l'audio verso CarPlay passa da un canale o da un'uscita che `STREAM_MUSIC` non governa. Prima di correggere servono dati dal box: nella 0.4.1 Diagnostica ha la sezione Audio (volume fisso, livelli, uscite, tre modi di abbassare il volume, registro dei cambi fatti dalla barra Ottocast o dalla manopola).
- 2026-11-19: pubblicata la v0.4.1 (codice 8) con la Diagnostica audio.
