import assert from "node:assert/strict";
import { test } from "node:test";
import { caretScrollDelta } from "../dist/caret-visibility.js";

test("caret already visible is not scrolled", () => {
  assert.equal(caretScrollDelta(140, 162, 0, 320), 0);
});
test("caret hidden behind on-screen keyboard scrolls down", () => {
  assert.equal(caretScrollDelta(282, 306, 0, 300), 30);
});
test("caret above visible viewport scrolls back up", () => {
  assert.equal(caretScrollDelta(12, 35, 0, 300), -12);
});
test("visual viewport top offset is respected", () => {
  assert.equal(caretScrollDelta(10, 25, 40, 240), -54);
});
test("small viewport clamps margins instead of oscillating", () => {
  assert.equal(caretScrollDelta(75, 85, 0, 80), 25);
});
test("invalid geometry never scrolls", () => {
  assert.equal(caretScrollDelta(Number.NaN, 5, 0, 250), 0);
  assert.equal(caretScrollDelta(10, 5, 0, 250), 0);
});
