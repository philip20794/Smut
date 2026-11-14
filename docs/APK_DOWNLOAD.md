# Test-APK ohne eigenen Rechner herunterladen und installieren

**Gültig ab F-06 und nur nach grünem Emulator-Gate.** Die Test-APK ist ein zeitlich befristetes GitHub-Actions-Artefakt und **noch kein signiertes stabiles Release**.

## Schritte am Android-Smartphone

1. [GitHub Actions – Smut](https://github.com/philip20794/Smut/actions) im Browser öffnen und den obersten **grünen** Lauf von `main` mit dem Namen „Smut CI“ auswählen.
2. Unter **Artifacts** den Eintrag `smut-tested-dev-<vollständige Commit-ID>` öffnen. Auf mobilen Browsern eventuell „Desktopansicht“ wählen. Ist der Eintrag nicht vorhanden, wurde diese Revision nicht vollständig freigegeben – keine rohe Build-APK als geprüft betrachten.
3. Das ZIP herunterladen und entpacken. Es enthält `smut-dev-<Commit-ID>.apk` und `SHA256SUMS`. Die SHA-256-Prüfsumme erlaubt eine Integritätsprüfung des APK-Downloads.
4. Die APK auf dem Telefon öffnen und die Installation aus dieser Quelle erlauben, falls Android danach fragt. Die App heißt „Smut“ und verwendet die Paketkennung `io.github.philip20794.smut.dev`.
5. Beim Testen Version/Commit notieren und UI-, Tastatur- oder WebView-Probleme mit Android-Version unter [GitHub Issues](https://github.com/philip20794/Smut/issues) melden.

**Achtung zur Signierung:** GitHub Actions erzeugt derzeit Debug-APKs mit möglicherweise wechselndem Debug-Schlüssel. Ein Update über eine installierte ältere Debug-APK kann daher wegen einer Signaturabweichung scheitern. Nicht einfach deinstallieren, wenn später Bücher in der App gespeichert sind: Deinstallation löscht normalerweise private App-Daten. Verlässliche, signierte Updates und Backups werden erst mit den späteren Arbeitspaketen eingeführt.

**Qualitätsgrenze:** In F-06 muss für genau diesen Commit TypeScript-Typprüfung, Sicherheitslint, Node-Tests, Android-JVM-Tests, Android-Lint, APK-Assetprüfung und Android-API-35-Emulator-Smoke bestanden sein. Ein fehlgeschlagener oder übersprungener Pflichtcheck darf kein `smut-tested-dev-...` veröffentlichen.

Die APK enthält bereits einen editierbaren A5-Canvas-Prototyp mit Android-Tastatureingabe, Zoom, Langdruck-Textauswahl und mobilen Auswahlgriffen. Seit B-01 ist eine native Bücherliste mit Anlegen, Öffnen in einer Leseansicht, Umbenennen, Duplizieren und bestätigtem Löschen vorhanden. Kapitelverwaltung mit Umbenennen, Umordnen, Navigation und bestätigtem Löschen ist seit B-02 enthalten. B-03 ergänzt die gespeicherte Leseposition, Android-Zurück-Navigation und das kompakte Buchmenü. Direkte Bearbeitung der strukturierten Bücher und der Benutzer-PDF-Export fehlen noch. Der letzte vollständig geprüfte Code-Stand ist [CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487); er wurde nur im Emulator, nicht auf einem echten Smartphone getestet.

## Aktueller B-03-/PDF-Prüfstand

Die neueste geprüfte Entwickler-APK und die A5-PDF-Testprobe liegen unter [CI 43600342487](https://github.com/philip20794/Smut/actions/runs/43600342487): `smut-tested-dev-5e89c932846a096b5b238008fc477ccb933e46f2` und `smut-e04-a5-pdf-probe-5e89c932846a096b5b238008fc477ccb933e46f2`. Die Artefakte sind 30 Tage bis zum 14.12.2026 verfügbar. Die Backup-Oberfläche lässt sich über die Bücherliste und im A5-Prototyp öffnen. Sie arbeitet ausschließlich mit strukturierten Büchern. Die bestehende A5-Prototypseite wird erst bei W-01 migriert und nicht automatisch in die neue Sicherung aufgenommen. PDF bleibt eine technische Offline-Probe, keine druckreife Benutzerfunktion. Die aktuelle Entwickler-APK wurde im Emulator, nicht auf echtem Gerät geprüft.
