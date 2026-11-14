import test from "node:test";
import assert from "node:assert/strict";
import {
  BOOK_SCHEMA_VERSION, DEFAULT_BOOK_LAYOUT, createEmptyBook,
  parseBookDocument, serializeBookDocument, validateBookDocument,
} from "../dist/book-model.js";
import { migratePrototypeSnapshot } from "../dist/legacy-migration.js";

const NOW = "2026-11-14T10:00:00.000Z";
const opts = { bookId: "buch-1", chapterId: "kapitel-1", title: "Äpfel & Öl", chapterTitle: "Anfang", createdAt: NOW };
const make = () => createEmptyBook("buch-1", "Äpfel & Öl", NOW, "kapitel-1");
const clone = value => structuredClone(value);

test("D-01: leeres A5-Buch hat ein editorunabhängiges Schema", () => {
  const book = make();
  assert.equal(book.schemaVersion, BOOK_SCHEMA_VERSION);
  assert.deepEqual(book.layout, DEFAULT_BOOK_LAYOUT);
  assert.equal(book.chapters[0].title, "Kapitel 1");
  assert.deepEqual(parseBookDocument(serializeBookDocument(book)), book);
});

test("D-01: Kapitelreihenfolge, Überschrift, Absatz, Trenner, Seitenumbruch und Unicode bleiben erhalten", () => {
  const book = make();
  const content = {
    ...book, author: "Runa", chapters: [
      { id: "kapitel-2", title: "Zweites", blocks: [
        { id: "h2", type: "heading", level: 2, runs: [{ text: "Überschrift" }] },
        { id: "p2", type: "paragraph", runs: [{ text: "Grüße – 🙂", marks: { bold: true, italic: true, fontSizePt: 13, fontFamily: "serif" } }] },
        { id: "s2", type: "sceneBreak" }, { id: "pb2", type: "pageBreak" },
      ] },
      { id: "kapitel-1", title: "Erstes", blocks: [{ id: "p1", type: "paragraph", runs: [] }] },
    ],
  };
  assert.deepEqual(parseBookDocument(serializeBookDocument(content)), content);
  assert.deepEqual(parseBookDocument(serializeBookDocument(content)).chapters.map(c => c.id), ["kapitel-2", "kapitel-1"]);
});

test("D-01: ungültige Versionen, unbekannte Felder, doppelte IDs, kaputte Daten und Grenzen werden abgewiesen", () => {
  const changes = [
    x => { x.schemaVersion = 2; },
    x => { x.title = ""; },
    x => { x.updatedAt = "2026-11-13T10:00:00.000Z"; },
    x => { x.createdAt = "2026-02-30T00:00:00.000Z"; },
    x => { x.layout.pageWidthMm = 200; },
    x => { x.layout.fontSizePt = Number.NaN; },
    x => { x.layout.marginsMm.top = Infinity; },
    x => { x.chapters[0].id = x.id; },
    x => { x.chapters[0].blocks = [{ id: "block-1", type: "unknown" }]; },
    x => { x.chapters[0].blocks = [{ id: "block-1", type: "heading", level: 3, runs: [{ text: "X" }] }]; },
    x => { x.chapters[0].blocks = [{ id: "block-1", type: "paragraph", runs: [{ text: "x", marks: { bold: false } }] }]; },
    x => { x.chapters[0].blocks = [{ id: "block-1", type: "paragraph", runs: [{ text: "x", javascript: "bad" }] }]; },
    x => { x.hacker = true; },
  ];
  for (const mutate of changes) {
    const candidate = clone(make());
    mutate(candidate);
    assert.throws(() => serializeBookDocument(candidate), /Ungültiges Buchformat/);
  }
  for (const json of ["bad", "{}", JSON.stringify({ ...make(), schemaVersion: 10 }), " ".repeat(8_000_001)]) {
    assert.throws(() => parseBookDocument(json), /Ungültiges Buchformat/);
  }
  assert.throws(() => validateBookDocument(null));
});

test("D-01: deterministische Migration des E-02-Prototyps erhält Absatztext und Inline-Markierungen", () => {
  const source = { main: [
    { value: "Äpfel", bold: true },
    { value: " und Öl\nGrüße", italic: true },
    { value: "\n\nEnde", font: "serif", size: 12 },
  ] };
  const migrated = migratePrototypeSnapshot(source, opts);
  assert.deepEqual(migrated.warnings, []);
  assert.equal(migrated.originalJson, JSON.stringify(source));
  assert.equal(migrated.book.chapters[0].blocks.length, 4);
  assert.deepEqual(migrated.book.chapters[0].blocks.map(b => b.runs.map(r => r.text).join("")),
    ["Äpfel und Öl", "Grüße", "", "Ende"]);
  assert.deepEqual(migrated.book.chapters[0].blocks[0].runs[0].marks, { bold: true });
  assert.deepEqual(migrated.book.chapters[0].blocks[0].runs[1].marks, { italic: true });
  assert.deepEqual(parseBookDocument(serializeBookDocument(migrated.book)), migrated.book);
  assert.deepEqual(migratePrototypeSnapshot(source, opts).book, migrated.book);
});

test("D-01: unbekannte Canvas-Metadaten werden gemeldet und Original bleibt unverändert", () => {
  const source = { main: [{ value: "Text", customVendorAttribute: { nested: "nicht verlieren" } }] };
  const migration = migratePrototypeSnapshot(source, opts);
  assert.match(migration.warnings.join(" "), /customVendorAttribute/);
  assert.deepEqual(JSON.parse(migration.originalJson), source);
  assert.equal(migration.book.chapters[0].blocks[0].runs[0].text, "Text");
});

test("D-01: nicht bekannte Strukturtypen und beschädigte Prototypen verhindern stille Datenverluste", () => {
  for (const source of [
    null, { main: "string" }, { main: [{ value: 5 }] },
    { main: [{ value: "Text", type: "picture" }] },
    { main: [{ value: "Text", bold: "yes" }] },
    { main: [{ value: "Text", font: 42 }] },
    { main: [{ value: "Text\rOops" }] },
  ]) assert.throws(() => migratePrototypeSnapshot(source, opts), /Canvas-Migration/);
  assert.deepEqual(migratePrototypeSnapshot({ main: [{ value: "A\r\nB" }] }, opts).book.chapters[0].blocks.map(x=>x.runs[0]?.text), ["A", "B"]);
});
