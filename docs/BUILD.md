# F-01 – Projektstruktur, Toolchain und Buildkonventionen

**Stand: 2026-11-13.** Dies ist die verbindliche Baseline für [F-01](IMPLEMENTATION_PLAN.md). **Noch keine ausführbare Android-App vorhanden.** F-02 ergänzt die Gradle-Projektdateien und den Wrapper; F-03 ergänzt die lokale TypeScript-WebView-App. Die unten aufgeführten Buildbefehle sind daher **zukünftige CI-Befehle, keine bereits erfolgreich ausgeführten Tests**.

## 1. Projektstruktur

| Pfad | Aufgabe / Stand |
| --- | --- |
| `app/` | Android-App mit Kotlin und Jetpack Compose; Modulaufbau **F-02** |
| `editor/` | offline gebündelte TypeScript-Buchseiten-UI; Build **F-03** |
| `gradle/libs.versions.toml` | zentrale Plugin-Versionen für das kommende Gradle-Projekt; **F-01** |
| `.node-version` | exakt gepinnte Node-Version für spätere Builds; **F-01** |
| `.gitignore` und `.editorconfig` | Build-, Sicherheits- und Formatierungsregeln; **F-01** |
| `docs/` | Produkt, Architektur, Aufgaben, Fortschritt und Entscheidungen |
| `.github/workflows/` | noch nicht vorhanden; GitHub Actions **F-04/F-05/F-06** |
| `gradle/wrapper/` und `gradlew` | noch nicht vorhanden; Wrapper **F-02**, inklusive signierbarem/checksummierbarem JAR |

Keine Android-/TypeScript-Quelldateien oder erzeugten Binärdateien wurden in F-01 angelegt. Git speichert keine leeren Ordner; die zwei Modul-README-Dateien reservieren deren Verzeichnisse.

## 2. Versionierte Baseline

| Komponente | Festlegung für F-02/F-03 | Quelle im Git | Hinweis |
| --- | --- | --- | --- |
| Android Gradle Plugin | **8.13.2** | `gradle/libs.versions.toml` | Stable 8.13; geeignet für API 36 und Gradle 8.13 |
| Gradle Wrapper | **8.13** | Ab F-02: `gradle/wrapper/gradle-wrapper.properties` | Wrapper-Skripte und Wrapper-JAR **mit committen** |
| Kotlin / Compose-Compiler-Plug-in | **2.2.21** | `gradle/libs.versions.toml` | beide verwenden exakt dieselbe Kotlin-Version |
| JDK | **17** | F-02/F-04 Buildkonfiguration | mit AGP 8.13 kompatibel |
| `compileSdk` | **36** | F-02 `app/build.gradle.kts` | API 36 (Android 16) |
| `targetSdk` | **36** | F-02 `app/build.gradle.kts` | spätere Updates bewusst testen |
| `minSdk` | **26** | F-02 `app/build.gradle.kts` | Android 8.0 und neuer |
| Node.js | **24.22.3 LTS** | `.node-version` | vollständige Patchversion; GitHub Action `setup-node` unterstützt die Versionsdatei |
| npm | **npm (mit gepinntem Node geliefert)** | F-03 `package-lock.json` und ggf. `packageManager` | `npm ci` statt nichtdeterministischem `npm install` in CI |
| TypeScript | **5.9.3 als Startkandidat** | erst F-03 `editor/package.json` und Lockfile | vor Festlegung gegen Editor-Toolchain testen |

Die Werte sind absichtlich konservativ und **noch nicht durch einen tatsächlichen Build validiert**. Die Weiterentwicklung von Jetpack Compose/AndroidX darf in F-02 eine dokumentierte Anpassung auslösen (mit aktualisiertem Versionskatalog und ADR). Keine wildcard-/`latest`-Versionen in CI oder Releasekonfiguration.

