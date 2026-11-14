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

Die APK enthält bereits einen editierbaren A5-Canvas-Prototyp mit Android-Tastatureingabe, Zoom, Langdruck-Textauswahl und mobilen Auswahlgriffen. Eine vollständige Buch-/Kapitelverwaltung sowie der PDF-Export fehlen weiterhin. Der letzte vollständig geprüfte Stand ist [CI 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623); er wurde nur im Emulator, nicht auf einem echten Smartphone getestet. Die getrennte D-03-Buchpersistenz besitzt noch keine sichtbare Buchverwaltungsoberfläche.

## Aktueller D-03-/PDF-Prüfstand

Die geprüfte APK und die separate A5-PDF-Testdatei stehen im [GitHub-Actions-Lauf 43589383623](https://github.com/philip20794/Smut/actions/runs/43589383623) bereit: `smut-tested-dev-bfd2c82e085844930bd5490924186c5f287bc8c0` und `smut-e04-a5-pdf-probe-bfd2c82e085844930bd5490924186c5f287bc8c0`. Beide Artefakte sind 30 Tage verfügbar. Die PDF enthält Testprosa statt Nutzerdaten und belegt A5-Geometrie und durchsuchbaren Text; sie ist keine druckreife Buch-PDF. Die Buchspeicherung und Recovery sind auf Modell-/Bridge-Ebene getestet, aber noch nicht in der Canvas-Oberfläche freigeschaltet.
