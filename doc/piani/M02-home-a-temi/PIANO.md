---
milestone: M02
stato: in-corso
data: 2026-11-18
lettori: entrambi
---

# M02 · La Home vera, in otto temi

## In una pagina

La Home provvisoria di M01 diventa quella disegnata nella tavola «Launcher auto — 8 temi Home» (artifact Design `FB5G7dpEA4q1GcwEvhxoXL`). I temi sono otto e si sceglie quale usare in Impostazioni › Tema; la scelta vale subito.

| Tema | Carattere | Cosa domina |
|---|---|---|
| 1 Matrix | terminale a fosfori verdi, riquadri alla tmux | ora |
| 2 HUD futuristico | cruscotto sci-fi, quadrante a 60 tacche | ora |
| 3 Brutalismo | blocchi grezzi, tipografia enorme | app |
| 4 Art Déco | oro su nero, trittico simmetrico | ora |
| 5 Solari | tabellone partenze a palette | ora |
| 6 Cyberpunk | giallo neon, angoli smussati | musica |
| 7 Braun | apparecchio Dieter Rams, tasti tondi | app |
| 8 Synthwave | tramonto anni '80, orizzonte | musica |

I temi cambiano l'aspetto e la disposizione, non le funzioni. Ogni tema ha le stesse tre schermate:

- **Home con musica**: ora e data, meteo (attuale, minima, massima), il brano in ascolto con copertina e comandi, sei app (Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok), quattro servizi.
- **Home senza musica**: al posto del brano, «Apri Spotify» e un tasto play.
- **Benzinai**: distributori vicini ordinati per «conviene», prezzo o distanza; in cima i preferiti, tre comuni con il loro distributore più economico (D-6); la scheda del distributore toccato.

I quattro servizi sono Benzinai, Tutte le app, Impostazioni di Android, Impostazioni del launcher.

Le funzioni nuove, uguali per tutti i temi:

| Funzione | Da dove arriva |
|---|---|
| Musica | Le sessioni media di Android; serve una volta il permesso «accesso alle notifiche» |
| Meteo | Open-Meteo via internet, ogni 30 minuti |
| Benzinai | I prezzi pubblici del Ministero (MIMIT), con la posizione del box |
| Tutte le app | Una griglia con tutte le app installate |

Si lavora a tappe: prima le funzioni e il selettore con il tema Matrix, poi gli altri temi a gruppi. Le decisioni prese sono in `DOMANDE.md`.

Si chiude quando tutti e otto i temi funzionano sull'emulatore `box`, l'avvio a freddo resta sotto il mezzo secondo e la prova sul box è fatta.

## Per chi esegue

**Riferimento grafico.** Le tavole stanno nell'artifact Design: `project/T<n>Home.dc.html` (Home; la variante senza musica è la stessa tavola con `music=false`, anche come tavola a sé in `project/T<n>HomeB.dc.html`), `project/T<n>Benzinai.dc.html`, e in `project/canvas.json` le note `t<n>spec` con palette, font, raggi e misure dei bersagli di ogni tema. Le misure sono in px a 1244×1194: in Kotlin si usano i px così come sono (il box è fisso a 1244×1194, 200 dpi), con un helper `px(Int)` che non converte.

**Struttura.**

