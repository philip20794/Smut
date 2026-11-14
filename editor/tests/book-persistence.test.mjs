import test from "node:test";
import assert from "node:assert/strict";
import { createEmptyBook } from "../dist/book-model.js";
import {
  createBookLoadRequest, createBookSaveRequest, parseBookReply, BookAutosaveQueue,
} from "../dist/book-persistence.js";

const book = () => createEmptyBook("book1", "Mein Buch", "2026-11-14T10:00:00.000Z", "chap1");
const position = () => ({ chapterId: "chap1", blockId: null, offset: 0, scrollY: 99 });

test("book bridge keeps revision/position and rejects malformed data", () => {
  assert.equal(JSON.parse(createBookLoadRequest("load", "book1")).bookId, "book1");
  const save = JSON.parse(createBookSaveRequest("save", book(), 0, position()));
  assert.equal(save.type, "book-save");
  assert.equal(save.position.scrollY, 99);
  assert.equal(parseBookReply(JSON.stringify({
    version: 1, type: "book-save-result", requestId: "save", state: "saved", revision: 1,
  }), "save").revision, 1);
  const loaded = parseBookReply(JSON.stringify({
    version: 1, type: "book-load-result", requestId: "load", state: "ok", revision: 1,
    document: book(), position: position(), recovered: true,
  }), "load");
  assert.equal(loaded?.state, "ok");
  assert.equal(loaded?.recovered, true);
  assert.equal(parseBookReply(JSON.stringify({
    version: 1, type: "book-save-result", requestId: "save", state: "saved", revision: -1,
  }), "save"), null);
  assert.equal(parseBookReply(JSON.stringify({
    version: 1, type: "book-save-result", requestId: "save", state: "saved", revision: 1, extra: true,
  }), "save"), null);
  assert.throws(() => createBookSaveRequest("save", book(), -1, position()));
  assert.throws(() => createBookSaveRequest("save", book(), 0, { ...position(), chapterId: 123 }));
  assert.throws(() => createBookLoadRequest("load", "../secrets"));
});

test("rapid changes are coalesced into one sequential atomic save", async () => {
  const calls = [];
  const queue = new BookAutosaveQueue(0, async (doc, pos, revision) => {
    calls.push({ doc, pos, revision });
    return revision + 1;
  }, 60_000);
  queue.changed(book(), position());
  queue.changed({ ...book(), title: "Neuer Titel" }, position());
  assert.equal(queue.dirty, true);
  assert.equal(await queue.flush(), true);
  assert.equal(queue.dirty, false);
  assert.equal(queue.currentRevision, 1);
  assert.equal(calls.length, 1);
  assert.equal(calls[0].doc.title, "Neuer Titel");
  queue.dispose();
});

test("edits arriving during a save are sequenced, never overwrite later data", async () => {
  const calls = [];
  let resolveFirst;
  const queue = new BookAutosaveQueue(0, (doc, _pos, rev) => {
    calls.push([doc.title, rev]);
    if (rev === 0) return new Promise((resolve) => { resolveFirst = resolve; });
    return Promise.resolve(rev + 1);
  }, 60_000);
  queue.changed(book(), position());
  const saving = queue.flush();
  await Promise.resolve();
  queue.changed({ ...book(), title: "Zweite Fassung" }, position());
  resolveFirst(1);
  assert.equal(await saving, true);
  assert.deepEqual(calls, [["Mein Buch", 0], ["Zweite Fassung", 1]]);
  assert.equal(queue.currentRevision, 2);
  queue.dispose();
});

test("failed autosave keeps dirty manuscript and retries without revision gaps", async () => {
  let failures = 1;
  const queue = new BookAutosaveQueue(0, async (_doc, _pos, rev) => {
    if (failures--) throw new Error("storage full");
    return rev + 1;
  }, 60_000);
  queue.changed(book(), position());
  assert.equal(await queue.flush(), false);
  assert.equal(queue.dirty, true);
  assert.equal(queue.currentRevision, 0);
  assert.equal(await queue.flush(), true);
  assert.equal(queue.currentRevision, 1);
  queue.dispose();
});
