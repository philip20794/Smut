import { copyFile, cp, mkdir, stat } from "node:fs/promises";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const dist = resolve(root, "dist");
const vendor = resolve(root, "node_modules", "@hufe921", "canvas-editor", "dist");
await mkdir(dist, { recursive: true });
for (const file of ["index.html", "style.css"]) {
  await copyFile(resolve(root, "src", file), resolve(dist, file));
}
// Immer mit festem npm-Lockfile bereitgestellter Quelltext, kein externer
// Webserver und kein Laden über ein CDN zur Laufzeit.
await cp(vendor, resolve(dist, "vendor"), { recursive: true, force: true });
await stat(resolve(dist, "vendor", "canvas-editor.js"));
