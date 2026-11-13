# Entwicklungs- und Wiederaufnahmeprotokoll für Codex

Diese Datei beschreibt, wie ein neuer Agent dieselbe Arbeit nach Kontextverlust, unterbrochener Sitzung oder fehlgeschlagenem CI-Lauf **ohne Nachfragen und ohne Doppelarbeit** fortsetzt. Sie ist eine Ergänzung zu [AGENTS.md](../AGENTS.md).

## 1. Erste Schritte bei JEDER neuen Sitzung

1. Repository `philip20794/Smut` öffnen und die aktuelle `main`-Revision ansehen. Nicht von einer älteren Chatnachricht oder einem zuvor gespeicherten Commit ausgehen.
2. In dieser Reihenfolge lesen:
   - `AGENTS.md`
   - `docs/PROGRESS.md` (**aktiver Stand**)
   - `docs/IMPLEMENTATION_PLAN.md` (**Aufgabenkatalog**)
   - `README.md`, `docs/PRODUCT.md`, `docs/ARCHITECTURE.md`, `docs/ACCEPTANCE.md`, `docs/CI-CD.md`
   - `docs/DECISIONS.md` (**getroffene technische Entscheidungen**).
3. `git status`, relevante letzte Commits, Verzeichnisstruktur und für Codeänderungen letzte GitHub-Actions-Läufe prüfen.
4. Reichen Angaben im Tracker nicht, den tatsächlichen Stand aus implementiertem Code und Tests rekonstruieren; dann **erst** `PROGRESS.md` korrigieren. Nicht blind fortfahren.
5. Die nächste ausführbare Task-ID anhand der Abhängigkeiten wählen. Falls eine Aufgabe `IN_PROGRESS` war, zunächst ihre Teilumsetzung und CI prüfen, statt erneut von vorne anzufangen.
6. Konkreten nächsten Schritt, den Status und Blocker in `PROGRESS.md` eintragen und committen.
7. Arbeit in möglichst kleinen, unabhängig prüfbaren Schritten ausführen; nach jedem relevanten Teilschritt Code + Nachweis + Fortschritt committen.

## 2. Status-Lebenszyklus

`TODO → IN_PROGRESS → IN_REVIEW → DONE`

`IN_PROGRESS` oder `IN_REVIEW` kann zu `BLOCKED` werden; nach Behebung des Blockers wieder `IN_PROGRESS`/ `IN_REVIEW`. Ein zuvor bestandenes Ergebnis kann bei späterer Regression wieder geöffnet werden; Grund dokumentieren.

- **TODO:** noch nicht begonnen.
- **IN_PROGRESS:** konkrete Arbeit begonnen; muss einen Handoff mit nächstem Schritt besitzen.
- **IN_REVIEW:** Code ist vorhanden, aber Pflichtprüfungen/Feedback stehen noch aus.
- **BLOCKED:** klarer Hinderungsgrund mit Nachweis und Entsperrbedingung.
- **DONE:** Abnahmekriterien und relevante Tests bestanden, CI grün, Nachweis im Tracker.

Keine bloßen Codeänderungen als `DONE` bewerten. Ein Bericht „Tests sollten funktionieren“ ist kein Prüfbeleg.

## 3. Definition einer abgeschlossenen Aufgabe

- Der festgelegte Liefergegenstand liegt im Repository.
- Es existieren passende Tests bzw. nachvollziehbare Dokumentationsprüfung.
- Alle zum Arbeitspaket gehörenden Pflichtprüfungen sind tatsächlich grün.
- Für Änderungen nach F-06 steht bei erfolgreich geprüftem `main`-Build eine installierbare Entwicklungs-APK als Artifact bereit.
- Bei PDF- oder UI-Arbeiten sind Screenshots/Golden-Tests/Emulatorberichte soweit erforderlich vorhanden.
- `docs/PROGRESS.md` enthält Task-ID, tatsächlichen Git-Commit oder Workflow-Run, Ergebnis, offene Risiken und Folgetask.
- Benutzer-Abnahme auf realer Hardware bei `E-05`, `Q-04` und `R-03` darf **nur mit ausdrücklichem tatsächlichem Befund** als erledigt markiert werden.

Für frühe Basisaufgaben, bevor CI existiert, den nachweisbaren lokalen oder Cloud-Buildstatus ehrlich ausweisen; fehlende CI darf ab F-04 nicht als Normalfall fortbestehen.

## 4. Ablauf innerhalb einer Sitzung

1. **Reconcile:** prüfen, ob Task nicht schon implementiert ist; verbleibende Lücken benennen.
2. **Markieren:** im Tracker `IN_PROGRESS`, nächsten kleinsten Schritt notieren.
3. **Implementieren:** nur das aktuelle Arbeitspaket plus unmittelbar nötige Test-/Buildhilfen, keine nicht beauftragten Features.
4. **Prüfen:** Unit-/Integrationstests, Codequalität; nach verfügbarer CI echte Workflow-Ergebnisse abwarten/auswerten.
5. **Fehler korrigieren:** beim roten Check möglichst gezielte Korrektur, keine unkontrollierten Test-Skips.
6. **Sichern:** Commits mit Task-ID (z. B. `F-02: Android-Startprojekt anlegen`). Bei längeren Arbeiten Zwischenstand mit klarer Kennzeichnung einchecken.
7. **Nachtragen:** Tracker mit Testergebnis, Commit/Run-Link, ggf. Screenshot und verbleibenden Problemen aktualisieren.
8. **Abschließen:** `DONE` nur nach Belegen; ansonsten `IN_REVIEW` oder `BLOCKED`. Nächste Aufgabe sichtbar vormerken.

