# E-03 – Seitenfluss, Zoom und Auswahl im Android-Prototyp

Stand: 13.11.2026. Status: **IN_PROGRESS**, keinesfalls bereits vollständiges E-03-Gate.

## Umgesetzt und in der Cloud bestätigt

- Zoom direkt in der A5-Buchansicht mit Plus/Minus, Grenzen 50–100 Prozent. Der Zoom ändert nur die Darstellung und nicht den Dokumenttext; `editor/src/zoom.ts` enthält die deterministische Zoomlogik samt Unit-Tests.
- Instrumentierter Emulator-Test bedient die Zoomschaltflächen über tatsächlichen Android-Touch und kontrolliert den angezeigten Zoom, mindestens eine Canvas-Seite und unveränderte Buchdaten.
- Separater Test erzeugt im bestehenden Canvas Editor vorübergehend 75 deutschsprachige Abschnitte. Er überprüft, dass mehrere echte A5-Canvas-Seiten entstehen und der Schlussabsatz im Dokument enthalten bleibt.
- Android-15+-Insets: Der WebView wird mit `safeDrawingPadding().imePadding()` unterhalb der Systemleiste und oberhalb der Tastatur positioniert. Native Touch-Events an Zoom-, Speichern- und Recheck-Schaltflächen werden im Emulator ausdrücklich geprüft; der rote Diagnosefall [43478967287](https://github.com/philip20794/Smut/actions/runs/43478967287) ist mit [grünem Lauf 43480206233](https://github.com/philip20794/Smut/actions/runs/43480206233) behoben.
- Das vorbestehende Dokument wird dabei vorab gesichert und im `finally`-Block wiederhergestellt. Der spezielle Testmodus unterdrückt die automatische Speicherung dieser Testdaten. Dies ist keine Funktion der Nutzeroberfläche.

## Noch offen bis E-03 DONE

- Langdruck auf einzelne Wörter und sinnvolle mobile Auswahlgriffe.
- Auswahl und Ersetzen über Absatz- sowie Seitengrenzen.
- Scrollen und Cursorführung bei offener Android-Tastatur sowie Zoom-/Scroll-Wechselwirkungen.
- Dokumentierte und reproduzierbare Einschränkungen; ggf. begründeter Fallback auf eine andere Editorengine.

Prüfbeleg: [Smut CI 43480206233](https://github.com/philip20794/Smut/actions/runs/43480206233) ist vollständig grün (TypeScript, Kotlin, Android-Lint, vier instrumentierte Tests und geprüfte Test-APK). Dieser Beleg betrifft Zoom, Mehrseitenfluss und bisherige IME-Interaktionen, **nicht** die noch fehlenden Auswahl-/Scroll-Nachweise. Der echte Smartphone-Test gehört zu E-05 und bleibt separat offen.
