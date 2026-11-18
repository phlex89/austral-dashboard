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

Ancora da costruire: il primo traguardo è in [doc/piani/M01-catena-aggiornamenti](doc/piani/M01-catena-aggiornamenti/PIANO.md).

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
