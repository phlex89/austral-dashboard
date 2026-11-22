---
milestone: M05
stato: proposto
data: 2026-11-22
lettori: persone
---

# M05 · Fase 2: le tavole

Questo è il prompt da incollare in Claude Design, nel progetto delle tavole degli otto temi (artifact Design `FB5G7dpEA4q1GcwEvhxoXL`). Le tavole servono alla fase 3; la fase 1 non le aspetta.

```text
In questo progetto ci sono le tavole degli otto temi del mio launcher Android per l'auto: per ogni tema T1–T8 la Home con la musica (T<n>Home), la stessa con gli stati vuoti (T<n>HomeB), il pannello Benzinai (T<n>Benzinai), e in canvas.json le note t<n>spec con palette, font, misure e la sezione DETTAGLI.

Devo cambiare alcune cose in tutti e otto i temi: un elemento si toglie, altri si aggiungono. Ogni tema le deve disegnare nel suo linguaggio (Matrix a terminale, HUD, Brutalismo, Art Déco, Solari a palette, Cyberpunk, Braun, Synthwave), come se ci fossero state fin dall'inizio, non come pezzi incollati sopra.

Vincoli che non cambiano:
- Area 1244×1194 px a 200 dpi, misure in px. Lo schermo è quello dell'auto via CarPlay: solo tap e swipe semplici, bersagli grandi (mai più piccoli dei servizi attuali, salvo dove indicato), tema scuro ad alto contrasto.
- Sulla Home restano tutti gli elementi di oggi tranne il volume: ora e data, meteo con la pioggia in arrivo, scheda della musica con i comandi, playlist preferite, le sei app, i quattro servizi (Benzinai, Tutte le app, Impostazioni di Android, Launcher), Wi-Fi e Bluetooth. Puoi ridistribuire spazi e proporzioni, ma niente scorrimento e niente pagine nuove.
- Nel pannello Benzinai resta tutto quello di oggi: elenco con prezzo, andamento ▲/▼, distanza, ordinamenti, scheda del distributore aperto, tasto Rifornimenti con la spesa del mese.
- Ogni dettaglio porta un dato vero. Senza il dato l'elemento resta spento o sparisce, mai con un valore inventato.
- Solo i font che ogni tema usa già. Icone nuove come SVG con viewBox 48, in un solo colore (nel codice le coloro io).

Cosa cambia:

1. Home, via il volume. Togli «−», il livello e «+» da tutti i temi. Lo spazio che si libera serve ai punti 2 e 3.

2. Home, Wi-Fi e Bluetooth con il dettaglio. Ognuno ha due bersagli:
   - il comando, come oggi, con i suoi stati. Wi-Fi: spento, acceso ma non connesso, connesso; un tap apre il pannello Wi-Fi. Bluetooth: spento, acceso; un tap ora lo accende o lo spegne.
   - accanto, un «›» (o il segno che il tema preferisce per «apri dettagli») che apre le impostazioni complete. Può essere più stretto del comando, ma alto quanto lui e mai sotto gli 80 px di lato.

3. Home, tasto «Tema». Un comando che a ogni tap passa al tema successivo. Etichetta «Tema» e un'icona nuova (tavolozza, campioni di colore o quello che si sposa col tema). Raggiungibile con un tocco, insieme a Wi-Fi e Bluetooth o vicino ai servizi.

4. Home, il luogo del meteo. Il nome del luogo di cui si mostra il meteo, sempre visibile nel blocco del meteo: testo fino a 30 caratteri, da accorciare con eleganza se lungo (esempi: «Roma», «Sesto San Giovanni», «Posizione attuale»). Quando il meteo segue il GPS accanto al nome c'è una piccola spia di posizione (icona nuova, una puntina o un mirino); quando il luogo è fisso la spia non c'è.

5. Home, «Domani». Nel blocco del meteo, compatto: la parola «Domani», l'icona del cielo (le stesse icone del meteo di oggi), minima e massima (esempio: «Domani ☁ 8° / 15°»). Se il dato manca, sparisce.

6. Benzinai, «Intorno a me». Un pulsante con due stati: spento (elenco normale: vicini più il più economico di ogni comune preferito) e acceso (solo i distributori vicini, posizione appena ripresa dal GPS). Da acceso deve vedersi bene che il filtro è attivo.

7. Benzinai, «Raggio». Un pulsante che mostra il raggio attuale («Raggio 10 km») e a ogni tap passa a 5 → 10 → 20 → 30 km. Vale in tutti e due gli stati di «Intorno a me». Se nel raggio non c'è nessun distributore, l'elenco mostra «Nessun distributore entro 5 km» (col raggio vero).

8. Benzinai, «Mappa». Un pulsante che apre la mappa dei distributori (la mappa è una schermata a parte, non va disegnata). Icona nuova (mappa piegata o segnaposto).

I pulsanti 6, 7 e 8 stanno nell'intestazione del pannello o vicino agli ordinamenti, come viene meglio nel tema.

Cosa mi serve indietro:
- Le tavole aggiornate con gli stessi nomi. T<n>Home con tutto acceso: musica, tre playlist, pioggia tra 20 minuti, Wi-Fi connesso, Bluetooth acceso, luogo «Sesto San Giovanni» con la spia del GPS, Domani nuvoloso 8° / 15°. T<n>HomeB con gli stati vuoti: senza musica, senza playlist, senza pioggia, Wi-Fi spento, Bluetooth spento, luogo fisso «Roma» senza spia, senza Domani. T<n>Benzinai con «Intorno a me» acceso, raggio 20 km, prezzi in salita, in discesa e uguali.
- Le note t<n>spec aggiornate: misure, colori e font degli elementi nuovi, e nella sezione DETTAGLI cosa mostra ognuno in ogni stato (compreso «Intorno a me» spento).
- Le SVG delle icone nuove.
- Un riepilogo per tema di cosa hai spostato o ridimensionato.
```
