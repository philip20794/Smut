import test from "node:test";
import assert from "node:assert/strict";
import { createEmptyBook, serializeBookDocument } from "../dist/book-model.js";
import { canvasForChapter, reconcileCanvasChapter, textForChapter } from "../dist/chapter-canvas.js";
const now = "2026-11-14T10:00:00.000Z";
function book(blocks) {
  const base = createEmptyBook("book123", "Test", now, "chapter123");
  return { ...base, chapters: [{ id: "chapter123", title: "Kapitel 1", blocks }] };
}
const p = (id, ...runs) => ({ id, type: "paragraph", runs });
const run = (text, marks) => marks ? { text, marks } : { text };
function changed(doc, value, id = () => "newPara") {
  return reconcileCanvasChapter(doc, "chapter123", { main: [{ value }] }, id, 125);
}
test("W-01: structured runs project to the original A5 Canvas element format", () => {
  const doc = book([p("p1", run("Hallo ", { italic: true }), run("Welt")), p("p2", run("Nacht"))]);
  assert.deepEqual(canvasForChapter(doc, "chapter123").main.map(e => e.value), ["Hallo ", "Welt", "\n", "Nacht"]);
  assert.equal(textForChapter(doc, "chapter123"), "Hallo Welt\nNacht");
});
test("W-01: insertion keeps existing rich runs and independent chapters untouched", () => {
  const doc = book([p("p1", run("Äpfel", { italic: true }), run(" und Welt"))]);
  const snapshot = structuredClone(doc);
  const result = changed(doc, "Äpfel schön und Welt");
  assert.equal(result.changed, true);
  assert.deepEqual(result.document.chapters[0].blocks[0].runs, [
    run("Äpfel", { italic: true }), run(" schön und Welt"),
  ]);
  assert.deepEqual(doc, snapshot);
  assert.deepEqual(result.position, {chapterId: "chapter123", blockId: "p1", offset: 12, scrollY: 125});
  assert.ok(serializeBookDocument(result.document));
});
test("W-01: split creates a new paragraph and keeps old identity", () => {
  const doc = book([p("p1", run("HalloWelt"))]);
  const result = changed(doc, "Hallo\nWelt");
  assert.deepEqual(result.document.chapters[0].blocks.map(b => b.id), ["p1", "newPara"]);
  assert.deepEqual(result.document.chapters[0].blocks.map(b => b.runs.map(r => r.text).join("")), ["Hallo", "Welt"]);
});
test("W-01: reopening and second paragraph edit preserves unrelated block and ID", () => {
  const doc = book([p("p1", run("Erster Absatz")), p("p2", run("Zweiter Absatz", { bold: true }))]);
  const result = changed(doc, "Erster Absatz\nZweiter langer Absatz");
  assert.deepEqual(result.document.chapters[0].blocks.map(b => b.id), ["p1", "p2"]);
  assert.deepEqual(result.document.chapters[0].blocks[1].runs, [run("Zweiter langer Absatz", {bold: true})]);
});
test("W-01: deleting paragraph separator retains first original block ID", () => {
  const doc = book([p("p1", run("Hallo")), p("p2", run("Welt"))]);
  const result = changed(doc, "HalloWelt");
  assert.deepEqual(result.document.chapters[0].blocks.map(b => b.id), ["p1"]);
  assert.equal(textForChapter(result.document, "chapter123"), "HalloWelt");
});
test("W-01: empty book starts typing without touching the legacy format", () => {
  const doc = book([]);
  const result = changed(doc, "Hallo 😀");
  assert.equal(result.document.chapters[0].blocks[0].id, "newPara");
  assert.equal(result.position.offset, 8);
});
test("W-01: unsupported structures reject edits before data loss", () => {
  const doc = book([{ id: "break1", type: "pageBreak" }]);
  assert.throws(() => canvasForChapter(doc,"chapter123"), /Strukturelemente/);
  assert.throws(() => changed(doc, "weg"), /Strukturelemente/);
});
test("W-01: no change does not save or modify a valid manuscript", () => {
  const doc = book([p("p1", run("ABC", { bold: true }))]);
  const r = changed(doc, "ABC");
  assert.equal(r.changed, false);
  assert.equal(r.document, doc);
});
test("W-01: unknown Canvas element types fail closed", () => {
  assert.throws(() => reconcileCanvasChapter(book([]),"chapter123", {main:[{type:"image",value:"x"}]}, () => "x"), /nicht unterstützten/);
});
