# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – **P0-Gate abgeschlossen (F-01 bis F-06)**, P1 gestartet: E-01-Technologiematrix erstellt. A5-Offline-WebView und CI-geprüfte Debug-APK mit Emulator-Smoke vorhanden; echter Rich-Text-Prototyp folgt E-02.
- **Meilenstein:** `P1 – Machbarkeitsnachweis und Technologieentscheidung`
- **Aktive Aufgabe:** keine; F-06 und E-01 abgeschlossen
- **Nächste ausführbare Aufgabe:** `E-02` – im A5-Canvas-Editor direkt tippen, Android-IME/Unicode und lokales Save/Reload im Emulator nachweisen
- **Abgeschlossene Aufgaben:** **7 / 35 (20,0 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** [Smut CI – 43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955), `success`: Editor + Android + API-35-Emulator + geprüfte APK. [F-06-Negativprobe 43451561861](https://github.com/philip20794/Smut/actions/runs/43451561861), `failure`: alle nachgelagerten Jobs/`tested-apk` `skipped`, **keine Artifacts**.
- **Letzte geprüfte APK:** `smut-tested-dev-bbafe4f01e3b02b7dc3df8e9bb2594f4edf18bc0` (ca. 9,5 MB, [GitHub Actions 43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955), SHA256SUMS enthalten, temporäres 30-Tage-Artifact). **Nicht auf realem Telefon getestet.**
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner für E-02; Canvas-Editor-IME und lokale Speicherintegration sind technisch noch ungeprüft.

## 2. Verbindlicher nächster Schritt

**E-02 – Direktes Schreiben und Android-Tastatur im Prototyp.**

1. [E-01-Technologiematrix](EDITOR_EVALUATION.md) und [ADR-008](DECISIONS.md) lesen. `@hufe921/canvas-editor@1.1.0` ist nur der **erste Testkandidat**, keine endgültige Wahl.
2. Die vollständig offline gebündelte WebView mit einem **echten** Canvas-Editor auf A5 ausstatten (nicht mit vorgeschaltetem Overlay-Textfeld).
3. Einfache deutsche Testpassage, Cursor per Tap, IME, Umlaute, Autokorrektur, Enter/Backspace und lokale Speicherung/Neustart über die instrumentierte App prüfen.
4. CI/Emulator-Ergebnisse und ggf. technische Einschränkungen nachweisen; nur dann `E-02` auf `DONE`. Bei kritischem Fehler Fallback evaluieren statt ungeprüft weiterbauen.

**P0-Abnahme:** [grüner Run 43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955) mit [APK-Artefakt](https://github.com/philip20794/Smut/actions/runs/43451063955); [absichtlich roter PR #2](https://github.com/philip20794/Smut/pull/2) zeigte [korrekt übersprungenes APK-Publishing und null Artifacts](https://github.com/philip20794/Smut/actions/runs/43451561861). Anleitung: [APK_DOWNLOAD.md](APK_DOWNLOAD.md).

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | DONE | [Baseline-Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1), [Build-Doku](BUILD.md); alle F-01-Dateien auf `main` geprüft, kein Build erwartet |
| P0 | F-02 | DONE | [Basiscommit](https://github.com/philip20794/Smut/commit/bd859c9357d24db4ed11a38b0c06407c6f9b6859), [Wrapper-Commit](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4), [grüner CI-Lauf mit Unit-Test/Lint/APK](https://github.com/philip20794/Smut/actions/runs/43441065967) |
| P0 | F-03 | DONE | [Code-Commit d85503b](https://github.com/philip20794/Smut/commit/d85503bfa990b31ad8cc6bc63253e680138798d1), [grüner Workflow 43445552751](https://github.com/philip20794/Smut/actions/runs/43445552751): TS-Typecheck/Lint/Tests, Kotlin-Unit/Lint, APK-Assets/Artifact |
| P0 | F-04 | DONE | [CI-Commit 8670def](https://github.com/philip20794/Smut/commit/8670def9a98a642d97a1508e006acca6dfd5029f), [grüner Push 43447739353](https://github.com/philip20794/Smut/actions/runs/43447739353), [roter PR 43447797215](https://github.com/philip20794/Smut/actions/runs/43447797215); abhängiger Android-Job korrekt übersprungen; PR #1 geschlossen, nicht gemergt |
| P0 | F-05 | DONE | [Emulator-Code 4ddc6a3](https://github.com/philip20794/Smut/commit/4ddc6a37911e5b45dd9b4a4e62a448d44bda233a), [sh-Fix d132b72](https://github.com/philip20794/Smut/commit/d132b7279d260f26b9dd375948e958cee9a27e13), [grüner Run 43449455346](https://github.com/philip20794/Smut/actions/runs/43449455346): API-35-Emulator, offline WebView/Handshake und Button-Interaktion bestanden |
| P0 | F-06 | DONE | [CI-/APK-Commit bbafe4f](https://github.com/philip20794/Smut/commit/bbafe4f01e3b02b7dc3df8e9bb2594f4edf18bc0), [Run 43451063955 grün](https://github.com/philip20794/Smut/actions/runs/43451063955), `smut-tested-dev-bbafe4f...` inkl. SHA256SUMS; [roter PR #2, null Artifacts](https://github.com/philip20794/Smut/actions/runs/43451561861) |
| P1 | E-01 | DONE | [Datierte MIT-/Offline-/IME-/A5-/PDF-Entscheidungsmatrix](EDITOR_EVALUATION.md), [ADR-008](DECISIONS.md): Canvas Editor 1.1.0 zuerst als **Testkandidat**, keine vorzeitige E-04-GO-Freigabe |
| P1 | E-02 | TODO | – |
| P1 | E-03 | TODO | – |
| P1 | E-04 | TODO | – |
| P1 | E-05 | TODO | – |
| P2 | D-01 | TODO | – |
| P2 | D-02 | TODO | – |
| P2 | D-03 | TODO | – |
| P2 | D-04 | TODO | – |
| P3 | B-01 | TODO | – |
| P3 | B-02 | TODO | – |
| P3 | B-03 | TODO | – |
| P4 | W-01 | TODO | – |
| P4 | W-02 | TODO | – |
| P4 | W-03 | TODO | – |
| P4 | W-04 | TODO | – |
| P4 | W-05 | TODO | – |
| P5 | L-01 | TODO | – |
| P5 | L-02 | TODO | – |
| P5 | L-03 | TODO | – |
| P5 | L-04 | TODO | – |
| P6 | Q-01 | TODO | – |
| P6 | Q-02 | TODO | – |
| P6 | Q-03 | TODO | – |
| P6 | Q-04 | TODO | – |
| P7 | R-01 | TODO | – |
| P7 | R-02 | TODO | – |
| P7 | R-03 | TODO | – |
| P7 | R-04 | TODO | – |

## 4. Phasenstatus und Qualitäts-Gates

| Phase | Fortschritt | Abschlussbedingung | Gate |
| --- | --- | --- | --- |
| P0 | 6/6 | Build, CI, Emulator-Smoke und Test-APK | **erfüllt** |
| P1 | 1/5 | Machbarkeit/ADR; echter Gerätetest separat | offen |
| P2 | 0/4 | Modell, Operationen, sichere Persistenz, Backup | offen |
| P3 | 0/3 | Bücher-/Kapitelverwaltung | offen |
| P4 | 0/5 | Direktbearbeitung und Textfunktionen | offen |
| P5 | 0/4 | A5-Satz und textbasierter PDF-Export | offen |
| P6 | 0/4 | Offline, Performance, Zuverlässigkeit, Gerätetest | offen |
| P7 | 0/4 | Signierung, Releases, Update, v1-Abnahme | offen |

**Gate-Abhängigkeiten:** siehe [Implementierungsplan](IMPLEMENTATION_PLAN.md). Für P1 kann `E-05` auf eine Geräteabnahme warten, während unabhängige Arbeit fortgesetzt wird. Der Status bleibt `BLOCKED` und zählt nicht als abgeschlossen; spätestens vor P7 muss ein echter Gerätetest vorliegen.

## 5. Letzte Arbeitssitzung / Handoff

- **Datum:** 2026-11-13
- **Aktive Task-ID:** keine; F-06 und E-01 `DONE`.
- **Erreicht:** F-01 bis F-06 (P0-Gate) vollständig abgeschlossen, plus E-01 datierte Technologiebewertung einschließlich Lizenz- und Offline-Risiken.
- **Letzter bestätigter grüner Code-Commit:** [bbafe4f](https://github.com/philip20794/Smut/commit/bbafe4f01e3b02b7dc3df8e9bb2594f4edf18bc0) mit [Run 43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955) `success`.
- **APK:** `smut-tested-dev-bbafe4f01e3b02b7dc3df8e9bb2594f4edf18bc0`, ca. 9,5 MB im Workflow, SHA256SUMS im ZIP, 30 Tage gespeichert, paketiert als `io.github.philip20794.smut.dev`. Noch **keine** stabile Signierung oder Echtgerät-Abnahme.
- **Negativtest:** [PR #2](https://github.com/philip20794/Smut/pull/2) wurde **nicht gemergt** und geschlossen; [Lauf 43451561861](https://github.com/philip20794/Smut/actions/runs/43451561861) `failure`, Editor rot, Android/Emulator/Veröffentlichung übersprungen, **kein einziges Artifact**.
- **E-01:** [EDITOR_EVALUATION.md](EDITOR_EVALUATION.md), [ADR-008](DECISIONS.md): Canvas Editor als erster testbarer Kandidat, Tiptap ohne kostenpflichtige Pages/Conversion-Funktion als Fallback. Mobile Texteingabe/PDF bleiben ungeprüft.
- **Funktioniert nachweislich:** isolierte Offline-WebView mit Button und Bridge, CI-/APK-Ausgabe nach Emulator-Go; fehlerhafte Tests sperren geprüfte APK.
- **Was fehlt:** E-02 echtes Rich-Text-Tippen/Android-IME und Speicherung, E-03 Selektion/Seitenwechsel, E-04 A5-PDF/GO-Entscheidung, später echter Smartphone-Test.
- **Nächster kleinster Schritt:** E-02 Canvas-Editor-Prototyp und gepinnte WebView-Assets.
- **Nutzeraktion nötig?:** nein.

## 6. Aktualisierungsregeln

1. Vor der Arbeit die aktuelle Version auf `main` und die GitHub-Actions-Läufe prüfen; bei Abweichung zwischen Tracker und Code/CI zuerst **reconciliieren**.
2. Genau eine aktiv bearbeitete Aufgabe als `IN_PROGRESS` markieren und die `Nächste ausführbare Aufgabe` aktualisieren.
3. Wenn möglich einen kleinen Code-/Dokumentationscommit mit überprüfbarer Teilfunktion erstellen, dann Tests/CI ausführen.
4. In der Tabellenzeile einen konkreten Prüfbeleg eintragen: Commit, GitHub-Actions-Lauf, Testreport oder bei manuellen Aufgaben Issue/Feedback. Nicht einfach „fertig“ schreiben.
5. `DONE` nur mit bestandenem für die Aufgabe erforderlichem Nachweis. `BLOCKED` mit Grund und exakter Entsperrbedingung. Für offene manuelle Aufgaben nie „automatisch bestanden“ schreiben.
6. Nach jeder abgeschlossenen Aufgabe **Zusammenfassung, letzte Arbeitssitzung, Nächster Schritt, Zähler, Phasenstatus** aktualisieren und committen.
7. Nach Crash/Abbruch eine `IN_PROGRESS`-Aufgabe anhand von Git und CI neu bewerten; Änderungen nicht doppelt ausführen.
8. Keine Aufgabe überspringen, außer die Abhängigkeiten erlauben es und der Grund wird hier dokumentiert. Neue Arbeitspakete unter neuen, stabilen IDs ergänzen; anschließend Zähler aktualisieren.
9. Ein defekter Workflow, eine fehlende APK oder eine fehlgeschlagene Pflichtprüfung wird als Blocker dokumentiert. Keine falsche grüne Bewertung.
10. Die spezifische Wiederaufnahmeroutine steht in [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md).

## 7. Wiederaufnahme-Kurzfassung für Codex

`git fetch` → `main` und letzte Commits prüfen → `AGENTS.md` → `docs/PROGRESS.md` → zugehörige Aufgabe in `docs/IMPLEMENTATION_PLAN.md` → aktuelle Checks/Artifacts → ggf. offenen Task reconciliieren → **exakt den dokumentierten nächsten Einzelschritt umsetzen**. Nach jedem Teilergebnis Fortschritt und Nachweise in Git sichern.