Wenn Zeit, Kontext oder Werkzeugzugriff enden, **zuerst** einen kurzen, korrekten Übergabeeintrag committen. Keine Hintergrundfortsetzung versprechen, die nicht tatsächlich eingerichtet ist.

## 5. Reconciliation nach Abbruch oder Kontextverlust

| Feststellung | Maßnahme |
| --- | --- |
| Tracker sagt `IN_PROGRESS`, Code fehlt | Task fortsetzen, konkreten fehlenden Schritt eintragen |
| Tracker sagt `TODO`, Code liegt schon vor | Tests und CI prüfen; erst dann Tracker auf korrekten Status bringen |
| Tracker sagt `DONE`, CI ist rot | Task wieder öffnen oder Regression als neue Fehleraufgabe dokumentieren; keinen Erfolg behaupten |
| Code vorhanden, aber kein Commit | Änderungen sichern, Status und Prüfbeleg aktualisieren |
| CI grün, aber kein APK-Artefakt | F-06 oder betroffene Pipeline reparieren, nicht als vollständigen Build werten |
| Touch/IME/PDF-Kandidat scheitert | P1-ADR mit `NO-GO` aktualisieren, Fallback evaluieren, keine abhängigen Features blind bauen |
| Manueller Smartphone-Test fehlt | `BLOCKED` samt konkreter Bitte für den Nutzer; nur unabhängig ausführbare Aufgaben fortsetzen |
| Release-Signierschlüssel fehlen | R-01 `BLOCKED`; sichere Einrichtung und externe Backup-Schritte dokumentieren, niemals geheimen Schlüssel einchecken |
| Plan und Code widersprechen sich | Primäranforderungen lesen, Abweichung begründen, Plan/Status synchronisieren |

## 6. Verlässliche Belege und Links

- **Git:** stabile Commit-URLs wie `https://github.com/philip20794/Smut/commit/<sha>` und Dateipfade.
- **CI:** konkrete GitHub-Actions-Run-URL, Jobname, Testreport und Ergebnis; nicht nur der Name „Actions“.
- **APK:** Artifact-Name und Workflow-Run/Release-URL; Debug-APKs unter Artifacts sind temporär.
- **Abnahme:** bei realem Gerät konkretes Nutzerfeedback, Datum und getestete APK-Version.
- **Entscheidungen:** begründeter Eintrag in `docs/DECISIONS.md` einschließlich Alternativen und Folgen.
- **Blocker:** was fehlt, warum es fehlt, genaue Entsperrbedingung und nächste ohne Blocker machbare Aufgabe.

Keine erfundenen Prüf- oder Release-Links. Wenn ein Test nicht ausgeführt werden konnte, ausdrücklich „nicht ausgeführt“ vermerken.

## 7. Qualitätssicherung und Architekturentscheidungen

- Vor P2 darf der Editor-Stack nicht allein aufgrund der README als endgültig gelten.
- P1 muss mindestens einen funktionierenden Android-IME-/Touch-Prototyp, eine durchsuchbare A5-PDF und eine dokumentierte Entscheidung liefern.
- Der konkrete Nutzer hat keinen lokalen Entwicklungsrechner. Cloud-Tests müssen deshalb so eingerichtet werden, dass Fehler über GitHub nachvollziehbar sind.
- Eine Compose/WebView-basierte Eingabe kann sich auf verschiedenen Android-Tastaturen unterscheiden. Emulator-Test ist notwendig, reale Geräteabnahme für Release ebenso.
- Android-App bleibt offlinefähig; keine HTTP-Assets im Produktivbetrieb.
- Vektor-/Text-PDF statt Screenshot-PDF; Buchquellen werden getrennt vom Export gespeichert.
- Unklarheiten und neue Risiken als Entscheidung, Blocker oder Task festhalten; niemals in Lücken des Chatkontexts verschwinden lassen.

## 8. Vorlage für Handoff in PROGRESS.md

```text
Datum:
Aktive Task-ID:
Status:
Letzter Code-Commit:
Letzter CI-Lauf und Ergebnis:
Was funktioniert nachweislich:
Was fehlt / ist blockiert:
Nächster kleinster ausführbarer Schritt:
Entscheidung / Risiken:
Nutzeraktion nötig?:
```

Die Vorlage ist ein Pflichtschema für den Tracker, aber **kein Ersatz** für die tabellarische Task- und Phasenübersicht.

## 9. Abschluss

Der Agent soll nicht wiederholt neue Implementierungspläne entwerfen, wenn bereits ein gültiger vorliegt. Stattdessen den nächsten überprüfbaren Task ausführen, dessen Fortschritt festhalten und anschließend fortfahren, soweit die Sitzung und Werkzeuge dies ermöglichen. Ein automatischer Weiterlauf über beendete Sitzungen hinweg ist **nicht** allein durch diese Datei gewährleistet; die Dokumentation macht eine neue Sitzung jedoch nahtlos anschlussfähig.