- `temi/Tema.kt`: `interface Tema { val id: String; val nome: String; val idea: String; fun home(s: Schermata, azioni: AzioniHome): VistaHome; fun benzinai(s: Schermata, azioni: AzioniBenzinai): VistaBenzinai }`. `VistaHome` espone `view`, `mostraMeteo(Meteo?)`, `mostraMusica(Musica?)`; `VistaBenzinai` espone `view`, `mostra(ElencoBenzinai)`. L'ora è un `TextClock`, si aggiorna da sé.
- `temi/Temi.kt`: l'elenco degli otto in ordine e `scelto(context)`, letto da `SharedPreferences` `impostazioni`, chiave `tema`, predefinito `matrix`.
- Un file per tema: `temi/Matrix.kt`, `Hud.kt`, `Brutalismo.kt`, `ArtDeco.kt`, `Solari.kt`, `Cyberpunk.kt`, `Braun.kt`, `Synthwave.kt`. Costruiscono le view in codice, come oggi, senza XML di layout.
- `temi/Grafica.kt`: i pezzi grafici che più temi riusano: bordi e riempimenti (`GradientDrawable`), angoli smussati a 45° (`Drawable` con `Path`), gradiente lineare, paletta Solari con giunzione, cornice doppia con rombi, quadrante a 60 tacche (`View` che disegna su `Canvas`), barra a celle del meteo.
- Icone: le SVG delle tavole come `VectorDrawable` in `res/drawable/` (meteo per codice WMO, precedente, play, pausa, successivo, benzinaio, griglia, cursori, ingranaggio, chiudi, indietro), colorate dal tema con `setTint`.
- Font: VT323, Orbitron, Limelight, Rajdhani (solo i pesi 600 e 700 della tavola), Audiowide in `res/font/` (licenza OFL), caricati con `resources.getFont`; ognuno arriva con il suo tema. Gli altri sono di sistema: `sans-serif-condensed` per Roboto Condensed, `serif` per Noto Serif, `monospace` per Roboto Mono, `sans-serif-light` per Roboto Light.
- `HomeActivity`: costruisce la Home del tema scelto; in `onResume` la ricostruisce se il tema è cambiato. Registra gli ascoltatori di musica e meteo in `onStart` e li toglie in `onStop`.
- `BenzinaiActivity`: pannello Benzinai del tema scelto.
- `TutteLeAppActivity`: griglia delle app con `ACTION_MAIN` + `CATEGORY_LAUNCHER`, ordinate per nome, nei colori del tema scelto (sfondo, testo, accento: `Tema` espone una piccola `Palette`).
- `ImpostazioniActivity`: aggiunge «Tema» (apre `TemiActivity`), «Consenti la lettura della musica» (apre `ACTION_NOTIFICATION_LISTENER_SETTINGS`, nascosto se già concesso) e «Città di riserva» (apre `CittaActivity`: si scrive il nome, Open-Meteo Geocoding propone le città, un tocco salva nome e coordinate).
- `TemiActivity`: elenco degli otto temi, una riga grande per tema con nome e idea, il corrente segnato; un tocco salva e torna indietro.

**Funzioni.**

