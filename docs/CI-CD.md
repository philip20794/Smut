# GitHub-only Entwicklung, Tests und APK-Auslieferung

## Ziel

Die gesamte Entwicklung von **Smut** soll ohne lokalen Entwicklungsrechner möglich sein. Codex erstellt Code im GitHub-Repository. GitHub Actions kompiliert, testet und veröffentlicht installierbare Android-APKs. Der Nutzer kann auf dem Android-Smartphone den Buildstatus lesen, Testberichte prüfen, eine APK herunterladen, installieren und Feedback geben.

**Keine Pflicht für Codemagic, einen lokalen PC oder einen externen Buildserver.** Eine Internetverbindung ist nur für GitHub/Entwicklung/Downloads nötig, nicht für die eigentliche Nutzung der App.

Diese Datei definiert die **später zu implementierende CI/CD-Infrastruktur**. Im aktuellen Dokumentationszustand sind noch keine Android-App und keine CI-Jobs vorhanden.

## 1. Plattform und Werkzeuge

- GitHub als Source of Truth.
- GitHub Actions auf gehostetem Ubuntu/Linux für alle regulären Builds und Tests.
- JDK, Android SDK, Gradle Wrapper und Node-Toolchain (für TypeScript-Editor) werden von Workflows versionskontrolliert aufgesetzt.
- Android-Emulator auf GitHub Actions, z. B. `ReactiveCircus/android-emulator-runner`; Hardwarebeschleunigung auf dem Runner prüfen.
- Gradle/Kotlin für Android; npm/pnpm für den Editor (Lockdatei verpflichtend).
- Aktionen/Abhängigkeiten möglichst auf vertrauenswürdige Versionen/Commit-SHAs fixieren; Dependabot oder vergleichbare Aktualisierungen testen.
- Kein separates Codemagic-Projekt.

## 2. CI-Ablauf bei jedem Commit

Trigger: Push auf `main`, Pull Requests (falls genutzt) sowie manueller Start über `workflow_dispatch`.

1. Repository auschecken, JDK/Android SDK/Node installieren, Gradle- und npm-Caches nutzen.
2. Lockdateien prüfen und Frontend-Abhängigkeiten reproduzierbar installieren.
3. TypeScript-Typprüfung, Lint, Kotlin-/Android-Lint und Formatprüfungen.
4. JVM-/Kotlin-Unit-Tests, TS-Unit-Tests und Android-Unit-Tests.
5. Integrationstests zwischen Kotlin-WebView-Bridge, Editor-Datenmodell und Speicherung.
6. Android-App sowie instrumentierte Tests bauen.
7. Emulator starten; Smoke- und kritische UI-Tests ausführen.
8. PDF-Golden-Tests, Text- und Seitengrößenprüfungen; wesentliche Layoutabweichungen als Fehler bewerten.
9. **Nur bei grünen Pflichtprüfungen** eine installierbare Entwicklungs-APK erzeugen/bereitstellen. Eine fehlerhafte Version darf nicht als „geprüft“ veröffentlicht werden.
10. Testberichte, Screenshots bei UI-Fehlern und Debug-Logs als Workflow-Artefakte bereitstellen, auch wenn Tests scheitern. Sensible Buchinhalte dürfen nicht in CI-Logs erscheinen.

Aus Effizienzgründen darf ein Plan Schnelltests und zeitintensivere Testmatrizen unterscheiden. **Für veröffentlichte Builds ist die vollständige definierte Release-Prüfung Pflicht.**

## 3. Testpyramide

### Schnelltests ohne Android-Emulator

- Dokumentmodell, Formatierungsbefehle, Absatz-/Kapiteloperationen, Undo/Redo, Suche/Ersetzen.
- Datenmigrationen, lokale Speicherung, atomare Schreibvorgänge und Wiederherstellung.
- TypeScript-Editorlogik, Bridge-Serialisierung, PDF-Datenaufbereitung.
- Kotlin-Unit-Tests und bei Bedarf Robolectric.

