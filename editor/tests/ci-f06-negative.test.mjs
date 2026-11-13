import test from "node:test";
import assert from "node:assert/strict";

// F-06 Negativprobe: bewusst roter Test, niemals nach main mergen.
test("Eine fehlerhafte Revision darf keine geprüfte APK veröffentlichen", () => {
  assert.fail("Erwarteter F-06-Negativtest");
});
