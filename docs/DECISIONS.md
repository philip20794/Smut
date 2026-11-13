# Architekturentscheidungen (ADR-Register)

Hier werden langlebige technische Entscheidungen festgehalten. Änderungen nicht stillschweigend überschreiben, sondern mit Grund und Datum nachtragen. Der Fortschrittsstatus von Implementierungsaufgaben steht ausschließlich in [PROGRESS.md](PROGRESS.md).

## ADR-001 – Editierbares Dokument statt PDF als Quelle

- **Datum:** 2026-11-13
- **Status:** angenommen
- **Entscheidung:** Das Buch wird als strukturiertes, versioniertes Dokument gespeichert. Der Nutzer bearbeitet es direkt in einer drucknahen Seitenansicht. Eine PDF ist ein Exportergebnis, kein editierbarer Primärspeicher.
- **Grund:** Eine PDF besitzt keine verlässlich rückwärts abbildbare Kapitel-, Absatz- und Inlineformatierungsstruktur. Kontinuierliches Tippen erfordert einen echten Dokumenteditor.
- **Folgen:** Export-PDF muss Layout und Text der Editoransicht möglichst genau abbilden. Beliebige bestehende PDFs sind nicht Bestandteil der v1-Editierbarkeit.

## ADR-002 – Android-Technologie (vorläufig)

- **Datum:** 2026-11-13
- **Status:** vorgeschlagen, **noch nicht bestätigt**
- **Vorschlag:** Kotlin + Jetpack Compose für die Android-Hülle, lokale WebView mit einem TypeScript-Editor für die Buchseite. `@hufe921/canvas-editor` ist der erste Kandidat, ProseMirror/Tiptap ein Fallback.
- **Offen:** Android-IME, Touch-Cursor, längere Selektion, A5-Seitenfluss, Offlinebetrieb, Wartung/Lizenz sowie PDF-Export mit echtem Text.
- **Entscheidungspunkt:** E-04 dokumentiert `GO`/`NO-GO`, Testergebnisse und die gewählte Kombination. Bis dahin keine irreversible Architekturbindung.

## ADR-003 – Entwicklung und APK-Auslieferung ausschließlich über GitHub

- **Datum:** 2026-11-13
- **Status:** angenommen
- **Entscheidung:** GitHub Actions führt Builds und automatisierte Tests auf gehosteten Runnern aus. Entwicklungs-APKs als Actions Artifacts, signierte Versionen als GitHub Releases. Kein zwingender lokaler Rechner und kein Codemagic.
- **Folgen:** CI früh in P0 aufbauen. Android-Emulator auf Linux-Runnern testen; Toolchains/Versionen fixieren. GitHub Actions selbst behebt keine Fehler.
- **Referenz:** [android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner) als möglicher Instrumentation-Test-Runner (vor Einsatz im Projekt prüfen).

## ADR-004 – Nachweisorientiertes Fortschritts-Tracking

- **Datum:** 2026-11-13
- **Status:** angenommen
- **Entscheidung:** [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) enthält stabile Task-IDs und Anforderungen; [PROGRESS.md](PROGRESS.md) den einzigen laufenden Status und das Übergabeprotokoll.
- **Folgen:** `DONE` nur mit verifizierten Commits/Tests/CI. Nach Kontextverlust gegen Git und CI abgleichen. Echte Smartphone-Abnahmen werden gesondert markiert.

## ADR-005 – Releases erfordern abgesicherte Android-Signierung

- **Datum:** 2026-11-13
- **Status:** angenommen als Anforderung, Einrichtung ausstehend
- **Entscheidung:** Eine gleichbleibende Signatur und Paketkennung sind für direkt installierbare App-Updates notwendig. Secrets und private Keystores dürfen niemals ins Repository.
- **Folgen:** R-01 klärt Schlüsselerzeugung, sichere GitHub-Secret-Bereitstellung, externe Sicherung und Update-Verifikation. Ohne diesen Nachweis keine v1-Veröffentlichung.
