# Lokaler Editorcontainer (`editor/`)

**F-03 umgesetzt und über [GitHub Actions 43445552751](https://github.com/philip20794/Smut/actions/runs/43445552751) erfolgreich gebaut und getestet.** TypeScript (5.9.3) wird ohne Rich-Text-Bibliothek zu statischen ES-Modulen kompiliert. Der Android-Gradle-Build installiert die gepinnten npm-Pakete und kopiert `dist/` automatisch als `assets/editor/` in die APK. `dist/` und `node_modules/` gehören nicht ins Git.

```bash
cd editor
npm ci --ignore-scripts
npm run typecheck
npm run lint
npm test
npm run build
```

- `src/index.html`, `style.css`, `main.ts`: rein lokale, noch nicht editierbare A5-Containeransicht.
- `src/bridge.ts`: Handshake mit `version: 1`, `type: "ready"`/`"ready-ack"` und `requestId` (1–40 Zeichen, `[A-Za-z0-9_-]`). Andere Nachrichten werden verworfen.
- `tests/bridge.test.mjs`: erlaubte/ungültige Nachrichten und Schema-Test.
- `scripts/lint.mjs`: Sicherung gegen versehentliche externe URL-Referenzen, `eval`, fehlende CSP-Grenzen. Kein Ersatz für die späteren umfangreichen Lint-Regeln aus F-04.
- `scripts/copy-static.mjs`: kopiert HTML/CSS nach `dist/`; keine CDN-, Internet-, Webserver- oder Cloud-Abhängigkeit zur Laufzeit.

**Sicherheit:** Die App lädt nur `https://appassets.androidplatform.net/assets/editor/*` über `WebViewAssetLoader` aus der APK. Fremde Requests werden blockiert, kein `INTERNET`-Manifest-Recht, kein `file://`- oder `content://`-Zugriff. `SmutAndroid.postMessage` verarbeitet in F-03 ausschließlich die `ready`-Nachricht und antwortet mit einer JSON-quotierten Quittung. Es gibt absichtlich **keinen** Datei- oder Buch-Datenzugriff.

In F-05 muss die WebView auf dem Emulator tatsächlich gestartet und der Bridge-Handshake beobachtet werden. Die endgültige Rich-Text-Engine, PDF-Technik und Geräteakzeptanz gehören zu P1, nicht zu F-03.

Siehe [BUILD.md](../docs/BUILD.md), [ARCHITECTURE.md](../docs/ARCHITECTURE.md) und [PROGRESS.md](../docs/PROGRESS.md).

**Cloud-Gate P0:** [43451063955](https://github.com/philip20794/Smut/actions/runs/43451063955) mit erfolgreich ausgeführtem API-35-Offline-WebView-Smoketest. E-01 bewertet [Canvas Editor 1.1.0 und Fallbacks](../docs/EDITOR_EVALUATION.md); E-02 fügt erst danach die echte Rich-Text-Bearbeitung hinzu.

## E-02: echter Canvas-A5-Editor (Prototyp)

- `@hufe921/canvas-editor@1.1.0` wird beim Build via npm-Lockfile unter `dist/vendor/` vollständig in die APK kopiert und ausschließlich lokal importiert. Keine endgültige Editorwahl bis zum E-04-GO.
- A5-Prototyp (559 × 794 CSS-Pixel bei 96 dpi, proportional zu 148 × 210 mm) bietet eine echte Canvas-Schreibfläche. Die Pixelmaße sind **noch kein druckvalidierter A5-PDF-Export**.
- Die native JSON-Bridge verarbeitet `ready`, `load`, `save` mit Version/Request-ID, begrenzter Nachrichtenlänge und validierten Dokumentelementen. `AtomicFile` schützt die temporäre private Testdatei beim Schreiben.
- Der Editor speichert als vorläufiges Bibliotheksformat, nicht als endgültiges Buch-/Kapitelmodell. Datenmodell, Backups und sichere Migration folgen P2.
- Tests: Unit-Verträge für erlaubte/verweigerte Dokumente und instrumentierte Prüfung von Start, Speichern, Neustart und Reload. **Noch erforderlich:** Cursor-/IME-/Autokorrektur-/Backspace-/Enter-Tests auf Android; bevor diese grün sind, darf E-02 nicht `DONE` sein.
