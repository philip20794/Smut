# Technische Architektur und Machbarkeit

## 1. Grundsatzentscheidung

Der Editor arbeitet **nicht** auf einem PDF und muss für Texteingaben nicht auf eine LaTeX-Kompilierung warten. Er arbeitet mit einem strukturierten, lokalen Dokumentmodell. Der Renderer zeigt daraus fortlaufend paginierte A5-Buchseiten. Ein PDF-Exporter verwendet nach Möglichkeit dieselben Layoutdaten.

## 2. Vorschlag für die Komponenten

- **Android-Hülle:** Kotlin, Jetpack Compose, Android Storage Access Framework für Import/Export, Android-Tastaturintegration.
- **Editor:** lokal gebündelte TypeScript/JavaScript-Anwendung innerhalb einer WebView; kein externer Dienst oder CDN zur Laufzeit.
- **Editor-Kandidat A:** `@hufe921/canvas-editor` für Cursor, Selektion, Umbruch, Paginierung, Formatierung.
- **PDF-Kandidat:** `canvas-editor-pdf` oder ein kompatibler vektorbasierter Exportweg; unbedingt prüfen, ob die PDF auf dem Zielgerät echte Unicode-Textobjekte und eingebettete oder korrekt referenzierte Schriftarten enthält.
- **Fallback B:** Tiptap/ProseMirror, falls Canvas Editor die wesentlichen mobilen Anforderungen nicht erfüllt; benötigt dann eine eigenständige Paginierungsstrategie und einen geprüften PDF-Export.
- **Speicherung:** Android Room für Buchmetadaten und Kapitelindex; editierbare Kapitel-/Layoutdaten als versionierte JSON-Dokumente in privaten App-Dateien. Atomare Writes/Transaktionen und Sicherungen sind verpflichtend.
- **Bridge:** eine schmale, dokumentierte und abgesicherte Schnittstelle für Speichern, Öffnen, Export und Navigationsereignisse zwischen WebView und Kotlin. Nur vertrauenswürdige lokal gebündelte Inhalte laden; keine unnötig breit freigegebene Javascript-Bridge.

## 3. Zu beweisende Machbarkeit VOR der Hauptimplementierung

Eine kleine, vertikale Prototypstrecke auf einem realen Android-Smartphone muss folgende Punkte nachweisen:

1. **Touch-Textcursor:** einzelnes Antippen setzt den Cursor zwischen den beabsichtigten Zeichen.
2. **Android-Tastatur:** Eingeben, Löschen, Autokorrektur, Umlaute, Zwischenablage und Cursorbewegung funktionieren stabil.
3. **Langdruck-Selektion:** Auswahlgriffe, Text ersetzen und über Zeilen/Absätze hinweg markieren.
4. **Seitenwechsel:** Tippen am Ende einer vollen A5-Seite führt zu korrektem Textfluss auf die nächste Seite, ohne Cursorverlust.
5. **Tastatur und Zoom:** Cursor bleibt sichtbar, die Seite springt nicht unmotiviert.
6. **Speichern/Neustart:** Dokument bleibt nach Prozessabbruch und erneutem Start konsistent.
7. **PDF-Fidelity:** A5-Maße, Seitenzahlen, Zeilenumbrüche, Schriften, Umlaute sowie Textsuche und Textselektion im PDF stimmen.
8. **Lange Texte:** ein realistisches Buch mit vielen Kapiteln lässt sich ohne merkbare Eingabeverzögerungen bearbeiten.
9. **Offline-Test:** App funktioniert vollständig im Flugmodus, auch nach Neuinstallation des Builds mit lokal gebündelten Fonts und Assets.

Scheitert ein kritischer Test ohne vertretbaren Fix, muss Codex den Fallback prüfen, statt Workarounds um einen ungeeigneten Editor anzuhäufen. Die Entscheidung einschließlich der gemessenen Ergebnisse gehört in die spätere technische Dokumentation.

## 4. Datenmodell (fachlich)

