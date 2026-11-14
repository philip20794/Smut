import { copyFile, cp, mkdir, stat } from "node:fs/promises";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const dist = resolve(root, "dist");
const vendor = resolve(root, "node_modules", "@hufe921", "canvas-editor", "dist");
const pdfVendor = resolve(root, "node_modules", "canvas-editor-pdf", "dist", "canvas-editor-pdf.umd.js");
const jsPdfVendor = resolve(root, "node_modules", "jspdf", "dist", "jspdf.umd.min.js");
await mkdir(dist, { recursive: true });
for (const file of ["index.html", "style.css"]) {
  await copyFile(resolve(root, "src", file), resolve(dist, file));
}
// Immer mit festem npm-Lockfile bereitgestellter Quelltext, kein externer
// Webserver und kein Laden über ein CDN zur Laufzeit.
await cp(vendor, resolve(dist, "vendor"), { recursive: true, force: true });
await stat(resolve(dist, "vendor", "canvas-editor.js"));
// PDF engine is copied into the APK at BUILD time; no runtime CDN or fetch.
await copyFile(pdfVendor, resolve(dist, "vendor", "canvas-editor-pdf.umd.js"));
await copyFile(jsPdfVendor, resolve(dist, "vendor", "jspdf.umd.min.js"));
await stat(resolve(dist, "vendor", "canvas-editor-pdf.umd.js"));
await stat(resolve(dist, "vendor", "jspdf.umd.min.js"));