### Echte Android-Integrationstests im Cloud-Emulator

- Buch erstellen, öffnen, Text direkt im A5-Seitenlayout eingeben und speichern.
- Textcursor durch Tippen setzen, Langdruck und Auswahlgriffe nutzen.
- Android-IME/Bildschirmtastatur, Eingabe deutscher Sonderzeichen, Zwischenablage.
- Neue Absätze, automatische Seitenwechsel, Suche/Ersetzen, Rückgängig/Wiederholen.
- App beenden/neustarten und unveränderte Daten wiederfinden.
- PDF exportieren und Inhalt sowie Maße prüfen.
- Offline-Betrieb im Emulator ohne externe Netzwerkanfragen, soweit zuverlässig simulierbar.

### Satz- und PDF-Regressionstests

Testdokumente im Repository mit bekannten Referenzen:
- A5 (148 × 210 mm) und gültige PDF-Metadaten.
- Echter, durchsuchbarer/markierbarer Unicode-Text statt Seiten-Screenshots.
- Umlaute, Ligaturen, Zitate, Gedankenstriche, Absatzwechsel und Seitenumbrüche.
- Lange Absätze, Kapitelwechsel, Szenentrenner, Seitenzahlen, verschiedene Schriften.
- Bildvergleich/Golden Screenshots mit nachvollziehbaren Toleranzen für Android-Rendering.
- Layout-Äquivalenz zwischen Editoransicht und exportierter PDF; Abweichungen dokumentieren, nicht stillschweigend ignorieren.

### Lasttests

- Umfangreiche Testbücher mit 300–500 Seiten und vielen Kapiteln.
- Messung von Eingabelatenz, Scrollverhalten, Speicherbedarf und PDF-Export.
- Keine starre Performance-Zahl erfinden, sondern zunächst Baseline auf einer festgelegten Testkonfiguration erfassen und anschließend Grenzwerte festlegen.

## 4. APKs und Downloads

### Entwicklungs-APK

- Automatischer Build nach erfolgreicher CI auf `main`.
- Bereitstellung als GitHub Actions Artifact (temporärer Download unter Actions).
- Datei eindeutig benennen, z. B. `smut-dev-<short-sha>.apk`.
- Keine ungeprüfte APK als bestandene Version kennzeichnen.
- In der README auf den Weg GitHub -> Actions -> letzter grüner Lauf -> Artifacts hinweisen.
- GitHub Actions Artifacts sind zeitlich befristet (typischerweise 90 Tage), keine dauerhafte Distribution.

### Stabile Version

- Ein Versionstag wie `v1.0.0` oder ein bewusst manuell gestarteter Release-Workflow löst die vollständige Testsuite aus.
- Bei Erfolg wird eine **signierte** APK an eine GitHub Release angehängt.
- GitHub Releases ist der dauerhafte Downloadort, nicht ein Build-Artefakt im Git-Quelltextbaum.
- Optional ein deutlich gekennzeichneter Vorab-Release-Channel für neueste getestete Versionen. Kein unkontrolliertes Veröffentlichen bei fehlerhaften Tests.
- Als zusätzliche Integritätsinformation SHA-256-Prüfsumme und ggf. Provenance/Attestation veröffentlichen.

### Android-Signierung

- Android erfordert APK-Signaturen. Ein temporärer Debug-Key pro CI-Lauf eignet sich zum Testen, aber **nicht zuverlässig für Updates einer bereits installierten App**.
- Für installierbare Updates muss derselbe Signaturschlüssel (bei gleicher Paketkennung) bestehen bleiben.
- Für Release-APKs einen langfristigen privaten Keystore sicher erzeugen und geschützt als GitHub Actions Secrets bereitstellen, niemals als Git-Datei.
- Schlüssel und Passwörter dauerhaft außerhalb des Repositories sicher sichern; Verlust des Schlüssels kann Updates verhindern.
- Eine optionale Entwicklungs-App mit eigenständiger Paketkennung (`applicationIdSuffix`) erlaubt parallele Installation zur stabilen App.
- Kein automatisches Überschreiben installierter Nutzerdaten; Backup/Restore testen.

