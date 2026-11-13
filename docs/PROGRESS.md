# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – P0 läuft; F-01 bis F-03 abgeschlossen. Der lokale TypeScript-/WebView-Container ist mit Node-/Kotlin-Tests, Android-Lint und gebündelter Debug-APK im Cloud-Build nachgewiesen.
- **Meilenstein:** `P0 – Projektgerüst und Cloud-Build`
- **Aktive Aufgabe:** `F-05` – `IN_REVIEW`: Emulator- und UI-Test-Implementierung eingecheckt, Cloud-Nachweis offen
- **Nächste ausführbare Aufgabe:** F-05-Emulator-GitHub-Actions auswerten, Fehler beheben; danach `F-06`
- **Abgeschlossene Aufgaben:** **4 / 35 (11,4 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** [Smut CI – 43447739353](https://github.com/philip20794/Smut/actions/runs/43447739353) – `success` für Editor-Typprüfung/Lint/Unit-Tests und Android-JVM-Tests/Lint/APK. [Negativprobe 43447797215](https://github.com/philip20794/Smut/actions/runs/43447797215) wurde mit absichtlich fehlschlagendem Node-Test rot; Android-Job übersprungen.
- **Letzte geprüfte APK:** `smut-f03-debug-d85503bfa990b31ad8cc6bc63253e680138798d1` (ca. 9,5 MB, Actions-Artefakt im [erfolgreichen Lauf](https://github.com/philip20794/Smut/actions/runs/43445552751)); **nicht auf realem Gerät getestet**.
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner für `F-05`; der Cloud-Emulator-Smoke-Test fehlt.

## 2. Verbindlicher nächster Schritt

**F-05 – Implementierung in Prüfung.** Ein Android-UI-Test auf API 35 soll die lokale WebView bei abgeschaltetem WLAN/Mobilfunk starten, den nativen Bridge-Handshake bestätigen, den neuen UI-Button tatsächlich betätigen und die Antwort prüfen.

**Nächster Schritt:** Den neuen Actions-Lauf auf Emulator-Start, Instrumentationstest und Fehler-Reports prüfen. Bei Fehlern ohne Testabschwächung korrigieren; erst bei nachgewiesen grünem Lauf `F-05` auf `DONE` setzen. Danach folgt `F-06`.

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | DONE | [Baseline-Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1), [Build-Doku](BUILD.md); alle F-01-Dateien auf `main` geprüft, kein Build erwartet |
| P0 | F-02 | DONE | [Basiscommit](https://github.com/philip20794/Smut/commit/bd859c9357d24db4ed11a38b0c06407c6f9b6859), [Wrapper-Commit](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4), [grüner CI-Lauf mit Unit-Test/Lint/APK](https://github.com/philip20794/Smut/actions/runs/43441065967) |
| P0 | F-03 | DONE | [Code-Commit d85503b](https://github.com/philip20794/Smut/commit/d85503bfa990b31ad8cc6bc63253e680138798d1), [grüner Workflow 43445552751](https://github.com/philip20794/Smut/actions/runs/43445552751): TS-Typecheck/Lint/Tests, Kotlin-Unit/Lint, APK-Assets/Artifact |
| P0 | F-04 | DONE | [CI-Commit 8670def](https://github.com/philip20794/Smut/commit/8670def9a98a642d97a1508e006acca6dfd5029f), [grüner Push 43447739353](https://github.com/philip20794/Smut/actions/runs/43447739353), [roter PR 43447797215](https://github.com/philip20794/Smut/actions/runs/43447797215); abhängiger Android-Job korrekt übersprungen; PR #1 geschlossen, nicht gemergt |
| P0 | F-05 | IN_REVIEW | Instrumentierter Offline-WebView-/Button-/Handshake-Test für Android API 35; erfolgreicher CI-Lauf ausstehend |
| P0 | F-06 | TODO | – |
| P1 | E-01 | TODO | – |
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
| P0 | 4/6 | Build, CI, Emulator-Smoke und Test-APK | offen |
| P1 | 0/5 | Machbarkeit/ADR; echter Gerätetest separat | offen |
| P2 | 0/4 | Modell, Operationen, sichere Persistenz, Backup | offen |
| P3 | 0/3 | Bücher-/Kapitelverwaltung | offen |
| P4 | 0/5 | Direktbearbeitung und Textfunktionen | offen |
| P5 | 0/4 | A5-Satz und textbasierter PDF-Export | offen |
| P6 | 0/4 | Offline, Performance, Zuverlässigkeit, Gerätetest | offen |
| P7 | 0/4 | Signierung, Releases, Update, v1-Abnahme | offen |

**Gate-Abhängigkeiten:** siehe [Implementierungsplan](IMPLEMENTATION_PLAN.md). Für P1 kann `E-05` auf eine Geräteabnahme warten, während unabhängige Arbeit fortgesetzt wird. Der Status bleibt `BLOCKED` und zählt nicht als abgeschlossen; spätestens vor P7 muss ein echter Gerätetest vorliegen.

## 5. Letzte Arbeitssitzung / Handoff

- **Datum:** 2026-11-13
- **Aktive Task-ID:** keine; F-04 `DONE`.
- **Erreicht:** CI bei Push auf `main`, Pull Requests und manuellem Start, mit Node-24-/Gradle-/JDK-/Android-Toolchains, npm-/Gradle-Caches, TypeScript-Typprüfung, Sicherheitslint, Node-Tests, Kotlin-Unit-Tests, Android-Lint und Debug-APK.
- **Letzter Code-Commit:** [8670def](https://github.com/philip20794/Smut/commit/8670def9a98a642d97a1508e006acca6dfd5029f).
- **Positiver Nachweis:** [Workflow 43447739353](https://github.com/philip20794/Smut/actions/runs/43447739353), `success`.
- **Kontrollierter negativer Nachweis:** [PR #1](https://github.com/philip20794/Smut/pull/1) mit absichtlich fehlschlagendem Node-Test; [Workflow 43447797215](https://github.com/philip20794/Smut/actions/runs/43447797215) `failure`, abhängiger Android-Job `skipped`; PR ohne Merge geschlossen. Der Negativtest liegt nicht auf `main`.
- **Funktioniert nachweislich:** F-03-Offline-WebView kompiliert, CI erkennt defekte Tests und stoppt den nachgelagerten Android-Job.
- **Was fehlt:** F-05 instrumentierter Emulator-Smoke (WebView starten/bedienen, Screenshots/Logcat bei Fehlern), danach F-06 geprüfte APK-Auslieferung.
- **Nächster kleinster ausführbarer Schritt:** Android-Instrumentationstest und Emulator-Runner in GitHub Actions ergänzen, CI prüfen.
- **Nutzeraktion nötig?:** keine.

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
