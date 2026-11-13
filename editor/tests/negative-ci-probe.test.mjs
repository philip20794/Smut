import test from "node:test";
import assert from "node:assert/strict";

// Isolierter Negativtest für F-04. Darf NIEMALS nach main gemergt werden.
test("F-04 stoppt einen absichtlich fehlschlagenden Unit-Test", () => {
  assert.fail("F-04 Negativprobe: Workflow muss rot werden");
});
