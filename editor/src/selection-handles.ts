/** Geometry used for Android selection handles on Canvas Editor 1.1.0.
 *
 * RangeContext.rangeRects are measured in the editor's page coordinates and
 * include getPageOffset(pageNo, true). Its Y offset is UNscaled, whereas the
 * actual canvas boxes reflect the configured zoom. Remove that source offset
 * and add the measured on-screen canvas origin instead.
 */
export interface SelectionRect {
  x: number;
  y: number;
  width: number;
  height: number;
}
export interface PageBounds {
  left: number;
  top: number;
}
export type SelectionEdge = "start" | "end";
export interface GripPoint {
  x: number;
  y: number;
  textX: number;
  textY: number;
}

/** Return null for invalid or missing page geometry rather than placing a
 * touch target on top of unrelated toolbar controls. */
export function selectionGripPoint(
  rect: SelectionRect | undefined,
  pageNo: number,
  edge: SelectionEdge,
  canvas: PageBounds | undefined,
  paperHeight: number,
  pageGap: number,
): GripPoint | null {
  if (!rect || !canvas ||
      ![rect.x, rect.y, rect.width, rect.height, canvas.left, canvas.top,
        pageNo, paperHeight, pageGap].every(Number.isFinite) ||
      pageNo < 0 || !Number.isInteger(pageNo) ||
      rect.width < 0 || rect.height <= 0 || paperHeight <= 0 || pageGap < 0) {
    return null;
  }
  const x = canvas.left + rect.x + (edge === "end" ? rect.width : 0);
  const top = canvas.top + rect.y - pageNo * (paperHeight + pageGap);
  const y = top + rect.height;
  return {
    x,
    y,
    textX: x + (edge === "start" ? 1 : -1),
    textY: top + rect.height / 2,
  };
}
