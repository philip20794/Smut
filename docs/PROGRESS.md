# Fortschritt und Übergabe – Smut

> **Dies ist die einzige operative Statusquelle.** Der stabile Arbeitskatalog steht in [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md). Nach jedem umgesetzten und getesteten Teilschritt aktualisieren. Nicht auf Chatverlauf verlassen.

## 1. Aktueller Stand

- **Stand:** 2026-11-14.
- **Projektstatus:** `IN_PROGRESS` – P0 und P1 E-01 bis E-04 abgeschlossen; **P1-GO für Canvas Editor 1.1.0 + lokal gebündelten PDF-Exporter 0.8.0** dokumentiert. Die vollständige Buchverwaltung, professionelle Typografie und der Endnutzer-PDF-Export gehören später zu P2–P5.
- **Meilenstein:** `P2 – Dokumentmodell und Datensicherheit` (technische Arbeit freigegeben).
- **Letzte abgeschlossene Aufgabe:** `E-04 DONE`: echte mehrseitige A5-PDF aus der Offline-WebView, suchbare Umlaute, korrektes PDF-Seitenmaß, kein Bild-PDF und gleiche Seitenanzahl in Editor und PDF. Siehe [E04_PDF_PROBE.md](E04_PDF_PROBE.md) und ADR-009 in [DECISIONS.md](DECISIONS.md).
- **Aktive Aufgabe:** `D-01 IN_PROGRESS` – Buch-/Kapitel-/Blockmodell und Legacy-Übernahme sind implementiert; GitHub-CI-Nachweis steht noch aus. Der echte Gerätetest `E-05` bleibt `BLOCKED` und kann unabhängig erfolgen.
- **Abgeschlossene Aufgaben:** **10 / 35 (28,6 %)**; nur `DONE` zählt.
- **Letzter verifizierter grüner Code-CI-Lauf:** [Smut CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300), Commit [dc47fd4](https://github.com/philip20794/Smut/commit/dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5): TypeScript, Node-Tests, Kotlin/Lint/Build, 13 API-35-Emulatortests einschließlich übereinstimmender PDF-/Editor-Seitenanzahl, pypdf-Seitenmaß-/Unicode-/Textprüfung und geprüfte APK.
- **Letzte geprüfte APK:** `smut-tested-dev-dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5` aus [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300); PDF-Testartefakt `smut-e04-a5-pdf-probe-dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5`. Beide zeitlich befristet. **Kein echter Smartphone-Test.**
- **Letztes Release:** keines; signierte Releases stehen aus.
- **Technische Entscheidung:** **GO** für die offlinefähige Prototyp-Architektur; **kein druckfertiges Benutzer-PDF**: problematische Worttrennungen/Blocksatz, nur PDF-Standardfonts ohne eingebettete lokale Buchschrift, fehlender Zeilen-Goldenvergleich und noch keine anwenderseitige Export-UI. Das sind Pflichtpunkte für P5 L-01 bis L-04, ggf. Engine-Fallback bei Nichterfüllung.
- **Nutzeraktion:** E-05 auf realem Smartphone (später); keine Aktion zum Start von D-01 notwendig.

## 2. Verbindlicher nächster Schritt

**D-01 – Versioniertes Buch-/Kapitel-/Blockmodell** nach dokumentiertem [P1-GO](DECISIONS.md).

1. Dokumentstruktur, Kapitelreihenfolge, semantische Absätze/Überschriften/Szenentrenner und Layoutparameter als versioniertes, editorunabhängiges Modell definieren.
2. Migration vom jetzigen Canvas-Prototyp, Serialisierung/Validierung und fehlerhafte Daten testbar machen.
3. Unit-Tests und sämtliche Pflichtchecks grün nachweisen; erst dann `D-01 DONE` markieren.
4. `E-05` bleibt separat `BLOCKED` bis zur tatsächlichen Geräteprüfung; keine simulierte Nutzerabnahme. `P5` darf die hier dokumentierten Typografie-/Font-/PDF-Grenzen nicht ignorieren.

**P1-Abnahme:** [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300) mit überprüfbarer [E-04-PDF-Probe](https://github.com/philip20794/Smut/actions/runs/43578985300), [technischem Testbericht](E04_PDF_PROBE.md) und ADR-009. Der PDF-Exporter ist ein Machbarkeitsprototyp; noch keine v1-Exportfunktion für Benutzer.

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
| P2 | D-01 | IN_PROGRESS | [D-01-Schema und Migration](D01_DOCUMENT_MODEL.md), TypeScript- und Node-Tests ergänzt; CI ausstehend |
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
| P1 | 4/5 | E-01…E-04 GO, echter Gerätetest E-05 separat | **GO für D-01; E-05 offen** |
| P2 | 0/4 | Modell, Operationen, sichere Persistenz, Backup | offen |
| P3 | 0/3 | Bücher-/Kapitelverwaltung | offen |
| P4 | 0/5 | Direktbearbeitung und Textfunktionen | offen |
| P5 | 0/4 | A5-Satz und textbasierter PDF-Export | offen |
| P6 | 0/4 | Offline, Performance, Zuverlässigkeit, Gerätetest | offen |
| P7 | 0/4 | Signierung, Releases, Update, v1-Abnahme | offen |

**Gate-Abhängigkeiten:** siehe [Implementierungsplan](IMPLEMENTATION_PLAN.md). Für P1 kann `E-05` auf eine Geräteabnahme warten, während unabhängige Arbeit fortgesetzt wird. Der Status bleibt `BLOCKED` und zählt nicht als abgeschlossen; spätestens vor P7 muss ein echter Gerätetest vorliegen.

## 5. Letzte Arbeitssitzung / Handoff

### D-01 in Prüfung – 14.11.2026

- Neue reine TypeScript-Module `editor/src/book-model.ts` und `editor/src/legacy-migration.ts`: versionierte Buchdaten, A5-Layoutparameter, Textmarkierungen, Strict-Validator, Serialisierungs-Rundreise und nicht schreibender Canvas-Migrationsadapter.
- `editor/tests/book-model.test.mjs`: neue Tests für Reihenfolge, Unicode, Blockarten, fehlerhafte Eingaben und verlustbewusste Legacy-Übernahme. Lokal mit TypeScript 5.8.3 und Node 22 bestanden; verbindlicher Node-24-CI-Lauf steht aus.
- Bestehendes E-02-Laufzeitverhalten und Android-Brücke bleiben unverändert. Keine automatische Migration produktiver Nutzerdaten.
- Bei grüner vollständiger CI D-01 auf `DONE` setzen, `10 / 35` auf `11 / 35` aktualisieren und D-02 als nächsten Schritt eintragen. Bei Rot Fehler korrigieren, Status nicht vorzeitig erhöhen.



### Aktuelle Übergabe – E-04 DONE / D-01 als nächstes (14.11.2026)

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
- **Nutzeraktion:** jetzt keine; reales Smartphone bleibt E-05/Q-04.

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
