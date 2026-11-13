# E-02: Android-Tastatur und direktes Schreiben – reproduzierbare Abnahme

Stand: 13.11.2026. Technischer Testkandidat: `@hufe921/canvas-editor@1.1.0`.

## Automatisierte Cloud-Prüfung

Testdatei: `app/src/androidTest/java/io/github/philip20794/smut/OfflineEditorSmokeTest.kt`

- Android-35-Emulator (Pixel 6, Google APIs), App vollständig ohne Netzwerkberechtigung.
- Tippen auf die sichtbare Buchseite mittels UIAutomator-Touch in Bildschirmkoordinaten.
- Prüfung: Das Canvas-Editor-eigene `textarea.ce-inputarea` erhält den Fokus; kein getrenntes Demo-Overlay.
- Texteingabe über `WebView.onCreateInputConnection(EditorInfo)` und `InputConnection.commitText` einschließlich `Äöüß`.
- Androids `InputConnection.setComposingText` mit Korrektur eines laufenden Wortes und `finishComposingText` (Prüfung auf genau die korrigierte Fassung).
- Enter und Backspace über `InputConnection.sendKeyEvent`; anschließend Inhalt kontrollieren.
- Speichern über die WebView-Schaltfläche, Activity neu erstellen, gespeicherten Text vergleichen.
- Vorhandener Offline-Smoke bestätigt Editorinitialisierung, Handshake, Laden und atomare Speicherung.

Der Test nutzt echte Android-Touchereignisse und die IME-Eingabeverbindung, aber keine physische Android-Tastatur. Die Langzeitstabilität der Tastaturvorschläge auf einem konkreten Smartphone bleibt der Geräteabnahme E-05 vorbehalten. **E-02 abgeschlossen:** Der vollständige [CI-Lauf 43467760666](https://github.com/philip20794/Smut/actions/runs/43467760666) hat die Android-IME-/Kompositionstests, Emulator-Smoke, Tests und APK-Gate bestanden. Der reale Smartphone-Test E-05 bleibt ausdrücklich offen.

## Prüfschritte am Smartphone (E-05; noch nicht als bestanden markieren)

1. Letzte vollständig geprüfte Entwickler-APK aus dem grünen GitHub-Actions-Lauf installieren. WLAN und mobile Daten ausschalten.
2. Buchseite öffnen, an Anfang, Mitte und Ende des sichtbaren Absatzes tippen; der Cursor soll an der berührten Textposition erscheinen, die Android-Tastatur sichtbar werden.
3. `Äpfel, Öl, Grüße – „Hallo“ ä ö ü ß` schreiben. Wortvorschläge/Autokorrektur der üblichen Tastatur verwenden; es dürfen keine Zeichen doppelt oder vertauscht erscheinen.
4. Einen Tippfehler durch Vorschlag ersetzen, danach Zeichen mit Backspace löschen; neue Absätze mit Enter erzeugen.
5. `Jetzt speichern` betätigen, App schließen und neu öffnen; Inhalt und Absatzstruktur prüfen.
6. Tastatur schließen/öffnen, mit offenem Eingabefeld scrollen und Text an mehreren Stellen ergänzen.
7. Auffälligkeiten mit Android-Version, Tastatur-App, APK-Commit und konkreten Reproduktionsschritten dokumentieren.

## Einschränkungen und Sicherheitsgrenzen

- Dies ist nur der erste A5-Editor-Prototyp. Das vorläufige native Dokumentformat ist kein dauerhaftes Smut-Buchformat.
- Noch keine garantierte Datenmigration oder wiederholbar signierte Updates; vor einem Versionswechsel wertvolle Texte separat sichern.
- Textauswahl per langem Drücken, Zoom, mehrere Seiten sowie PDF sind E-03/E-04 und gelten durch E-02 nicht als geprüft.
