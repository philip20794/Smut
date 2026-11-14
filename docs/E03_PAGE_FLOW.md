# E-03 – Seitenfluss, Zoom und Auswahl im Android-Prototyp

Stand: 14.11.2026. Status: **DONE** nach vollständigem grünem [CI-Lauf 43574312605](https://github.com/philip20794/Smut/actions/runs/43574312605). Dies ist der automatisierte Emulator-Nachweis, kein echter Smartphone-Test.

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
- Der tatsächliche Canvas-Cursor (`.ce-cursor`) wird nach Android-IME-Eingaben, Seitenumbruch und Viewport-Resize in den sichtbaren Bereich gescrollt. Die Scrollmenge wird durch eine reine, getestete Funktion berechnet; `visualViewport`-Inset und Android-`imePadding` begrenzen den Sichtbereich. Der instrumentierte Test tippt auf A5-Seite 2, öffnet wirklich die Android-Tastatur, fügt Unicode über `InputConnection` ein und kontrolliert den sichtbaren Cursor; [Smut CI 43497267481, Versuch 2](https://github.com/philip20794/Smut/actions/runs/43497267481) grün mit zehn Instrumentierungstests. Versuch 1 scheiterte an einem übergreifenden Emulator-Fokusproblem (ADB) und wurde ohne Testlockerung erfolgreich wiederholt.
- Das vorbestehende Dokument wird dabei vorab gesichert und im `finally`-Block wiederhergestellt. Der spezielle Testmodus unterdrückt die automatische Speicherung dieser Testdaten. Dies ist keine Funktion der Nutzeroberfläche.

## Historische offene Punkte vor der E-03-Abnahme

- **Wortauswahl und Verlängern per echter Android-Fingergeste über zwei Seiten sind nachgewiesen.** Visuelle mobile Auswahlgriffe fehlen noch.
- **Programmatische** Auswahl/Ersetzung über den ganzen mehrseitigen Testtext und **native** Auswahl von Seite 1 nach Seite 2 sind nachgewiesen. Eine separate Prüfung längerer Drag-Strecken mit Rand-Autoscroll und Textänderungen nach nativer Mehrseitenauswahl fehlt.
- Cursor-Sichtbarkeit mit offener Android-Tastatur auf späterer Seite ist nachgewiesen. Wechselwirkungen mit Zoom und längeren Tippsequenzen noch separat prüfen.
- Dokumentierte und reproduzierbare Einschränkungen; ggf. begründeter Fallback auf eine andere Editorengine.

Prüfbeleg: [Smut CI 43497267481, Versuch 2](https://github.com/philip20794/Smut/actions/runs/43497267481) vollständig grün: TypeScript, Kotlin, Android-Lint, **zehn** instrumentierte API-35-Emulatortests und geprüfte APK. Native Seiten-Drag-Auswahl und sichtbarer Cursor beim Tippen mit geöffneter Tastatur nachgewiesen; Auswahlgriffe, Rand-Autoscroll-Geste und kombinierte Zoom-/IME-Szenarien noch offen. Echter Smartphone-Test erst E-05.

## Entwicklung 14.11.2026 – mobile Auswahlgriffe

Die Auswahl wird mit zwei separaten 44-Pixel-Touchzielen am realen Anfang/Ende der Textmarkierung ausgestattet. Positionen kommen aus der Engine (`getRangeContext`), werden anhand tatsächlich gezeichneter Seiten gegen den aktuellen Zoom abgeglichen und bei Scrollen/IME/Zoom aktualisiert. Beim Ziehen bleibt jeweils die Gegenseite verankert; die Editorbibliothek selbst übernimmt Trefferposition und Erweiterung der Range. Ein neuer Android-Instrumentierungstest prüft den echten Fingerzug am Endgriff. **Noch keine Freigabe ohne neuen grünen CI-Nachweis und echten Smartphone-Test.**

## E-03-Abnahme am 14.11.2026

Der [CI-Lauf 43574312605](https://github.com/philip20794/Smut/actions/runs/43574312605) ist vollständig grün: TypeScript-Prüfung, Node-Unit-Tests, Android-Build/Lint, instrumentierter API-35-Emulator und Freigabe der geprüften Test-APK. Er enthält einen echten Android-Fingerzug auf dem Auswahl-Endgriff, der eine zuvor per Langdruck erstellte Wortauswahl verlängert und den Buchtext unverändert lässt. Die geometrische Platzierung der beiden Auswahlgriffe wird zudem per Unit-Tests auf der ersten und einer folgenden A5-Seite geprüft. Vorausgehende Läufe belegen native Auswahl über A5-Seiten hinweg, Rand-Autoscroll und Zoom/IME/Cursor-Regressionen.

Der frühere Abschnitt „Historische offene Punkte“ dokumentiert den Zwischenstand vor diesem Nachweis und ist nicht mehr die operative Aufgabenliste. E-03 kann als `DONE` geführt werden. Die erste praktische Prüfung auf einem echten Smartphone gehört ausdrücklich zu E-05; das A5-PDF-Gate und die abschließende Technologieentscheidung folgen in E-04.
