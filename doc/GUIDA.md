# Austral Dashboard: guida

## Per cominciare

**In breve.** Austral Dashboard è il launcher del box Ottocast in auto. La Home mostra ora, meteo, il brano in ascolto, le playlist preferite, sei app, quattro servizi, Wi‑Fi, Bluetooth e volume; l'aspetto dipende dal tema scelto in Impostazioni.

| Schermata | Come ci arrivi | Cosa trovi |
|---|---|---|
| Home | Tasto casa della barra Ottocast | Ora e data, meteo con la pioggia in arrivo, brano in ascolto con i comandi, fino a tre playlist preferite, sei app (all'inizio Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok), i quattro servizi, Wi‑Fi, Bluetooth e volume |
| Benzinai | Home → Benzinai | Distributori entro 15 km con il prezzo della benzina self, ordinati per «Conviene», «Prezzo» o «Distanza». In cima i preferiti: per ogni comune scelto in Impostazioni il distributore più economico, anche se è lontano. Accanto al prezzo, ▲ o ▼ con la differenza dal download precedente. In alto «Rifornimenti» con la spesa del mese |
| Rifornimenti | Benzinai → Rifornimenti | A ogni pieno scrivi euro, litri e contachilometri e tocchi «Salva». Sotto: spesa del mese, consumo medio in km/l, € ogni 100 km, gli ultimi 20 pieni con «Togli» |
| Tutte le app | Home → Tutte le app | Tutte le app installate, in ordine alfabetico. In alto a destra «Aggiorna app» apre il Play Store sulla pagina delle tue app, dove tocchi «Aggiorna tutto» |
| Impostazioni | Home → Launcher | Tema, lettura della musica, città di riserva, comuni preferiti, app della Home, playlist preferite, aggiornamenti, Diagnostica, impostazioni di sistema, launcher predefinito |
| Diagnostica | Impostazioni → Diagnostica | Versione, dimensione, memoria, tempi di avvio, frame in ritardo, esito dell'ultimo controllo, ultimo crash. In fondo la sezione Audio: livelli del volume, tre pulsanti di prova e l'elenco dei cambi di volume, anche quelli fatti dalla barra Ottocast o dalla manopola dell'auto |

Un'app della Home che non è installata resta spenta. Toccando la copertina o il titolo del brano si apre l'app che lo suona; senza musica, «Apri Spotify» apre Spotify e il tasto play riprende l'ultima app musicale.

«Conviene» mette in cima il distributore con cui spendi meno contando un pieno da 40 litri e il viaggio di andata e ritorno. I prezzi si scaricano quando apri Benzinai, al massimo una volta al giorno, dopo la pubblicazione del mattino.

Wi‑Fi apre il pannello delle reti di Android, Bluetooth le sue impostazioni; «−» e «+» cambiano il volume di un passo. Una playlist si fa partire con un tocco. «Piove» o «Pioggia tra 20 min» compare nel meteo solo se la pioggia arriva entro due ore.

Il consumo si calcola da pieno a pieno: fai sempre il pieno e scrivi il contachilometri di ogni rifornimento. Servono almeno due pieni.

Se il launcher si chiude da solo, Diagnostica mostra in fondo quando è successo, con quale versione e l'errore: è quello che serve per correggerlo.

## Prima configurazione

**In breve.** Tre permessi e qualche scelta, una volta sola.

1. Alla prima apertura della Home Android chiede la posizione: consenti. Serve al meteo e ai Benzinai.
2. Impostazioni → **Consenti la lettura della musica** → attiva Austral Dashboard. Serve a mostrare il brano e a comandarlo; il pulsante sparisce quando il permesso c'è.
3. Impostazioni → **Città di riserva**: scrivi la città, tocca **Cerca** e scegli. Si usa solo quando il box non sa dove si trova.
4. Impostazioni → **Comuni preferiti**: apri prima una volta Benzinai, che scarica l'elenco; poi scrivi l'inizio del nome, tocca **Cerca** e scegli. Al massimo tre; **Togli** li rimuove.
5. Impostazioni → **App della Home**: tocca una delle sei posizioni e scegli l'app tra quelle installate. La Home cambia subito.
6. Playlist preferite, al massimo tre: in Spotify apri la playlist, **Condividi → Altro → Austral Dashboard**, correggi il nome se vuoi e tocca **Salva**. In Impostazioni → **Playlist preferite** le vedi e le togli.

Per cambiare l'aspetto: Impostazioni → **Tema**, poi tocca quello che vuoi. La Home cambia subito.

Ogni tema ha piccoli indicatori che si aggiornano da soli: il giorno della settimana in evidenza, quanta parte della giornata è passata, dove sta la temperatura di adesso tra la minima e la massima, una spia quando la musica suona. Nei Benzinai ogni distributore ha il suo numero di posizione e la terza cifra del prezzo è più piccola, come sui cartelli. Finché il meteo non arriva, o se quello salvato ha più di tre ore, i suoi indicatori restano spenti.

## Prima installazione sul box

**In breve.** Si fa una volta sola, a mano; dopo il launcher si aggiorna da solo.

1. Sul box apri il browser e vai su `github.com/phlex89/austral-dashboard/releases/latest`.
2. Scarica `austral-dashboard.apk` e aprilo. Se Android lo chiede, consenti l'installazione da questa origine.
3. Premi il tasto casa e scegli **Austral Dashboard**, «Sempre».
4. Apri Impostazioni → Controlla aggiornamenti: al primo aggiornamento Android chiede di consentire le installazioni da Austral Dashboard. Consenti.

Per tornare al launcher di prima: Impostazioni → **Cambia launcher predefinito**.

## Aggiornare

**In breve.** Sul Mac `./release.sh`; in auto l'aggiornamento arriva da solo all'accensione, o subito con **Controlla aggiornamenti**.

| Dove | Cosa fai |
|---|---|
| Mac | Committa le modifiche, poi `./release.sh` (oppure `./release.sh minor` o `major`) |
| Auto | Un minuto dopo l'accensione, e poi ogni sei ore, il launcher controlla, scarica e installa; se Android chiede conferma, tocca **Aggiorna** |
| Auto, subito | Impostazioni → **Controlla aggiornamenti** |

Dopo `./release.sh` aspetta un paio di minuti prima di controllare: GitHub impiega 1–2 minuti a segnalare la versione nuova. Al primo aggiornamento Android chiede di consentire le installazioni da Austral Dashboard e poi «Aggiornare?»: tocca **Impostazioni**, attiva l'interruttore, torna indietro e tocca **Aggiorna**. Dalla versione successiva alla 0.2.1 in poi, dopo questo primo aggiornamento confermato a mano, gli altri si installano senza chiedere niente; funziona anche se una versione sbagliata si chiude all'avvio.

Se serve la conferma e il launcher non è in primo piano, compare una notifica «Aggiornamento pronto»; la conferma si apre comunque da sola appena torni alla Home.

## La chiave di firma

**In breve.** Ogni versione è firmata con la stessa chiave: se la perdi, il box non accetta più aggiornamenti e va reinstallato a mano. Tienine una copia.

| File | Dove | Cosa fare |
|---|---|---|
| Chiave | `~/.android-keys/austral-dashboard.jks` | Copia nel password manager o in un backup cifrato |
| Password | `keystore.properties` nella cartella del progetto | Copia insieme alla chiave; non va mai nel repository |
