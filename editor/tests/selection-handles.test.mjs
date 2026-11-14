import assert from "node:assert/strict";
import { test } from "node:test";
import { selectionGripPoint } from "../dist/selection-handles.js";

test("start and end handles map the selection boundaries on the first A5 page", () => {
  const rect = { x: 32, y: 45, width: 78, height: 16 };
  const canvas = { left: 22, top: 80 };
  assert.deepEqual(selectionGripPoint(rect, 0, "start", canvas, 794, 20),
    { x: 54, y: 141, textX: 55, textY: 133 });
  assert.deepEqual(selectionGripPoint(rect, 0, "end", canvas, 794, 20),
    { x: 132, y: 141, textX: 131, textY: 133 });
});

test("second-page handle uses scaled real canvas position, not unscaled page gap", () => {
  const rect = { x: 35, y: 814 + 42, width: 14, height: 14 };
  const canvas = { left: 20, top: 550 };
  assert.deepEqual(selectionGripPoint(rect, 1, "end", canvas, 794, 20),
    { x: 69, y: 606, textX: 68, textY: 599 });
});

test("invalid rectangles and page geometry never produce visible handles", () => {
  const rect = { x: 4, y: 5, width: 8, height: 12 };
  assert.equal(selectionGripPoint(rect, -1, "start", { left: 0, top: 0 }, 794, 20), null);
  assert.equal(selectionGripPoint(rect, 0, "start", undefined, 794, 20), null);
  assert.equal(selectionGripPoint({ ...rect, x: NaN }, 0, "start", { left: 0, top: 0 }, 794, 20), null);
  assert.equal(selectionGripPoint({ ...rect, height: 0 }, 0, "start", { left: 0, top: 0 }, 794, 20), null);
});
