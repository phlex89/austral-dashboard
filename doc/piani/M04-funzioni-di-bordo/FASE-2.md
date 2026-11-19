---
milestone: M04
stato: in-corso
data: 2026-11-19
lettori: persone
---

# M04 · Fase 2: le tavole

La fase 2 porta le funzioni nuove negli otto temi, e parte dalle tavole aggiornate. Questo è il prompt da incollare in Claude Design, nel progetto delle tavole degli otto temi (artifact Design `FB5G7dpEA4q1GcwEvhxoXL`).

```text
In questo progetto ci sono le tavole degli otto temi del mio launcher Android per l'auto: per ogni tema T1–T8 la Home con la musica (T<n>Home), la stessa senza musica (T<n>HomeB), il pannello Benzinai (T<n>Benzinai), e in canvas.json le note t<n>spec con palette, font, misure e la sezione DETTAGLI.

Devo aggiungere cinque elementi nuovi a tutti e otto i temi. Ogni tema li deve disegnare nel suo linguaggio (Matrix a terminale, HUD, Brutalismo, Art Déco, Solari a palette, Cyberpunk, Braun, Synthwave), come se ci fossero stati fin dall'inizio, non come pezzi incollati sopra.

Vincoli che non cambiano:
- Area 1244×1194 px a 200 dpi, misure in px. Lo schermo è quello dell'auto via CarPlay: solo tap e swipe semplici, bersagli grandi (mai più piccoli dei servizi attuali), tema scuro ad alto contrasto.
- Sulla Home restano tutti gli elementi di oggi: ora e data, meteo, scheda della musica con i comandi, le sei app, i quattro servizi (Benzinai, Tutte le app, Impostazioni di Android, Launcher). Puoi ridistribuire spazi e proporzioni, ma niente scorrimento e niente pagine nuove.
- Ogni dettaglio porta un dato vero. Senza il dato l'elemento resta spento o sparisce, mai con un valore inventato.
- Solo i font che ogni tema usa già. Icone nuove come SVG con viewBox 48, in un solo colore (nel codice le coloro io).

Gli elementi nuovi:

1. Home, scorciatoie di sistema. Tre comandi:
   - Wi-Fi, con tre stati: spento, acceso ma non connesso, connesso. Un tap apre il pannello Wi-Fi di Android.
   - Bluetooth, con due stati: spento, acceso. Un tap apre le impostazioni Bluetooth.
   - Volume: «−», il livello attuale (da 0 a 15: barra, tacche o numero, come viene meglio nel tema), «+». Ogni tap cambia di un passo.
   Possono stare in una fascia propria o accanto ai servizi; devono essere raggiungibili con un tocco, senza aprire menu.

2. Home, playlist preferite. Fino a tre pulsanti, ognuno con il nome della playlist (testo libero fino a 40 caratteri, da accorciare con eleganza se lungo). Un tap la fa partire in Spotify. Stanno meglio vicino alla scheda della musica. Se non ce ne sono, spariscono; con una o due si adattano.

3. Home, pioggia in arrivo, dentro il blocco del meteo. Un testo breve: «Piove» se sta piovendo adesso, «Pioggia tra 20 min» se arriva entro due ore (i minuti vanno a passi di 5). Se nelle prossime due ore non piove, non compare niente.

4. Benzinai, andamento del prezzo. Per ogni distributore, accanto al prezzo, una freccia su o giù con la differenza rispetto al download precedente (esempio: ▲ 0,012 oppure ▼ 0,005). Se il prezzo è uguale o non si conosce il precedente, niente.

5. Benzinai, ingresso «Rifornimenti». Un pulsante, nell'intestazione o vicino agli ordinamenti, che apre il registro dei pieni. Porta con sé la spesa del mese (esempio: «Rifornimenti · 142 € a novembre»); se nel mese non ci sono rifornimenti, solo «Rifornimenti».

Cosa mi serve indietro:
- Le tavole aggiornate con gli stessi nomi. T<n>Home con tutto acceso: musica, tre playlist, pioggia tra 20 minuti, Wi-Fi connesso, Bluetooth acceso, volume a 9. T<n>HomeB con gli stati vuoti: senza musica, senza playlist, senza pioggia, Wi-Fi spento, Bluetooth spento. T<n>Benzinai con prezzi in salita, in discesa e uguali.
- Le note t<n>spec aggiornate: misure, colori e font degli elementi nuovi, e nella sezione DETTAGLI cosa mostra ognuno in ogni stato.
- Un riepilogo per tema di cosa hai spostato o ridimensionato per fare spazio.
```
