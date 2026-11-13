# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – P0 läuft; F-01 abgeschlossen, F-02 (startbare Android-App) als Nächstes. Noch keine App, CI oder APK.
- **Meilenstein:** `P0 – Projektgerüst und Cloud-Build`
- **Aktive Aufgabe:** `F-02` – Android-Grundprojekt und Cloud-Buildnachweis
- **Nächste ausführbare Aufgabe:** `F-02` abschließen: Android-App kompilieren, Smoke-Test und APK verifizieren
- **Abgeschlossene Aufgaben:** **1 / 35 (2,9 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** keiner; noch keine GitHub Actions Workflows.
- **Letzte geprüfte APK:** keine.
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner für `F-02`. Die Baseline muss in F-02 erstmals mit einem echten Gradle-Build geprüft werden.

## 2. Verbindlicher nächster Schritt

**Aufgabe:** `F-02` – Minimale startbare Android-App.

**Ziel:** Die in F-01 festgelegte Kotlin-/Gradle-Baseline erstmalig tatsächlich kompilieren und eine installierbare Debug-APK erzeugen.

**Konkret fortfahren mit:**
1. `main`, [Buildkonventionen](BUILD.md), [ADR-006](DECISIONS.md) und Aufgabe F-02 im [Implementierungsplan](IMPLEMENTATION_PLAN.md) lesen.
2. Gradle Wrapper **inklusive Wrapper-JAR** für Gradle 8.13, `settings.gradle.kts`, Root-Build und `app/build.gradle.kts` sauber erzeugen. Versionen aus `gradle/libs.versions.toml` beziehen. Paketkennung `io.github.philip20794.smut` beibehalten.
3. Minimale Compose-Startoberfläche, Android-Manifest, Ressourcen und Unit-Smoke-Test ergänzen; `minSdk = 26`, `compileSdk/targetSdk = 36`.
4. Gradle Build und Tests **wirklich** ausführen. Ohne lokalen Rechner nach Möglichkeit einen geeigneten Cloud-Runner/Workflow für den ersten Build einrichten; F-04 bleibt der Ausbau der vollständigen CI. Keine nicht ausgeführten Prüfungen als grün markieren.
5. Gradle-/Kotlin-Versionen bei Unverträglichkeit gemeinsam und mit ADR-Grund aktualisieren. Dann verifizierten Commit, Testnachweis und nächsten Schritt im Tracker festhalten.

**Letzter Arbeitsnachweis (F-01):** [Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1). Repository-Dateien, Versions-Pins und Dokumentationsverweise wurden via GitHub auf `main` geprüft. **Kein Gradle-Build, keine Tests und keine APK vorhanden**; das ist bis F-02/F-06 erwartbar.

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | DONE | [Baseline-Commit](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1), [Build-Doku](BUILD.md); alle F-01-Dateien auf `main` geprüft, kein Build erwartet |
| P0 | F-02 | IN_PROGRESS | Compose-Startprojekt, Gradle Wrapper und minimaler Cloud-Build werden eingerichtet; Prüfbericht steht aus |
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
| P0 | 1/6 | Build, CI, Emulator-Smoke und Test-APK | offen |
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
- **Aktive Task-ID:** keine; F-01 `DONE`.
- **Erreicht:** `.gitignore`, `.editorconfig`, `.node-version`, `gradle/libs.versions.toml`, `app/README.md`, `editor/README.md`, [BUILD.md](BUILD.md), ADR-006, README- und AGENTS-Links direkt auf `main`.
- **Letzter verifizierter Code-/Konfigurations-Commit:** [5801f814](https://github.com/philip20794/Smut/commit/5801f814e619999645b658fdd9930b02c5fb00c1) (vor Aktualisierung dieses Trackers).
- **Build-/Test-Evidenz:** GitHub-Dateiprüfung bestätigt alle F-01-Pfade, die Versionen `AGP 8.13.2`/`Kotlin 2.2.21`/`Node 24.22.3`, Paketkennung und Dokumentationslinks. **Kein ausführbarer Build/Test**; Gradle Wrapper, Quellcode und CI sind noch nicht vorhanden.
- **Implementierter Android-Code:** keiner. Die Modulordner sind reserviert.
- **Risiko:** Kompatibilität der konkreten Plugin- und SDK-Versionen muss mit tatsächlichem F-02-Build bestätigt werden; Android-IME, PDF-Fidelity bleiben P1-Risiken.
- **Was fehlt:** F-02 sowie alle Folgeaufgaben. Eine Build-APK wird erstmals in F-02 erzeugt und ab F-06 zuverlässig verteilt.
- **Nächster kleinster ausführbarer Schritt:** F-02 beginnen; Gradle Wrapper und Android-Grundprojekt erzeugen, dann echten Build verifizieren.
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
