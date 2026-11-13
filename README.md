# BuchEditor – Android-Bucheditor

## Ziel

Eine schlanke, vollständig offline nutzbare Android-App, in der Bücher direkt in ihrer späteren Seitendarstellung geschrieben und bearbeitet werden können. Die Oberfläche soll so einfach wie möglich sein: Buch wählen, Text antippen, schreiben. Der Nutzer muss weder LaTeX-, HTML- noch Markdown-Code kennen.

Die Arbeitsansicht ist **ein editierbares Buchlayout**, keine bereits gerenderte PDF. Aus demselben Dokument und denselben Layouteinstellungen wird beim Export eine druckfähige, durchsuchbare PDF erzeugt. Eine häufige Neukompilierung während des Schreibens ist nicht vorgesehen.

## Leitprinzipien

- Direkte Bearbeitung im Seitenlayout ohne modale Texteingabe im Standardfall.
- A5-Hochformat als Standard, realistische Seitenränder und Seitenumbrüche.
- Wenige Bedienelemente, für Touch und Android-Tastaturen optimiert.
- Alle Bücher und Schriftarten lokal, ohne Konto, Internet, Werbung oder externe Server.
- Automatische, robuste Speicherung; Rückgängig/Wiederholen auch nach versehentlichen Änderungen.
- PDF ist ein **Exportformat**, nicht das bearbeitbare Datenformat.

## Kernablauf

1. App starten und Buch aus einer übersichtlichen Bücherliste wählen oder ein neues Buch anlegen.
2. Das Buch öffnet sich sofort an der zuletzt bearbeiteten Stelle im A5-Seitenlayout.
3. Text antippen, Cursor setzen und unmittelbar mit der Bildschirmtastatur schreiben.
4. Text durch langes Drücken markieren; per kleiner Kontextleiste formatieren, ersetzen, kopieren oder löschen.
5. Mit Enter einen neuen Absatz anlegen. Ein Szenentrenner und ein manueller Seitenumbruch sind explizite Befehle, nicht wiederholte Leerzeilen.
6. Änderungen werden automatisch lokal gespeichert; Seiten fließen während des Schreibens neu um.
7. Über das Menü eine druckfertige PDF erzeugen, ansehen oder teilen.

## Für die erste Version vorgesehen

- Bücher erstellen, öffnen, umbenennen, duplizieren, löschen (mit Bestätigung).
- Kapitel anlegen, umbenennen, verschieben und über eine kompakte Kapitelübersicht anspringen.
- Direktes Schreiben, Auswahl von Wörtern und Textbereichen, Ausschneiden/Kopieren/Einfügen, Suchen/Ersetzen.
- Absatzwechsel, weicher Zeilenumbruch, Szenentrenner, manueller Seitenumbruch.
- Rückgängig/Wiederholen, automatisches Speichern und Wiederherstellung nach App-Abbruch.
- Fett, Kursiv, Schriftfamilie, Schriftgröße und Absatzstil (Normaltext, Kapitelüberschrift, Zwischenüberschrift).
- A5-Layout mit einstellbaren Rändern, Zeilenabstand und Seitenzahlen.
- Vergrößern/Verkleinern sowie automatisches Sichtbarhalten des Cursors bei geöffneter Tastatur.
- PDF-Export mit möglichst identischer Paginierung zur Arbeitsansicht und als echtem Text (durchsuchbar/markierbar), nicht als Screenshot.
- Lokales Datenformat mit exportierbarem Buch-Backup.

## Nicht Teil der ersten Version

- Cloud-Anmeldung, gemeinsame Live-Bearbeitung, Abos oder Werbung.
- Bearbeitung beliebiger fremder PDFs, da dort die ursprüngliche Dokumentstruktur in der Regel fehlt.
- Verlustfreie automatische Übernahme jedes LaTeX-Makros oder bestehender LaTeX-Projekte.
- Komplexe wissenschaftliche Formeln, Fußnoten und umfangreiche Tabellen.

## Technologieentscheidung

**Vorgeschlagener Ansatz:** Android mit Kotlin und Jetpack Compose für die App-Hülle; ein lokal gebündelter TypeScript-Editor in einer Android-WebView für die Buchseiten.

**Bevorzugter Editor-Kandidat:** `@hufe921/canvas-editor` (MIT). Er unterstützt seitengenaue Layouts und Textbearbeitung. PDF-Export kann mit `canvas-editor-pdf` untersucht werden. Diese Bibliothekswahl ist **vorläufig**, bis ein Android-Prototyp die kritischen Anforderungen erfüllt.

**Alternative:** Tiptap/ProseMirror mit selbst entwickelter oder freier Seitendarstellung, falls Touch-Eingaben oder die PDF-Fidelity mit Canvas Editor nicht überzeugen. Keine kostenpflichtige Tiptap-Pages-Erweiterung als zwingende Voraussetzung.

## Ohne lokalen Computer entwickeln und testen

Codex entwickelt direkt im Repository. GitHub Actions soll ab dem ersten lauffähigen Projektgerüst bei jeder Änderung automatisch Codeprüfungen, Unit- und Android-Emulatortests sowie einen APK-Build ausführen. Erfolgreiche Entwicklungs-APKs stehen temporär unter **Actions** bereit; signierte Versionen werden unter **Releases** veröffentlicht. Ein eigener Rechner oder Codemagic sind nicht erforderlich. Signierung und dauerhafte Installierbarkeit von Updates sind in [docs/CI-CD.md](docs/CI-CD.md) spezifiziert. **Die CI/CD-Workflows müssen erst noch implementiert werden.**

## Dokumentation

- [Produkt- und Bedienkonzept](docs/PRODUCT.md)
- [Technische Architektur und Machbarkeit](docs/ARCHITECTURE.md)
- [Akzeptanzkriterien und Testfälle](docs/ACCEPTANCE.md)
- [GitHub Actions, Tests und APK-Veröffentlichung](docs/CI-CD.md)
- [Anweisungen für Codex](AGENTS.md)

Diese Unterlagen sind **Anforderungen für einen späteren Codex-Implementierungsplan**, noch kein Implementierungsplan. Unbestätigte technische Annahmen sind ausdrücklich als Prüfaufgaben gekennzeichnet.
