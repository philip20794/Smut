# B-03 – Letzte Buchposition und minimale Navigation

**Implementierung:** 14.11.2026. **Status:** `IN_REVIEW` bis der vollständige Pflicht-CI-Lauf inklusive Android-Emulator und geprüfter Test-APK grün ist.

## Verhalten

- Die native Buchansicht liest den in D-03 persistent gespeicherten `BookPosition` (Kapitel-ID, Block-ID, UTF-16-Offset, Scroll-Y) aus der autoritativen Buchdatei. Das aktuell gespeicherte Kapitel bleibt nach Rückkehr zur Bücherliste, erneutem Öffnen und Activity-Neustart erhalten.
- Die Lesevorschau zeigt bei vorhandener Textverankerung das Umfeld um den gespeicherten Cursor (`▏`) statt immer nur den Kapitelanfang. Die aktuelle Position wird gekennzeichnet. Die Scrollposition wird bei Öffnen nach dem ersten Layoutdurchlauf auf die native Ansicht angewendet, bei kurzem Inhalt auf den verfügbaren Bereich begrenzt.
- Die native System-Zurück-Geste bzw. -Taste und die sichtbare Schaltfläche `Zur Bücherliste` führen zur Bibliothek. `Buchmenü` enthält die vorhandene Funktion `Buch-Backup und Import`; Kapitel-CRUD bleibt erreichbar.
- Alte, aber korrekt signierte Snapshots mit strukturell gültiger, inzwischen verwaister Kapitel- oder Block-ID werden auf den ersten gültigen Kapitelanfang beziehungsweise den Anfang des vorhandenen Kapitels zurückgesetzt. Zu große oder ein UTF-16-Surrogatpaar teilende Offsets werden auf eine gültige Textposition gekürzt. Ein gültiger Anker wird unverändert übernommen. JSON, Prüfsumme und Dokumentstruktur müssen weiterhin vollständig gültig sein. Reines Lesen verändert die Datei nicht und überschreibt keine konkurrierenden Revisionen. Die Ansicht weist auf eine reparierte Position hin.
- Die Buchansicht ist weiterhin **nur lesend**. Sie zeichnet keine neue Cursorposition auf und überschreibt deshalb beim Zurückgehen nicht die vom eigentlichen A5-Prototyp beziehungsweise vom D-03-Modell gespeicherten Editor-Anker. Das produktive Erzeugen neuer Cursor-/Scrollpositionen und der direkte strukturierte A5-Editor sind W-01.

## Regressionen

`BookPositionNavigationInstrumentedTest` prüft das Öffnen einer gespeicherten Unicode-Textposition, Android-System-Zurück, erneut Öffnen, Activity-Recreation, Buchmenü und unveränderte Revisions-/Positionsdaten. Weitere Tests prüfen korrekt gehashte, aber veraltete Kapitel-/Blockanker und ein Offset mitten in einem Emoji; ein gefälschter Hash bleibt defekt. Bestehende Kapitel-, Speicher-, Backup-, Offline-Canvas- und A5-PDF-Tests müssen weiterhin grün bleiben. Ein Test auf einem echten Smartphone steht separat unter E-05 aus.
