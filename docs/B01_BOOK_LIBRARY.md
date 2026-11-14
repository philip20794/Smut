# B-01 – Native Bücherliste und CRUD

**Status:** DONE am 14.11.2026. **Beleg:** [vollständig grüne GitHub-CI 43594430969](https://github.com/philip20794/Smut/actions/runs/43594430969); [Implementierung und Tests](https://github.com/philip20794/Smut/commit/fcf7642272d0ec716b57bd83dce67c2916377359). E-05 bleibt offen, da noch kein Test auf echtem Android-Gerät vorliegt.

## Implementierung

- Der Android-Launcher ist `LibraryActivity`; die bisherige `MainActivity` mit Offline-Canvas-WebView ist weiterhin über „A5-Prototyp öffnen“ erreichbar und bleibt vom neuen Buchformat getrennt.
- Die Compose-Bücherübersicht zeigt den Room-Index aus `BookStore.listEntries()` und bietet Anlegen, Öffnen, Umbenennen, Duplizieren, Löschen (mit Rückfrage) sowie Backup/Import.
- Ein neues Buch wird mit einer eigenen stabilen Buch-ID, einem leeren Kapitel, A5-Layoutwerten und `BookPosition(chapterId, null, 0, 0)` angelegt. Titel sind nach dem Trimmen nicht leer und haben höchstens 240 Zeichen.
- Umbenennen erfolgt als revisionsgeschütztes Speichern des vollständigen strukturierten Dokuments. Duplizieren kopiert Inhalt, Formatierungen, Layout und Position, erzeugt aber neue IDs für Buch, Kapitel und Blöcke. Das Original wird nicht verändert.
- Beim Löschen wird zuerst eine persistente Löschmarkierung geschrieben. Danach werden Dokumentdateien, Recovery-Dateien und Index entfernt. Nach Prozessabbruch wird die Bereinigung beim nächsten Lesen der Bücherliste fortgesetzt; ein markiertes Buch ist beim Laden und Speichern gesperrt und verschwindet aus der Bücherliste. Eine beschädigte, nicht lesbare Buchdatei wird nicht stillschweigend gelöscht.
- Dateizugriffe und Datenbankoperationen laufen auf einem seriellen Worker. UI-Aktionen sind während der Bearbeitung gesperrt, und Fehler werden als Fehler angezeigt.

## Tests

Die komplette grüne Pipeline prüft TypeScript-Typecheck/Lint/Unit-Tests, Android-Kotlin-Unit/Lint/APK und offline instrumentierte Emulatorfälle auf API 35.

`BookLibraryStoreInstrumentedTest` umfasst Neuanlage/ersten Cursoranker, Formatierungs-/Unicode-Inhaltskopie, Unabhängigkeit der IDs, Umbenennen, Wiederöffnung der Room-Datenbank, Löschen und Recovery nach einer simulierten Unterbrechung mit persistenter Löschmarkierung. Ungültige Titel und beschädigte Quelldaten dürfen nicht verändert werden.

`BookLibraryUiInstrumentedTest` prüft die echte Compose-Bedienung für Anlage, Öffnen, Umbenennen, Kopieren, bestätigtes Löschen, Abbruch des Löschdialogs und Aktivitätsneustart. Die bisherigen Offline-WebView-/Tastatur-/Auswahl- und A5-PDF-Regressionstests bleiben grün. Die Entwickler-APK wird nur nach vollständigem Erfolg veröffentlicht.

## Grenzen und Übergabe

Das Öffnen eines strukturierten Buchs zeigt in B-01 eine **native Leseansicht** mit Kapitelüberschriften. Es ist noch **kein** direkter Einstieg in den A5-Texteditor: Die sichere Migration und Kopplung der Modelle ist W-01. Der E-02-Prototyp bleibt separat unter `prototype-editor-v1.json` gespeichert und wird nicht automatisch durch D-04-Backups erfasst.

**B-02** ergänzt Kapitelverwaltung mit Anlage, Umbenennen, Umsortieren, Löschbestätigung und Navigation; die Kapitelstruktur und Cursorposition sollen bei Fehlern oder Neustart unverändert gültig bleiben. **B-03** ergänzt letzte Position und Minimalnavigation. **E-05** verlangt weiterhin die echte Smartphone-Abnahme.
