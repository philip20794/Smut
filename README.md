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

**P0 (F-01 bis F-06) und E-01 bis E-03 sind abgeschlossen.** Android-App und Offline-TypeScript-Container bestehen Editor-/Android-Checks und einen instrumentierten Emulator-Smoke. A5-Canvas-Seiten, Android-Tastatur, Wortmarkierung per Langdruck, native Auswahl über zwei Seiten, Rand-Autoscroll, Zoom/IME/Cursor-Verhalten und zwei verschiebbare Android-Auswahlgriffe sind automatisiert getestet. [CI 43574312605](https://github.com/philip20794/Smut/actions/runs/43574312605) ist vollständig grün und stellt eine geprüfte Entwickler-APK bereit. E-04 ist durch eine [mehrseitige textbasierte A5-PDF-Probe](docs/E04_PDF_PROBE.md), die vollständige [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300) und [ADR-009](docs/DECISIONS.md) mit eingeschränktem P1-GO abgeschlossen. E-05 bleibt eine gesonderte echte Geräteabnahme; als nächstes D-01 (versioniertes Buch-/Kapitelmodell). Die PDF-Probe ist noch keine druckreife Exportfunktion. Einstieg für Codex: [Fortschritt und nächste Aufgabe](docs/PROGRESS.md). Der [Implementierungsplan](docs/IMPLEMENTATION_PLAN.md) umfasst 35 einzeln prüfbare Arbeitspakete, beginnend mit `F-01`. Nach jeder Entwicklungsrunde werden Commits, Tests, Blocker und der nächste Schritt im Tracker festgehalten, damit ein neuer Agent die Arbeit fortsetzen kann.

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

Der Implementierungsplan ist vorhanden. Unbestätigte technische Annahmen bleiben bis zum P1-Machbarkeitsnachweis als Prüfaufgaben gekennzeichnet. Ein Android-Grundprojekt, der offizielle Gradle Wrapper, ein Unit-Test und ein erster erfolgreicher [GitHub-Actions-Build](https://github.com/philip20794/Smut/actions/runs/43441065967) sind vorhanden. Der TypeScript-Container, die Pflicht-CI und der Emulator-Smoke sind vorhanden. Ein direkt editierbarer A5-Prototyp mit lokalem Speichern, getesteter Wortauswahl und mobilen Auswahlgriffen ist vorhanden. Vollständige Buch-/Kapitelverwaltung, anwenderseitiger druckfertiger PDF-Export und signierte Releases stehen noch aus; eine technisch geprüfte textbasierte A5-PDF-Testprobe liegt vor.

## Android-Test-APK

Nach dem vollständigen Erfolg aller Pflichtchecks erscheint unter [Smut GitHub Actions](https://github.com/philip20794/Smut/actions) ein `smut-tested-dev-<Commit-ID>`-Artefakt. Eine Anleitung für Android ohne lokalen PC steht in [docs/APK_DOWNLOAD.md](docs/APK_DOWNLOAD.md). **Der Lauf [43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955) enthält die erste nach allen Gates geprüfte APK.** Die letzte geprüfte Entwickler-APK und die dreiseitige PDF-Testprobe liegen unter [CI 43578985300](https://github.com/philip20794/Smut/actions/runs/43578985300). Die App ist weiterhin keine fertige Buchverwaltung.

