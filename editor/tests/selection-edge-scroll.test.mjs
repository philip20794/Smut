import assert from "node:assert/strict";
import test from "node:test";
import { selectionEdgeScrollDelta } from "../dist/selection-edge-scroll.js";

test("selection edge scrolls only near visible viewport edges", () => {
  assert.equal(selectionEdgeScrollDelta(400, 0, 800), 0);
  assert.ok(selectionEdgeScrollDelta(12, 0, 800) < 0);
  assert.ok(selectionEdgeScrollDelta(788, 0, 800) > 0);
  assert.equal(selectionEdgeScrollDelta(400, 100, 700), 0);
  assert.ok(selectionEdgeScrollDelta(110, 100, 700) < 0);
  assert.ok(selectionEdgeScrollDelta(690, 100, 700) > 0);
});

test("selection scroll is bounded and rejects invalid viewports", () => {
  assert.equal(selectionEdgeScrollDelta(-10000, 0, 800), -18);
  assert.equal(selectionEdgeScrollDelta(10000, 0, 800), 18);
  assert.equal(selectionEdgeScrollDelta(10, 50, 50), 0);
  assert.equal(selectionEdgeScrollDelta(NaN, 0, 800), 0);
  assert.equal(selectionEdgeScrollDelta(10, 0, Infinity), 0);
});
