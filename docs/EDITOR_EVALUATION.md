# E-01 – Evaluation der Editor- und PDF-Technik

**Stand:** 13.11.2026. **Status:** dokumentierte Vorauswahl, keine endgültige Freigabe. Entscheidend sind die folgenden Android-Emulator- und späteren Gerätetests. Den P1-GO-Beschluss legt erst E-04 fest.

## Anforderungen / harte Ausschlüsse

- Direktes Tippen **in** einer paginierten, visuell drucknahen A5-Seite, nicht in einem Overlay-Textfeld und nicht in der PDF.
- Offline in der Android-WebView: JavaScript, CSS, Fonts und Exporttechnik vollständig in der APK. Keine CDN-/Cloud-/Token-Pflicht.
- Deutsche Unicode-Eingabe, Android-IME/Autokorrektur, Selektion, Seitenwechsel und Cursorstabilität.
- Textbasierte, durchsuchbare, geometrisch korrekte A5-PDF; identische Umbruchgeometrie in Editor und Export ist separat nachzuweisen.
- MIT-/Apache-kompatible Bibliotheken, exakte Paketversionen und Lizenzhinweise vor Einbindung.

## Kandidaten, Stand der veröffentlichten Pakete

| Kriterium | Canvas Editor | Tiptap/ProseMirror | Compose-Textfeld mit eigener Buchsatzengine |
| --- | --- | --- | --- |
| Paket für Prototyp | `@hufe921/canvas-editor@1.1.0` (npm, 13.11.2026) | `@tiptap/core@3.33.1` plus passendes StarterKit, exakt zu pinnen | AndroidX Compose aus bestehendem Android-Modul |
| Lizenz | MIT (Projekt und npm) | MIT für Open-Source-Core; **Pages nicht mit MIT-Core verwechseln** | AndroidX unter Apache 2.0 |
| Offline im APK | Ja, bei lokalem Bundle; Fonts gezielt einbinden | Ja für Open-Source-Core; keinen externen Dienst voraussetzen | Ja |
| Direktbearbeitung | Eigener Cursor-/Canvas-Renderer, eingebauter Seitenumbruch | DOM/contenteditable, bewährter ProseMirror-Transaktionskern | Native IME, aber eigener Renderer/Seitenwechsel nötig |
| A5-Seiten | Engine unterstützt Papierformate, Ränder, Seitenumbrüche | Open-Source-Core ohne integrierte Buchseiten; offizielle Pages-Erweiterung ist Team-Plan/Pro-Registry | Vollständig selbst zu entwickeln |
| PDF | `canvas-editor-pdf@0.8.0` (MIT, jsPDF/Context2d; separates Projekt) als zu testender Kandidat | Offizieller PDF/Conversion-Weg ist Pro/mit Token/REST; eigene lokale Exportpipeline erforderlich | Eigener PDF-Renderer erforderlich |
| Android-Touch/IME | **Offen:** Canvas-IME, Langdruck und Seitenwechsel müssen praktisch getestet werden | **Offen:** WebView-IME günstige Ausgangslage durch DOM, aber Selektion über optisch getrennte Seiten schwierig | Native Eingabe eher robust; individuelle Paginierung teuer |
| Reife / Pflege | Aktives 1.x-Projekt; eigene Text-/Paginierungsengine | Aktiver, weit verbreiteter Editor; große Extension-Ökonomie | Bestehender Android-Stack, aber viel eigene komplexe Logik |

