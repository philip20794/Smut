/**
 * Scroll the document only by the amount needed to make the actual Canvas
 * Editor caret visible. All coordinates are CSS viewport pixels.
 */
export function caretScrollDelta(
  caretTop: number,
  caretBottom: number,
  viewportTop: number,
  viewportBottom: number,
  margin = 24,
): number {
  if (![caretTop, caretBottom, viewportTop, viewportBottom, margin].every(Number.isFinite)
    || caretBottom < caretTop || viewportBottom <= viewportTop) return 0;
  const safeMargin = Math.min(Math.max(margin, 0), (viewportBottom - viewportTop) / 4);
  const top = viewportTop + safeMargin;
  const bottom = viewportBottom - safeMargin;
  if (caretTop < top) return caretTop - top;
  if (caretBottom > bottom) return caretBottom - bottom;
  return 0;
}
