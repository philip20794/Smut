import test from "node:test";
import assert from "node:assert/strict";
import { BRIDGE_VERSION, createReadyMessage, parseReadyAck } from "../dist/bridge.js";

test("Handshake erzeugt exakt den versionierten Vertrag", () => {
  assert.equal(
    createReadyMessage("startup"),
    JSON.stringify({ version: BRIDGE_VERSION, type: "ready", requestId: "startup" }),
  );
  assert.throws(() => createReadyMessage("x\";evil()"), /Ungültige/);
});

test("nur gültige native Quittungen werden akzeptiert", () => {
  const ok = JSON.stringify({ version: 1, type: "ready-ack", requestId: "startup" });
  assert.deepEqual(parseReadyAck(ok), { version: 1, type: "ready-ack", requestId: "startup" });
  for (const payload of [
    null,
    "not-json",
    JSON.stringify({ version: 9, type: "ready-ack", requestId: "startup" }),
    JSON.stringify({ version: 1, type: "save", requestId: "startup" }),
    JSON.stringify({ version: 1, type: "ready-ack", requestId: "bad id" }),
    JSON.stringify({ version: 1, type: "ready-ack", requestId: "startup", secret: "x" }),
    "x".repeat(513),
  ]) {
    assert.equal(parseReadyAck(payload), null);
  }
});