**Versionsquellen (zum Zeitpunkt der Bewertung):** [Canvas Editor npm](https://www.npmjs.com/package/@hufe921/canvas-editor), [Canvas Editor Hauptrepo/Lizenz](https://github.com/Hufe921/canvas-editor), [Tiptap Core npm](https://www.npmjs.com/package/@tiptap/core), [Tiptap Lizenz](https://github.com/ueberdosis/tiptap/blob/main/LICENSE.md), [PDF-Exporter npm](https://www.npmjs.com/package/canvas-editor-pdf), [PDF-Exporter Repo/Lizenz](https://github.com/douglasmatheus/canvas-editor-pdf).

## Spezifische PDF- und Lizenzrisiken

1. `canvas-editor-pdf` zeichnet laut Projektbeschreibung über jsPDF-`Context2d` PDF-Anweisungen statt Seitenscreenshots. **Daraus folgt noch nicht**, dass deutsche Zeichen als suchbarer Unicode-Text erhalten bleiben oder Font-Einbettung und Layout mit der Android-WebView übereinstimmen. A5-Geometrie, PDF-Textanalyse, Schriften, mehrseitige Absätze und Auswahleigenschaften sind zwingend im E-04-Test zu prüfen.
2. Der PDF-Exporter verwendet bei `loadDefaultFonts: true` im **Browser standardmäßig ein CDN**. Diese Einstellung ist für Smut verboten. Lokale, separat lizenzierte Fonts müssen gebündelt und zur Laufzeit ohne Netzwerk geladen werden; die Lizenz der im Projektbeispiel genannten proprietären Schriftfamilien ist **nicht** pauschal auf Smut übertragbar.
3. Die offizielle [Tiptap-Pages-Erweiterung](https://tiptap.dev/docs/pages/getting-started/overview) liegt im Team-Plan und einer privaten Registry; [Tiptap Conversion](https://tiptap.dev/docs/pages/getting-started/install) benötigt Token/Backend für Export. Deshalb keine Basis für eine kostenlos vollständig offline laufende Smut-v1. Open-Source-Tiptap mit eigener Paginierung ist ein anderer, größerer Lösungsweg.
4. Die PDF-Exporter-Version und ihr Lockfile müssen vor Einbau gegen den tatsächlich publizierten Package-Tarball sowie die Canvas-Editor-Peer-Range (`>=0.9.133 <2.0.0` im Repository) kontrolliert werden. Nicht allein auf GitHub-`main` verlassen.
5. Keine Unklarheit durch einen Render-Test verschleiern: native IME-Vorschläge, Mehrzeilenselektion und Layoutgleichheit brauchen eigene Emulatortests und reale Smartphone-Abnahme.

## Vorläufige Arbeitswahl für E-02

**Canvas Editor als erster Prototyp**, weil A5-Paginierung und Editieren bereits Bestandteil derselben Engine sind. Das ist **keine finale Architekturfreigabe**. Im E-02-Test muss die Android-IME direkt im Canvas-Editor erscheinen und dort echte Zeichen verändern; ein vorgeschaltetes Textfeld als Demo ist nicht akzeptabel.

**Frühe Abbruch-/Fallback-Kriterien:** falls der Canvas-Editor auf dem API-35-Emulator bei Tap/Backspace/Enter/deutschen Umlauten keine stabile Eingabe ermöglicht, zunächst Tiptap/ProseMirror mit dokumentiertem Seitenlayout-Fallback untersuchen. Keinen großen Funktionsausbau auf Basis eines defekten IME-Prototyps beginnen.

## E-02- bis E-04-Testmatrix

| Nachweis | Erwartung | Status |
| --- | --- | --- |
| Eingabe an getippter Zeichenposition | Sichtbarer Cursor und echte Editoränderung | Offen E-02 |
| Deutsch/Unicode (ä ö ü ß „ “ —), IME, Backspace, Enter | Keine verlorenen oder vertauschten Zeichen | Offen E-02 |
| Speichern und Neustart (lokale Datei/Preferences vorläufig) | Dokument und Position nachvollziehbar wiederhergestellt | Offen E-02 |
| Langdruck, Auswahl, Ersetzen, Cross-Page und Zoom | Ohne falschen Cursoranker oder unerwartete Sprünge | Offen E-03 |
| A5-PDF 148 × 210 mm, mehrseitig, Textauswahl/Suche | Parser und unabhängiger PDF-Viewer bestehen | Offen E-04 |
| Seitenanzahl und Umbruchvergleich | Gemessene und dokumentierte Toleranzen | Offen E-04 |
| Offline/Font-Laden ohne Netzwerk | Test durch deaktiviertes WLAN/Daten und APK-Assetprüfung | Offen |
| Realer Android-Gerätetest | Ausdrückliches Feedback des Nutzers, nie automatisch | Offen E-05 |

Die Versionen sind Bewertungsstände, **noch keine hinzugefügten Abhängigkeiten**. Vor E-02 werden npm-Lockfile, Paketgröße und Browser-Kompatibilität durch CI verifiziert.
