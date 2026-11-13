import { copyFile, mkdir } from "node:fs/promises";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
await mkdir(resolve(root, "dist"), { recursive: true });
for (const file of ["index.html", "style.css"]) {
  await copyFile(resolve(root, "src", file), resolve(root, "dist", file));
}
