# E-03 – Seitenfluss, Zoom und Auswahl im Android-Prototyp

Stand: 13.11.2026. Status: **IN_PROGRESS**, keinesfalls bereits vollständiges E-03-Gate.

## Umgesetzt und zur Cloud-Verifikation eingereicht

- Zoom direkt in der A5-Buchansicht mit Plus/Minus, Grenzen 50–100 Prozent. Der Zoom ändert nur die Darstellung und nicht den Dokumenttext; `editor/src/zoom.ts` enthält die deterministische Zoomlogik samt Unit-Tests.
- Instrumentierter Emulator-Test bedient die Zoomschaltflächen über tatsächlichen Android-Touch und kontrolliert den angezeigten Zoom, mindestens eine Canvas-Seite und unveränderte Buchdaten.
- Separater Test erzeugt im bestehenden Canvas Editor vorübergehend 75 deutschsprachige Abschnitte. Er überprüft, dass mehrere echte A5-Canvas-Seiten entstehen und der Schlussabsatz im Dokument enthalten bleibt.
- Das vorbestehende Dokument wird dabei vorab gesichert und im `finally`-Block wiederhergestellt. Der spezielle Testmodus unterdrückt die automatische Speicherung dieser Testdaten. Dies ist keine Funktion der Nutzeroberfläche.

## Noch offen bis E-03 DONE

- Langdruck auf einzelne Wörter und sinnvolle mobile Auswahlgriffe.
- Auswahl und Ersetzen über Absatz- sowie Seitengrenzen.
- Scrollen und Cursorführung bei offener Android-Tastatur sowie Zoom-/Scroll-Wechselwirkungen.
- Dokumentierte und reproduzierbare Einschränkungen; ggf. begründeter Fallback auf eine andere Editorengine.

Prüfbeleg: Die aktuelle Revision wird in [Smut CI](https://github.com/philip20794/Smut/actions) geprüft. Erst eine vollständig grüne CI inklusive Android-Emulator bestätigt die implementierten Teilfunktionen; die noch offenen Punkte brauchen weitere Tests. Der echte Gerätetest gehört zu E-05 und darf nie durch Emulatorresultate ersetzt werden.
