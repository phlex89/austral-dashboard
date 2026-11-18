---
milestone: M02
stato: in-corso
data: 2026-11-18
lettori: persone
---

# M02 · Domande

## D-1 · In che ordine si lavora?

Le tavole danno per scontate funzioni che oggi non esistono (musica, meteo, Benzinai, app).

- **Proposta:** prima le funzioni e il selettore con il solo tema Matrix, poi gli altri temi a gruppi (2–4, poi 5–8). Ogni tappa si prova subito.
- Alternativa: prima gli otto temi con dati finti, poi le funzioni.

**Risposta (2026-11-18):** come la proposta.

## D-2 · I font dei temi

Cinque temi usano un font che Android non ha: VT323, Orbitron, Limelight, Rajdhani, Audiowide.

- **Proposta:** metterli nell'APK. Pesano qualche centinaio di KB in tutto: l'APK passa da 41 KB a circa mezzo MB, ancora molto sotto il limite di `DECISIONI.md`.
- Alternativa: sostituirli con font di sistema; i temi perdono parte del carattere.

**Risposta (2026-11-18):** come la proposta.

## D-3 · Le sei app della Home

Le tavole mostrano Netflix, Prime Video, Disney+, YouTube, RaiPlay, Spotify.

- **Proposta:** sei posti scelti in Impostazioni › App della Home, con queste sei come partenza.
- Alternativa: fisse queste sei; si cambiano solo nel codice.

**Risposta (2026-11-18):** fisse: Netflix, Prime Video, Disney+, YouTube, Stremio, TikTok. Spotify si apre dalla scheda del brano.

## D-4 · Quando scaricare i prezzi dei Benzinai

I file del Ministero coprono tutta Italia e pesano alcuni MB (da misurare); il box usa la rete del telefono.

- **Proposta:** solo quando si apre il pannello Benzinai, al massimo una volta al giorno (i prezzi cambiano ogni mattina).
- Alternativa: ogni mattina in automatico, così il pannello si apre già pronto.

**Risposta (2026-11-18):** come la proposta.

## D-5 · Se il box non ha una posizione

Il GPS del box è ancora da verificare (prove di M01). Senza posizione non ci sono meteo né Benzinai.

- **Proposta:** una città di riserva scelta in Impostazioni, usata solo quando la posizione manca.
- Alternativa: nessun ripiego; meteo e Benzinai mostrano «posizione non disponibile».

**Risposta (2026-11-18):** come la proposta.

## D-6 · Preferiti dei Benzinai: Torino, Cellamare, Valenzano

Richiesta (2026-11-18): come preferiti solo Torino, Cellamare (Bari) e Valenzano (Bari), anche già pronti nell'app. Oggi un preferito è un singolo distributore. Nei dati del MIMIT a Cellamare c'è 1 distributore, a Valenzano 5, a Torino 213.

- **Proposta:** i preferiti diventano luoghi. In cima al pannello Benzinai, per ognuno dei tre comuni, il distributore con la benzina self più economica, anche se è lontano; un tocco apre la sua scheda. I tre comuni sono già nell'app; la ★ sui singoli distributori sparisce.
- Alternativa A: come la proposta, ma i 3 più economici per comune invece di uno.
- Alternativa B: i preferiti restano distributori singoli. Già pronti: quello di Cellamare e i 5 di Valenzano; per Torino servono nomi o indirizzi dei distributori che usi.

**Risposta (2026-11-18):** come la proposta.

## D-7 · Commit della tappa 2 e passo successivo

Misure e confronto con le tavole sono finiti; le correzioni di HUD, Brutalismo, Art Déco e `Ui.adatta` non sono ancora nel commit.

- **Proposta:** commit adesso, poi la tappa 3: rilascio con `./release.sh` e prova sul box. Prima del rilascio si rifà la prova della catena di aggiornamento sull'emulatore.
- Alternativa: commit, poi i preferiti di D-6 prima della tappa 3.
- Alternativa: nessun commit per ora.

**Risposta (2026-11-18):** come la proposta; i preferiti di D-6 si fanno prima del rilascio, perché erano chiesti nello stesso messaggio.
