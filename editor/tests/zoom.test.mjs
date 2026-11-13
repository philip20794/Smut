import test from "node:test";
import assert from "node:assert/strict";
import {
  DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, nextZoom, formatZoom,
} from "../dist/zoom.js";

test("A5-Ansicht zoomt in stabilen 10-Prozent-Stufen", () => {
  assert.equal(DEFAULT_ZOOM, 0.6);
  assert.equal(nextZoom(0.6, 1), 0.7);
  assert.equal(nextZoom(0.7, -1), 0.6);
  assert.equal(formatZoom(0.7), "70 %");
});

test("Zoom bleibt zwischen 50 und 100 Prozent", () => {
  assert.equal(nextZoom(MAX_ZOOM, 1), MAX_ZOOM);
  assert.equal(nextZoom(MIN_ZOOM, -1), MIN_ZOOM);
  assert.throws(() => nextZoom(Number.NaN, 1), /Invalid zoom/);
});
