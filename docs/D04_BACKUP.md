# D-04 – Portables Buch-Backup und sicherer Import

Stand: 14.11.2026 – Implementierung, CI-Prüfung ausstehend.

## Austauschformat

Eine einzelne, unkomprimierte UTF-8-JSON-Datei pro strukturiertem Smut-Buch (`.smut.json`, MIME `application/json`).

- `format: "smut-book-backup"`, `backupVersion: 1`, `schemaVersion: 1`
- `revision`: positive, im JavaScript-Safe-Integer-Bereich liegende D-03-Revision
- `documentJson`: vollständiges D-01-Buch mit Metadaten, Kapiteln, Blöcken, Formatierungen und Layout
- `positionJson`: Kapitel-/Block-ID, UTF-16-Cursoroffset, Scrollposition
- `sha256`: SHA-256 über Formatkennung, Version, Revision, Dokument- und Positions-JSON in definierter Reihenfolge

Die SHA-Prüfsumme weist zufällige bzw. nachträgliche Dateiveränderungen nach; sie ist **keine Authentifizierung und keine Verschlüsselung**. Exportierte Bücher enthalten private Inhalte im Klartext: Sicherungsdateien nur an vertrauenswürdigen Orten ablegen. Keine ZIP-Einträge, Dateipfade, ausführbaren Felder oder Netzverbindungen. Die Datei ist formatversioniert und von Canvas/PDF unabhängig.

## Importvertrag

1. Datei ausschließlich via Android Storage Access Framework und `ContentResolver` einlesen; höchstens 20 MB inklusive JSON-Hülle.
2. Strikt decodierbares UTF-8, ein JSON-Objekt ohne angehängte Daten, exakte Feldmenge, bekannte Versionen, gültige SHA-256-Summe sowie D-01-Schemavalidierung und valide Textanker prüfen.
3. Vor jeglichem Schreiben die Ziel-Buch-ID validieren. Bestehen bereits Room-Eintrag, Hauptdatei, Rückfallrevision oder ein unvollständiger atomarer Schreibversuch, den Import ohne Änderungen ablehnen. Auch korrupte vorhandene Daten werden **niemals** überschrieben.
4. Die vollständige geprüfte Buchkopie und Cursorposition atomar in den privaten Android-Speicher schreiben; ursprüngliche Datumswerte, IDs, Formatierungen und Revision erhalten. Anschließend Room-Index ergänzen. Falls eine Index-Aktualisierung fehlschlägt, bleibt die neue valide Datei erhalten; `load` repariert den Index.
5. Nicht unterstützte ältere oder zukünftige Backupversionen klar ablehnen. Ein automatisches, stilles Format-Downgrade ist untersagt.

## Android-Bedienung

Im vorhandenen A5-Prototyp führt die Schaltfläche **„Buch-Backup“** zu einer separaten nativen Ansicht, ohne die Canvas-Seitengeometrie zu verändern. Dort öffnet **„Buch aus Datei importieren“** `ActivityResultContracts.OpenDocument`; jeder Eintrag der neuen strukturierten Buchliste bietet **„Sichern“** via `CreateDocument`. Ein abgebrochener Dateidialog verändert keine gespeicherten Bücher. Sämtliche Dateizugriffe erfolgen in einem Hintergrund-Executor, nie auf dem UI-Thread. Der Dateianbieter bestimmt das tatsächliche Speicherziel.

**Abgrenzung:** Bis B-01/W-01 arbeitet die sichtbare Canvas-Buchseite weiterhin auf `prototype-editor-v1.json`. Sie kann über diese D-04-Funktion **noch nicht** als strukturiertes Buch exportiert werden. Die D-01-Konvertierung existiert als isolierter, sicherer Migrationsbaustein, wird aber erst bei W-01 nach Backup des unveränderten Legacy-Snapshots in den Nutzerfluss eingebunden. D-04 verändert die Legacy-Datei nicht.

## Tests

- Kotlin-JVM: UTF-8-/Unicode-Rundreise, alle Buch- und Positionsfelder erhalten, falsche Prüfsummen, andere Versionsnummern, angehängte Daten, übergroße Dateien und fehlerhafte Strukturen abweisen.
- Emulator API 35: echtes D-03-Buch speichern → exportieren → ausschließlich einzigartige Testkopie löschen → importieren → identisches Dokument samt Position und Revision, bestehende IDs und selbst korrupte Daten nie überschreiben.
- Emulator: ContentResolver-/MediaStore-URI statt gewöhnlicher Dateipfade für Export/Import; Legacy-Datei vor und nach der Rundreise identisch; Android-Oberfläche über lokale WebView erreichbar.
- Pflicht-Gate: TypeScript, Android Unit/Lint/APK, Offline-Emulator und bestehende PDF-Probe vollständig grün. Erst dann D-04 `DONE`.

Nächster Meilenstein nach erfolgreicher D-04-Abnahme: P3 / B-01 – sichtbare Bücherliste, Erstellen, Öffnen, Umbenennen, Duplizieren und sicheres Löschen.
