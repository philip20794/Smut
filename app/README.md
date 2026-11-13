# Android-Modul (`app/`)

F-02: Kotlin/Jetpack Compose-App mit absichtlich einfacher, ehrlicher Startansicht. **Dies ist noch kein Bucheditor:** keine Bücherverwaltung, kein Rich-Text-Editor, kein PDF-Export.

- `namespace` / Release-`applicationId`: `io.github.philip20794.smut`
- Debug-`applicationId`: `io.github.philip20794.smut.dev`, separat installierbar
- `minSdk = 26`, `compileSdk = 36`, `targetSdk = 36`
- Gradle 8.13, AGP 8.13.2, Kotlin + Compose Compiler 2.2.21, JDK 17
- Compose BOM 2025.09.00, Activity Compose 1.11.0

Build: `./gradlew :app:assembleDebug` · Unit-Test: `./gradlew :app:testDebugUnitTest`.

Der erste Cloud-Build wird durch den Bootstrap-Workflow angelegt. Sobald die Wrapper-JAR aus dem geprüften Gradle-8.13-Runner in `main` liegt, funktioniert der Wrapper auf weiteren Rechnern direkt. Die eigentlichen Editor-WebView-Assets folgen in F-03, die vollständige CI in F-04, Emulator-Tests in F-05.
