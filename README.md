# Smut – Android-Bucheditor

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

Die Entwicklung läuft direkt im Repository. GitHub Actions prüft bei jeder Änderung TypeScript, Kotlin, Android-Lint und den instrumentierten Emulator-Smoke; nach vollständigem Erfolg wird eine Debug-Test-APK als Artefakt bereitgestellt. Erfolgreiche Entwicklungs-APKs stehen temporär unter **Actions** bereit; signierte Versionen werden unter **Releases** veröffentlicht. Ein eigener Rechner oder Codemagic sind nicht erforderlich. Signierung und dauerhafte Installierbarkeit von Updates sind in [docs/CI-CD.md](docs/CI-CD.md) spezifiziert. **Die CI ist vorhanden; die Signier-/Release-Pipeline folgt später.**

## Implementierung und Projektstand

**P0, P1 E-01 bis E-04, P2 und P3 B-01/B-02 sind abgeschlossen (16/35 Aufgaben, 45,7 %).** Der Offline-Canvas-Prototyp unterstützt die A5-Direktbearbeitung mit Android-Tastatur, Auswahlgriffen und Zoom. D-01/D-02 implementieren das strukturierte Buchmodell mit Editieroperationen und Undo/Redo. D-03 ergänzt [Room-/AtomicFile-Speicherung mit Recovery](docs/D03_PERSISTENCE.md), D-04 den [portablen JSON-Backup-/Importweg](docs/D04_BACKUP.md) über Android Storage Access Framework mit SHA-256 und Konfliktschutz. Die [vollständige CI 43597289709](https://github.com/philip20794/Smut/actions/runs/43597289709) prüft neben der Bücherliste jetzt Kapitel-Anlage, Umbenennen, Umsortieren, Löschen mit Bestätigung, kapitelgenaue Navigation, Revisionskonflikte, Unicode-Cursoranker und Wiederöffnung. Geprüfte Entwickler-APK vorhanden.

**Wichtige Grenze:** Die sichtbare A5-Prototypseite verwendet weiterhin die alte E-02-Canvas-Datei; D-04 sichert zunächst **nur strukturierte Bücher**. Die endgültige Editorintegration samt Migration erfolgt in W-01. Die Buchverwaltung B-01 ist abgeschlossen: die App startet mit einer Compose-Bücherliste; ein strukturiertes Buch öffnet sich zunächst als Leseansicht. B-02 ist abgeschlossen: Kapitel lassen sich in der nativen Leseansicht verwalten und gezielt öffnen, aber noch nicht direkt im A5-Editor bearbeiten. B-03 (letzte Position und Minimalnavigation) folgt; E-05 erfordert einen echten Smartphone-Test. Die technisch geprüfte A5-PDF-Probe ist noch kein druckfertiger Export. [Implementierungsplan](docs/IMPLEMENTATION_PLAN.md) und [operative Übergabe](docs/PROGRESS.md) führen alle 35 Aufgaben.

## Dokumentation

- [Produkt- und Bedienkonzept](docs/PRODUCT.md)
- [Technische Architektur und Machbarkeit](docs/ARCHITECTURE.md)
- [Akzeptanzkriterien und Testfälle](docs/ACCEPTANCE.md)
- [GitHub Actions, Tests und APK-Veröffentlichung](docs/CI-CD.md)
- [Anweisungen für Codex](AGENTS.md)
- [Implementierungsplan – Meilensteine und Aufgaben](docs/IMPLEMENTATION_PLAN.md)
- [Fortschrittstracker und Übergabe](docs/PROGRESS.md)
- [Entwicklungs- und Wiederaufnahmeprotokoll](docs/DEVELOPMENT_WORKFLOW.md)
- [Technische Entscheidungen](docs/DECISIONS.md)
- [Projektstruktur, Toolchains und Buildkonventionen](docs/BUILD.md)
- [APK-Download und Installation am Android-Smartphone](docs/APK_DOWNLOAD.md)
- [Technologievergleich und E-01-Evaluation](docs/EDITOR_EVALUATION.md)
- [D-03: Speicherung, Autosave und Recovery](docs/D03_PERSISTENCE.md)
- [D-04: Portables Buch-Backup und sicherer Import](docs/D04_BACKUP.md)
- [B-01: Bücherliste, Datenoperationen und Abnahme](docs/B01_BOOK_LIBRARY.md)
- [B-02: Kapitelverwaltung, Navigation und Tests](docs/B02_CHAPTER_MANAGEMENT.md)

Der Implementierungsplan ist vorhanden. Unbestätigte technische Annahmen bleiben bis zum P1-Machbarkeitsnachweis als Prüfaufgaben gekennzeichnet. Ein Android-Grundprojekt, der offizielle Gradle Wrapper, ein Unit-Test und ein erster erfolgreicher [GitHub-Actions-Build](https://github.com/philip20794/Smut/actions/runs/43441065967) sind vorhanden. Der TypeScript-Container, die Pflicht-CI und der Emulator-Smoke sind vorhanden. Ein direkt editierbarer A5-Prototyp mit lokalem Speichern, getesteter Wortauswahl und mobilen Auswahlgriffen ist vorhanden. Letzte Positions-/Zurück-Navigation, direkte Bearbeitung des neuen Buchmodells, anwenderseitiger druckfertiger PDF-Export und signierte Releases stehen noch aus; Backup/Import des strukturierten Buchmodells und eine textbasierte A5-PDF-Testprobe sind technisch geprüft.

## Android-Test-APK

Nach dem vollständigen Erfolg aller Pflichtchecks erscheint unter [Smut GitHub Actions](https://github.com/philip20794/Smut/actions) ein `smut-tested-dev-<Commit-ID>`-Artefakt. Eine Anleitung für Android ohne lokalen PC steht in [docs/APK_DOWNLOAD.md](docs/APK_DOWNLOAD.md). **Der Lauf [43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955) enthält die erste nach allen Gates geprüfte APK.** Die neueste geprüfte Entwickler-APK mit B-02 und die A5-PDF-Testprobe liegen unter [CI 43597289709](https://github.com/philip20794/Smut/actions/runs/43597289709). D-02 ist mit Unicode-, Auswahl- und Undo/Redo-Tests in [dieser CI](https://github.com/philip20794/Smut/actions/runs/43585173130) abgenommen; die Android-Oberfläche nutzt noch den bisherigen Prototyp. Bücherliste und Kapitelverwaltung sind funktionsfähig; B-03 ergänzt die Minimalnavigation, W-01 die Anbindung an den editierbaren A5-Bereich.

