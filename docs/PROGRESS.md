# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-13
- **Projektstatus:** `IN_PROGRESS` – P0-Gate abgeschlossen, P1 E-01 und E-02 abgeschlossen; echte Android-IME-Eingabe, Unicode, Wortkomposition, Enter/Backspace und Speichern/Neustart im Offline-Canvas-Editor im Emulator erfolgreich. E-03 (Seitenfluss, Auswahl und Zoom) begonnen.
- **Meilenstein:** `P1 – Machbarkeitsnachweis und Technologieentscheidung`
- **Aktive Aufgabe:** `E-03` – `IN_PROGRESS`: Zoomsteuerung sowie isolierten 75-Absatz-Mehrseitentest hinzugefügt; Cloud-Nachweis für diese Änderungen offen
- **Nächste ausführbare Aufgabe:** E-03: im [roten Emulatorlauf 43470049490](https://github.com/philip20794/Smut/actions/runs/43470049490) nicht ausgelösten nativen Zoom-Tap untersuchen und mit belastbarem Test beheben; danach Langdruck-Auswahl, seitenübergreifende Selektion, Ersetzen und IME-Scroll nachweisen.
- **Abgeschlossene Aufgaben:** **8 / 35 (22,9 %)**; ausschließlich Aufgaben mit `DONE` zählen.
- **Letzter verifizierter grüner CI-Lauf:** [Smut CI – 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666), `success`: Editor + Android + API-35-Emulator + geprüfte APK. [F-06-Negativprobe 43451561861](https://github.com/philip20794/Smut/actions/runs/43451561861), `failure`: alle nachgelagerten Jobs/`tested-apk` `skipped`, **keine Artifacts**.
- **Letzte geprüfte APK:** `smut-tested-dev-0d0dfc2707d0bab0cd9a4e44d62ff250ef18ec2a` (ca. 10,8 MB als ZIP, [GitHub Actions 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666), SHA256SUMS enthalten, temporäres 30-Tage-Artifact). **Nicht auf realem Telefon getestet.**
- **Letztes dokumentiertes Release:** keines.
- **Offene technische Entscheidungen:** Editorbibliothek und PDF-Pipeline bleiben bis `E-04` vorläufig.
- **Abhängigkeit vom Nutzer:** echte Smartphone-Abnahmen `E-05`/`Q-04` und spätere sichere Bereitstellung von Release-Signatur-Geheimnissen; jetzt noch nicht erforderlich.
- **Unmittelbarer Blocker:** E-03-Zoom-Touch-Test in [CI 43470049490](https://github.com/philip20794/Smut/actions/runs/43470049490) rot: Android-Tap auf `zoom-in` lässt `getZoom()` bei 0,6 statt 0,7. Die anderen drei Emulator-Tests bestanden; Ursache (Touchkoordinaten oder Zoom-API) noch zu diagnostizieren. E-05 weiterhin manuell offen.

## 2. Verbindlicher nächster Schritt

**E-03 – Markieren, Zoom und dynamischer Seitenwechsel.**

1. E-02-Gate erfüllt: [Smut CI 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666) grün, einschließlich echter Android-Touches, `InputConnection.commitText`, Komposition, Enter, Backspace und erneuter Speicherung. [IME-Testprotokoll](E02_ANDROID_IME_TEST.md).
2. Zoomsteuerung und isolierte Mehrseiten-Testpassage implementiert, Android-Regression und Node-Tests eingecheckt; aktuellen Cloud-Lauf abwarten und etwaige Fehler beheben. [E-03-Testmatrix](E03_PAGE_FLOW.md).
3. Danach langen mehrseitigen deutschen Testtext in isolierter Emulator-Testumgebung prüfen: Anzahl und Inhalt der Seiten, Auswahl/Ersetzen über Grenzen, Scroll bei geöffneter IME. Bei Bedarf Canvas-Editor-Limitierung mit reproduzierbarem Befund dokumentieren.
4. `E-03` nur nach vollständigem erfolgreichen Emulator-Nachweis als `DONE` markieren; E-04 (A5-PDF-Probe) folgt erst danach.

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
| P1 | E-02 | DONE | Android-API-35-Emulator: echter Touch, Fokus des Canvas-Editors, Eingabe über Android-IME einschließlich Unicode und Wortkomposition/Autokorrektur, Enter/Backspace, atomarer Save/Reload; vollständiger [grüner CI-Lauf 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666); [Testprotokoll](E02_ANDROID_IME_TEST.md). Kein Echtgerätetest (E-05) |
| P1 | E-03 | IN_PROGRESS | Touch-Zoomsteuerung und isolierter Mehrseitentest implementiert, [Testmatrix](E03_PAGE_FLOW.md); [CI 43470049490](https://github.com/philip20794/Smut/actions/runs/43470049490): Zoom-Touch rot (`0,6` statt `0,7`), drei weitere Emulator-Tests bestanden; Longpress/Cross-Page und IME-Scroll offen |
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
| P1 | 2/5 | Machbarkeit/ADR; echter Gerätetest separat | offen |
| P2 | 0/4 | Modell, Operationen, sichere Persistenz, Backup | offen |
| P3 | 0/3 | Bücher-/Kapitelverwaltung | offen |
| P4 | 0/5 | Direktbearbeitung und Textfunktionen | offen |
| P5 | 0/4 | A5-Satz und textbasierter PDF-Export | offen |
| P6 | 0/4 | Offline, Performance, Zuverlässigkeit, Gerätetest | offen |
| P7 | 0/4 | Signierung, Releases, Update, v1-Abnahme | offen |

**Gate-Abhängigkeiten:** siehe [Implementierungsplan](IMPLEMENTATION_PLAN.md). Für P1 kann `E-05` auf eine Geräteabnahme warten, während unabhängige Arbeit fortgesetzt wird. Der Status bleibt `BLOCKED` und zählt nicht als abgeschlossen; spätestens vor P7 muss ein echter Gerätetest vorliegen.

## 5. Letzte Arbeitssitzung / Handoff

- **Datum:** 2026-11-13
- **Aktive Task-ID:** E-03 `IN_PROGRESS` – Zoomsteuerung und Mehrseitentest für A5-Prototyp implementiert, CI noch nicht abschließend bewertet.
- **Erreicht:** F-01 bis F-06 und E-01/E-02 vollständig DONE (8 von 35), darunter Android-IME-Unicode, Android-Kompositionskorrektur, Enter, Backspace und Wiederherstellung. Fokusprobleme in [roten Zwischenläufen 43463790793](https://github.com/philip20794/Smut/actions/runs/43463790793) und [43466552817](https://github.com/philip20794/Smut/actions/runs/43466552817) durch Emulator-WakeUp, Keyguard-Abbau und expliziten Foreground-Start behoben.
- **Letzter bestätigter grüner Code-Commit:** [0d0dfc2](https://github.com/philip20794/Smut/commit/0d0dfc2707d0bab0cd9a4e44d62ff250ef18ec2a) mit [Run 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666) `success`.
- **Letzte sicher geprüfte APK:** `smut-tested-dev-0d0dfc2707d0bab0cd9a4e44d62ff250ef18ec2a`, ZIP mit SHA256SUMS, GitHub-Actions-Artefakt 30 Tage, Entwickler-Paket-ID `io.github.philip20794.smut.dev`. Noch **keine** stabile Release-Signierung oder reale Smartphone-Abnahme.
- **E-03-Code:** [Zoom-Implementierung im Editor](../editor/src/main.ts), Grenzen 50–100 % und UI-Zoomschaltflächen, isolierter 75-Absatz-Test auf mehrere A5-Seiten, Unit-/Emulator-Tests. Dies zählt noch nicht als E-03-DONE.
- **Was fehlt:** E-03-Seitenwechsel, Long-Press-Auswahl, Selektion/Ersetzen und Android-IME-Scroll; E-04 PDF-Probe/GO; Geräteabnahme E-05 und alle späteren Phasen P2–P7.
- **Nächster kleinster Schritt:** Zoom-Tap-Bug aus Run 43470049490 diagnostizieren (angezeigtes Canvas/DOM-Touchziel, Fokus, `executePageScale`); echten UI-Touch reparieren und die vollständige CI erneut grün bringen; erst danach E-03-Langdruck/Selektion.
- **Nutzeraktion nötig?:** derzeit nein; E-05 braucht später manuelles Feedback.

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
