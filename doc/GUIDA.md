# Austral Dashboard: guida

## Per cominciare

**In breve.** Austral Dashboard è il launcher del box Ottocast in auto. Per ora mostra l'ora, la versione e le Impostazioni; il resto arriva con i prossimi piani.

| Schermata | Come ci arrivi | Cosa trovi |
|---|---|---|
| Home | Tasto casa della barra Ottocast | Ora, versione, Impostazioni |
| Impostazioni | Home → Impostazioni | Controlla aggiornamenti, Diagnostica, Impostazioni di sistema, Cambia launcher predefinito |
| Diagnostica | Impostazioni → Diagnostica | Versione, dimensione, memoria, tempi di avvio, frame in ritardo, esito dell'ultimo controllo |

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
