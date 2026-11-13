# Abnahmekriterien und Testfälle

## Grundbedienung

- [ ] Eine neue App-Installation erlaubt ohne Login und ohne Internet die Erstellung eines Buchs.
- [ ] Ein neu angelegtes Buch öffnet sich direkt als editierbare A5-Seitenansicht.
- [ ] Ein Tippen in einen Satz positioniert den Cursor an der ausgewählten Textstelle.
- [ ] Mit geöffneter Android-Tastatur können deutsche Umlaute, Satzzeichen und Zeilenumbrüche eingegeben werden.
- [ ] Der Cursor bleibt nach automatischem Seitenwechsel im richtigen Absatz sichtbar.
- [ ] Durch langes Drücken lässt sich ein Wort auswählen und durch Ziehen eine längere Passage markieren.
- [ ] Die markierte Passage lässt sich ersetzen, löschen und kursiv formatieren.
- [ ] Enter erzeugt einen Absatz; ein Szenentrenner ist gesondert einfügbar.
- [ ] Rückgängig/Wiederholen funktionieren nach Tippen, Löschen und Formatänderung.
- [ ] Das Schließen oder Abstürzen der App führt nicht zum Verlust bereits automatisch gespeicherter Änderungen.
- [ ] Beim erneuten Öffnen erscheinen die Kapitel und das zuletzt bearbeitete Textumfeld wieder.

## Bücher und Navigation

- [ ] Mehrere Bücher sind unabhängig anlegbar und auswählbar.
- [ ] Kapitel können angelegt, benannt, sortiert und direkt angesprungen werden.
- [ ] Bücher können dupliziert und als vollständiges editierbares Backup exportiert werden.
- [ ] Das Löschen eines Buchs erfordert eine explizite Bestätigung.

## Satzqualität und PDF

- [ ] Der PDF-Export hat physische A5-Abmessungen von 148 × 210 mm.
- [ ] Die PDF enthält auswählbaren und durchsuchbaren Text, keine reinen Screenshots.
- [ ] Umlaute, typografische Anführungszeichen, Gedankenstriche und mehrseitige Kapitel bleiben korrekt.
- [ ] Mit identischen Schriften und Layouteinstellungen entsprechen Seitenanzahl, Textreihenfolge und Umbrüche so weit wie technisch spezifiziert der Buchansicht.
- [ ] Ein langer Absatz fließt korrekt über eine Seitengrenze.
- [ ] Ein manueller Seitenwechsel und Kapitelbeginn werden korrekt im PDF ausgegeben.
- [ ] Seitenzahlen und Ränder folgen den Buchlayout-Einstellungen.
- [ ] Die exportierte PDF lässt sich in einer unabhängigen Android-PDF-App öffnen und nach Worten durchsuchen.

## Zuverlässigkeit und Performance

- [ ] Alle Kernfunktionen einschließlich PDF-Export funktionieren im Flugmodus.
- [ ] Keine Schriftart oder JavaScript-Bibliothek wird während der Nutzung per CDN nachgeladen.
- [ ] Ein großer Testroman lässt sich mit geöffnetem On-Screen-Keyboard flüssig bearbeiten.
- [ ] Während des PDF-Exports bleiben die gespeicherten Quelldaten unverändert.
- [ ] Das Zusammenspiel von Zoom, Tastatur und Eingabe wurde auf einem realen Android-Gerät getestet.

## Dokumentationspflichten für Codex

- [ ] Vor dem vollständigen Implementierungsplan existiert ein dokumentierter Technologietest für Cursor, Touch-Selektion, Tastatur und PDF-Export.
- [ ] Abweichungen und nicht bestandene Tests werden ausdrücklich festgehalten.
- [ ] Der Implementierungsplan verknüpft konkrete Arbeitsschritte mit diesen Abnahmekriterien.

## GitHub-only Entwicklung, CI und APK-Verteilung

- [ ] Build, alle Pflichtprüfungen und APK-Veröffentlichung sind über GitHub Actions ohne lokalen Entwicklungsrechner ausführbar.
- [ ] Jeder Push auf `main` und jeder Pull Request (falls verwendet) startet reproduzierbare Checks.
- [ ] Kotlin-, TypeScript-, Modell- und Integrationstests sind bei relevantem Code verpflichtend; die Testresultate werden in GitHub angezeigt.
- [ ] Kritische Android-Bedienabläufe werden auf einem GitHub-gehosteten Emulator getestet.
- [ ] Schlägt ein Pflichtcheck fehl, wird keine APK als erfolgreich geprüfte Version veröffentlicht.
- [ ] Nach erfolgreichem `main`-Build steht eine installierbare Test-APK mit nachvollziehbarer Commit-ID unter GitHub Actions bereit.
- [ ] Ein expliziter Versionstag bzw. Release-Auslöser erzeugt nach vollständiger Prüfung eine signierte, dauerhaft in GitHub Releases herunterladbare APK.
- [ ] Signaturschlüssel und Passwörter werden nicht in Git eingecheckt; der Schlüssel wird sicher gesichert und ermöglicht Updates bereits installierter Versionen.
- [ ] CI-Fehler sind ohne lokalen Rechner durch Logs, Testberichte und bei Bedarf Screenshots diagnostizierbar.
- [ ] Die App kann auf einem realen Android-Smartphone aus der veröffentlichten APK installiert und überprüft werden.
- [ ] Ein technischer Fehler in CI ist dokumentiert und wird vor Abnahme des Arbeitspakets behoben; Actions allein behebt keine Fehler automatisch.