- `Book`: stabile ID, Titel, optional Autor, Erstellungs-/Änderungsdatum, Layoutkonfiguration, geordnete Kapitel-IDs, Dokumentformatversion.
- `Chapter`: stabile ID, Titel, Reihenfolge, strukturierte Blöcke.
- `Block`: Absatz, Überschrift, Szenentrenner, erzwungener Seitenumbruch (weitere Typen später).
- `InlineMark`: z. B. fett, kursiv, Schriftfamilie, Schriftgröße für Zeichenbereiche.
- `EditorState`: Cursoranker, aktuelle Textauswahl, Zoom, zuletzt sichtbare Position.
- `Revision`: kurzfristige Undo/Redo-Historie während der Bearbeitung; bei Bedarf sichere Wiederherstellungspunkte auf Dateiebene.

Das Datenformat muss versioniert sein. Keine ungesicherten direkten String-Ersetzungen in HTML-/LaTeX-Code als primäre Bearbeitungsmethode.

## 5. Buchsatz und PDF

- Standardgröße A5 exakt 148 × 210 mm im PDF; mögliche Anschnitt-/Druckvorgaben können später ergänzt werden.
- Eine definierte Layout-Engine für Zeilen-, Absatz- und Seitenumbruch; Änderungen an Schrift oder Rändern lösen einen Neu-Umbruch aus, nicht eine PDF-Rückimportierung.
- Druckbare PDF soll Text als Text enthalten, Suchbarkeit bieten und Fonts korrekt handhaben.
- Visueller Abgleich von Editorseiten und Export-PDF mit Referenztests. Abweichungen müssen dokumentiert und gegebenenfalls durch einen Export-Renderer mit derselben Paginierungslogik behoben werden.
- Als Schutz gegen Performanceprobleme PDF-Erzeugung getrennt von Textbearbeitung durchführen; bei Bedarf Fortschritt anzeigen.

## 6. Risiken und Gegenmaßnahmen

| Risiko | Gegenmaßnahme |
| --- | --- |
| Canvas-Editor nicht touch-tauglich auf Android | Frühzeitiger Geräte-Prototyp; danach Fallback zu ProseMirror prüfen |
| PDF-Export weicht vom Editor ab | Golden-Document-Tests mit Seiten- und Textvergleich; nur bestandene Kombination übernehmen |
| Schriftdateien müssten online geladen werden | Lokal lizenzierte und gebündelte Schriften, Flugmodus-Test |
| Hunderte Seiten bremsen die Eingabe | Kapitelbezogene Datenhaltung, inkrementelles Layout, Performance-Test auf Mittelklassegerät |
| Mehrere Leerzeilen führen zu schlechtem Buchsatz | Semantische Absätze und Szenentrenner statt manueller Abstandstricks |
| App-Absturz beschädigt Buch | Atomare Speicheroperationen, autosave, Wiederherstellungsszenario |
| Bestehendes LaTeX lässt sich nicht verlustfrei importieren | Separater Migrationstest; kein stiller Ersatz des Ursprungsprojekts |

## 7. Verbindliche Entwicklungsreihenfolge auf Planebene

Der [Implementierungsplan](IMPLEMENTATION_PLAN.md) liegt als **vorläufige, durch Qualitäts-Gates abgesicherte Roadmap** bereits vor. Codex soll zunächst das Android-/GitHub-Actions-Grundgerüst (P0) herstellen, anschließend Bibliotheken, Lizenzbedingungen und mobile Eigenschaften überprüfen (P1) und eine kleine Ende-zu-Ende-Strecke aus Editieren → Speichern → A5-PDF testen. **Erst nach dokumentiertem P1-GO** wird die Auswahl der Editor- und Exporttechnik verbindlich und die davon abhängige Architektur ausgebaut. Die App darf nicht auf einer ungeprüften PDF-Ausgabe oder Touch-Bedienung aufgebaut werden. Der echte Smartphone-Test bleibt vor der finalen Veröffentlichung verpflichtend.
