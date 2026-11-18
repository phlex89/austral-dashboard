# Austral Dashboard

## Vincoli del progetto

- Un solo dispositivo di destinazione: Ottocast OttoAibox P3, Android 12 (API 32), area app 1244×1194 px a 200 dpi. Nessuna compatibilità con altri dispositivi o versioni di Android.
- Leggerezza prima di tutto: Kotlin con le View native, nessuna dipendenza di runtime (niente AndroidX, Compose, librerie esterne). Le dipendenze solo per i test sono ammesse.
- Si sviluppa e si prova solo sul Mac, con l'emulatore configurato come il box. Le verifiche sul box si fanno solo nei momenti previsti dai piani.
- Il repository è pubblico: mai keystore, password o token nel repository.
- Interfaccia per l'auto: tema scuro ad alto contrasto, bersagli grandi, solo tap e swipe semplici.
- Dopo la prima installazione a mano (APK dal browser o da Drive) il launcher si aggiorna da solo, senza più spostare APK. Mai rompere la catena: stessa chiave di firma, stesso `applicationId`, aggiornatore indipendente dall'interfaccia. Se un cambio tocca `aggiornamenti/`, il manifest, la firma o la build, prima di pubblicarlo si prova sull'emulatore che la nuova versione riceva ancora l'aggiornamento successivo.

## Documentazione

- `README.md` dice cos'è il progetto; `doc/GUIDA.md` come si usa (persone); `doc/RIFERIMENTO.md` com'è fatto (AI); `doc/DECISIONI.md` perché.
- I piani sono cartelle in `doc/piani/` con `PIANO.md`: `M<nn>-<nome>/`, o `<TASK>-<n>[-<nome>]/` (es. `HER-12/`) se nati da un task; un piano nuovo è una cartella nuova. Le idee senza numero in `doc/idee/`; i lavori chiusi in `doc/archivio/`.
- Ogni file in `doc/piani/` e `doc/idee/` ha il frontmatter: `milestone` (o `task`), `stato`, `data`, `lettori`.
- Un piano ha `## In una pagina` (persone, al massimo 40 righe) e `## Per chi esegue` (AI, completo).
- Le domande per l'utente vanno nel `DOMANDE.md` del piano, numerate da D-1.
- La guida e il riferimento si aggiornano nello stesso commit del comportamento che cambia.
- Ogni voce nuova di `DECISIONI.md` apre con `**In breve.**` (al massimo 5 righe) e poi `**Dettagli.**`.
- Controllo: `node "/Users/stefanotedeschi/Lavoro/Personale/progetti/herdr-orchestrator/mod-claude/documentazione/controlla.mjs"`; i lavori aperti: lo stesso comando con `--aperti`.
