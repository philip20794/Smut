import test from "node:test";
import assert from "node:assert/strict";
import { createEmptyBook, parseBookDocument, serializeBookDocument } from "../dist/book-model.js";
import {
  applyEditOperation, applyHistoryOperation, createEditHistory, undoHistory, redoHistory,
} from "../dist/edit-operations.js";

const AT = "2026-11-14T10:00:00.000Z";
const p = (id, offset, chapterId = "c1") => ({ chapterId, blockId: id, offset });
const range = (a, b) => ({ anchor: a, focus: b });
const text = (book, chapter = 0, index = 0) =>
  book.chapters[chapter].blocks[index].runs.map(r => r.text).join("");
const b = (id, value, marks) => ({ id, type: "paragraph", runs: value === "" ? [] : [marks ? { text: value, marks } : { text: value }] });
const book = (blocks = [b("a", "abc")]) => {
  const empty = createEmptyBook("book-1", "Prosa", AT, "c1");
  return { ...empty, chapters: [{ id: "c1", title: "Kapitel 1", blocks }] };
};
const run = (document, op, trackedSelection) => applyEditOperation(document, op, trackedSelection);

test("D-02: Einfügen in einen Run erbt Schrift und ändert die Eingabe nicht", () => {
  const original = book([b("a", "ab", { italic: true })]);
  const snapshot = structuredClone(original);
  const result = run(original, { type: "insert", at: p("a", 1), text: "Ö🙂" });
  assert.equal(text(result.document), "aÖ🙂b");
  assert.deepEqual(result.document.chapters[0].blocks[0].runs, [{ text: "aÖ🙂b", marks: { italic: true } }]);
  assert.deepEqual(result.caret, p("a", 4));
  assert.deepEqual(result.selection, range(p("a", 4), p("a", 4)));
  assert.equal(result.changed, true);
  assert.deepEqual(original, snapshot);
  assert.deepEqual(parseBookDocument(serializeBookDocument(result.document)), result.document);
});

test("D-02: markierte Zeichen ersetzen und ursprüngliche Auswahlanker stabil verschieben", () => {
  const initial = book([b("a", "abcdef"), b("b", "uvwxyz")]);
  const selected = range(p("a", 2), p("b", 5));
  const tracked = range(p("a", 1), p("b", 6));
  const result = run(initial, { type: "replace", range: selected, text: "🙂" }, tracked);
  assert.deepEqual(result.document.chapters[0].blocks.map(x => x.id), ["a"]);
  assert.equal(text(result.document), "ab🙂z");
  assert.deepEqual(result.caret, p("a", 4));
  assert.deepEqual(result.selection, range(p("a", 1), p("a", 5)));
  assert.equal(text(initial, 0, 1), "uvwxyz");
});

test("D-02: reverse Auswahl über mehrere Absätze funktioniert und erhält den ersten Block", () => {
  const initial = book([b("a", "Hallo"), b("b", "Mitte"), b("c", "Welt")]);
  const result = run(initial, { type: "delete", range: range(p("c", 2), p("a", 2)) });
  assert.equal(text(result.document), "Halt");
  assert.deepEqual(result.document.chapters[0].blocks.map(x => x.id), ["a"]);
  assert.deepEqual(result.caret, p("a", 2));
});

test("D-02: Textformatierung funktioniert über Run- und Blockgrenzen, ohne andere Zeichen zu ändern", () => {
  const initial = book([
    { id: "a", type: "paragraph", runs: [{ text: "ab", marks: { italic: true } }, { text: "cd" }] },
    b("b", "ef"),
  ]);
  const applied = run(initial, { type: "format", range: range(p("a", 1), p("b", 1)), patch: { bold: true, fontSizePt: 14 } });
  assert.deepEqual(applied.document.chapters[0].blocks[0].runs, [
    { text: "a", marks: { italic: true } },
    { text: "b", marks: { italic: true, bold: true, fontSizePt: 14 } },
    { text: "cd", marks: { bold: true, fontSizePt: 14 } },
  ]);
  assert.deepEqual(applied.document.chapters[0].blocks[1].runs, [
    { text: "e", marks: { bold: true, fontSizePt: 14 } },
    { text: "f" },
  ]);
  const cleared = run(applied.document, {
    type: "format", range: range(p("a", 1), p("b", 1)),
    patch: { bold: false, fontSizePt: null },
  });
  assert.deepEqual(cleared.document.chapters[0].blocks[0].runs, initial.chapters[0].blocks[0].runs);
  assert.equal(text(cleared.document, 0, 1), "ef");
  assert.deepEqual(cleared.document.chapters[0].blocks[1].runs, initial.chapters[0].blocks[1].runs);
});

