# E-03 – Seitenfluss, Zoom und Auswahl im Android-Prototyp

Stand: 13.11.2026. Status: **IN_PROGRESS**, keinesfalls bereits vollständiges E-03-Gate.

## Umgesetzt und in der Cloud bestätigt

- Zoom direkt in der A5-Buchansicht mit Plus/Minus, Grenzen 50–100 Prozent. Der Zoom ändert nur die Darstellung und nicht den Dokumenttext; `editor/src/zoom.ts` enthält die deterministische Zoomlogik samt Unit-Tests.
- Instrumentierter Emulator-Test bedient die Zoomschaltflächen über tatsächlichen Android-Touch und kontrolliert den angezeigten Zoom, mindestens eine Canvas-Seite und unveränderte Buchdaten.
- Separater Test erzeugt im bestehenden Canvas Editor vorübergehend 75 deutschsprachige Abschnitte. Er überprüft, dass mehrere echte A5-Canvas-Seiten entstehen und der Schlussabsatz im Dokument enthalten bleibt.
- Android-15+-Insets: Der WebView wird mit `safeDrawingPadding().imePadding()` unterhalb der Systemleiste und oberhalb der Tastatur positioniert. Native Touch-Events an Zoom-, Speichern- und Recheck-Schaltflächen werden im Emulator ausdrücklich geprüft; der rote Diagnosefall [43478967287](https://github.com/philip20794/Smut/actions/runs/43478967287) ist mit [grünem Lauf 43480206233](https://github.com/philip20794/Smut/actions/runs/43480206233) behoben.
- Ein Android-Langdruck löst auf einer Canvas-Seite ein echtes `contextmenu` aus. Der Touch-Adapter im Editor nutzt den Bibliotheks-Mauspfad für Wortmarkierung. Die mobile Wortauswahl und das Ersetzen des markierten Worts über Android-`InputConnection.commitText` sind getestet.
- Zusätzlicher Technologie-Nachweis: In der isolierten mehrseitigen Test-Fixture erzeugt der Canvas-Befehl `executeSelectAll` eine mehrseitige Auswahl; Android-IME kann diese ersetzen, danach bleibt der ursprüngliche persistierte Inhalt unverändert. Das prüft die Engine, nicht die Bedienung über native Auswahlgriffe.
- Die Speichern-Schaltfläche steht im oberen Editorbereich, damit sie bei sichtbarer IME erreichbar bleibt.
- Echte Android-Longpress-und-Zieh-Geste kann eine Auswahl von Canvas-Seite 1 auf Canvas-Seite 2 verlängern: [CI 43493716491](https://github.com/philip20794/Smut/actions/runs/43493716491), inklusive Prüfung zweier unterschiedlicher sichtbarer A5-Canvas-Ziele und isolierter, wiederhergestellter Test-Fixture. Die Touch-Weiterleitung wählt beim Ziehen die nächste Canvas-Seite und begrenzt die Pointerkoordinate auf deren Fläche.
- Beim Ziehen nahe der oberen/unteren WebView-Kante ist Scrollen mit nachgeführter Textauswahl implementiert, aber ein eigener Emulator-Beleg für diesen Sonderfall steht noch aus.
- Das vorbestehende Dokument wird dabei vorab gesichert und im `finally`-Block wiederhergestellt. Der spezielle Testmodus unterdrückt die automatische Speicherung dieser Testdaten. Dies ist keine Funktion der Nutzeroberfläche.

## Noch offen bis E-03 DONE

- **Wortauswahl und Verlängern per echter Android-Fingergeste über zwei Seiten sind nachgewiesen.** Visuelle mobile Auswahlgriffe fehlen noch.
- **Programmatische** Auswahl/Ersetzung über den ganzen mehrseitigen Testtext und **native** Auswahl von Seite 1 nach Seite 2 sind nachgewiesen. Eine separate Prüfung längerer Drag-Strecken mit Rand-Autoscroll und Textänderungen nach nativer Mehrseitenauswahl fehlt.
- Scrollen und Cursorführung bei offener Android-Tastatur sowie Zoom-/Scroll-Wechselwirkungen.
- Dokumentierte und reproduzierbare Einschränkungen; ggf. begründeter Fallback auf eine andere Editorengine.

Prüfbeleg: [Smut CI 43493716491](https://github.com/philip20794/Smut/actions/runs/43493716491) ist vollständig grün: TypeScript, Kotlin, Android-Lint, **acht** instrumentierte API-35-Emulatortests und geprüfte Test-APK. Reale Zwei-Seiten-Drag-Auswahl nachgewiesen; native Auswahlgriffe, Rand-Autoscroll und Cursor-/Tastatur-Scroll sind noch offen. Echter Smartphone-Test erst E-05.
