# D-01 – Versioniertes Buch-, Kapitel- und Blockmodell

Stand: 14.11.2026. **Vertrag und Migrationseinstieg**, noch keine produktive Umschaltung des E-02-Prototyps.

## V1-Dokument

Das in `editor/src/book-model.ts` definierte `BookDocument` ist ein reines TypeScript-Datenmodell ohne Canvas-, Android- oder PDF-Abhängigkeiten. Es enthält:

- `schemaVersion: 1`, stabile, buchweit eindeutige IDs (`[A-Za-z0-9][A-Za-z0-9_-]{0,63}`), Titel, optionalen Autor sowie UTC-Zeitstempel.
- Eine A5-Layoutkonfiguration (148 × 210 mm) mit Innen-/Außen- und oberen/unteren Rändern, Schriftfamilie/-größe, Zeilenabstand und Seitenzahlen.
- `chapters`: geordnetes Array; die Reihenfolge ergibt sich ausschließlich aus der Position, nicht aus separat gepflegten Indizes.
- `blocks`: `paragraph`, `heading` (Ebene 1/2), `sceneBreak` und `pageBreak`.
- Textblöcke mit Unicode-`runs` und optionalen Marken `bold`, `italic`, `fontFamily`, `fontSizePt`. Leere Absätze haben `runs: []`. Ein Zeilenumbruch innerhalb von `run.text` ist nicht kanonisch.

Die Struktur ist weder eine PDF noch ein Canvas-Editor-Objekt; die Editor-Engine soll später über einen Adapter mit diesem Modell sprechen. Stabile IDs werden durch spätere Editoroperationen erhalten; D-01 legt bewusst **keine** Undo-/Redo-Operationen fest (D-02).

### Vertrag

- `createEmptyBook(bookId, title, timestamp, chapterId)` erstellt ein leeres Modell mit validierten Parametern.
- `validateBookDocument(unknown)` lehnt unbekannte Schema-Versionen, zusätzliche Felder, falsche Typen, ungültige Zeitstempel, doppelte IDs, nicht endliche Maße, ungültige Blöcke und unsinnige A5-Ränder explizit ab.
- `serializeBookDocument(unknown)` und `parseBookDocument(string)` liefern eine prüfbare JSON-Rundreise. Schutzgrenze: 8.000.000 UTF-8-Bytes je Dokument.
- Bewusst keine automatische Schema-Hochstufung: Unbekannte zukünftige Versionen führen zum Fehler, bis ein explizit getesteter Migrationspfad existiert.

## Einmalige Übernahme aus dem E-02-Canvas-Prototyp

`editor/src/legacy-migration.ts` stellt `migratePrototypeSnapshot(snapshot, options)` bereit.

- Der bisherige `main`-Text wird in Absatzblöcke aufgeteilt; Zeilenumbrüche erzeugen separate Absätze, leere Absätze bleiben bestehen.
- Bekannte Inline-Eigenschaften `bold`, `italic`, `font`, `size` werden als Markierungen übernommen.
- Unbekannte Zusatzfelder werden **gemeldet**. Der unveränderte ursprüngliche JSON-Snapshot wird als `originalJson` im Migrationsergebnis zurückgegeben und muss bei späterer persistenter Migration gesichert werden.
- Unbekannte Strukturtypen und fehlerhafte Werte werden abgewiesen, statt als einfacher Text stillschweigend verloren zu gehen.
- Ausführung ist deterministisch: IDs, Titel und Erstellungszeit kommen aus den Parametern; der Adapter schreibt **nichts** in die aktuelle Android-Protoypdatei.

**Grenzen:** Die Übernahme ist für die bisher belegten einfachen Canvas-Text-/Inline-Fälle vorbereitet. Native Sonderblöcke, Styles auf Absatzebene sowie Canvas-spezifische Layoutdetails brauchen vor produktiver Übernahme eigene Adapter. Die reine Warnung ersetzt keine menschliche/technische Überprüfung; auch `originalJson` ist noch kein persistentes Backup.

## Test- und Integrationsgrenzen

`editor/tests/book-model.test.mjs` prüft die Unicode-JSON-Rundreise, stabile Kapitelreihenfolge, vier Blockarten, Inline-Marken, Fehlerfälle, deterministische Legacy-Übernahme und unveränderten Quellsnapshot. Der Test läuft im bestehenden `npm test` und damit in GitHub Actions.

Die aktuelle App schreibt vorerst weiter das E-02-Prototypformat. D-02 ergänzt Textoperationen; D-03 führt das BookDocument als dauerhaften Speicher inklusive Migration/Recovery ein; D-04 ergänzt Backup und Import. W-01 verbindet das Modell endgültig mit dem Canvas-Editor. Diese Schritte dürfen die Originaldaten nicht stillschweigend überschreiben oder beim ersten Start automatisch migrieren.
