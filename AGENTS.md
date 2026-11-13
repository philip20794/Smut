# Anweisungen für Codex und andere Entwicklungsagenten

## Pflichtlektüre und Wiederaufnahme bei jedem Start

1. [docs/PROGRESS.md](docs/PROGRESS.md) – **einzige operative Fortschrittsquelle**, enthält nächste Task-ID und Handoff.
2. [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md) – verbindlicher Aufgabenplan mit stabilen IDs, Abhängigkeiten und Gates.
3. [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) – Regeln zur Übernahme nach Abbruch oder Kontextverlust.
4. [README.md](README.md), [docs/PRODUCT.md](docs/PRODUCT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md), [docs/CI-CD.md](docs/CI-CD.md) – Anforderungen und Qualität.
5. [docs/DECISIONS.md](docs/DECISIONS.md) – getroffene und noch offene Architekturentscheidungen.
6. [docs/BUILD.md](docs/BUILD.md) – Projektstruktur, Toolchain-Pins, Buildbefehle und Sicherheitskonventionen.

**Vor jedem Weiterarbeiten:** Aktuellen `main`-Stand und die letzten relevanten Commits/CI-Runs prüfen. Einen möglicherweise veralteten Fortschrittstracker anhand von Code und Tests korrigieren. Danach die aktive oder nächste ausführbare Aufgabe übernehmen – nicht ohne Anlass den Gesamtplan neu schreiben.

## Grundregeln

- Entwicklung **vollständig ohne lokalen Computer** des Nutzers. GitHub Actions für Tests und Android-APK-Builds; Codemagic ist keine Voraussetzung.
- Änderungen auf `main` committen, sofern der Repositoryzustand das zulässt. Keine History-Rewrites oder Force-Pushes. CI-Fehler als sichtbare Blocker behandeln und beheben.
- CI und eine installierbare Test-APK bereits in P0 aufbauen; später jede Funktion mit passenden Unit-, Integrations- und ggf. Emulator-/UI-Tests absichern.
- Tests nicht deaktivieren, überspringen oder lockern, nur um einen Build grün zu bekommen. `DONE` nur nach belegter Prüfung.
- Keine Zugangsdaten oder Signaturschlüssel ins Repository committen; Release-Signierung nur über sichere Secrets und zusätzliche externe Sicherung.
- App vollständig offline benutzbar halten. Keine CDN-/Serverabhängigkeit für die Editorlaufzeit.
- Das Buch direkt in paginierten A5-Seiten editieren; **PDF ist Export, nicht die editierte Quelle**.
- Die Wahl des Editors und der PDF-Technik ist bis zum P1-Machbarkeitstest vorläufig; bei `NO-GO` Fallback untersuchen.
- Zwischen „implementiert“, „automatisch getestet“, „auf echtem Gerät getestet“ und „freigegeben“ unterscheiden.
- Echte manuelle Geräteabnahmen nur nach tatsächlichem Nutzerfeedback auf erledigt setzen.
- Bei Änderungen an Verhalten, Architektur oder Buildprozess die passenden Dokumente aktualisieren.
- Das Fortschrittsprotokoll nach jedem überprüfbaren Arbeitsschritt in Git sichern: aktive ID, Status, Commit/CI-Nachweis, Blocker, nächster Schritt.
- Bei unerwartetem Abbruch: nicht auf Chatkontext verlassen, sondern die Routine in `docs/DEVELOPMENT_WORKFLOW.md` anwenden.

## Arbeitsauftrag für folgende Implementierungssitzungen

Der **Implementierungsplan ist erstellt**. Der bisherige Projektstand ändert sich mit jeder Aufgabe. Starte stets mit der **aktuell** in `docs/PROGRESS.md` bezeichneten nächsten Aufgabe (nach F-06 und E-01: `E-02`), nicht mit einer statischen ID aus diesem Dokument, und arbeite schrittweise entlang der Abhängigkeiten.

Eine Dokumentation allein startet keine selbständig laufende Hintergrundarbeit. Setze in der tatsächlich laufenden Sitzung um, was mit den verfügbaren Werkzeugen machbar ist, und committe vor Sitzungsende eine präzise Übergabe.

Widersprüche oder technische Fehlschläge als konkrete Entscheidung bzw. Blocker dokumentieren; nicht stillschweigend Funktionen behaupten oder bereits erledigte Aufgaben erneut ausführen.
