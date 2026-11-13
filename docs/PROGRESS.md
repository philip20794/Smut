# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – P0 läuft; F-01 und F-02 abgeschlossen. Minimale Compose-App, Unit-Test und Cloud-Debug-APK nachgewiesen; der lokale Editor fehlt noch.
- **Meilenstein:** `P0 – Projektgerüst und Cloud-Build`
- **Aktive Aufgabe:** keine; F-02 erfolgreich abgeschlossen
- **Nächste ausführbare Aufgabe:** `F-03` – lokal gebündelter Editor-Container (TypeScript + WebView)
- **Abgeschlossene Aufgaben:** **2 / 35 (5,7 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** [Android F-02 Bootstrap – 43441065967](https://github.com/philip20794/Smut/actions/runs/43441065967) – Erfolg, Unit-Test/Lint/Debug-APK gebaut. Vollständige CI folgt in F-04.
- **Letzte geprüfte APK:** `smut-f02-debug-ea85d41e72b226d014e65bdd806befa2cdb7d729` (Actions-Artefakt im [erfolgreichen Lauf](https://github.com/philip20794/Smut/actions/runs/43441065967)); **nicht auf realem Gerät getestet**.
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner für `F-03`. Das Gradle-/Compose-Grundprojekt wurde im Cloud-Build verifiziert.

## 2. Verbindlicher nächster Schritt

**Aufgabe:** `F-03` – Lokal gebündelter Editor-Container.

**Ziel:** Die vorhandene Compose-App um eine offline funktionierende WebView erweitern, in der zunächst ein kleines lokal gebautes TypeScript-UI läuft. Die endgültige Rich-Text-Engine wird erst in P1 entschieden.

**Konkret fortfahren mit:**
1. Die aktuelle `main`-Revision, [AGENTS.md](../AGENTS.md), [F-03 im Plan](IMPLEMENTATION_PLAN.md) und [BUILD.md](BUILD.md) lesen; F-02 nach grüner CI nicht erneut aufbauen.
2. Im Ordner `editor/` ein reproduzierbares TypeScript-Projekt mit `package.json`, Lockdatei und Skripten für Typprüfung, Lint, Tests und Build anlegen.
3. TypeScript/HTML/CSS offline als Assets in die APK integrieren, mit sicherer WebView-Konfiguration und schmaler Kotlin-JavaScript-Bridge.
4. Bridge-Vertrag und lokalen Asset-Zugriff testen, keine externen Netzwerkzugriffe oder CDNs. Rich-Text-Bibliothek nicht vor P1-GO festlegen.
5. Mit GitHub Actions den Android-Build einschließlich lokaler Editor-Assets tatsächlich verifizieren und Test-/Commit-Links hier dokumentieren.

**Vorhandene Grundlage:** [GitHub Actions Lauf 43441065967](https://github.com/philip20794/Smut/actions/runs/43441065967) ist grün; `testDebugUnitTest`, `lintDebug`, `assembleDebug` bestanden, offizielle Wrapper-JAR wurde nach Prüfsumme committed. Test-APK und JUnit-Reports wurden als Artefakte hochgeladen.

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | DONE | [Baseline-Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1), [Build-Doku](BUILD.md); alle F-01-Dateien auf `main` geprüft, kein Build erwartet |
| P0 | F-02 | DONE | [Basiscommit](https://github.com/philip20794/Smut/commit/bd859c9357d24db4ed11a38b0c06407c6f9b6859), [Wrapper-Commit](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4), [grüner CI-Lauf mit Unit-Test/Lint/APK](https://github.com/philip20794/Smut/actions/runs/43441065967) |
| P0 | F-03 | TODO | – |
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
- **Aktive Task-ID:** keine; F-02 `DONE`.
- **Erreicht:** Kotlin-/Jetpack-Compose-Android-Modul mit Startscreen, Manifest, Strings, A5-Konfigurationswerten und JUnit-Test; Gradle Root/Versionen, offizieller Wrapper samt JAR; minimaler automatischer Cloud-Build als Bootstrap-Workflow.
- **Letzter verifizierter Code-Commit:** [954d2e89](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4) (offizielle Wrapper-JAR nach [Basiscommit](https://github.com/philip20794/Smut/commit/bd859c9357d24db4ed11a38b0c06407c6f9b6859) und [SDK-Fix](https://github.com/philip20794/Smut/commit/ea85d41e72b226d014e65bdd806befa2cdb7d729)).
- **Verifizierte Builds/Tests:** [Workflow 43441065967](https://github.com/philip20794/Smut/actions/runs/43441065967) `success`; `app:testDebugUnitTest`, `app:lintDebug`, `app:assembleDebug` erfolgreich.
- **APK-Artefakt:** `smut-f02-debug-ea85d41e72b226d014e65bdd806befa2cdb7d729` (ca. 9,4 MB als GitHub-Artifakt); Report `smut-f02-test-reports`.
- **Erster CI-Fehler und Lösung:** [Lauf 43440943578](https://github.com/philip20794/Smut/actions/runs/43440943578) scheiterte am veralteten SDK-Paket `tools`. Entfernt; Folgelauf grün.
- **Funktioniert nachweislich:** Gradle-Build, Kotlin/Compose-Kompilierung, Unit-Test, Lint, APK-Erstellung. **Nicht nachgewiesen:** Installation auf echtem Telefon, vollständige UI-Gesten, Editor, PDF, Signier-/Updatepfad.
- **Offene Risiken:** Android-IME/Touch-Editor und PDF-Fidelity; Technologiewahl erst P1. F-04 erweitert den derzeitigen Bootstrap-Build zur vollständigen CI, F-05 ergänzt Emulator-UI-Tests.
- **Was fehlt:** F-03 bis R-04.
- **Nächster kleinster ausführbarer Schritt:** F-03 beginnen, lokales TypeScript-Projekt und Offline-WebView integrieren.
- **Benötigte Nutzeraktion:** keine.

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
