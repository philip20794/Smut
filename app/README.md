# Android-Modul (`app/`)

F-02: Kotlin/Jetpack Compose-App mit absichtlich einfacher, ehrlicher Startansicht. **Dies ist noch kein Bucheditor:** keine Bücherverwaltung, kein Rich-Text-Editor, kein PDF-Export.

- `namespace` / Release-`applicationId`: `io.github.philip20794.smut`
- Debug-`applicationId`: `io.github.philip20794.smut.dev`, separat installierbar
- `minSdk = 26`, `compileSdk = 36`, `targetSdk = 36`
- Gradle 8.13, AGP 8.13.2, Kotlin + Compose Compiler 2.2.21, JDK 17
- Compose BOM 2025.09.00, Activity Compose 1.11.0

Build: `./gradlew :app:assembleDebug` · Unit-Test: `./gradlew :app:testDebugUnitTest`.

Der erste Cloud-Build ist grün: [GitHub Actions 43441065967](https://github.com/philip20794/Smut/actions/runs/43441065967). Der geprüfte offizielle Gradle-Wrapper einschließlich JAR wurde [in `main` committed](https://github.com/philip20794/Smut/commit/954d2e89e98117377d09f047f46bbeca3cf073e4). Die erzeugte Debug-APK ist als Workflow-Artefakt verfügbar. Editor-WebView-Assets folgen in F-03, vollständige CI in F-04 und Emulator-Tests in F-05.
