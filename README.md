# Austral Dashboard

Launcher Android personale, leggerissimo, fatto su misura per un solo dispositivo: il box Ottocast OttoAibox P3 collegato via CarPlay alla Renault Austral. Non è pensato per essere distribuito.

| | |
|---|---|
| Dispositivo | Ottocast OttoAibox P3, Android 12 (API 32), Qualcomm QCM6125 (modulo Quectel SLM920), 8 GB RAM |
| Area disponibile per l'app | 1244×1194 px a 200 dpi (la barra in basso è di Ottocast) |
| Schermo dell'auto | Renault Austral, openR Link via CarPlay, 1244×1304 |
| Linguaggio | Kotlin, View native di Android, nessuna dipendenza |
| Aggiornamenti | Il launcher li scarica da solo dalle GitHub Releases di questo repository |

## Come funziona il ciclo di lavoro

```
Mac: sviluppo → emulatore come il box → ./release.sh
                                            │
                              GitHub Releases (APK + versione.json)
                                            │
Auto: all'accensione o dal tasto in Impostazioni → scarica → installa
```

## A che punto è

| Lavoro | Stato |
|---|---|
| [M01 · La catena di aggiornamento](doc/piani/M01-catena-aggiornamenti/PIANO.md) | Fatto sull'emulatore; manca il test sul box, previsto il 2026-11-19 |
| [M02 · La Home vera, in otto temi](doc/piani/M02-home-a-temi/PIANO.md) | Otto temi con i dettagli delle tavole nuove, pubblicati nella v0.2.1; manca la prova sul box, prevista il 2026-11-19 |
| [M03 · Catena robusta e rifiniture](doc/piani/M03-catena-robusta-e-rifiniture/PIANO.md) | 0.3.0 in uso sul box; pubblicata la 0.3.1, da ricevere sul box |
| [M04 · Funzioni di bordo](doc/piani/M04-funzioni-di-bordo/PIANO.md) | Pubblicata la 0.4.1; il volume non funziona sul box, si indaga con Diagnostica → Audio |

Prima installazione sul box, aggiornamenti e chiave di firma: [doc/GUIDA.md](doc/GUIDA.md).

## I documenti

| Documento | Dove | Lettori | Cosa contiene |
|---|---|---|---|
| Questo file | `README.md` | persone | Cos'è il progetto e come si avvia |
| Regole per le sessioni | `CLAUDE.md` | AI | Le regole fisse del lavoro |
| Guida | `doc/GUIDA.md` | persone | Come si usa |
| Riferimento | `doc/RIFERIMENTO.md` | AI | Com'è fatto dentro |
| Decisioni | `doc/DECISIONI.md` | entrambi | Perché è fatto così |
| Piani | `doc/piani/M<nn>-<nome>/` | entrambi | Cosa faremo e cosa abbiamo fatto |
| Idee | `doc/idee/<nome>.md` | persone | Idee senza un numero |
| Archivio | `doc/archivio/` | nessuno di routine | Lavori chiusi e storici |

Se due documenti dicono cose diverse vince, in ordine: il codice, la guida e il riferimento, le decisioni, i piani.
