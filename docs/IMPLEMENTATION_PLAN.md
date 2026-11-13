# Implementierungsplan: Smut

Stand: 2026-11-13 · Planversion 1 · **Planung als stabiler Aufgabenkatalog; Implementierung läuft gemäß PROGRESS.md**  
Verbindliche Quellen: [README](../README.md), [Produkt](PRODUCT.md), [Architektur](ARCHITECTURE.md), [Abnahme](ACCEPTANCE.md), [CI/CD](CI-CD.md).  
**Operativer Fortschritt:** [PROGRESS.md](PROGRESS.md). **Wiederaufnahmeprotokoll:** [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md).

## 1. Ziel und Grenzen

Smut ist eine offline nutzbare Android-App zum Schreiben direkt in einer paginierten, drucknahen A5-Buchansicht. Tippen, Markieren und Formatieren finden auf den sichtbaren Buchseiten statt. Das editierbare Dokument ist ein versioniertes lokales Datenmodell; die PDF wird daraus exportiert und ist kein Eingabeformat.

Die Entwicklung läuft ohne lokalen Computer des Nutzers: Codex arbeitet über GitHub; GitHub Actions prüft Änderungen und liefert Test-APKs. Codemagic ist nicht erforderlich. **PDF-Import als frei editierbares Dokument, Cloud-Sync und verlustfreie Konvertierung beliebiger LaTeX-Dateien gehören nicht zur v1.**

## 2. Planungskonventionen

