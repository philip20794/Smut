/** E-03: bounded prototype view scale (document typography stays unchanged). */
export const DEFAULT_ZOOM = 0.6;
export const MIN_ZOOM = 0.5;
export const MAX_ZOOM = 1.0;
export const ZOOM_STEP = 0.1;

export function nextZoom(current: number, direction: -1 | 1): number {
  if (!Number.isFinite(current)) throw new Error("Invalid zoom");
  const next = Math.round((current + direction * ZOOM_STEP) * 10) / 10;
  return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, next));
}

export function formatZoom(scale: number): string {
  return `${Math.round(scale * 100)} %`;
}
