import { readFile, readdir } from "node:fs/promises";
import { resolve } from "node:path";
import assert from "node:assert/strict";

const root = resolve(import.meta.dirname, "..");
const src = resolve(root, "src");
const resources = await readdir(src);
const html = await readFile(resolve(src, "index.html"), "utf8");
assert.match(html, /Content-Security-Policy/);
assert.match(html, /connect-src 'none'/);
assert.match(html, /frame-src 'none'/);
assert.match(html, /worker-src 'self' blob: data:/);
assert.doesNotMatch(html, /script-src[^;]*'unsafe-inline'/);
for (const filename of resources.filter((name) => /\.(html|css|ts)$/.test(name))) {
  const content = await readFile(resolve(src, filename), "utf8");
  assert.doesNotMatch(content, /https?:\/\//i, `${filename}: keine externen Laufzeit-Assets`);
  assert.doesNotMatch(content, /\beval\s*\(/, `${filename}: kein eval`);
}
console.log("Editor-Asset-Sicherheitslint bestanden.");