- Jede Aufgabe besitzt eine stabile ID. IDs niemals umnummerieren, bereits erledigte Aufgaben nicht stillschweigend löschen.
- Status, nächste Schritte, Blocker, CI-Belege und Übergabevermerk stehen **nur** in [PROGRESS.md](PROGRESS.md); dieser Plan beschreibt Reihenfolge und Abnahmebedingungen.
- Statuswerte: `TODO`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`. Bei Neustart gilt `IN_PROGRESS` **nicht** als erledigt.
- Eine Aufgabe gilt erst als `DONE`, wenn ihr Akzeptanznachweis dokumentiert und die zugehörigen Pflichtchecks für den Code-Commit grün sind. Für reine Dokumentationsaufgaben genügt ein verifizierter Commit.
- `MANUAL` markiert nötige Abnahmen auf dem echten Android-Gerät. Sie dürfen nie automatisch als bestanden fingiert werden.
- Große Aufgaben in kleinen, lauffähigen Commits abarbeiten; nach jedem überprüfbaren Teilschritt den Fortschritt sichern.
- Bestehende Vorgaben sind wichtiger als Toolpräferenzen. Bibliotheksauswahl bleibt bis zum erfolgreichen Machbarkeitstest vorläufig.
- Planänderungen als eigenen Commit mit Grund und ggf. neuen Aufgaben-IDs erfassen. Ändern sich Akzeptanzkriterien, passende Tests und Referenzdokumente ebenfalls anpassen.

## 3. Meilensteine und Reihenfolge

| Phase | Schwerpunkt | Ausgang | Abschluss-Gate |
| --- | --- | --- | --- |
| P0 | Reproduzierbares Android-Projekt + GitHub Actions | Dokumentations-Repo | `main` kann eine prüfbare Test-APK bauen; Emulator-Smoke-Test und Reports |
| P1 | Technologie-Prototyp und verbindliche Auswahl | P0 | Direktbearbeitung, Touch/IME, Seitenwechsel und PDF-Probe nachweislich funktionieren; Alternativen bewertet |
| P2 | Dokumentmodell und Datensicherheit | P1-Entscheidung | Strukturierte Dokumente überstehen Änderungen, Absturz und Migration |
| P3 | Bücher und Kapitel | P2 | Mehrere Bücher und Kapitel sind über Android-UI verwaltbar |
| P4 | Editor-Funktionen | P1–P3 | Alltagstaugliches Schreiben, Auswählen, Formatieren, Finden, Rückgängig |
| P5 | Buchsatz und PDF | P4 | Export ist A5, textbasiert, drucknah und regressionstestbar |
| P6 | Qualität und Geräteabnahme | P5 | Offline, robuste Datenhaltung, Leistung und Smartphone-Tests |
| P7 | Signierung und Veröffentlichung | P6 | Installierbare signierte v1-APK in GitHub Releases, Updatepfad geprüft |

Die vollständige Planung kann **vor** dem Prototyp bestehen; ihre technische Ausführung ab P2 ist durch den P1-Machbarkeitsentscheid bedingt. Die frühere Formulierung „Implementierungsplan erst nach Prototyp“ ist als „endgültige Technologieentscheidung erst nach Prototyp“ zu verstehen.

## 4. Aufgabenliste mit konkreten Nachweisen

### P0 – Projektgerüst und Cloud-Build (F-01 bis F-06)

**F-01 – Repo-Baseline und Buildkonventionen**  
Abhängigkeit: keine.  
Ergebnis: Projektstruktur für Android und lokalen Editor, `.gitignore`, Versions-/Lizenzentscheidungen, Paketkennung, minimale Build-/Testanleitung ohne PC. Keine Secrets und keine Build-Binaries einchecken.  
Prüfung: dokumentierte Befehle, konsistente Gradle-/Node-Versionen, Commit auf `main`.

**F-02 – Minimale startbare Android-App**  
Abhängigkeit: F-01.  
Ergebnis: Kotlin/Jetpack-Compose-Projekt mit Startscreen und Android-Grundkonfiguration, Gradle Wrapper einschließlich Wrapper-JAR und Versionsbindung.  
Prüfung: sauberer Gradle-Build, Android-Unit-Smoke-Test, installierbare Debug-APK.

**F-03 – Lokal gebündelter Editor-Container**  
Abhängigkeit: F-02.  
Ergebnis: WebView-Container, TypeScript-Build, Lockdatei und lokale Assets; sichere minimale Kotlin-WebView-Kommunikation; keinerlei CDN-Zugriffe. Noch keine Festlegung auf einen Rich-Text-Editor.  
Prüfung: Test der Bridge-Verträge und lokaler Asset-Auslieferung, Start ohne Netzwerk.

**F-04 – GitHub Actions für statische Checks und Unit-Tests**  
Abhängigkeit: F-02, F-03.  
Ergebnis: Workflows für Push auf `main`, PRs und manuelle Starts, reproduzierbare Toolchains, Cache, Lint, Typecheck, Unit-Tests, Buildfehler als rote Checks.  
Prüfung: nachweislich erfolgreicher Workflow und kontrolliert fehlschlagender Test, der den Workflow rot macht.

**F-05 – Cloud-Emulator-Smoke-Test**  
Abhängigkeit: F-04.  
Ergebnis: Android-Emulator-Runner, App-Installation/Start, einfache UI-Interaktion, aufbewahrte Logs und Fehler-Screenshots.  
Prüfung: grüner instrumentierter CI-Test auf festgelegtem Android-API-Level; dokumentierte Runner-Konfiguration.

**F-06 – Geprüfte Test-APK als Actions-Artefakt**  
Abhängigkeit: F-04, F-05.  
Ergebnis: nach erfolgreichem Pflichtcheck automatisch bereitgestellte APK mit Commit-ID und eindeutiger Dev-Paketkennung; Dokumentation zum Download am Handy. Nicht als unveränderliches Release kennzeichnen.  
Prüfung: grüner Workflow enthält ladbare APK; beim absichtlich roten Check keine als „geprüft“ deklarierte APK.

**Gate P0:** erst mit F-01…F-06 `DONE` gilt die cloudbasierte Entwicklungsumgebung als einsatzfähig.

### P1 – Machbarkeitsnachweis und Technologieentscheidung (E-01 bis E-05)

**E-01 – Editorkandidaten und Lizenzen prüfen**  
Abhängigkeit: P0.  
Ergebnis: nachvollziehbarer Vergleich von Canvas Editor, ProseMirror/Tiptap und ggf. begründeter Alternative. Prüfkriterien: MIT/kompatible Lizenz, Offlinefähigkeit, Android-WebView, Seitenlayout, IME, PDF-Export, Pflegezustand.  
Prüfung: datierte Entscheidungsmatrix mit Quellen, Paketversionen und Risiken.

**E-02 – Direktes Schreiben und Android-Tastatur im Prototyp**  
Abhängigkeit: E-01.  
Ergebnis: A5-Testseite im Editor, Cursor per Tap, Unicode/Deutsch, Autokorrektur, Backspace/Enter, Speichern/Neuladen in Emulator-App.  
Prüfung: automatisierter Instrumentationstest und reproduzierbare manuelle Testschritte mit APK. Keine Fake-Demo mit bloßem Overlay-Textfeld.

**E-03 – Markieren, Zoom und dynamischer Seitenwechsel**  
Abhängigkeit: E-02.  
Ergebnis: Langdruck-Auswahl, Auswahl über Absatz-/Seitengrenzen, Ersetzen, Scrollen mit geöffneter Tastatur, Umbruch auf nächste Seite.  
Prüfung: Emulator-Tests, Screenshots und dokumentierte technische Einschränkungen.

**E-04 – A5-PDF-Probe und Architektur-ADR**  
Abhängigkeit: E-02, E-03.  
Ergebnis: Beispiel-PDF mit A5-Maßen, mehreren Seiten, deutschen Sonderzeichen und durchsuchbarem Text. Abgleich der Layoutpositionen; endgültige Wahl der Editor-/Exportbibliotheken samt begründetem Fallback oder Abbruchentscheidung.  
Prüfung: automatisierte PDF-Text-/Geometrietests, Befund und dokumentierter `GO`/`NO-GO`-Beschluss in [DECISIONS.md](DECISIONS.md).

**E-05 – Frühe echte Smartphone-Abnahme (MANUAL)**  
Abhängigkeit: F-06, E-02…E-04.  
Ergebnis: Nutzer installiert APK und prüft direktes Tippen, Tastatur, Cursor, Selektion, Zoom und Export auf seinem Android-Gerät.  
Prüfung: Ergebnis und ggf. Issue/Feedback-Link im Fortschrittstracker; **niemals aufgrund reiner Emulatorergebnisse auf `DONE` setzen**. Ist kein Test möglich, `BLOCKED`/„wartet auf Gerätetest“ notieren; unabhängige Datenmodell-Arbeit darf fortgesetzt werden, aber kein finales Release.

**Gate P1:** E-01…E-04 `DONE` und dokumentiertes `GO`; bei `NO-GO` Alternative prüfen und diesen Abschnitt aktualisieren. E-05 bleibt spätestens vor P7 verpflichtend.

### P2 – Dokumentmodell, Speichern und Backup (D-01 bis D-04)

**D-01 – Versioniertes Buch-/Kapitel-/Blockmodell**  
Abhängigkeit: P1-GO.  
Ergebnis: IDs, Kapitelreihenfolge, semantische Absätze/Überschriften/Szenentrenner/Seitenwechsel, Inline-Formatierung, Layoutparameter, Validierung und Migrationseinstieg.  
Prüfung: deterministische Unit-Tests für Serialisierung, ungültige Daten und Kapitelreihenfolge.

**D-02 – Editieroperationen und Undo/Redo**  
Abhängigkeit: D-01.  
Ergebnis: insert/delete/replace/split/merge/format als überprüfbare Operationen; Cursor- und Auswahlanker bleiben stabil.  
Prüfung: Unit-Tests inklusive Änderungen über Absatzgrenzen, Unicode und mehrfachem Undo/Redo.

**D-03 – Lokale Speicherung, Autosave und Recovery**  
Abhängigkeit: D-01, D-02.  
Ergebnis: Room-Metadaten, versionierte Dokumentdateien, atomare Speicherung, Fehlerhandling, Rückkehr zu Buch/Kapitel/Position.  
Prüfung: Crash-/Neustarttests, Simulationsfälle für unterbrochene Writes und beschädigte Dateien; keine stillen Datenverluste.

**D-04 – Portables Buch-Backup und Migration**  
Abhängigkeit: D-03.  
Ergebnis: vollständiger Backup-/Restore-Pfad per Android-Dateiauswahl mit Formatversion und Integritätsprüfung; dokumentierte Migration älterer Formate.  
Prüfung: Rundreise Export → Löschen der Testkopie → Import → identischer Inhalt; manipulierte Backups werden sicher abgelehnt.

**Gate P2:** alle Datenmodell-/Wiederherstellungstests grün; Quellmodell bleibt stets unabhängig von PDFs.

### P3 – Bücher- und Kapitelverwaltung (B-01 bis B-03)

**B-01 – Schlichte Bücherliste**  
Abhängigkeit: P2.  
Ergebnis: Bücher erstellen, anzeigen, öffnen, umbenennen, duplizieren und mit Bestätigung löschen.  
Prüfung: Compose-UI-/Emulatortests; sämtliche Operationen überstehen Neustart.

**B-02 – Kapitelverwaltung und Navigation**  
Abhängigkeit: B-01.  
Ergebnis: Kapitel erstellen, umbenennen, umsortieren, löschen/bestätigen und gezielt anspringen.  
Prüfung: UI- und Datenmodelltests auch mit leeren und langen Kapiteln.

**B-03 – Letzte Position und Minimalnavigation**  
Abhängigkeit: B-01, B-02.  
Ergebnis: beim Öffnen letzte sinnvolle Cursor-/Scroll-/Kapitelposition, klare Zurück-Navigation, Buchmenü ohne Bedienüberladung.  
Prüfung: End-to-End-Test Schließen → Öffnen → korrekte Position; Rückkehr aus der Buchansicht ohne Datenverlust.

### P4 – Alltagstauglicher Editor (W-01 bis W-05)

**W-01 – Persistentes direktes Schreiben**  
Abhängigkeit: P1-GO, P2, P3.  
Ergebnis: Datenmodell und gewählter Editor arbeiten zusammen; Tippen und Einfügen an beliebiger Textposition; Absatz/Zeilenumbruch und Textfluss bleiben erhalten.  
Prüfung: Emulator-Tests für Eingabe und Reload, Integrationstests für Bridge und Dokumentoperationen.

**W-02 – Auswahl, Zwischenablage und Ersetzen**  
Abhängigkeit: W-01.  
Ergebnis: Wort-/Bereichsauswahl, Auswahlgriffe, Copy/Cut/Paste, markierten Text löschen/ersetzen, Unicode korrekt.  
Prüfung: Emulator-Tests einschließlich Auswahl über mehrere Absätze.

**W-03 – Strukturelle Textbefehle**  
Abhängigkeit: W-01, W-02.  
Ergebnis: Enter/neuer Absatz, weicher Umbruch, Szenentrenner, manueller Seitenwechsel, Überschriftenstil; keine Leerzeilen-Hacks.  
Prüfung: Dokumentmodelltests und sichtbarer Umbruch in A5-Ansicht.

**W-04 – Reduzierte Formatwerkzeuge**  
Abhängigkeit: W-02, W-03.  
Ergebnis: Fett, Kursiv, Schriftauswahl/-größe, Absatzformat per Auswahl oder Cursor, kontextuelle Werkzeugleiste.  
Prüfung: Eingaben vor/nach Auswahl, partielle Formatierungen, Persistenz und sichtbare Ausgabe.

**W-05 – Suche/Ersetzen, Undo/Redo und Eingabestabilität**  
Abhängigkeit: W-01…W-04.  
Ergebnis: Kapitel-/buchweite Suche und Ersetzung sowie konsistente Rückgängig/Wiederholen-Historie; Tastatur und Scrollposition bleiben stabil.  
Prüfung: Unit-/Integrationstests und Emulator-End-to-End-Szenarien, auch über Seitenwechsel.

### P5 – Buchlayout und druckfähige PDF (L-01 bis L-04)

**L-01 – Buchweite A5-Typografie und Einstellungen**  
Abhängigkeit: W-01…W-04.  
Ergebnis: A5 148×210 mm, Grundschrift, Größe, Zeilenabstand, Ränder/Bundsteg, Absatzgestaltung und Seitenzahlen; lokal lizenzierte Fonts.  
Prüfung: Einstellungs-/Layouttests und gemessene Seitengeometrie.

**L-02 – Absatz- und Seitenumbruch für deutsche Prosa**  
Abhängigkeit: L-01, W-03.  
Ergebnis: fließende Seitenumbrüche, Kapitelstarts, mehrseitige Absätze, deutsche Umlaute/Zitate/Gedankenstriche; Trennung und problematische Umbrüche nach dokumentiertem Umfang.  
Prüfung: Regressionstexte, Golden-Seiten, dokumentierte noch nicht unterstützte Satzregeln.

**L-03 – Lokaler A5-PDF-Export**  
Abhängigkeit: L-01, L-02.  
Ergebnis: PDF mit echten Unicode-Textobjekten, korrekt eingebundenen Schriften, Seitenzahlen und lokalem Android-Share/Save; Export darf Quelle nicht verändern.  
Prüfung: PDF-Parser prüft Seitenmaß/Text und PDF-Viewer öffnet/sucht Testtext.

**L-04 – Regressionsschutz für Buchansicht↔PDF**  
Abhängigkeit: L-03.  
Ergebnis: reproduzierbare PDF-/Bildvergleichstests, dokumentierte und begründete Toleranzen sowie Fehlerberichte bei Abweichungen.  
Prüfung: Golden-Test-Datensatz für mehrseitige Kapitel, Formatierungen und Seitenzahlen; erzwungene Layoutregression führt zu rotem Test.

### P6 – Zuverlässigkeit, Zugänglichkeit und reale Abnahme (Q-01 bis Q-04)

**Q-01 – Offline und Accessibility**  
Abhängigkeit: P4, P5.  
Ergebnis: keine Netzwerkpflicht, zugängliche Touchziele, Screenreader-Beschriftungen der Controls, systemkonforme Navigations- und Textskalierung.  
Prüfung: Offline-Emulatortest, Compose-A11y-Prüfungen und manuelle Checkliste.

**Q-02 – Große Manuskripte und Performance**  
Abhängigkeit: P4, P5.  
Ergebnis: generierte Testbücher von 300–500 Seiten, messbare Eingabelatenz, Speicher- und Exportdauer; ggf. Optimierung/Paginierung pro Kapitel.  
Prüfung: festgelegte Baseline/Grenzwerte und wiederholbarer Lasttest, ohne willkürlich erfundene Leistungsversprechen.

**Q-03 – Robustheit, Datenmigration und Fehlerzustände**  
Abhängigkeit: P2…P5.  
Ergebnis: Recovery bei App-Abbruch, fehlgeschlagener PDF, beschädigter Datei, Speicherknappheit und fehlerhaftem Backup; keine Datenkorruption.  
Prüfung: Fault-Injection-/Migrationstests, verständliche Fehlermeldungen, Testbericht.

**Q-04 – Echte Geräteabnahme (MANUAL)**  
Abhängigkeit: E-05, Q-01…Q-03.  
Ergebnis: Nutzer installiert aktuelle APK, bearbeitet ein mehrseitiges Buch, testet IME/Selektion und exportiert eine PDF; dokumentiertes Feedback wird eingearbeitet.  
Prüfung: expliziter Nutzerbefund und ggf. geschlossene Issues; bei fehlendem Test `BLOCKED`, nicht `DONE`.

### P7 – Verteilung, Signierung und v1 (R-01 bis R-04)

**R-01 – Dauerhafte Signierung und Update-Strategie**  
Abhängigkeit: P0, Q-04 für finales Release.  
Ergebnis: dokumentierte Schlüsselerstellung, GitHub-Secrets-Einrichtung, sichere externe Keystore-Sicherung, stabile Paketkennung und Versionierung. Kein privater Schlüssel im Git-Verlauf.  
Prüfung: signierte Release-APK und dokumentierter Update-Test mit derselben Signatur. **Nötige Nutzeraktion für Secrets/Backup offen ausweisen.**

**R-02 – GitHub-Release-Automation**  
Abhängigkeit: R-01, P5, P6.  
Ergebnis: Tag- oder kontrolliert manuell ausgelöster Workflow mit vollständigen Pflichtchecks, signierter APK, SHA-256-Prüfsumme und Release-Notizen.  
Prüfung: bei Rot kein Release; bei Grün APK als GitHub-Release-Asset und reproduzierbarer Commitbezug.

**R-03 – Installation und Update am Smartphone**  
Abhängigkeit: R-02.  
Ergebnis: verständliche Android-Anleitung zum Download/Installieren/Aktualisieren, parallele Entwicklungsinstallation nach Bedarf, Backup vor kritischen Updates.  
Prüfung: manueller Installations- und Update-Test auf realem Gerät, Daten bleiben erhalten.

**R-04 – v1-Release-Gate**  
Abhängigkeit: alle P0–P6-Aufgaben, R-01…R-03.  
Ergebnis: keine unbehandelten kritischen Bugs, vollständige Abnahmematrix, dokumentierte Versionsnummer, fertiger GitHub-Release-Eintrag.  
Prüfung: alle Pflichtaufgaben `DONE`, CI grün, Nutzerabnahme dokumentiert, Releasedateien überprüft.

## 5. Querschnittsregeln

**Tests:** Bei jeder neuen Funktion zuerst passende Prüffälle spezifizieren. Besonders wichtig sind Android-IME/Touch, Autosave/Recovery und Seitentreue des PDF-Exports. Keine Tests entfernen, überspringen oder abschwächen, nur um einen Build grün zu bekommen.

**Builds:** Jeder von Codex abgeschlossene Code-Arbeitsschritt muss auf `main` nachvollziehbar sein, einen grünen vollständigen Pflichtcheck besitzen und – sobald F-06 erledigt ist – eine testbare APK erzeugen. Ein fehlgeschlagener Build ist ein Blocker, kein erledigter Schritt.

**Release-Assets:** APKs gehören in GitHub Actions Artifacts (temporär) bzw. Releases (dauerhaft), nicht als binäre Dateien in den Git-Quellbaum.

**Transparenz:** Automatisierte Emulator-Tests beweisen nicht, dass Touch und Tastatur auf dem Handy des Nutzers identisch reagieren. Daher gesonderte echte Geräteabnahmen E-05 und Q-04.

**Noch nicht enthalten:** Nachträglicher Import beliebiger PDFs, LaTeX-Komplettkonvertierung, synchronisierte Cloud-Bibliothek oder ein App-Store-Release. Bei Bedarf später als neue Aufgaben mit separater Priorisierung ergänzen.

## 6. Abschluss und Wiederaufnahme

Der Agent arbeitet **eine aktive Aufgabe gleichzeitig**. Vor jedem Task die fünf Pflichtfelder in [PROGRESS.md](PROGRESS.md) aktualisieren: Aufgabe, Status, geplanter nächster Einzelschritt, Blocker, letzte Evidenz. Nach Code-/Testschritt: Commit und CI prüfen, Nachweise verlinken, dann erst `DONE` setzen und die nächste Aufgabe starten.

Bei Neustart ist [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md) verbindlich. Nicht die zuletzt gesendete Chatnachricht, sondern Repository, CI und `PROGRESS.md` bestimmen den tatsächlichen Zustand.
