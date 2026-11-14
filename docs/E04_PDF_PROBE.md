# E-04 – Offline-A5-PDF-Probe und Abnahme

**Stand: 14.11.2026. Status: E-04 DONE als Machbarkeits- und Architektur-Gate, nicht als Freigabe einer druckreifen Endkunden-PDF.**

## Reproduzierbare Ausführung

Quelle: [GitHub-Actions-Lauf 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300), [Commit dc47fd4](https://github.com/philip20794/Smut/commit/dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5).

1. Das vorübergehende Testmanuskript besteht aus 24 wiederholten deutschsprachigen Textabschnitten mit Äpfel/Öl/Grüße/Straße. Der echte persistierte Text wird vor dem Test gesichert und anschließend unverändert wiederhergestellt.
2. Der Android-WebView-Editor berechnet seine A5-Canvas-Seiten. `generatePdfProbe()` nimmt denselben `getValue()`-Snapshot einschließlich der Layoutoptionen, aber rendert im Papiermaßstab 1 statt im Bildschirmzoom.
3. Lokal gebündelte UMD-Module `jspdf.umd.min.js` und `canvas-editor-pdf.umd.js` ohne Netzwerk- oder CDN-Zugriff erzeugen die PDF. `loadDefaultFonts:false` verhindert den externen Font-Download.
4. Versionierte Android-Bridge validiert eine in Umfang/Format begrenzte Base64-PDF und schreibt atomar. Der Emulator öffnet diese Datei mit `PdfRenderer`, misst die erste A5-Seite und vergleicht die Seitenanzahl mit der Editoransicht.
5. Das Android-Instrumentierungstestprogramm kopiert die Datei vor dem Paketabbau über MediaStore nach Downloads. GitHub Actions liest die Datei per `adb pull` aus und prüft mit dem unabhängigen Python-`pypdf`-Parser das MediaBox, echte extrahierbare Textobjekte, deutsche Suchbegriffe und den Ausschluss rasterisierter Seiten.
6. Nur bei erfolgreichem gesamten Gate werden die PDF-Probe und die getestete Entwickler-APK als getrennte GitHub-Artefakte veröffentlicht.

## Messwerte aus dem grünen Lauf

| Kriterium | Ergebnis |
| --- | --- |
| Android-Tests | 13/13 erfolgreich (API 35, WLAN und Daten deaktiviert) |
| Editor-/PDF-Seitenanzahl | **3 / 3**, instrumentierter Gleichheitstest erfolgreich |
| PDF-Papiermaß | **419,25 × 595,50 pt** = etwa **147,90 × 210,08 mm** |
| A5-Nennmaß | 148 × 210 mm; Abweichung unter 0,1 mm pro Achse |
| PDF-Text je Seite | 1.405 / 1.347 / 367 extrahierte Zeichen |
| Testwörter | `Absatz`, `Äpfel`, `Öl`, `Grüße`, `Straße` in durchsuchbaren Textobjekten |
| Seite als Bild? | Nein, alle Seiten haben Font-Ressourcen und keine gerasterten Seitenbilder |
| Linker Textrand | x = **37,5 pt**; 50 CSS-px × 0,75 pt/px wie in den Editor-Margins |
| Seitenzahlen | Im gerenderten Beispiel auf allen drei Seiten sichtbar |
| Testartefakte | `smut-e04-a5-pdf-probe-dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5`, `smut-tested-dev-dc47fd4c75df96a6dd93304c7f67edf90e4a1bc5` |

## Sichtprüfung und Grenzen

Die dreiseitige PDF wurde zusätzlich als Bild gerendert und geprüft. Die Schriften und deutschen Umlaute sind sichtbar; die Seitennummerierung, A5-Geometrie und Seitenwechsel sind technisch vorhanden.

**Noch nicht druckfertig:** Die Probe hat teils übermäßig große Abstände im Blocksatz sowie Umbrüche mitten im deutschen Wort ohne Trennzeichen, etwa `rü` / `ckte`. Das Testdokument ist bewusst ein isolierter technischer Langtext, kein fertiges Kapitelmodell mit semantischen Absätzen. Aktuell nutzt jsPDF integrierte Type1-PDF-Standardschriften, **keine eingebetteten lizenzierten Buchfonts**. Einzelne Textfragmente sind beim PDF-Parser nicht perfekt kodiert; die automatisierte Suche testet mehrere repräsentative Zeichenfolgen, keine vollständige Glyphen-für-Glyphen-Identität. Die Übereinstimmung der **Seitenanzahl und des Satzrandes** ist belegt, nicht jedoch die exakte Lage jeder Textzeile oder jedes Buchstabens. Für eine Druckfreigabe braucht P5 L-01 bis L-04 zusätzliche Font-, Textfluss-/Silbentrennungs-, Zeilen-/Bildvergleichs- und Exporttests.

Die WebView ist vollständig offline; kein Android-Internetrecht. Die PDF-Testdatei landet nicht in der normalen Buchverwaltung und es gibt noch keine Benutzerschaltfläche zum PDF-Export. Ein echter Smartphone-Test wurde nicht durchgeführt (E-05).

## Architekturentscheid

[ADR-009](DECISIONS.md): **GO** für Canvas Editor 1.1.0 und canvas-editor-pdf 0.8.0 als Grundlage des P2-Dokumentmodells und der späteren PDF-Pipeline. Der nachweisbar funktionierende technische Pfad `Editor → Dokument-Snapshot → lokaler Text-PDF-Export` rechtfertigt Weiterentwicklung ohne Engine-Wechsel. **Kein GO für druckreife Endkunden-PDFs**; der P5-Qualitätsnachweis bleibt zwingend. Werden die sichtbaren Satz- oder Font-Probleme nicht beherrschbar, ist die Exporttechnik erneut zu bewerten.
