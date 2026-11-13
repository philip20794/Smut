/**
 * Scroll speed for a held native selection drag at the visible WebView edge.
 * Coordinates are CSS pixels; the result is pixels per animation frame.
 * Ordinary swipes must never call this function.
 */
export function selectionEdgeScrollDelta(
  pointerY: number,
  viewportTop: number,
  viewportBottom: number,
): number {
  if (![pointerY, viewportTop, viewportBottom].every(Number.isFinite)
      || viewportBottom <= viewportTop) return 0;
  const height = viewportBottom - viewportTop;
  const edge = Math.min(72, height * 0.14);
  if (edge <= 0) return 0;
  const top = viewportTop + edge;
  const bottom = viewportBottom - edge;
  // Clamp the speed when the finger is held outside the visible viewport.
  if (pointerY < top) return -Math.min(18, Math.max(2, (top - pointerY) * 0.16));
  if (pointerY > bottom) return Math.min(18, Math.max(2, (pointerY - bottom) * 0.16));
  return 0;
}
