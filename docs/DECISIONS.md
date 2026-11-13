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

## ADR-006 – F-01: konservative, versionierte Android-/Node-Baseline

- **Datum:** 2026-11-13
- **Status:** vorläufig für den ersten Build festgelegt; durch `F-02` zu validieren
- **Entscheidung:** Android-Quellmodule unter `app/`, WebView-Editor unter `editor/`; Gradle-Plugin-Versionen zentral in `gradle/libs.versions.toml`; Node in `.node-version`. Ausgangsstack: AGP 8.13.2, Gradle 8.13, JDK 17, Kotlin/Compose-Compiler 2.2.21, `compileSdk/targetSdk = 36`, `minSdk = 26`, Node 24.22.3 LTS, npm. Für TypeScript ist 5.9.3 ein bis F-03 zu prüfender Startkandidat.
- **Android-ID:** `io.github.philip20794.smut`, Entwicklerbuild später `io.github.philip20794.smut.dev`, Anzeigename Smut.
- **Warum:** AGP 8.13 bietet eine dokumentierte Kombination aus Gradle 8.13 und JDK 17 sowie Unterstützung für API 36. Der explizite Kotlin-Compose-Compiler-Plugin-Ansatz reduziert Wechselwirkungen mit AGP-9-Built-in-Kotlin-Migrationen im ersten Projektgerüst. Node 24 ist eine unterstützte LTS-Reihe. Diese Baseline priorisiert Vorhersagbarkeit gegenüber ungeprüftem Aktualisieren auf die neuesten Plugin-Major-Versionen.
- **Prüfung:** Die konkreten Gradle-/Android-/Compose-Versionen sind noch **nicht gebaut oder getestet**. F-02 muss sie in einem tatsächlichen Android-Build verifizieren oder mit dokumentiertem Grund als zusammenpassende Gruppe korrigieren. F-03 fixiert eine getestete TypeScript-Version durch `package.json` und Lockdatei.
- **Lizenz:** Für das Repository selbst wurde noch keine Lizenz vom Eigentümer festgelegt; deswegen keine beliebige `LICENSE` erzeugen. Lizenzen eingebundener Bibliotheken und Schriftarten werden vor Integration separat dokumentiert.
- **Folgen:** Gradle Wrapper samt Wrapper-JAR in F-02 einchecken, Lockdatei in F-03; Secrets/APKs bleiben aus dem Git-Quellbaum. Editor- und PDF-Bibliotheksentscheid bleiben bis E-04 offen.
- **Dokumentation:** [docs/BUILD.md](BUILD.md).
- **Offizielle Kompatibilitätsquellen:** [AGP 8.13](https://developer.android.com/build/releases/agp-8-13-0-release-notes), [Kotlin Releases](https://kotlinlang.org/docs/releases.html), [Node 24 LTS](https://nodejs.org/en/blog/release/v24.22.3).