**Quellen für die Kompatibilitätsentscheidung:**
- [Android Gradle Plugin 8.13 – JDK 17, Gradle 8.13, API 36](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Kotlin-Versionen](https://kotlinlang.org/docs/releases.html) und [Compose-Compiler-Plug-in](https://kotlinlang.org/docs/compose-compiler-migration-guide.html)
- [Gradle 8.13](https://docs.gradle.org/8.13/release-notes.html)
- [Node.js 24.22.3 LTS](https://nodejs.org/en/blog/release/v24.22.3)
- [Node.js Release-Zeitplan](https://github.com/nodejs/Release)

## 3. Identität der Android-Anwendung

- App-Anzeigename: `Smut`
- Kotlin-`namespace` und reguläre `applicationId`: `io.github.philip20794.smut`
- Entwicklerpaket: `io.github.philip20794.smut.dev` über `applicationIdSuffix = ".dev"` (spätere parallele Installation)
- `versionCode` beginnt bei **1**, `versionName` bei **0.1.0-dev** (in F-02 umsetzen)
- Schlüssel für die Release-Signierung **niemals** im Git-Repository speichern; die Einrichtung folgt in R-01.
- `applicationId` nach öffentlicher Auslieferung nicht beiläufig ändern: Sonst erscheinen Updates auf Android als andere App.

Die Paketkennung ist für ein neues, rein sideload-basiertes Projekt als stabile Ausgangsentscheidung festgelegt. Bei einem späteren Store-Release ist die Eindeutigkeit erneut zu prüfen.

## 4. Dateikonventionen und Sicherheit

- UTF-8, LF, Abschlussnewline, standardmäßig zwei Leerzeichen; Kotlin/Kotlin-DSL vier Leerzeichen (`.editorconfig`).
- Kommende Module: Kotlin unter `app/src/main/java/io/github/philip20794/smut/`; Ressourcen unter `app/src/main/res/`; Editorquellen unter `editor/src/`.
- Lockdateien sind **einzuchecken**. `gradle/wrapper/gradle-wrapper.jar` gehört trotz Binärformat ausdrücklich ins Git, um ohne eigenen Rechner bauen zu können.
- **Nicht einchecken:** `local.properties`, `.env`-Dateien, APK/AAB, Keystores, Gradle-Buildordner, `node_modules`, generierte `editor/dist`-Assets, personenbezogene Testmanuskripte.
- Kein Konto und keine externen Runtime-Abhängigkeiten in der fertigen App. GitHub-/Maven-/npm-Zugriff während des Builds ist erlaubt.
- Android/Compose/Gradle/Kotlin behalten ihre jeweiligen Originallizenzen. **Für Smut selbst wurde noch keine Projektlizenz festgelegt**; das öffentliche GitHub-Repository ist deshalb nicht automatisch mit einer Open-Source-Lizenz versehen. Bibliothekslizenzen und gebündelte Fonts werden vor Aufnahme geprüft (E-01, P5).

## 5. Geplante Build- und Testbefehle (ab F-02/F-03)

```bash
# Ab F-02: Android Build und Tests
./gradlew --no-daemon :app:assembleDebug
./gradlew --no-daemon :app:testDebugUnitTest
./gradlew --no-daemon :app:lintDebug

# Ab F-03: lokaler TypeScript-Editor
cd editor
npm ci
npm run typecheck
npm run lint
npm test
npm run build
```

**Derzeit nicht ausführbar:** `gradlew` und `editor/package.json` existieren noch nicht. F-02 muss den Gradle Wrapper inklusive Wrapper-JAR und eine startbare App auf `main` bereitstellen. F-03 muss `package.json`, Lockfile und Scripts erstellen; danach werden die npm-Befehle verbindlich.

## 6. Cloud-only Betrieb

1. **F-02:** Android-Grundgerüst auf `main` committen; Gradle Wrapper aus GitHub-Runner/zugelassener Entwicklungsumgebung erzeugen (keine manuell erfundenen Wrapper-Binärdateien).
2. **F-03:** lokale TypeScript-Assets inklusive Lockdatei erstellen und ohne Internet in WebView laden.
3. **F-04:** GitHub Actions checkt Code aus, setzt JDK 17/Node aus `.node-version`/Android SDK auf, führt die obigen Checks und einen Build durch.
4. **F-05:** Emulator-Start-/UI-Smoke-Test im Workflow.
5. **F-06:** bestandene APK als Artifact; später signierte Releases, die ohne PC auf dem Handy installiert werden können.

Für den Nutzer ist keine lokale Shell und kein Rechner notwendig. Codex/GitHub führt die Arbeit aus; GitHub Actions liefert Logs und APKs.

## 7. Abnahme von F-01 (nur Dokumentations- und Repository-Prüfung)

- Projektstruktur `app/`, `editor/`, `gradle/` vorhanden.
- Versionskatalog, `.node-version`, `.gitignore`, `.editorconfig` vorhanden und unter `main` abrufbar.
- Konsistente Versions- und Paketkennungsentscheidung im Git dokumentiert.
- Keine Konfiguration legt vor P1 den späteren Rich-Text-Editor oder die PDF-Library endgültig fest.
- **Keine erfolgreiche Kompilierung, kein Emulatorlauf und keine APK behauptet.** Diese Prüfungen gehören F-02 bis F-06.
- Nach erfolgreicher Dateiprüfung den F-01-Status und die nächste Task `F-02` in [PROGRESS.md](PROGRESS.md) fortschreiben.
