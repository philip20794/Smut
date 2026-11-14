# D-02 – Editieroperationen und Undo/Redo

Stand: 14.11.2026. Dieses Modul ist eine reine, testbare Bearbeitungsschicht auf dem in D-01 definierten Buchformat. **Noch keine Umschaltung der bestehenden Android-WebView auf das neue Format.**

## API und Invarianten

Quellcode: `editor/src/edit-operations.ts`; Tests: `editor/tests/edit-operations.test.mjs`.

- `applyEditOperation(document, operation, trackedSelection?)`: liefert `{document,selection,caret,changed}`, verändert das Eingabedokument nicht und schreibt keine Dateien.
- `insert`/ `delete`/ `replace`: einzelne Textzeilen und beliebige Textbereiche innerhalb desselben Kapitels. Bei bereichsübergreifender Löschung bleiben ID und Stil des ersten Textblocks erhalten; die nachfolgenden betroffenen Textblöcke werden zusammengeführt. Explizite Szene-/Seitenumbruchblöcke dürfen nicht stillschweigend gelöscht werden.
- `split`: teilt einen Textblock an einer zulässigen Position; die bestehende Block-ID bleibt links, rechts entsteht ein neuer Absatz mit vom Aufrufer gelieferter eindeutiger ID. Die linke Seite einer Überschrift muss Text behalten; die neue rechte Seite ist ein Absatz.
- `merge`: verbindet ausschließlich unmittelbar benachbarte Textblöcke in einem Kapitel, erhält die ID und den Absatz-/Überschriftentyp des ersten Blocks und überträgt die Formatruns der zweiten Seite.
- `format`: setzt/löscht `bold` und `italic` oder setzt/entfernt `fontFamily` und `fontSizePt` in einem ausgewählten Bereich. Unberührte Zeichen behalten ihre Markierungen. Benachbarte Runs mit gleichen Markierungen werden zusammengelegt.

`TextAnchor = {chapterId,blockId,offset}`; `TextSelection = {anchor,focus}` (auch rückwärtsgerichtete Auswahl). `offset` ist ein **UTF-16-Codeeinheiten-Offset**, passend zu JavaScript und den üblichen WebView-Text-APIs. Er muss an einer **Unicode-Graphemgrenze** liegen, damit Emoji, kombinierende Akzente und ZWJ-Sequenzen nicht auseinandergerissen werden. Der Algorithmus verwendet den lokal vorhandenen `Intl.Segmenter`. Nach einer Operation werden neu entstandene verschmolzene Grapheme mit Vorwärtsausrichtung korrigiert.

`caret` ist die typische neue Einfügeposition nach der Operation. Ohne `trackedSelection` ist `selection` auf `caret` zusammengefallen. Bei übergebener Auswahl werden `anchor` und `focus` einzeln über Block-IDs und Zeichenpositionen abgebildet, auch wenn Absätze wegfallen. Nicht betroffene Kapitel und Blöcke behalten ihre IDs und Anker.

`applyEditOperation` validiert das gesamte Eingabedokument sowie das Ergebnis nach dem D-01-Schema und dessen 8-MB-Grenze. Ungültige Operationen werfen einen Fehler; die Eingabedaten bleiben unverändert. Ein No-op liefert `changed:false`.

## Undo/Redo

`createEditHistory(document,selection,maxDepth=100)` erstellt eine eigene validierte Anfangskopie. `applyHistoryOperation` legt nur tatsächliche Änderungen in der begrenzten Undo-History ab, setzt den Standardcursor auf den Bearbeitungspunkt und verwirft bei neuer Änderung einen etwaigen Redo-Zweig. `undoHistory` und `redoHistory` stellen sowohl Dokument als auch Auswahl aus den gespeicherten Snapshots wieder her. Alle Funktionen sind deterministisch und unabhängig von Android, Canvas oder Systemzeit.

Der Datenmodell-Zeitstempel `updatedAt` wird durch D-02 bewusst nicht verändert; in D-03 übernimmt das persistente Commit-/Autosave-Protokoll dessen Aktualisierung. Die History befindet sich nur im Arbeitsspeicher. Eine dauerhafte History über einen Prozessneustart ist **nicht** Gegenstand dieses Schritts.

## Grenzen und nächster Anschluss

- Kapitelübergreifende Selektion sowie Löschung expliziter Szene-/Seitenumbrüche sind absichtlich nicht als generische Textlöschung verfügbar; spätere Strukturkommandos müssen dafür eigene Semantik erhalten.
- Es gibt keine Schreiboperation auf produktive E-02-Canvas-JSON-Dateien, keine Firebase-Verbindung und keinen Netzwerkzugriff. D-03 baut eine sichere lokale Persistenz/Recovery für das versionierte Modell. W-01 bindet den Editor an die hier definierten Editieroperationen.
- Beim Zusammenführen bleibt der Typ des ersten Textblocks bestehen; dadurch bleibt etwa eine Überschrift nur dann gültig, wenn ihr Text nicht vollständig entfernt wird.
- Die Tests prüfen Unicode-/Graphemgrenzen, rückwärtsgerichtete und mehrabsätzige Bereiche, Absatztrennung/-vereinigung, Inlineformatierung, Ankerabbildung, Fehlerfall-Atomizität, begrenzte History und mehrstufiges Undo/Redo.
