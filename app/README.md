# Android-Modul (`app/`)

**Status: F-02 offen.** Dieser Ordner ist die reservierte Modulwurzel für die Android-App.

F-02 legt hier das echte Gradle-Android-Modul mit Kotlin, Jetpack Compose, Ressourcen, Manifest und einem ersten UI-/Unit-Smoke-Test an. In F-01 wurde absichtlich noch kein ausführbarer Quellcode erstellt.

Festgelegte Bezeichner:
- Namespace und Release-`applicationId`: `io.github.philip20794.smut`
- Spätere Entwickler-Variante: `io.github.philip20794.smut.dev` (`applicationIdSuffix = ".dev"`)
- `minSdk = 26`, `compileSdk = 36`, `targetSdk = 36` (Baseline, zu F-02 verifizieren)
- UI: Kotlin + Jetpack Compose
- JDK: 17, Android Gradle Plugin 8.13.2, Gradle 8.13, Kotlin 2.2.21

Quelle und Versionsbegründung: [Build-Baseline](../docs/BUILD.md). Editor-/PDF-Bibliotheken sind ausdrücklich noch nicht ausgewählt.
