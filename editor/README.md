# Lokaler Buchseiten-Editor (`editor/`)

**Status: F-03 offen.** Dieser Ordner reserviert die TypeScript-Editoroberfläche, die später als lokale WebView-Assets in die Android-App eingebettet wird. Für F-01 wurden weder ein npm-Paket noch ein Webserver oder eine Editor-Bibliothek eingeführt.

Grundsätze:
- Ohne CDN oder Netzwerkzugriff zur Laufzeit; sämtliche JavaScript-, CSS- und Font-Dateien werden im APK gebündelt.
- Node.js-Version aus [`.node-version`](../.node-version) verwenden, npm samt committeter `package-lock.json` ab F-03.
- TypeScript-Build und Bridge-API sind in F-03 zu realisieren; Rich-Text-Engine bleibt bis P1-Technologieprüfung offen.
- Im Entwicklungsprozess darf der Editor lokal gebaut werden. Die App darf keinen externen Server benötigen.

Siehe [Build-Baseline](../docs/BUILD.md) und [Implementierungsplan](../docs/IMPLEMENTATION_PLAN.md).