- Musica (`funzioni/Musica.kt`): `NotificationListenerService` vuoto (`AscoltoNotifiche`) dichiarato nel manifest, solo per avere il diritto di chiamare `MediaSessionManager.getActiveSessions`. Si segue la sessione in riproduzione, altrimenti la più recente: titolo, artista, copertina (`METADATA_KEY_ALBUM_ART` o `ART`), stato, nome dell'app. Comandi con `transportControls`. Senza sessione la Home mostra lo stato «senza musica»: «Apri Spotify» apre l'app, play apre Spotify e invia `KEYCODE_MEDIA_PLAY`. Il nome dell'app nelle etichette viene dalla sessione, non è fisso «Spotify».
- Meteo (`funzioni/Meteo.kt`): `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1`; nessuna chiave. Ultimo valore in `SharedPreferences` per mostrarlo subito all'avvio a freddo; nuovo controllo ogni 30 minuti mentre la Home è visibile. Codici WMO → descrizione italiana e icona.
- Posizione (`funzioni/Posizione.kt`): `LocationManager`, provider `fused`, poi `gps`, poi `network`; `getLastKnownLocation` subito, `getCurrentLocation` in seguito. Permesso `ACCESS_FINE_LOCATION`, chiesto al primo uso. Senza posizione si usa la città di riserva; senza nemmeno quella, meteo e Benzinai dicono che manca la posizione.
- Benzinai (`funzioni/Benzinai.kt`): i due CSV del MIMIT, `anagrafica_impianti_attivi.csv` e `prezzo_alle_8.csv` (`https://www.mimit.gov.it/images/exportCSV/`, separatore `|`, aggiornati ogni mattina; indirizzi e formato da verificare all'inizio del lavoro). Si scaricano solo all'apertura del pannello e al massimo una volta al giorno; si leggono in streaming e si salva un file compatto con tutti gli impianti che hanno il prezzo «Benzina» self (l'Austral va a benzina). A ogni apertura si filtra il file compatto: impianti entro 15 km dalla posizione, più i preferiti. Ordinamenti: «Prezzo», «Distanza» e «Conviene» = costo di 40 l più andata e ritorno a 6 l/100 km, al prezzo di quel distributore. Preferiti (D-6): tre comuni fissi nel codice, Torino, Cellamare e Valenzano; per ognuno il distributore con la benzina self più economica, in cima anche se oltre i 15 km, con il nome del comune come titolo della riga. Niente ★ sui distributori. Il parser è logica non banale: va coperto da unit test (righe valide, campi mancanti, virgola decimale, impianto senza prezzo self).
- App della Home: fisse, Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok. Un'app non installata resta visibile ma spenta. Spotify si apre toccando la scheda del brano.

**Prestazioni.** Si costruisce solo il tema scelto. Nessun lavoro di rete o di disco sul thread dell'interfaccia. Obiettivi sull'emulatore `box`: avvio a freddo sotto i 500 ms, PSS sotto i 40 MB, 0 frame in ritardo a riposo. APK sotto 1 MB con i cinque font.

**Documentazione.** Nello stesso commit del comportamento: `GUIDA.md` (scegliere il tema, permessi musica e posizione, Benzinai), `RIFERIMENTO.md` (file nuovi, fonti dei dati, permessi), `DECISIONI.md` (temi intercambiabili; fonti di meteo e prezzi).

**Passi.**

- [x] Risposte alle domande di `DOMANDE.md`.
- [x] Struttura dei temi, `TemiActivity`, voce in Impostazioni, preferenza `tema`.
- [x] Tema Matrix completo con tutte le funzioni: musica, meteo, posizione, app, Tutte le app, Benzinai (con test del parser).
- [x] Temi 2–4: HUD, Brutalismo, Art Déco.
- [x] Temi 5–8: Solari, Cyberpunk, Braun, Synthwave.
- [x] Misure sull'emulatore `box` e `box-lento` per ogni tema; confronto con le tavole.
- [x] Rilascio: v0.2.0.
- [x] Dettagli nuovi delle tavole (seconda versione) negli otto temi.
- [x] Rilascio: v0.2.1.
- [ ] Prova sul box (Prove manuali), prevista il 2026-11-19.

## Prove manuali

| Prova | Esito |
|---|---|
| Ogni tema si sceglie da Impostazioni e la Home cambia subito | da provare |
| Musica: brano, copertina e comandi con Spotify in CarPlay | da provare |
| Meteo con la posizione del box | da provare |
| Benzinai: elenco, ordinamenti, preferiti, scheda | da provare |
| Leggibilità al sole e tocchi via CarPlay per ogni tema | da provare |
| Avvio a freddo e memoria sul box, tema per tema | da provare |

## Diario

- 2026-11-18: piano scritto dalle tavole degli otto temi; risposte alle cinque domande.
- 2026-11-18: prima tappa fatta. Struttura dei temi, selettore, funzioni (musica, meteo, posizione, città di riserva, Benzinai, Tutte le app) e tema Matrix. Sull'emulatore `box`: APK 174 KB, PSS 23 MB, avvio a freddo 152–196 ms, 0 frame in ritardo; meteo e Benzinai provati con una posizione finta a Roma, prezzi reali scaricati in circa 4 s. Non provati: musica con un brano vero (sull'emulatore non c'è Spotify) e `box-lento`. Scostamenti: nei servizi del tema Matrix niente icone, perché accanto al tasto F e all'etichetta non ci stavano; la scheda del distributore ha altezza fissa (270 px) sotto l'elenco che scorre; i distributori senza aggiornamenti recenti mostrano la data vera (anche settimane prima).
- 2026-11-18: temi 2–4 fatti (HUD, Brutalismo, Art Déco) e provati sull'emulatore `box` con la Home senza musica e il pannello Benzinai. Avvio a freddo 141–226 ms per tutti; PSS 24 MB, HUD 34 MB; 0 frame in ritardo; APK 291 KB. Correzioni dopo la prova: testi centrati nel quadrante HUD e nell'ora Art Déco; `Ui.adatta` per i testi che si rimpiccioliscono (l'ellissi bloccava l'adattamento); larghezze fisse nella riga del meteo Art Déco. Scostamenti: in HUD l'etichetta della musica non mostra il nome dell'app (non ci stava); in Art Déco i nomi dei distributori sono in maiuscolo. Nuovo vincolo dell'utente: dopo la prima installazione a mano gli aggiornamenti devono essere del tutto autonomi (`CLAUDE.md`).
- 2026-11-18: prova della catena di aggiornamento con il codice dei temi 1–4, senza pubblicare. Build di prova con `versionCode` 1 sull'emulatore `box`: «Controlla aggiornamenti» ha trovato la 0.1.1 su GitHub, l'ha scaricata, verificata e installata dopo la conferma «Update» (l'APK di prova era stato installato con `adb`); l'app risulta poi installata da sé stessa. La build nuova ha lo stesso certificato della 0.1.1 (SHA-256 `7412986f…9e36`) e si installa sopra di essa.
- 2026-11-18: temi 5–8 fatti (Solari, Cyberpunk, Braun, Synthwave) e provati sull'emulatore `box` con la Home con e senza musica e il pannello Benzinai (selezione, scheda, preferiti). La scheda del brano è stata vista per la prima volta, in tutti e otto i temi, con un brano finto suonato da Chrome (metodo in `RIFERIMENTO.md` › Emulatori). Avvio a freddo 142–297 ms; PSS 23–31 MB (Cyberpunk il più alto); 0 frame in ritardo; APK 673 KB. Correzioni dopo la prova: `Ui.adatta` ora rimpicciolisce anche i testi su una riga e non taglia le lettere (vale per tutti i temi; i temi 1–4 restano uguali a vista); orologio di Solari a larghezza fissa (mancava l'ultima cifra) e descrizione del meteo che si rimpicciolisce; in Cyberpunk «Launcher» su una riga e la data sotto l'ora, che spariva; in Braun l'ora che tagliava la data e tutta la cella del tastierino toccabile. Scostamenti: la scheda del distributore ha altezza fissa (260–300 px) in tutti e quattro; in Synthwave niente sole né griglia, che la tavola non disegna.
- 2026-11-18: misure e confronto con le tavole (seconda tappa). Otto temi misurati su `box` e, per la prima volta, su `box-lento`: avvio a freddo 125–303 ms, 0 frame in ritardo; PSS 24–25 MB su `box` e 31–33 MB su `box-lento`, dove il PSS conta all'app una quota più grande delle librerie condivise (heap uguale). HUD scende da 34 a 24 MB: un font nativo per ogni testo con `fontVariationSettings`, ora uno per peso. Confronto a vista delle 24 schermate (Home con e senza musica, Benzinai) con le tavole disegnate da Chrome headless; corretti: l'ora di Brutalismo, più piccola del 35% fin dall'inizio (il riquadro più basso del font la faceva rimpicciolire), e i numeri romani dei servizi di Art Déco, a sinistra invece che centrati. `Ui.adatta` ora scala l'interlinea invece di azzerarla. Lasciati come sono: lo zero barrato di Synthwave (è il disegno di Audiowide), l'indirizzo di Solari su una sola paletta quando è lungo, piccoli spostamenti verticali (meno di 25 px) di titoli e date in Art Déco, Cyberpunk e Synthwave.
- 2026-11-18: preferiti dei Benzinai cambiati su richiesta dell'utente (D-6): non più distributori scelti con la ★ ma tre comuni fissi nell'app, Torino, Cellamare e Valenzano, ognuno con il suo distributore più economico in cima all'elenco. La ★ sparisce da righe e scheda in tutti e otto i temi; i preferiti salvati prima restano nelle `SharedPreferences` `benzinai` ma non si leggono più.
- 2026-11-18: prova della catena di aggiornamento prima del rilascio di M02, con il codice dei preferiti per comune. Build di prova con `versionCode` 1 sull'emulatore `box`: «Controlla aggiornamenti» ha installato la 0.1.1 pubblicata dopo la conferma «Update», e l'app risulta poi installata da sé stessa; la build normale si installa sopra la 0.1.1 con lo stesso certificato (SHA-256 `7412986f…9e36`). Gli otto temi provati con i nuovi preferiti: Torino, Cellamare e Valenzano in cima, senza ★, scheda che si apre.
- 2026-11-18: pubblicata la v0.2.0 (codice 3, APK 666 KB) con `./release.sh minor`: minor e non patch perché cambia tutta la Home. Sull'emulatore `box` la 0.1.1 ha ricevuto la 0.2.0 da sola; i primi due controlli, fatti subito dopo la pubblicazione, vedevano ancora la 0.1.1 (ritardo di GitHub già noto, `RIFERIMENTO.md` › Aggiornare). Le prove manuali sul box sono rimandate al 2026-11-19, insieme a quelle di M01.
- 2026-11-19: l'utente ha arricchito le tavole di ogni tema con dettagli che portano un'informazione (scheda `t<n>spec`, sezione DETTAGLI): strisce dei giorni con oggi in evidenza, barre o scale delle 24 ore, scale della temperatura tra minima e massima, spie «in riproduzione», cornici della copertina, posizione e millesimo del prezzo più piccolo nei Benzinai. Portati tutti negli otto temi, confrontando il diff tra le tavole vecchie e nuove; ogni dettaglio legge il dato vero (ora, giorno, meteo, posizione nell'elenco) e resta spento senza meteo. Nuova la risorsa `brutalismo_freccia.xml`. Matrix ha ora le icone nei servizi e Synthwave il mezzo sole sull'orizzonte, che prima mancavano. Confronto a vista delle 24 schermate con le tavole sull'emulatore `box`: corretti il tubo del meteo di Synthwave, che allargava la colonna fuori dallo schermo, il tasto «successivo» di Synthwave, tagliato di 8 px, e il brano di Art Déco, allineato a sinistra invece che centrato. Misure in release: avvio a freddo 130–268 ms, PSS 26–28 MB, 0 frame in ritardo, APK 690 KB. Scostamenti lasciati: le ★ e i tasti stella delle tavole restano fuori (D-6); la scala dei gradi di Matrix ha 9 celle fisse invece di una per grado; la tacca della barra sotto il brano in Cyberpunk è fissa, perché il launcher non legge la posizione del brano.
- 2026-11-19: l'utente ha guardato gli otto temi con i dettagli nuovi sull'emulatore `box` e li approva così. Restano le prove sul box. Documentazione aggiornata: guida (gli indicatori dei temi), riferimento (tavole `HomeB`, disegno delle tavole in locale, emulatore con l'audio, build di debug), decisioni (dettagli legati ai dati); l'idea «Funzioni del launcher», diventata questo piano, è in archivio.
- 2026-11-19: pubblicata la v0.2.1 (codice 4, APK 690 KB) con `./release.sh patch`: patch perché cambia solo l'aspetto dei temi, non le funzioni. Le modifiche non toccano `aggiornamenti/`, manifest, firma o build, quindi la prova della catena prima del rilascio non era obbligatoria; fatta dopo. Sull'emulatore `box` la v0.2.0 pubblicata, installata con `adb`, ha trovato la 0.2.1 con «Controlla aggiornamenti». Android ha chiesto il permesso «installa app sconosciute», azzerato da una disinstallazione precedente, e la conferma «Update». Dopo l'installazione l'app risulta installata da sé stessa, con i dettagli nuovi nella Home.
