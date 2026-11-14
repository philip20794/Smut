# D-03 – lokale Buchpersistenz, Autosave und Recovery

Status: DONE, 14.11.2026. [Code bfd2c82](https://github.com/philip20794/Smut/commit/bfd2c82e085844930bd5490924186c5f287bc8c0), vollständige [Smut CI 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623) erfolgreich (Editor- und Android-Tests, Offline-Emulator und PDF-Regression). D-02 ist die Grundlage; die Oberflächenintegration bleibt W-01.

## Persistenzvertrag

- **Autoritatives Buch:** `filesDir/books-v1/<id>.book.json` (private App-Datei, `AtomicFile`), inklusive `formatVersion:1`, monotoner Revision, streng nach D-01 validiertem `documentJson`, Cursor-/Scroll-`positionJson` und SHA-256 über Revision, Dokument und Position. Limits: 8 MB Dokument, 16,5 MB Speicherhülle. Buch-IDs werden vor Pfadbildung validiert.
- **Rückfallrevision:** `<id>.previous.json` erhält vor jedem Update die zuletzt validierte Hauptrevision. Bei abgebrochener atomarer Speicherung bleibt die bisherige Hauptdatei lesbar. Ist sie beschädigt, wird die gültige Rückfallkopie mit höchster Revision geladen und mit `recovered:true` gekennzeichnet. Sind beide beschädigt, meldet der Ladevorgang `corrupt`, ohne sie zu überschreiben.
- **Room-Datenbank:** `smut-books-index-v1.db`, Tabelle `book_entries` mit Buch-ID, Titel, Aktualisierungsdatum, Revision sowie Kapitel-/Block-/Cursor-/Scrollposition. Sie dient als wiederherstellbarer Index, nicht als Quelle des Buchtexts. Erst wird die Buchdatei atomar geschrieben, danach der Index aktualisiert; ein späteres `load` gleicht einen veralteten Index wieder ab.
- **Revisionsschutz:** `expectedRevision` ist Pflicht. Stale saves ergeben `conflict` statt stillem Überschreiben. Erfolgreiches Speichern setzt `updatedAt` auf einen kanonischen UTC-Millisekundenzeitstempel, nie vor `createdAt` oder dem bisherigen `updatedAt`.
- **Cursor:** zuletzt bearbeitetes Kapitel, optionaler Textblock, UTF-16-Offset und Scroll-Y werden mit der Revision atomar gespeichert. Offsets außerhalb des Textes und mitten in einem Surrogatpaar werden abgelehnt.
- **Bestandsschutz:** Die unveränderte Datei `prototype-editor-v1.json` sowie die bestehende E-02-Bridge werden nicht gelesen, migriert, umbenannt oder gelöscht. Eine produktive Canvas-zu-Buch-Übernahme braucht zuerst D-04 und W-01.

## Bridge und Autosave

`book-load` (`bookId`) und `book-save` (`expectedRevision`, `document`, `position`) verwenden versionierte und streng begrenzte WebView-Nachrichten. Antworten sind `book-load-result` mit `ok/missing/corrupt/error` oder `book-save-result` mit `saved/conflict/error`. Keine Datei- oder URL-Pfade werden aus der WebView angenommen.

`editor/src/book-persistence.ts` stellt strenge Nachrichten-Builder und -Parser sowie `BookAutosaveQueue` bereit. Die Queue bündelt schnelle Änderungen (350 ms), serialisiert Speichervorgänge, erhält ungespeicherte Änderungen nach Fehlern und aktualisiert die Revision erst nach Erfolg. Beim späteren Anschluss W-01 muss die WebView auf tatsächliche native ACKs warten und vor dem Schließen explizit `flush()` aufrufen; der Canvas-Prototyp speichert weiterhin unabhängig auf dem bisherigen Weg. Ein Queue-Objekt allein stellt keine Integration in die sichtbare Oberfläche dar.

## Prüffälle

- Kotlin-JVM: gültige/ungültige Buch-Bridge, strenge Struktur- und Positionsprüfung, Unicode/Surrogatgrenzen.
- TypeScript: sichere Buchnachrichten, zusammengefasste Saves, gleichzeitige Änderungen, Retry bei Schreibfehler.
- Android-API-35-Emulator: Room mit Dateischicht und Wiederöffnung, gültige Revision, Cursor/Scroll, simulierte Unterbrechung mitten in `AtomicFile`, korrupte Hauptdatei mit gültiger Rückfallrevision, korrupte Einzeldatei, Revisionskonflikt, Schutz des bisherigen Canvas-Snapshots.
- CI-Gate: Typecheck, Node-Tests, Kotlin-Unit, Lint, APK-Build und sämtliche Offline-Emulator-Tests müssen grün sein, bevor `PROGRESS.md` auf DONE wechselt.

## Begrenzungen

- Noch keine Buchliste in Compose (B-01), keine Backup-Auswahl über das Android-Dateisystem (D-04), kein W-01-Anschluss des Canvas-Editors.
- Kein automatisches Reparieren zweier vollständig beschädigter Revisionen und kein synchroner Multi-Prozesszugriff; die Datei bleibt zum manuellen Recovery erhalten.
- Undo-/Redo-Snapshots bleiben absichtlich nur im Arbeitsspeicher (D-02).
