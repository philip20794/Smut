# B-02 – Kapitelverwaltung und Navigation

**Status:** DONE am 14.11.2026. **Beleg:** [vollständig grüne Smut CI 43597289709](https://github.com/philip20794/Smut/actions/runs/43597289709), Code- und Testrevision [da2678b](https://github.com/philip20794/Smut/commit/da2678bbf93aa43196e5a5bc350f258cec17616c). `E-05` bleibt bis zu einem echten Smartphone-Test offen.

## Funktion

Die native Buchansicht hat eine Kapitelverwaltung mit `Neues Kapitel`, `Umbenennen`, `Hoch`, `Runter`, `Löschen` samt ausdrücklicher Bestätigung und `Kapitel öffnen`. Das ausgewählte Kapitel erscheint mit einer auf 400 Zeichen begrenzten Lesevorschau und Textblockzahl. Die direkte A5-Textbearbeitung dieser strukturierten Bücher bleibt dem Arbeitspaket `W-01` vorbehalten.

Die nativen Kapiteloperationen liegen im `BookStore` und arbeiten ausschließlich mit dem versionierten `D-01`-Buchmodell. Die gesamte Validierung, Mutation und das Speichern erfolgen unter dem prozessweiten Dateilock. Jede Mutation verlangt die vom Client zuletzt gelesene Revision; bei Konflikten wird die Änderung abgewiesen und die UI lädt den aktuellen Buchstand. Der Cursor wird beim Anlegen/Auswählen auf den neuen Kapitelanfang gesetzt. Umordnung und Umbenennung bewahren ihn. Beim Löschen des aktiven Kapitels wählt die Anwendung ein noch vorhandenes Kapitel mit sicherem `blockId = null, offset = 0, scrollY = 0`; das letzte Kapitel lässt sich nicht löschen. Kapitelinhalte, Layout, Reihenfolge und IDs werden persistent gespeichert. Der frühere `prototype-editor-v1.json`-Inhalt bleibt unberührt.

Nach `ActivityScenario.recreate()` beziehungsweise der Neuanlage einer Android-Activity lädt die Oberfläche das zuvor geöffnete Buch erneut, sofern die ID im Instanzzustand gespeichert war. Für die umfassende Navigation zu letzter Kapitel-/Text-/Scrollposition bleibt `B-03` zuständig.

## Tests und abgenommene Grenzen

`ChapterStoreInstrumentedTest` deckt leere Kapitel, Erstellen/Umbenennen/Umsortieren, lange deutsche Unicode-Texte mit Formatierung, unabhängige Positionen, Datenbankneustart, Kapitelwechsel, Schutz des letzten Kapitels, Konkurrenzrevisionen, Fehleingaben und Legacy-Unversehrtheit ab.

`ChapterLibraryUiInstrumentedTest` führt die native Bedienung im Android-35-Emulator aus: Kapitel anlegen, verschieben, umbenennen, öffnen, den Löschdialog abbrechen und endgültig bestätigen, Buch nach Activity-Recreation öffnen. Der gesamte Pflichtlauf inklusive alter B-01-/D-04-/Canvas-/PDF-Regressionen besteht. Test-APK und dreiseitige A5-PDF-Probe sind über [GitHub Actions](https://github.com/philip20794/Smut/actions/runs/43597289709) verfügbar. Das erste rote Ergebnis betraf eine Test-Position innerhalb eines Emoji-Surrogatpaars und einen fehlenden Await im UI-Test; beide Testannahmen wurden behoben, die produktiven Validierungsregeln wurden nicht abgeschwächt.

## Übergabe

Nächster Task: **B-03** (gespeicherte Kapitel-/Cursor-/Scrollposition und Minimalnavigation). Danach **W-01** (Verbindung des strukturierten Buchformats mit dem direkt editierbaren A5-Editor). Die frühe manuelle Geräteabnahme **E-05** ist weiterhin `BLOCKED`. Der PDF-Prototyp ist kein druckfertiger Buch-Export.
