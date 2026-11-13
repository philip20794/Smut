# Produkt- und Bedienkonzept

## 1. Ziel und Zielgruppe

Die App dient dem Schreiben und Überarbeiten längerer Prosatexte auf einem Android-Smartphone. Sie vereint eine ruhige, buchartige Darstellung mit der unmittelbaren Bearbeitbarkeit eines einfachen Texteditors. Die Oberfläche zeigt ausschließlich Bedienelemente, die zur aktuellen Aktion gehören.

## 2. Startbildschirm

- Titel und Liste der Bücher mit letztem Bearbeitungsdatum.
- Tippen öffnet das Buch an der zuletzt verwendeten Textposition.
- Eine deutlich erkennbare Aktion „Neues Buch“.
- Über das Kontextmenü: Umbenennen, Duplizieren, Backup exportieren, Löschen mit Bestätigung.
- Kein Dashboard, keine Registrierung, keine Vorlagen-Galerie als Pflichtschritt.

## 3. Buchansicht

- Standardmäßig **A5 (148 × 210 mm), Hochformat**.
- Weißes oder papierfarbenes Blatt auf unaufdringlichem Hintergrund, vertikales Scrollen durch die Seiten.
- Kapitelüberschriften, Absätze, optionale Seitenzahlen und sichtbare Seitenabstände.
- Über Zoomgeste oder einfache Zoomsteuerung skalierbar.
- Beim Öffnen Wiederherstellung von Buch, Kapitel, Cursorposition und sinnvoller Scrollposition.
- Minimale obere Leiste: Zurück, Buchtitel, Rückgängig, Menü.
- Formatwerkzeuge werden als kompakte untere oder kontextuelle Leiste eingeblendet, wenn sie gebraucht werden.

## 4. Direktes Schreiben

- Ein kurzes Tippen setzt den Cursor an eine plausible Zeichenposition, die Bildschirmtastatur öffnet sich.
- Eingabe verändert unmittelbar den Text auf der Seite. Neu-Umbruch geschieht laufend, ohne PDF-Neuberechnung oder Ladeanimation nach jedem Satz.
- Die App hält die Einfügestelle trotz Tastatur, Zoom und Seitenwechsel sichtbar; kein Springen an den Kapitelanfang.
- Enter = neuer Absatz. Ein separater Befehl erlaubt einen weichen Zeilenumbruch.
- Der Szenentrenner wird als semantischer Block angelegt, statt mit mehreren leeren Zeilen simuliert.
- Beim Löschen am Anfang eines Absatzes sollen Absätze erwartungsgemäß zusammengeführt werden.

## 5. Auswahl und Formatierung

- Langes Drücken markiert ein Wort, Auswahlgriffe erlauben das Erweitern des Bereichs.
- Markierte Bereiche können gelöscht, ersetzt, kopiert oder formatiert werden.
- Formatierungsleiste: Fett, Kursiv, Schriftgröße/Schriftfamilie und Absatzstil; weitere Details in einem schlanken Menü.
- Ohne Markierung gelten Blockbefehle für den aktuellen Absatz; Zeichenformatierungen für neue Eingaben oder die Auswahl.
- Rückgängig/Wiederholen müssen auch mehrere Eingaben, Formatierung und Änderungen an Absatzgrenzen sauber behandeln.

## 6. Buchstruktur und Layout

- Ein Buch besteht aus Metadaten und geordneten Kapiteln. Ein Kapitel besteht aus semantischen Textblöcken mit Zeichenformatierungen.
- Standard: A5, gut lesbare Serifenschrift für den Buchtext; Standardschrift ist durch eine mitgelieferte, entsprechend lizenzierte Schrift zu realisieren.
- Kapitel beginnen bei Bedarf auf einer neuen Seite; spezielle Einleitung/Titelblatt-Layouts sind später erweiterbar.
- Konfigurierbar: Grundschrift, Grundgröße, Ränder inklusive möglichem Bundsteg, Zeilenabstand und Absatzabstände/-einzüge, Seitenzahlen.
- Unterscheiden zwischen echtem Absatz, weichem Umbruch, Szenenwechsel und Seitenwechsel.
- Typografische Tests sollen deutsche Anführungszeichen, Umlaute, Gedankenstriche, Silbentrennung sowie deutsche Trennregeln umfassen.

## 7. Speicherung und Export

- Jeder Editiervorgang aktualisiert ein strukturiertes, editierbares Quelldokument. Dieses ist die einzige maßgebliche Datenquelle.
- Häufige automatische Zwischenspeicherung, atomare Dateischreibvorgänge und Wiederherstellung nach ungeplantem Beenden.
- Export als PDF in A5 und wahlweise als vollständiges Backup des editierbaren Buchprojekts.
- PDF muss durchsuchbaren und markierbaren Text enthalten. Bilder-PDFs sind kein akzeptabler Ersatz.
- Nachträgliche Bearbeitung erfolgt durch Öffnen des gespeicherten Buchprojekts, nicht durch erneutes Importieren der PDF.

## 8. Import bisheriger Bücher

- Die Anwendung soll perspektivisch Kapitel aus TXT, Markdown oder DOCX übernehmen können.
- Die Übernahme bestehender LaTeX-Buchprojekte ist ein eigener Importpfad und nicht automatisch verlustfrei; eigene Makros und Struktur müssen untersucht werden.
- Eine vorhandene PDF darf als Anzeige/Referenz dienen, aber ein PDF-Import ist **keine Zusage**, dass diese inhaltlich fehlerfrei wieder editierbar wird.
- Für ein bestehendes LaTeX-Buch ist zunächst eine Migration der Kapiteltexte und ausgewählter Formatierungen vorgesehen; das Originalprojekt bleibt unangetastet.

## 9. Qualitätsanforderungen

- Ruhige, reduzierte Oberfläche ohne sichtbaren Quellcode.
- Keine Pflichtregistrierung und keine Serverabhängigkeit.
- Flüssige Tastatureingabe auch bei längeren Romantexten.
- Accessibility: ausreichend große Touchflächen, dynamische Android-Schriftgrößen für die Bedienelemente, verständliche Beschriftungen.
- Eine fehlerhafte PDF-Erzeugung darf die bearbeitbare Buchdatei niemals beschädigen.
