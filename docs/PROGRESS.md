# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-14.
- **Projektstatus:** `IN_PROGRESS` – P0, P1 E-01 bis E-04, P2 sowie P3 B-01/B-02/B-03 abgeschlossen; W-01 (produktive Editorintegration) als Nächstes. Der A5-Prototyp nutzt bis W-01 weiterhin sein separates Legacy-Format.
- **Meilenstein:** `P4 – Alltagstauglicher Editor`, durch den vollständig grünen P3-Gate freigegeben.
- **Letzte abgeschlossene Aufgabe:** `B-03 DONE` – Wiederaufnahme des gespeicherten Kapitel-/Cursor-/Scrollankers, sicherer Fallback bei veraltetem Block/Kapitel, System-Zurück-Navigation und reduziertes Buchmenü; [B03_READING_POSITION.md](B03_READING_POSITION.md), [vollständige CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487) grün.
- **Nächste ausführbare Aufgabe:** `W-01 IN_PROGRESS` – strukturiertes D-01/D-03-Buchmodell mit editierbarem A5-Canvas verbinden, Migration ohne Datenverlust, echte Eingabe-/Autosave-/Emulator-Tests. `E-05` bleibt `BLOCKED` bis zum echten Smartphone-Test.
- **Abgeschlossene Aufgaben:** **17 / 35 (48,6 %)**; nur `DONE` zählt.
- **Letzter verifizierter grüner Code-CI-Lauf:** [Smut CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487), Commit [5e89c93](https://github.com/philip20794/Smut/commit/5e89c932846a096b5b238008fc477ccb933e46f2): TypeScript, Kotlin-Unit/Lint/APK, Offline-Emulator inklusive B-03-Positions-/Navigations-Regression sowie aller bisherigen B-02/B-01/D-04/Editor- und PDF-Tests erfolgreich.
- **Letzte geprüfte APK:** `smut-tested-dev-5e89c932846a096b5b238008fc477ccb933e46f2` aus [CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487), verfügbar bis 14.12.2026; PDF-Probe `smut-e04-a5-pdf-probe-5e89c932846a096b5b238008fc477ccb933e46f2`. **Kein echter Smartphone-Test.**
- **Letztes Release:** keines; signierte Releases stehen aus.
- **Technische Entscheidung:** **GO** für die offlinefähige Prototyp-Architektur; **kein druckfertiges Benutzer-PDF**: problematische Worttrennungen/Blocksatz, nur PDF-Standardfonts ohne eingebettete lokale Buchschrift, fehlender Zeilen-Goldenvergleich und noch keine anwenderseitige Export-UI. Das sind Pflichtpunkte für P5 L-01 bis L-04, ggf. Engine-Fallback bei Nichterfüllung.
- **Nutzeraktion:** E-05 auf echtem Smartphone (später); W-01 kann ohne Nutzerhardware umgesetzt werden.

## 2. Verbindlicher nächster Schritt

**W-01 – Persistentes direktes Schreiben** nach grünem B-03-Gate.

1. Die bis jetzt getrennte D-01/D-03-Buchstruktur und den editierbaren A5-Canvas verbinden, ohne `prototype-editor-v1.json` zu überschreiben. Migration erst nach nachgewiesenem verlustfreiem Test.
2. Kapitelwahl, Texteinfügung, Cursor-/Scrollanker und Autosave revisionssicher verbinden. Konflikte und Speicherfehler dürfen keine falschen Erfolgsmeldungen erzeugen.
3. Android-End-to-End-Tests für Tippen, Neustart, App-Abbruch und Wiederaufnahme über mehrere Buchseiten; bestehende Backup- und PDF-Regressionsprüfungen erhalten.
4. Vollständige Pflicht-CI inklusive Offline-Emulator vor `W-01 DONE`; `E-05` bleibt ein separater echter Smartphone-Test.

**Letzte grüne Codeprüfung:** [CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487) für [B-03](https://github.com/philip20794/Smut/commit/5e89c932846a096b5b238008fc477ccb933e46f2); strukturierte Bücher öffnen derzeit als native Leseansicht, editierbarer A5-Prototyp unabhängig. Direkte Anbindung ist der Kern von W-01.

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
| P1 | E-03 | DONE | [CI 43574312605](https://github.com/philip20794/Smut/actions/runs/43574312605) vollständig grün: native Auswahlgriffe mit echter Android-Endgriff-Geste, frühere Zwei-Seiten-Auswahl, Rand-Autoscroll, Zoom/IME und Cursor-Sichtbarkeit; [Code 1ef6753](https://github.com/philip20794/Smut/commit/1ef67535d34217d74a1e3ff060ed2ecfa5e447cf). E-05 bleibt gesonderter echter Gerätetest. |
| P1 | E-04 | DONE | [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300) vollständig grün: drei Seiten Editor/PDF, 419,25×595,50 pt, durchsuchbares Deutsch/Unicode, keine Rasterbilder, 13 Android-Emulatortests, geprüfte Test-APK. [Messbericht](E04_PDF_PROBE.md), [ADR-009](DECISIONS.md) (GO nur für Prototyp, druckreifer Satz später P5). |
| P1 | E-05 | BLOCKED | MANUAL: tatsächliche Bedienungs- und PDF-Sichtprüfung auf einem Android-Smartphone benötigt. Keine Emulator-Simulation als Freigabe. D-01 parallel möglich. |
| P2 | D-01 | DONE | [Code 512b0c5](https://github.com/philip20794/Smut/commit/512b0c5d2a422d7ec85ea0890c2fced6a8f66e38), [vollständige CI 43583247353](https://github.com/philip20794/Smut/actions/runs/43583247353) grün (TS/Lint/Unit, Kotlin/Lint/APK, Emulator/PDF), [Modell-/Migrationsvertrag](D01_DOCUMENT_MODEL.md) |
| P2 | D-02 | DONE | [Code a7df11d](https://github.com/philip20794/Smut/commit/a7df11d282226e82cbda4336086ff9a7ee383aef), [CI 43585173130](https://github.com/philip20794/Smut/actions/runs/43585173130) vollständig grün (Editor-Typecheck, Node-Unit, Kotlin/Lint/APK, Emulator und PDF-Probe); [Abnahmevertrag](D02_EDIT_OPERATIONS.md) |
| P2 | D-03 | DONE | [Code bfd2c82](https://github.com/philip20794/Smut/commit/bfd2c82e085844930bd5490924186c5f287bc8c0), [CI 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623) vollständig grün (Room, AtomicFile, Revisionsschutz, Emulator-Crash-/Recovery-Tests und Autosave-Node-Tests); [D03-Vertrag](D03_PERSISTENCE.md) |
| P2 | D-04 | DONE | [Code 1386ce4](https://github.com/philip20794/Smut/commit/1386ce4a9fa676bad8b533d28ad6d1d5c48d9811), [CI 43592192732](https://github.com/philip20794/Smut/actions/runs/43592192732) grün (SAF-ContentResolver, SHA-256, Export–Löschen–Import, Konflikte, parallele Zugriffe, bestehende Editor-/PDF-Tests); [D04-Vertrag](D04_BACKUP.md) |
| P3 | B-01 | DONE | [Code/Tests fcf7642](https://github.com/philip20794/Smut/commit/fcf7642272d0ec716b57bd83dce67c2916377359), [vollständige CI 43594430969](https://github.com/philip20794/Smut/actions/runs/43594430969) grün: native Buchliste, CRUD, Bestätigungsabbruch, eindeutige IDs, Room-/Datei-Neustart, Lösch-Recovery; [B-01-Doku](B01_BOOK_LIBRARY.md) |
| P3 | B-02 | DONE | [Code/Test-Fix da2678b](https://github.com/philip20794/Smut/commit/da2678bbf93aa43196e5a5bc350f258cec17616c), [vollständige CI 43597289709](https://github.com/philip20794/Smut/actions/runs/43597289709) grün: Kapitel CRUD, Reorder, Navigation, Emoji-Cursoranker, Revisionskonflikte, lange Kapitel, UI-Abbruch/Bestätigung, Neustart; [B-02-Doku](B02_CHAPTER_MANAGEMENT.md) |
| P3 | B-03 | DONE | [Code/Test 5e89c93](https://github.com/philip20794/Smut/commit/5e89c932846a096b5b238008fc477ccb933e46f2), [vollständige CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487) grün: echte Android-Navigation/Activity-Recreation, Kapitel-/Cursor-/Scrollposition, sichere Fallbacks für veraltete Anker, Prüfsummen-Schutz; [B-03-Doku](B03_READING_POSITION.md) |
| P4 | W-01 | IN_PROGRESS | Strukturierte Bücher an den Canvas-Editor anbinden; Unicode, Autosave, Recovery, Android-IME und Legacy-Isolation testen. CI ausstehend. |
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
| P1 | 4/5 | E-01…E-04 GO, echter Gerätetest E-05 separat | **GO für D-01; E-05 offen** |
| P2 | 4/4 | Modell, Operationen, sichere Persistenz, Backup | **erfüllt – P3 freigegeben** |
| P3 | 3/3 | Bücherliste, Kapitelverwaltung, Positionswiederaufnahme und Minimalnavigation fertig | **erfüllt – P4 freigegeben** |
| P4 | 0/5 | Direktbearbeitung und Textfunktionen | offen |
| P5 | 0/4 | A5-Satz und textbasierter PDF-Export | offen |
| P6 | 0/4 | Offline, Performance, Zuverlässigkeit, Gerätetest | offen |
| P7 | 0/4 | Signierung, Releases, Update, v1-Abnahme | offen |

**Gate-Abhängigkeiten:** siehe [Implementierungsplan](IMPLEMENTATION_PLAN.md). Für P1 kann `E-05` auf eine Geräteabnahme warten, während unabhängige Arbeit fortgesetzt wird. Der Status bleibt `BLOCKED` und zählt nicht als abgeschlossen; spätestens vor P7 muss ein echter Gerätetest vorliegen.

## 5. Letzte Arbeitssitzung / Handoff

### W-01 IN_PROGRESS – strukturierter A5-Editor

- Aktive Aufgabe: gespeicherte Bücher und Kapitel im lokalen A5-Canvas editierbar machen; transportiert werden nur D-01-Buchdaten über die bereits vorhandene, revisionsgeprüfte D-03-Bridge.
- Schutz: Legacy-Datei `prototype-editor-v1.json` bleibt unverändert; unbekannte Struktur-/Formatinformationen dürfen beim Schreiben nicht stillschweigend verworfen werden; bei Speicherfehlern oder Konflikten keine Erfolgsmeldung.
- Als nächstes: Android-Entry aus der Bücherliste, Buch-Canvas-Adapter und neue Node-/Android-Tests. `DONE` erst nach vollständiger grüner GitHub-CI und geprüfter APK.


### Aktuelle Übergabe – B-03 DONE / W-01 als nächstes (14.11.2026)

- **B-03 DONE:** [Code mit Navigation/Positionsanker f115440](https://github.com/philip20794/Smut/commit/f115440f90373253c30a39aa96bbf109cfac8ee0), [Regressionstests/Fix 5e89c93](https://github.com/philip20794/Smut/commit/5e89c932846a096b5b238008fc477ccb933e46f2), [vollständige CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487) grün und getestete Debug-APK als Artifact vorhanden.
- Das gespeicherte Kapitel und die Cursorumgebung werden in der nativen Lesevorschau beim Wiederöffnen angezeigt; nach Layout wird die gespeicherte Scrollposition auf die Leseseite angewendet. System-Zurück und die Schaltfläche führen zur Bücherliste; Buchmenü bleibt kompakt.
- Veraltete, aber prüfsummengültige Textanker werden in einen vorhandenen Kapitel-/Textanker normalisiert, ohne unbeteiligte Buchinhalte zu überschreiben. Fehlende Blocks/Chapters setzen den Anker auf einen gültigen Kapitelanfang zurück. Struktur-/Hash-Fehler bleiben Fehler; eine defekte aktuelle Revision fällt höchstens auf die letzte gültige Vorversion zurück.
- **Tests:** Android-Emulator prüft gespeicherten Unicode-Offset und Scrollanker, Zurück/erneutes Öffnen, Activity-Neustart, Buchmenü, ungültige Anker bis hin zum Emoji-Surrogatpaar sowie kaputte Prüfsumme. Bestehende Tests (P0–P3, Offline-Canvas und A5-PDF-Testprobe) vollständig grün.
- **Nächste Aufgabe W-01:** direktes Schreiben im strukturierten Buchformat und sichere Editor-/Autosave-Integration. Die native Leseansicht speichert absichtlich keine neuen Editor-Cursor; `prototype-editor-v1.json` bleibt bisher getrennt. **E-05** echter Smartphone-Test weiterhin `BLOCKED`.

### Aktuelle Übergabe – B-02 DONE / B-03 als nächstes (14.11.2026)

- **B-02 DONE:** [BookStore-Kapitelbefehle 2ee4302](https://github.com/philip20794/Smut/commit/2ee4302b984f46e32c6a2ce5737650344ddda33c), [Compose-UI 95b8f04](https://github.com/philip20794/Smut/commit/95b8f04d5e89667cb5b2ac238ec9f566d47de9bb), [Test-/Fix-Commit da2678b](https://github.com/philip20794/Smut/commit/da2678bbf93aa43196e5a5bc350f258cec17616c); [CI 43597289709](https://github.com/philip20794/Smut/actions/runs/43597289709) grün, geprüfte Debug-APK als Artifact vorhanden.
- Kapitel erzeugen, umbenennen, nach oben/unten bewegen, mit Bestätigung löschen und gezielt auswählen. Kapitel-ID und Texte bleiben beim Umordnen unverändert. Das aktive Kapitel wird samt Position in `BookStore` atomar gespeichert; das Löschen eines aktiven Kapitels setzt einen gültigen Anker auf ein verbleibendes Kapitel. Das letzte Kapitel ist geschützt.
- Optimistische Revisionsprüfung verhindert stilles Überschreiben konkurrierender Änderungen; UI lädt nach jeder Operation den aktuellen Stand erneut. Die Leseansicht zeigt die Kapitelüberschriften, Textblockzahl und eine begrenzte Textvorschau; der A5-Prototyp bleibt getrennt. Offenes Buch wird nach Android-Activity-Recreation wiederhergestellt.
- **Tests:** Backend mit leerem/mehrseitig langem Unicode-Kapitel, Speicher-/DB-Neustart, ID-/Reorder-Stabilität, Cursorposition, konkurrierender Bearbeitung, Löschschutz; Android-UI mit echten Dialogen, Kapitelwechsel und Löschabbruch. Erste [CI 43596596570](https://github.com/philip20794/Smut/actions/runs/43596596570) rot wegen einer absichtlich ungültig gewählten Test-Cursorposition mitten im Emoji und eines UI-Test-Timings; korrigiert in da2678b, danach komplette CI grün. Bestehende Tests bleiben unverändert.
- **Nächster Schritt B-03:** bei Schließen/Öffnen die letzte Kapitel-/Cursor-/Scrollposition wiederherstellen, kompakte Buchnavigation und angemessene Rückwärtsnavigation. `E-05` echter Smartphone-Test bleibt `BLOCKED`.

### Aktuelle Übergabe – B-01 DONE / B-02 als nächstes (14.11.2026)

- **B-01 DONE:** [Code-Commit 8bf2f71](https://github.com/philip20794/Smut/commit/8bf2f711c93409405b9eb58c6a458149dd017141), [Compose-Launcher 2ef40ea](https://github.com/philip20794/Smut/commit/2ef40eadb63a8a5a3ca1292f10e52b16ee0f5eeb), [Test-Commit fcf7642](https://github.com/philip20794/Smut/commit/fcf7642272d0ec716b57bd83dce67c2916377359); [CI 43594430969](https://github.com/philip20794/Smut/actions/runs/43594430969) vollständig grün und geprüfte Test-APK erzeugt.
- Neues `LibraryActivity` als Launcher mit Bücherliste, Titelvalidierung, Anlegen, Umbenennen, Duplizieren und bestätigtem Löschen; `BookStore` verwaltet ID-unabhängige Kopien, Buch-/Kapitel-/Block-IDs und gültige Cursoranker. Öffnen zeigt derzeit die native Kapitel-Leseansicht (noch nicht den editierbaren A5-Canvas).
- Persistenter Löschmarker vor Datei-/Indexbereinigung verhindert die Wiederkehr gelöschter Bücher nach Absturz. Android-Emulator prüft CRUD, Löschabbruch, App-/DB-Neustart, Deep Copy, Reparatur des Löschmarkers, Altformat-Abgrenzung, bestehende Backup-/PDF-Fälle.
- Der separat zugängliche `MainActivity`-Editor benutzt weiterhin `prototype-editor-v1.json`; bestehende Benutzerprototypen werden nicht in strukturierten Bücher-Backups gespeichert. Die editorseitige Migration erfolgt erst in W-01.
- **Nächster Schritt B-02:** Kapitelliste und Kapitel-CRUD mit Reorder und Navigation; B-03 danach. E-05 (echtes Smartphone) weiterhin `BLOCKED`.

### Aktuelle Übergabe – D-04 DONE / B-01 als nächstes (14.11.2026)

- **D-04 DONE:** [Commit 1386ce4](https://github.com/philip20794/Smut/commit/1386ce4a9fa676bad8b533d28ad6d1d5c48d9811), [vollständige CI 43592192732](https://github.com/philip20794/Smut/actions/runs/43592192732) erfolgreich. Erstes CI-Ergebnis schlug nur an einer Klammer der neuen Android-Testfixture fehl; nach Korrektur kein fehlgeschlagener Pflichtcheck mehr.
- Portables `.smut.json` für strukturierte Bücher, Formatversion 1 und SHA-256, definierte Speicher-/Importgrenzen. Unbekannte Versionen und defekte Archive werden abgewiesen.
- Neue native BackupActivity mit Android-Dateiauswahl (CreateDocument/OpenDocument), Buchliste, exportierendem Hintergrundprozess und kollisionssicherem Import in BookStore. Zugriff aus der lokalen WebView-Schaltfläche „Buch-Backup“.
- Tests: Kotlin-Codec-Tamper-/Rundreise, Emulator-Speichern–Export–Testkopie löschen–Import, MediaStore-ContentResolver, Schutz vor Überschreiben und Legacy-Unversehrtheit. **Vollständige CI 43592192732 grün; geprüfte Debug-APK veröffentlicht.**
- Die aktuelle E-02-Buchseite ist weiterhin Legacy-Canvas und noch nicht im D-01-Datenmodell; direkte Prototypmigration folgt nach sicherem Integrationsgate in W-01. Keine E-02-Datei wird verändert.
- **Nächste Aufgabe B-01:** echte Bücherliste inklusive Buchverwaltung auf Basis von D-03; E-05 bleibt BLOCKED bis zum tatsächlichen Smartphone-Test.



### Aktuelle Übergabe – D-03 DONE / D-04 als nächstes (14.11.2026)

- **D-03 DONE:** [Code bfd2c82](https://github.com/philip20794/Smut/commit/bfd2c82e085844930bd5490924186c5f287bc8c0), [CI 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623) vollständig grün. Room-Index ist nach Dateischreibvorgängen rekonstruierbar; Buchdaten werden atomar versioniert mit SHA-256 und letzter gültiger Rückfallkopie gespeichert.
- **Prüfung:** TypeScript-Autosave-Tests, Kotlin-Bridge-Unit-Tests, Android-Emulatorfälle für Neustart, Dateikorruption, unterbrochenen Write, Revisionskonflikte, Indexreparatur und Legacy-Datenschutz. Die komplette bisherige WebView-/PDF-Regression bleibt grün.
- **APK:** `smut-tested-dev-bfd2c82e085844930bd5490924186c5f287bc8c0` in [CI 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623). D-03 ist derzeit eine getrennte Modell-/Speicherschicht. Die sichtbare Android-Oberfläche verwendet weiterhin den Canvas-Prototyp; die produktive Anbindung kommt mit W-01.
- **Nächster Schritt:** D-04 – versioniertes Buch-Backup mit Android-Dateiauswahl, Importintegrität und Rundreisetests. Die E-02-Prototypdatei bleibt bis zu einer geprüften Migration unverändert. E-05 weiterhin `BLOCKED` bis zur echten Geräteabnahme.


### D-03 – Implementierung in CI-Prüfung (14.11.2026)

- Neue getrennte native Datenschicht: Room-Index, atomare versionierte JSON-Buchdateien mit SHA-256, validierte Rückfallrevision, Cursor-/Kapitelposition und Revisionskonfliktprüfung. Der bisherige E-02-Prototyp bleibt unberührt.
- Erweiterter separater Bridge-Vertrag für strukturierte Bücher und TypeScript-Autosave-Warteschlange; Android-Emulator-Tests prüfen Unterbrechung, Dateibeschädigung, Neustart und Legacy-Unversehrtheit. **Noch nicht als bestanden bestätigt.**
- **Kein D-03-DONE ohne vollständig grünen CI-Lauf.** Die vorhandene Canvas-Oberfläche wird erst in W-01 auf das neue Buchformat umgestellt. E-05 bleibt echte Smartphone-Abnahme.

### Vorherige Übergabe – D-02 DONE / D-03 als nächstes (14.11.2026)

- **D-02 DONE:** [Commit a7df11d](https://github.com/philip20794/Smut/commit/a7df11d282226e82cbda4336086ff9a7ee383aef). Neues `editor/src/edit-operations.ts` für alle sechs geforderten Operationen, graphemsichere UTF-16-Textanker, Trennungs-/Kapitelbarrieren, Auswahlverschiebung sowie begrenztes Undo/Redo.
- **Prüfung:** [Smut CI 43585173130](https://github.com/philip20794/Smut/actions/runs/43585173130) vollständig `success`: neue Node-Unit-Tests und alle bestehenden TypeScript-/Android-/Offline-Emulator-/PDF-Gates grün.
- **Test-APK:** `smut-tested-dev-a7df11d282226e82cbda4336086ff9a7ee383aef` als befristetes CI-Artefakt; reales Smartphone bleibt ausstehend (E-05).
- **Abgrenzung:** Die bisherige Canvas-Oberfläche nutzt noch das E-02-Prototypformat. D-02 ist eine eigenständige, geprüfte Modellebene; dauerhafte lokale Speicherung folgt D-03, Benutzerintegration W-01.
- **Nächste Aufgabe:** D-03 – Room-Metadaten, atomare versionierte Buchdateien, Autosave, Crash-/Fehler-Recovery; bestehende Prototypdaten vor Migration unverändert schützen.

### Historische Übergabe – D-01 DONE / D-02 zuvor als nächstes

- **D-01 DONE:** [Commit 512b0c5](https://github.com/philip20794/Smut/commit/512b0c5d2a422d7ec85ea0890c2fced6a8f66e38) implementiert reine Module `editor/src/book-model.ts` und `editor/src/legacy-migration.ts` und sechs zusätzliche Unit-Tests.
- **Nachweis:** [Smut CI 43583247353](https://github.com/philip20794/Smut/actions/runs/43583247353) vollständig `success`: Editor-Typecheck, Lint, Node-Tests, Kotlin-/Android-Build und Offline-Emulator einschließlich A5-PDF-Test bestanden. Geprüfte APK `smut-tested-dev-512b0c5d2a422d7ec85ea0890c2fced6a8f66e38` liegt als befristetes Artifact vor.
- **Abgrenzung:** Der Migrationseinstieg überschreibt keine Android-Prototypdaten und meldet unbekannte Canvas-Felder. D-03/D-04 müssen den Quellsnapshot bei der späteren produktiven Übernahme sichern. E-05 steht als echter Smartphone-Test weiterhin aus.
- **Nächstes Arbeitspaket:** D-02 – Editoroperationen, Cursoranker und Undo/Redo auf dem versionierten Modell. Kein Bedarf an lokaler Hardware.

### Historische Übergabe – E-04 DONE / D-01 zuvor als nächstes (14.11.2026)

- **E-04 DONE:** [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300), 13 Emulatorfälle grün. PDF textbasiert/selektierbar, drei A5-Seiten sowohl im Editor als auch im Export. Python-Parser weist A5 MediaBox (419,25 × 595,50 pt), Suchbegriffe Äpfel/Öl/Grüße/Straße und keine Rasterseiten nach. Paginierung gleich; noch kein vollständiger Zeilen-/Pixel-Goldenvergleich.
- **Sichtprüfung:** PDF renderbar, mit Seitenzahlen; aber ungünstige Blocksatzabstände und Wortumbrüche innerhalb deutscher Wörter ohne Trennstrich. Fonts derzeit Standard-PDF-Type1, noch keine lizenzierten eingebetteten Buchschriften. Diese Defizite sind ausdrücklich für P5 und E-05 dokumentiert.
- **ADR:** [ADR-009](DECISIONS.md) erteilt ein eingeschränktes **P1-GO für die technische Kombination**, nicht für druckreife PDF-Ausgabe.
- **Jetzt D-01:** editorunabhängiges Dokumentmodell und Migrationseintritt; technische Arbeit an P2 freigegeben. E-05 separat `BLOCKED` bis zum Smartphone-Test; nicht als erledigt verbuchen.
- **Geprüfte Artefakte:** [GitHub Actions 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300) (testierte Entwicklungs-APK und Beispiel-PDF).


### Historischer E-04-Zwischenstand (14.11.2026, durch grünen Run 43578985300 überholt)

- **Aktiv:** E-04 `IN_PROGRESS`, weiterhin 9/35 `DONE`.
- Implementiert wird eine eigenständige Android-WebView-Probe mit `canvas-editor-pdf@0.8.0` und `loadDefaultFonts:false`: kein CDN, kein Server und keine beliebigen Benutzerdateien. Der Export greift auf den unveränderten `getValue()`-Snapshot zurück und rendert bei 100% Papiermaß statt 60% Bildschirmzoom.
- Android-Bridge speichert nur die streng begrenzte PDF-Testdatei `files/e04-a5-probe.pdf`; keine allgemeine Dateifreigabe oder v1-PDF-UI. Emulatorprüfung misst MediaBox; pypdf liest anschließend den tatsächlichen PDF-Text und prüft Umlaute und A5 sowie fehlende Rasterbilder. Das Sample liegt nach komplett grünem CI als Actions-Artefakt.
- **Noch nicht freigegeben:** Vorliegende CI prüfen, reale Seitengleichheit zur Canvas-Vorschau dokumentieren und Bibliotheksentscheidung begründen. Keine E-04-`DONE`-Markierung ohne Nachweis; E-05 bleibt echter Gerätetest.

### Aktuelle Übergabe – 14.11.2026

- **E-03 DONE (9/35):** Neue mobile Auswahlgriffe inklusive Positionsberechnung, Drag-Anker, Scrolling-/Fokusbereinigung und Unit-Tests; Android-Instrumentierung prüft einen realen Finger-Drag am Endgriff und unveränderte Buchdaten.
- **Nachweis:** [vollständiger grüner Run 43574312605](https://github.com/philip20794/Smut/actions/runs/43574312605), geprüfte APK `smut-tested-dev-1ef67535d34217d74a1e3ff060ed2ecfa5e447cf`. Der erste neue Commit scheiterte nur an Nullable-TypeScript-Prüfungen, die [1ef6753](https://github.com/philip20794/Smut/commit/1ef67535d34217d74a1e3ff060ed2ecfa5e447cf) korrigierte; danach alle Pflichtchecks grün.
- **Nächster Schritt E-04:** textbasierten A5-PDF-Prototyp erstellen und automatisiert prüfen, Bibliotheksentscheidung dokumentieren.
- **Nutzeraktion:** E-05 auf echtem Smartphone (später); B-01 kann ohne Nutzerhardware umgesetzt werden.

### Historische Zwischenstände (ältere TODO-Angaben unten sind durch E-03-DONE überholt)


- **Datum:** 2026-11-13
- **Aktive Task-ID:** E-03 `IN_PROGRESS` – native Seitengrenzen-Auswahl und IME-Cursor-Autoscroll im Emulator grün; Rand-Autoscroll-Auswahlgeste, mobile Auswahlgriffe und Wechselwirkungen fehlen.
- **Erreicht:** F-01…F-06, E-01/E-02 `DONE` (8/35). Native Longpress-/Drag-Auswahl über A5-Seitengrenzen grün. Neu: editor/src/caret-visibility.ts mit deterministischer Sichtbarkeitsberechnung und Unit-Tests; in `main.ts` wird die tatsächliche Canvas-Cursor-Position nach Eingabe, Cursor-Neupositionierung und Verkleinerung des Viewports durch Android-IME mit `requestAnimationFrame` sichtbar gehalten. Android-Test nutzt die isolierte 75-Absatz-Fixture auf Seite 2, echten Touch, öffnet IME und tippt Unicode. Vollständiger grüner Lauf 43497267481 (Versuch 2).
- **Letzter grüner Code-Commit:** [fe4f90d](https://github.com/philip20794/Smut/commit/fe4f90d19b43901850331aeaad22aa19917c82cf), [Smut CI 43497267481, Versuch 2](https://github.com/philip20794/Smut/actions/runs/43497267481) `success`, mit zehn Emulator-Tests und geprüfter Dev-APK.
- **Letzte geprüfte APK:** `smut-tested-dev-f4920fc790742f98a36a37d49d1d4d739d86bbc7` ([GitHub Actions 43493716491](https://github.com/philip20794/Smut/actions/runs/43493716491), ZIP mit SHA256SUMS, 30-Tage-Artifact). **Nicht auf realem Smartphone getestet.**
- **Nachweise:** [CI 43497267481](https://github.com/philip20794/Smut/actions/runs/43497267481) Versuch 2 prüft native Android-IME/Cursor-Sichtbarkeit auf später A5-Seite und die bestehenden Gesten-/IME-Regressionen. Versuch 1 scheiterte am Emulator/ADB-Fensterfokus, nicht an einem isolierbaren Test der Cursorfunktion; Wiederholung grün. [CI 43493716491](https://github.com/philip20794/Smut/actions/runs/43493716491) weist die Zwei-Seiten-Drag-Auswahl nach.
- **Neuer Code-Zwischenstand (CI noch nicht bestätigt):** [bc01c21](https://github.com/philip20794/Smut/commit/bc01c21ded2f0ada1f4ce002e9e082e6fa8fe47f) ergänzt `selectionEdgeScrollDelta` mit Richtungs-, Inset- und Geschwindigkeits-Unit-Tests und integriert die begrenzte Rand-Autoscroll-Geschwindigkeit unter Berücksichtigung von `visualViewport`. Noch kein Emulator-Gestennachweis und kein grüner Pflichtcheck für diesen Commit ermittelt; daher E-03 weiterhin `IN_PROGRESS`.
- **Instrumentierter Rand-Autoscroll-Test (CI 43530940612 grün):** [e13df60](https://github.com/philip20794/Smut/commit/e13df60413de1fcac485da31f9bbd15f19c07a93) ergänzt `nativeSelectionDragNearBottomEdgeScrollsAndExtendsRange`: echte Android-Longpress-Drag-Geste mit 180 Schritten bis zur unteren WebView-Kante, Kontrolle von Scrollfortschritt und Auswahlbereich sowie Wiederherstellung des isolierten Testmanuskripts. Durch den nachfolgenden erfolgreichen Workflow 43530940612 abgenommen.\n- **Zoom-/IME-Regressionsschritt (CI 43530940612 grün):** [2d3e921](https://github.com/philip20794/Smut/commit/2d3e921cedb64b389ba32fdefa5642f49ccc0689) erweitert den bestehenden echten Android-IME-Test auf Seite 2: Bei sichtbar geöffneter Tastatur Zoom 60→70 %, Cursor innerhalb des `visualViewport`, Editor-Fokus und IME-Sichtbarkeit prüfen, weiteres Unicode-Schreiben über `InputConnection`, danach Zoom zurück auf 60 % und erneute Cursor-Sichtbarkeit. Test-Fixture bleibt isoliert und wird im `finally` zurückgesetzt. Der Zoom wird bewusst über die bestehende Schaltfläche per DOM-Click ausgelöst, um den Tastaturfokus nicht durch einen nativen Toolbar-Tap zu verlieren. Im nachfolgenden Workflow 43530940612 erfolgreich abgenommen.
- **Zusätzlicher Robustheitsfix (CI 43530940612 grün):** [12a4d87](https://github.com/philip20794/Smut/commit/12a4d87d03149254058b5a0fd764be551c0dd0e8) beendet eine laufende native Auswahlgeste und deren requestAnimationFrame-Rand-Autoscroll bei WebView-Fokusverlust, pagehide oder unsichtbarem Dokument. Android garantiert bei Home/Recents/Appwechsel kein touchcancel. Der Fix verhindert ein weiterlaufendes Scrollen nach Fokusverlust; im nachfolgenden CI-Lauf 43530940612 grün.
- **Neuer Teilschritt (CI offen):** [8174787](https://github.com/philip20794/Smut/commit/8174787edb226663a0d29329b4f730c7d453862e) ergänzt eine rein lesende Diagnose des aktiven Auswahl-Scroll-Zustands; [217bfaf](https://github.com/philip20794/Smut/commit/217bfafcaa723717f9f39eceaa85b8a98e199fd5) erweitert den echten Android-Rand-Autoscroll-Test um eine Prüfung nach Fingerfreigabe: RAF-Schleife muss beendet sein, Scrollposition stabil bleiben. Noch kein grüner CI-Nachweis für diese Ergänzung.\n- **CI-Reconciliation 14.11.2026:** Der abgefragte Workflow [43538131635](https://github.com/philip20794/Smut/actions/runs/43538131635) hat für Editor, Android, Emulator und APK `cancelled`, daher kein neuer Erfolgsbeleg. Der Code-Fix [1c604ab](https://github.com/philip20794/Smut/commit/1c604abbe7e93aa1d2bc88e4aa14c8b31fba237d) beendet noch ausstehende Cursor-RAF-Callbacks bei `blur`, `pagehide` und verstecktem Dokument; es wird weder ein manueller Test noch ein grüner CI-Status behauptet.\n- **Was fehlt:** vollständige CI für den neuen Gestentest und ggf. Fixes, mobile Auswahlgriffe und kombinierte Zoom-/IME-Interaktionen; danach E-04 A5-PDF-/Technologie-GO, E-05 echter Gerätetest sowie P2–P7.
- **Nächster kleinster Schritt:** GitHub Actions für `1c604ab` und diesen Dokumentationscommit prüfen; bei roten Tests Logs auswerten und beheben. Danach mobile Auswahlgriffe implementieren und instrumentiert testen. E-03 erst nach allen Pflichtnachweisen als DONE markieren.
- **Nutzeraktion nötig?:** jetzt nein, E-05 später durch manuelle Smartphone-Prüfung.

- **CI-Diagnose 14.11.2026:** [Run 43544310164](https://github.com/philip20794/Smut/actions/runs/43544310164) hat TypeScript und Android-Build grün, aber 10 von 11 Emulatorfällen scheitern bereits am gemeinsamen Fensterfokus-Setup (`Smut must own emulator window; wm=` leer). Dies ist kein isolierter Nachweis eines E-03-Produktfehlers. Testberichte: [Artifact 14095233322](https://github.com/philip20794/Smut/actions/runs/43544310164/artifacts/14095233322). Keine geprüfte APK aus diesem Lauf. [Commit b883bb6](https://github.com/philip20794/Smut/commit/b883bb6ac9593ed1f1f536c33df0595e93e69818) beendet unnötige Auswahl-Autoscroll-RAF-Wiederholungen, wenn der Finger außerhalb der Scroll-Randzone bleibt; dieser neue Code ist noch nicht CI-bestätigt. **Nächster Schritt:** neuen CI-Lauf prüfen; bei erneutem systemischem Fokusfehler Emulator-/Window-Setup diagnostizieren, ohne Tests zu lockern. Danach mobile Auswahlgriffe.


- **CI-Fokus-Recovery 14.11.2026:** [Commit 61bd9c7](https://github.com/philip20794/Smut/commit/61bd9c745a86298de24cb69e473db30914283e49) ergänzt im instrumentierten Android-Test nach zwei erfolglosen Fokusversuchen eine einmalige Home-Rückkehr und anschließenden Neustart der bestehenden Activity (ohne Test-Gate zu lockern). Bei weiterem Fehlschlag wird zusätzlich der Activity-Resumed-Zustand protokolliert. **CI-Nachweis für diesen Commit noch offen**; E-03 bleibt `IN_PROGRESS` (8/35). Nächster Schritt: CI prüfen, gegebenenfalls Fokusproblem weiter eingrenzen, anschließend Auswahlgriffe.


- **Rand-Autoscroll am Dokumentende (14.11.2026):** [Commit 9925897](https://github.com/philip20794/Smut/commit/99258977c39125d2e849ac8dc834c0a293cbbd18) stoppt die requestAnimationFrame-Schleife zusätzlich dann, wenn `window.scrollBy` wegen oberer/unterer Dokumentgrenze keine Scrollposition mehr verändert. Ein späteres `touchmove` kann sie erneut starten. **CI für diesen Commit noch nicht bestätigt.** E-03 bleibt `IN_PROGRESS` (8/35); als Nächstes CI einschließlich Emulator-Fokus-Recovery prüfen und anschließend mobile Auswahlgriffe implementieren.

- **Touch-Ende außerhalb des Editors (14.11.2026):** [Commit af9c9ff](https://github.com/philip20794/Smut/commit/af9c9ffdcf8f2d662399f49c20d64f65a14bc96c) verlagert die Beendigung der nativen Auswahlgeste auf `window`-weite `touchend`-/`touchcancel`-Listener. Damit wird die Auswahl-RAF-Schleife auch beim Loslassen über der Toolbar oder außerhalb des Editor-Containers beendet; bei `touchend` nur wenn keine Finger mehr aufliegen. **CI noch nicht bestätigt.** E-03 bleibt `IN_PROGRESS` (8/35). Als Nächstes Emulator-Gestentest für das Loslassen außerhalb des Editors ergänzen und CI prüfen; danach Auswahlgriffe.

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

- **E-03 Android-Auswahlgriffe (14.11.2026):** Zwei sichtbare, 44px-große Touchziele für Start und Ende nativer Canvas-Editor-Textauswahl implementiert. Positionierung aus `getRangeContext().rangeRects` und echten Canvas-Rechtecken, mit Seiten-/Zoom-Offset-Korrektur und Ausblendung außerhalb des Viewports. Griff-Drag nutzt den bestehenden Mouse-Hit-Test der Editor-Engine mit festem Gegenanker, Touch-Rand-Autoscroll sowie Touchend-/Fokus-Cleanup. Reine Unit-Tests für A5-/Mehrseiten-Koordinaten und instrumentierter Android-Fingerzug am Auswahlende ergänzt. **CI für diesen neuen Commit erst nach dem Push verifizieren; E-03 bleibt IN_PROGRESS bis zum grünen Emulatortest.** Letzter verifiziert grüner Stand vor dieser Änderung: [Run 43571125484](https://github.com/philip20794/Smut/actions/runs/43571125484) mit getesteter APK. Danach auf einem Gerät testen, E-03 vollständig bewerten und E-04 angehen.

- **E-04 CI-Diagnose 14.11.2026:** [Run 43577438827](https://github.com/philip20794/Smut/actions/runs/43577438827) bestand alle 13 Android-Instrumentierungstests einschließlich `PdfRenderer` der mehrseitigen A5-Probe. Die nachgeschaltete Python-Parserstufe wurde rot, weil `adb exec-out run-as` keinen PDF-Stream, sondern eine Shell-Fehlermeldung lieferte (`invalid pdf header: b'run-a'`). Korrektur: Test kopiert die Datei innerhalb der App über MediaStore nach Downloads; GitHub Actions holt die PDF über `adb pull` ab und prüft den Kopf vor dem Parser. **Text-/Unicode-Erfolg noch nicht nachgewiesen; E-04 bleibt IN_PROGRESS.**
