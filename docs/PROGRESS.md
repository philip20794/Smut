# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `PLANNED` – Dokumentation und Implementierungsplan vorhanden; Anwendung, Build-Workflows und Tests noch nicht implementiert.
- **Meilenstein:** `P0 – Projektgerüst und Cloud-Build`
- **Aktive Aufgabe:** keine
- **Nächste ausführbare Aufgabe:** `F-01` – Repo-Baseline und Buildkonventionen
- **Abgeschlossene Aufgaben:** **0 / 35 (0 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** keiner; noch keine GitHub Actions Workflows.
- **Letzte geprüfte APK:** keine.
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** keiner für `F-01`.

## 2. Verbindlicher nächster Schritt

**Aufgabe:** `F-01`

**Ziel:** Reproduzierbare Projektstruktur und dokumentierte Buildkonventionen vorbereiten.

**Konkret fortfahren mit:**
1. `main` inspizieren und prüfen, ob inzwischen Projektdateien/Workflows existieren.
2. Versionierte Android-/Kotlin-/Gradle- und Node/TypeScript-Toolchain festlegen; Lizenz-/Paketkennungsentscheidungen notieren, keine unbestätigten Bibliotheken endgültig wählen.
3. Struktur und Basiskonfiguration gemäß F-01 anlegen; `.gitignore`, README-Buildhinweise und ggf. ADR ergänzen.
4. Änderungen auf `main` committen und anhand der verfügbaren Prüfungen verifizieren; falls noch keine CI vorhanden, das als dokumentierte Ausnahme für F-01 deklarieren.
5. Erst nach belegtem Abschluss `F-01` auf `DONE` setzen, nächste Aufgabe `F-02` vormerken.

**Letzter Arbeitsnachweis:** [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) wurde mit allen 35 Tasks angelegt. Noch keine Implementierungsaufgabe wurde begonnen.

## 3. Task-Tracker

Statuswerte ausschließlich: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`.

| Phase | Task-ID | Status | Prüfbeleg / Blocker |
| --- | --- | --- | --- |
| P0 | F-01 | TODO | – |
| P0 | F-02 | TODO | – |
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
| P0 | 0/6 | Build, CI, Emulator-Smoke und Test-APK | offen |
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
- **Erreicht:** bestehende Dokumente gelesen, vollständigen Plan aufgestellt und Tracker initialisiert.
- **Implementierter Code:** keiner.
- **Geänderte Anforderungen:** Die technische Entscheidung wird nach dem P1-Prototyp getroffen; ein **vorläufiger** Gesamtplan besteht bereits.
- **Verifizierte Builds/Tests:** keine (noch nicht vorhanden).
- **Offen:** F-01.
- **Nächster konkreter Schritt:** `F-01` starten, Plan/AGENTS beachten, Basisprojekt und erste Buildfestlegungen einchecken.
- **Risiken:** Touch-/IME-Kompatibilität und PDF-Fidelity; als P1-Gates geplant.
- **Benötigte Nutzerentscheidung jetzt:** keine.

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
