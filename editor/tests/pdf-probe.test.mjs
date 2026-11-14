import test from "node:test";
import assert from "node:assert/strict";
import { A5_WIDTH_PX, A5_HEIGHT_PX, getPdfProbeOptions, bytesToBase64 } from "../dist/pdf-probe.js";

test("A5 is exported at print scale independent of editable zoom", () => {
  const old = { width: A5_WIDTH_PX, height: A5_HEIGHT_PX, scale: 0.6, margins: [54,50,54,50] };
  const pdf = getPdfProbeOptions(old);
  assert.equal(pdf.scale, 1);
  assert.equal(pdf.width, A5_WIDTH_PX);
  assert.equal(pdf.height, A5_HEIGHT_PX);
  assert.deepEqual(pdf.margins, old.margins);
  assert.equal(old.scale, 0.6);
});
test("PDF bridge base64 preserves arbitrary binary bytes", () => {
  assert.equal(bytesToBase64(new Uint8Array([37,80,68,70,0,255])), "JVBERgD/");
});

test("PDF vendor loading uses only locally copied UMD bundles", async () => {
  const { readFile, stat } = await import("node:fs/promises");
  const { resolve } = await import("node:path");
  const root = resolve(import.meta.dirname, "..");
  for (const file of ["jspdf.umd.min.js", "canvas-editor-pdf.umd.js"]) {
    assert.ok((await stat(resolve(root, "dist", "vendor", file))).size > 1000);
  }
  const source = await readFile(resolve(root, "src", "pdf-probe.ts"), "utf8");
  assert.match(source, /loadLocalScript\("\.\/vendor\/jspdf\.umd\.min\.js"\)/);
  assert.match(source, /loadLocalScript\("\.\/vendor\/canvas-editor-pdf\.umd\.js"\)/);
  assert.doesNotMatch(source, /https?:\/\//);
});
