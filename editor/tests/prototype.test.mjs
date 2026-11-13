import test from "node:test";
import assert from "node:assert/strict";
import {
  INITIAL_DOCUMENT, createDocumentRequest, isPrototypeData, parseDocumentResponse,
} from "../dist/prototype.js";

test("Startdaten enthalten deutsche Unicode-Zeichen", () => {
  assert.match(INITIAL_DOCUMENT.main[0].value, /Äpfel.*Grüße.*„Hallo“/);
  assert.ok(isPrototypeData(INITIAL_DOCUMENT));
});

test("Load und Save sind versioniert, ohne losen JavaScript-Code", () => {
  assert.deepEqual(JSON.parse(createDocumentRequest("load", "load-1")), {
    version: 1, type: "load", requestId: "load-1",
  });
  assert.deepEqual(JSON.parse(createDocumentRequest("save", "save-3", INITIAL_DOCUMENT)).document, INITIAL_DOCUMENT);
  assert.throws(() => createDocumentRequest("save", "bad id", INITIAL_DOCUMENT), /Request-ID/);
  assert.throws(() => createDocumentRequest("save", "save-1", undefined), /Dokument/);
});

test("Nur gültige, begrenzte native Antworten werden akzeptiert", () => {
  const answer = { version: 1, type: "load-result", requestId: "load-1", document: INITIAL_DOCUMENT };
  assert.deepEqual(parseDocumentResponse(JSON.stringify(answer)), answer);
  assert.deepEqual(parseDocumentResponse('{"version":1,"type":"save-result","requestId":"save-1","saved":true}'),
    { version:1, type:"save-result", requestId:"save-1", saved:true });
  for (const bad of [
    '{"version":1,"type":"load-result","requestId":"x","document":{"main":"bad"}}',
    '{"version":1,"type":"load-result","requestId":"x","document":{"main":[]}, "extra":1}',
    '{"version":1,"type":"save-result","requestId":"x","saved":"yes"}',
    '{"version":9,"type":"load-result","requestId":"x","document":null}',
    "<script>alert(1)</script>",
    "x".repeat(750_001),
  ]) assert.equal(parseDocumentResponse(bad), null);
});