test("D-02: Absatz teilen/verbinden erhält Block-IDs, Marken und remappt Cursoranker", () => {
  const initial = book([b("a", "A🙂B", { italic: true }), b("z", "Ende")]);
  const split = run(initial, { type: "split", at: p("a", 3), newBlockId: "new-1" },
    range(p("a", 0), p("a", 4)));
  assert.deepEqual(split.document.chapters[0].blocks.map(x => x.id), ["a", "new-1", "z"]);
  assert.equal(text(split.document), "A🙂");
  assert.equal(text(split.document, 0, 1), "B");
  assert.deepEqual(split.selection, range(p("a", 0), p("new-1", 1)));
  assert.deepEqual(split.caret, p("new-1", 0));
  const merged = run(split.document, {
    type: "merge", chapterId: "c1", firstBlockId: "a", secondBlockId: "new-1",
  }, range(p("a", 1), p("new-1", 1)));
  assert.equal(text(merged.document), "A🙂B");
  assert.deepEqual(merged.selection, range(p("a", 1), p("a", 4)));
  assert.deepEqual(merged.caret, p("a", 3));
  assert.deepEqual(merged.document.chapters[0].blocks.map(x => x.id), ["a", "z"]);
});

test("D-02: Überschrift behält bei Split links ihren Stil; rechts entsteht ein Absatz", () => {
  const initial = book([{ id: "h", type: "heading", level: 2, runs: [{ text: "Titel" }] }]);
  const split = run(initial, { type: "split", at: p("h", 3), newBlockId: "neu" });
  assert.equal(split.document.chapters[0].blocks[0].type, "heading");
  assert.equal(split.document.chapters[0].blocks[1].type, "paragraph");
  assert.equal(text(split.document, 0, 1), "el");
  assert.throws(() => run(initial, { type: "split", at: p("h", 0), newBlockId: "neu" }), /Ungültiges Buchformat/);
});

test("D-02: Unicode-Grapheme sind nicht aufteilbar (Surrogat, Kombinationszeichen, Emoji mit ZWJ)", () => {
  const original = book([b("a", "A🙂e\u0301👩‍💻Z")]);
  for (const offset of [2, 4, 6, 7, 8, 9, 10]) {
    // Offsets within some graphemes must fail, not corrupt UTF-16.
    const input = p("a", offset);
    const allowed = [...new Intl.Segmenter("de", { granularity: "grapheme" }).segment(text(original))].some(x => x.index === offset);
    if (!allowed && offset !== text(original).length) {
      assert.throws(() => run(original, { type: "insert", at: input, text: "x" }), /Graphemgrenze/);
    }
  }
  const valid = run(original, { type: "insert", at: p("a", 3), text: "Ä" });
  assert.equal(text(valid.document), "A🙂Äe\u0301👩‍💻Z");
  assert.equal(valid.caret.offset, 4);
});

test("D-02: Graphemverschmelzung macht den Endcursor nicht ungültig", () => {
  const initial = book([b("a", "e"), b("b", "\u0301x")]);
  const merged = run(initial, { type: "merge", chapterId: "c1", firstBlockId: "a", secondBlockId: "b" });
  assert.equal(text(merged.document), "e\u0301x");
  // If a merge produces a combining grapheme, the caret must move to the grapheme end.
  assert.deepEqual(merged.caret, p("a", 2));
});

test("D-02: Strukturtrennungen und Kapitelgrenzen werden nicht stillschweigend gelöscht", () => {
  const withBreak = book([b("a", "abc"), { id: "separator", type: "sceneBreak" }, b("b", "def")]);
  const withChapters = { ...book(), chapters: [book().chapters[0], {
    id: "c2", title: "Zwei", blocks: [b("x", "mehr")],
  }] };
  assert.throws(() => run(withBreak, { type: "delete", range: range(p("a", 1), p("b", 2)) }), /Strukturblock/);
  assert.throws(() => run(withBreak, {
    type: "merge", chapterId: "c1", firstBlockId: "a", secondBlockId: "b",
  }), /benachbart/);
  assert.throws(() => run(withChapters, {
    type: "replace", range: range(p("a", 1), p("x", 1, "c2")), text: "x",
  }), /Kapitelübergreifende/);
});

