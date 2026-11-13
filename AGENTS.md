# Anweisungen für Codex und andere Entwicklungsagenten

## Verbindliche Quellen

Vor Planung oder Codeänderungen lesen:
1. [README.md](README.md)
2. [docs/PRODUCT.md](docs/PRODUCT.md)
3. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
4. [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md)
5. [docs/CI-CD.md](docs/CI-CD.md)

Widersprüche dokumentieren und fachlich klären; unbelegte Annahmen nicht als bereits funktionierende Features ausgeben.

## Grundregeln

- Das Produkt wird **vollständig ohne lokalen Entwicklungscomputer** gebaut, getestet und über GitHub distribuiert.
- GitHub Actions für Tests und Android-APK-Builds verwenden; Codemagic ist keine Voraussetzung.
- Erste technische Meilensteine: reproduzierbarer Build, CI-Tests, APK-Artefakt, Android-WebView-Editor-Prototyp, anschließende vollständige App.
- Tests nicht deaktivieren, überspringen oder lockern, nur um CI grün zu bekommen.
- Für neue Features angemessene Unit-, Integrations- und gegebenenfalls Emulator-/UI-Tests implementieren.
- Keine Zugangsdaten oder Signaturschlüssel ins Repository committen; GitHub Secrets und sichere Schlüsselverwaltung spezifizieren.
- App vollständig offline benutzbar halten. Keine CDN-/Serverabhängigkeit für die Editorlaufzeit.
- Editor soll auf direktes Tippen in einem paginierten A5-Buchlayout ausgelegt sein; PDF ist Export, nicht die editierte Quelle.
- Keine große technische Abhängigkeit verbindlich festlegen, bevor Touch, Android-IME und PDF-Fidelity im Machbarkeitstest bestätigt sind.
- Jede fertige Funktion benötigt dokumentierte Abnahmekriterien und einen grünen CI-Lauf.
- Fehlende Tests, gebrochene Builds, Risiken und unerledigte Punkte in der Fortschrittsdokumentation sichtbar machen.
- Zwischen „Implementiert“, „automatisch getestet“, „auf echtem Gerät getestet“ und „noch offen“ ausdrücklich unterscheiden.
- Bei Änderungen die Projektdokumentation aktualisieren, wenn Verhalten/Architektur/Buildprozess betroffen sind.

## Implementierungsplanung

Erstelle auf Basis dieser Dokumente einen nachvollziehbaren Implementierungsplan mit Abhängigkeiten, Meilensteinen, Status und Tests pro Arbeitspaket. Plane früh einen kompilierbaren Minimalstand mit GitHub Actions ein. Priorisiere den Machbarkeitsnachweis für Android-Touch-Eingabe, Tastatur und A5-PDF, bevor viel Produktfunktionalität entsteht.

Diese Datei fordert **keine sofortige Implementierung**; sie legt Regeln für spätere Codex-Arbeit fest.
