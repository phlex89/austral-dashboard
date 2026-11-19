# Austral Dashboard: guida

## Per cominciare

**In breve.** Austral Dashboard è il launcher del box Ottocast in auto. La Home mostra ora, meteo, il brano in ascolto, sei app e quattro servizi; l'aspetto dipende dal tema scelto in Impostazioni.

| Schermata | Come ci arrivi | Cosa trovi |
|---|---|---|
| Home | Tasto casa della barra Ottocast | Ora e data, meteo, brano in ascolto con i comandi, Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok, i quattro servizi |
| Benzinai | Home → Benzinai | Distributori entro 15 km con il prezzo della benzina self, ordinati per «Conviene», «Prezzo» o «Distanza». In cima i preferiti: per Torino, Cellamare e Valenzano il distributore più economico del comune, anche se è lontano |
| Tutte le app | Home → Tutte le app | Tutte le app installate, in ordine alfabetico |
| Impostazioni | Home → Launcher | Tema, lettura della musica, città di riserva, aggiornamenti, Diagnostica, impostazioni di sistema, launcher predefinito |
| Diagnostica | Impostazioni → Diagnostica | Versione, dimensione, memoria, tempi di avvio, frame in ritardo, esito dell'ultimo controllo |

Un'app della Home che non è installata resta spenta. Toccando la copertina o il titolo del brano si apre l'app che lo suona; senza musica, «Apri Spotify» apre Spotify e il tasto play riprende l'ultima app musicale.

«Conviene» mette in cima il distributore con cui spendi meno contando un pieno da 40 litri e il viaggio di andata e ritorno. I prezzi si scaricano quando apri Benzinai, al massimo una volta al giorno.

## Prima configurazione

**In breve.** Tre permessi, una volta sola.

1. Alla prima apertura della Home Android chiede la posizione: consenti. Serve al meteo e ai Benzinai.
2. Impostazioni → **Consenti la lettura della musica** → attiva Austral Dashboard. Serve a mostrare il brano e a comandarlo; il pulsante sparisce quando il permesso c'è.
3. Impostazioni → **Città di riserva**: scrivi la città, tocca **Cerca** e scegli. Si usa solo quando il box non sa dove si trova.

Per cambiare l'aspetto: Impostazioni → **Tema**, poi tocca quello che vuoi. La Home cambia subito.

Ogni tema ha piccoli indicatori che si aggiornano da soli: il giorno della settimana in evidenza, quanta parte della giornata è passata, dove sta la temperatura di adesso tra la minima e la massima, una spia quando la musica suona. Nei Benzinai ogni distributore ha il suo numero di posizione e la terza cifra del prezzo è più piccola, come sui cartelli. Finché il meteo non arriva, i suoi indicatori restano spenti.

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
| Auto | All'accensione il launcher controlla e scarica; se Android chiede conferma, tocca **Installa** |
| Auto, subito | Impostazioni → **Controlla aggiornamenti** |

Dopo `./release.sh` aspetta un paio di minuti prima di controllare: GitHub impiega 1–2 minuti a segnalare la versione nuova. Al primo aggiornamento Android chiede di consentire le installazioni da Austral Dashboard e poi «Aggiornare?»: tocca **Impostazioni**, attiva l'interruttore, torna indietro e tocca **Aggiorna**.

Se il launcher non è in primo piano quando l'aggiornamento è pronto, compare una notifica «Aggiornamento pronto»: toccala per installare.

## La chiave di firma

**In breve.** Ogni versione è firmata con la stessa chiave: se la perdi, il box non accetta più aggiornamenti e va reinstallato a mano. Tienine una copia.

| File | Dove | Cosa fare |
|---|---|---|
| Chiave | `~/.android-keys/austral-dashboard.jks` | Copia nel password manager o in un backup cifrato |
| Password | `keystore.properties` nella cartella del progetto | Copia insieme alla chiave; non va mai nel repository |