test("D-02: Unbekannte IDs, doppelte Split-IDs, NUL, Zeilenumbrüche und fehlerhafte Marken schlagen atomar fehl", () => {
  const original = book();
  const before = structuredClone(original);
  for (const operation of [
    { type: "insert", at: p("unknown", 0), text: "x" },
    { type: "insert", at: p("a", -1), text: "x" },
    { type: "insert", at: p("a", 1.5), text: "x" },
    { type: "insert", at: p("a", 1), text: "a\nb" },
    { type: "insert", at: p("a", 1), text: "a\u0000b" },
    { type: "insert", at: p("a", 1), text: "x", marks: { bold: false } },
    { type: "delete", range: range(p("a", 0), p("nonexistent", 1)) },
    { type: "split", at: p("a", 1), newBlockId: "book-1" },
    { type: "format", range: range(p("a", 0), p("a", 2)), patch: { bold: 12 } },
    { type: "format", range: range(p("a", 0), p("a", 2)), patch: {} },
    { type: "format", range: range(p("a", 0), p("a", 2)), patch: { fontSizePt: Infinity } },
  ]) assert.throws(() => run(original, operation), /(Editieroperation|Ungültiges Buchformat)/);
  assert.deepEqual(original, before);
});

test("D-02: andere Kapitel und Anker bleiben bei Einfügen unverändert", () => {
  const initial = { ...book(), chapters: [book().chapters[0], {
    id: "c2", title: "Zwei", blocks: [b("x", "XYZ")],
  }] };
  const tracked = range(p("x", 1, "c2"), p("a", 3));
  const edited = run(initial, { type: "insert", at: p("a", 1), text: "++" }, tracked);
  assert.deepEqual(edited.selection, range(p("x", 1, "c2"), p("a", 5)));
  assert.equal(text(edited.document, 1), "XYZ");
});

test("D-02: Undo und Redo stellen Dokument und Auswahl nach mehreren Änderungen wieder her", () => {
  const original = book();
  let history = createEditHistory(original, range(p("a", 1), p("a", 3)));
  const operations = [
    { type: "insert", at: p("a", 3), text: "🙂" },
    { type: "split", at: p("a", 2), newBlockId: "next" },
    { type: "format", range: range(p("next", 0), p("next", 3)), patch: { bold: true } },
    { type: "replace", range: range(p("a", 1), p("next", 1)), text: "X" },
  ];
  const states = [history];
  for (const op of operations) {
    history = applyHistoryOperation(history, op);
    states.push(history);
  }
  assert.equal(history.past.length, 4);
  for (let i = operations.length - 1; i >= 0; i--) {
    history = undoHistory(history);
    assert.deepEqual(history.current, states[i].current);
  }
  assert.equal(undoHistory(history), history);
  for (let i = 1; i < states.length; i++) {
    history = redoHistory(history);
    assert.deepEqual(history.current, states[i].current);
  }
  assert.equal(redoHistory(history), history);
});

test("D-02: nach Undo löscht eine neue Aktion den Redo-Zweig", () => {
  let h = createEditHistory(book(), range(p("a", 0), p("a", 0)));
  h = applyHistoryOperation(h, { type: "insert", at: p("a", 1), text: "1" });
  h = applyHistoryOperation(h, { type: "insert", at: p("a", 2), text: "2" });
  const undone = undoHistory(h);
  assert.equal(undone.future.length, 1);
  h = applyHistoryOperation(undone, { type: "insert", at: p("a", 0), text: "N" });
  assert.equal(h.future.length, 0);
  assert.equal(text(h.current.document), "Na1bc");
});

test("D-02: History ist begrenzt und No-op erzeugt keinen zusätzlichen Undo-Eintrag", () => {
  let h = createEditHistory(book(), range(p("a", 0), p("a", 0)), 2);
  assert.equal(applyHistoryOperation(h, { type: "insert", at: p("a", 0), text: "" }), h);
  assert.equal(applyHistoryOperation(h, { type: "format", range: range(p("a", 1), p("a", 1)), patch: { italic: true } }), h);
  for (let i = 0; i < 5; i++) h = applyHistoryOperation(h, {
    type: "insert", at: p("a", i), text: "x",
  });
  assert.equal(h.past.length, 2);
  h = undoHistory(undoHistory(h));
  assert.equal(undoHistory(h), h);
});

test("D-02: deterministische Folge von Einfügen/Löschen behält den Originalzustand über Undo", () => {
  const original = book([b("a", "Äe\u0301🙂中文z")]);
  let h = createEditHistory(original, range(p("a", 0), p("a", 0)));
  const ops = [
    { type: "insert", at: p("a", 1), text: "12" },
    { type: "replace", range: range(p("a", 3), p("a", 5)), text: "ü" },
    { type: "delete", range: range(p("a", 3), p("a", 4)) },
    { type: "split", at: p("a", 3), newBlockId: "a-2" },
    { type: "merge", chapterId: "c1", firstBlockId: "a", secondBlockId: "a-2" },
  ];
  for (const op of ops) h = applyHistoryOperation(h, op);
  for (let i = 0; i < ops.length; i++) h = undoHistory(h);
  assert.deepEqual(h.current.document, original);
});
