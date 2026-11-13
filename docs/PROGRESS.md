# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – P0 läuft; F-01 und F-02 abgeschlossen, F-03 implementiert und in Cloud-Prüfung. Lokaler TypeScript-/WebView-Container, Bridge-Tests und Asset-Build sind eingecheckt; F-03 noch nicht als grün nachgewiesen.
- **Meilenstein:** `P0 – Projektgerüst und Cloud-Build`
- **Aktive Aufgabe:** `F-03` – `IN_REVIEW`, wartend auf einen erfolgreichen CI-Run der neuen Quellen
- **Nächste ausführbare Aufgabe:** F-03-CI prüfen und eventuelle Fehler beheben; danach `F-04`
- **Abgeschlossene Aufgaben:** **2 / 35 (5,7 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** [Android F-02 Bootstrap – 43441065967](https://github.com/philip20794/Smut/actions/runs/43441065967) – Erfolg, Unit-Test/Lint/Debug-APK gebaut. Vollständige CI folgt in F-04.
- **Letzte geprüfte APK:** `smut-f02-debug-ea85d41e72b226d014e65bdd806befa2cdb7d729` (Actions-Artefakt im [erfolgreichen Lauf](https://github.com/philip20794/Smut/actions/runs/43441065967)); **nicht auf realem Gerät getestet**.
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner; F-03 darf erst nach tatsächlich grünem Cloud-Build auf `DONE`.

## 2. Verbindlicher nächster Schritt

**F-03 – Code eingecheckt, Cloud-Verifikation offen.** TypeScript 5.9.3, npm-Lockdatei, lokale HTML-/CSS-/JS-Ausgabe, Kotlin-WebViewAssetLoader und enger versionierter JSON-Handshake sind implementiert. Der Bootstrap-Workflow prüft jetzt npm-Typecheck, Sicherheitslint, Node-Bridge-Tests, Android-Unit-Tests, Android-Lint und die Assets in der APK.

**Weiterarbeit:** Den GitHub-Actions-Lauf des F-03-Code-Commits auswerten, Fehler beheben und erneut prüfen. Bei Grün F-03 auf `DONE` setzen und `F-04` aktivieren. Keine reale Touch-/IME-Abnahme behaupten; diese kommt später.

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | DONE | [Baseline-Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1), [Build-Doku](BUILD.md); alle F-01-Dateien auf `main` geprüft, kein Build erwartet |
| P0 | F-02 | DONE | [Basiscommit](https://github.com/philip20794/Smut/commit/bd859c9357d24db4ed11a38b0c06407c6f9b6859), [Wrapper-Commit](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4), [grüner CI-Lauf mit Unit-Test/Lint/APK](https://github.com/philip20794/Smut/actions/runs/43441065967) |
| P0 | F-03 | IN_REVIEW | TypeScript-/WebView-Code + Tests auf `main`; neuer CI-Run noch ausstehend |
| P0 | F-04 | TODO | – |
| P0 | F-05 | TODO | – |
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
| P0 | 2/6 | Build, CI, Emulator-Smoke und Test-APK | offen |
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
- **Aktive Task-ID:** F-03 `IN_REVIEW`.
- **Erreicht:** TypeScript-Modul mit gepinnter Lockdatei und Node-Tests, statisches A5-Offline-HTML, Kotlin-WebView mit AssetLoader/URL-Allowlist, schmaler JSON-Handshake, negative JVM-Tests, Android-Gradle-Asset-Tasks sowie F-03-Cloud-Workflow.
- **Letzter verifizierter grüner CI-Lauf:** [43442035683](https://github.com/philip20794/Smut/actions/runs/43442035683) vom vorherigen Stand F-02, **nicht** F-03.
- **Letzter Code-Commit:** siehe Git-Commit direkt vor diesem Tracker; dessen F-03-Cloud-Workflow muss separat ausgewertet werden.
- **Was funktioniert nachweislich:** F-02-Android-Build und Tests. **Noch nicht nachgewiesen:** F-03-Compile/Assetpackaging, WebView-Laufzeit im Emulator, Echtgerät-Tests, Buchfunktionen und PDF.
- **Was fehlt / ist blockiert:** F-03-Workflow grün bestätigen; danach F-04 (PR/push/manuell, Pflichtchecks, Red-Test-Nachweis).
- **Nächster kleinster ausführbarer Schritt:** Actions-Lauf für F-03-Code prüfen und Tracker auf tatsächlichen Status bringen.
- **Entscheidung / Risiken:** Kein Rich-Text-Framework vor dem P1-GO; die native Bridge ist bewusst auf den `ready`-Handshake begrenzt.
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