## 5. Fehlerberichte für Codex

Jeder fehlgeschlagene Workflow muss klar sichtbar machen:
- Job und Testname, Grund des Fehlers, Logs/Stacktrace;
- Screenshots (bei UI-Fehlern), Testreports (JUnit/XML/HTML), ggf. Video, soweit technisch verfügbar;
- Commit-ID und verwendete Tool-/Emulatorversion;
- reproduzierbare Befehle für denselben Fehler.

**Wichtig:** GitHub Actions führt Tests und Builds aus, behebt aber nicht selbstständig Fehler. Codex kann anhand dieser Berichte einen nachfolgenden Fix erstellen und erneut pushen; ein autonomer Reparaturkreislauf erfordert eine separat eingerichtete Agenten-Automation und ist nicht vorausgesetzt.

## 6. Tests auf dem echten Smartphone

Der Nutzer braucht keinen PC:
1. GitHub auf dem Handy öffnen.
2. Unter `Actions` den letzten grünen Build wählen und die APK herunterladen, gegebenenfalls ZIP entpacken.
3. APK über Android installieren (ggf. Installation aus dieser Quelle erlauben).
4. Cursorposition, Bildschirmlayout, virtuelle Tastatur, Auswahlgriffe, Gesten und PDF-Export real testen.
5. Fehler mit Beschreibung, Android-Version und optional Screenshot als GitHub Issue erfassen.

Ein Emulator kann echte Touch-/IME-Probleme des konkreten Telefons nicht vollständig ersetzen; Smartphone-Abnahme ist deshalb Teil des Releaseprozesses.

## 7. Reihenfolge für Codex

1. Reproduzierbares Android-/TypeScript-Projektgerüst mit Gradle Wrapper und Lockdateien.
2. Sofort einen minimalen GitHub-Actions-Workflow anlegen: Lint, Unit-Tests, Build, APK-Artefakt.
3. Editor/Android-IME/PDF-Machbarkeitsprototyp herstellen und Cloud-Emulatortests integrieren.
4. PDF-Regressionstests und Datensicherheits-/Migrationsprüfungen erweitern.
5. Signierte GitHub Releases mit sicherem Keystore-Handling erst nach grundlegend funktionierender App freigeben.
6. Alle weiteren Funktionen testgetrieben ergänzen, CI als verpflichtendes Qualitätsgate beibehalten.

## 8. Definition of Done

Eine Funktion gilt nur als umgesetzt, wenn sie im Code vorhanden ist, passende automatisierte Tests besitzt, die CI grün ist, die README/Anforderungsdokumentation angepasst ist und ein installierbarer Build in der definierten Stufe verfügbar ist. Manuelle Abnahmeschritte am Smartphone sind bei Touch/IME/Layout explizit benannt.

## Aktueller Prüfpfad F-05/F-06

- [Grüner F-05-Emulatorlauf 43449455346](https://github.com/philip20794/Smut/actions/runs/43449455346). Kein manuelles Telefon-Feedback vorliegend.
- F-06-Actions-Artefakt wird nur im `tested-apk`-Job erzeugt: `needs: [editor, android, emulator]`, nur `main`, SHA-256-Prüfsumme und vollständige Commit-ID. Die vorgeschaltete interne Build-APK darf nicht als geprüft angeboten werden.
- [Anleitung am Smartphone](APK_DOWNLOAD.md); dauerhafte Release-Signatur bleibt R-01.

**F-06 nachgewiesen:** [grüner Run 43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955), geprüftes APK-Artefakt mit SHA-256. [Roter Test 43451561861](https://github.com/philip20794/Smut/actions/runs/43451561861) veröffentlichte keine geprüfte APK und überhaupt keine Artefakte. Cloud-CI für P0 funktioniert ohne lokalen PC. Das ist noch keine dauerhafte Release-Signierung.
