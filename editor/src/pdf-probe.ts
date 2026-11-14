/**
 * E-04: intentionally isolated feasibility probe, not the v1 export API.
 * The Canvas Editor owns the structured data and layout options.
 * The PDF renderer uses those exact inputs at native scale (not the 60%
 * touch-view zoom). No browser CDN fonts or server calls are permitted.
 */
import type { PrototypeData } from "./prototype.js";

export const A5_WIDTH_PX = 559;
export const A5_HEIGHT_PX = 794;

export interface PdfSource {
  options: Record<string, unknown>;
  data: PrototypeData;
}
interface PdfEngine {
  defaultFontsLoadedPromise: Promise<boolean>;
  render(): void;
  getPdf(): {
    getNumberOfPages(): number;
    output(type: "arraybuffer"): ArrayBuffer;
  };
}
interface PdfEngineModule {
  DrawPdf: new (
    options: Record<string, unknown>,
    data: PrototypeData,
    settings: { loadDefaultFonts: false }
  ) => PdfEngine;
}

export function getPdfProbeOptions(options: Record<string, unknown>): Record<string, unknown> {
  // Canvas page dimensions are 559x794 CSS px = A5 within 0.2 mm.
  // The library's jsPDF unit:px uses the px_scaling hotfix (72/96 points).
  return { ...options, width: A5_WIDTH_PX, height: A5_HEIGHT_PX, scale: 1 };
}

export function bytesToBase64(bytes: Uint8Array): string {
  // Bounded test fixture; chunk to avoid stack/argument overflows.
  let binary = "";
  for (let index = 0; index < bytes.length; index += 8192) {
    const slice = bytes.subarray(index, index + 8192);
    for (const byte of slice) binary += String.fromCharCode(byte);
  }
  return btoa(binary);
}

export async function generatePdfProbe(source: PdfSource): Promise<{ bytes: Uint8Array; pageCount: number }> {
  // This module lives inside assets/editor/vendor, never fetched from the web.
  const url = new URL("./vendor/canvas-editor-pdf.es.js", import.meta.url);
  const { DrawPdf } = await import(url.href) as PdfEngineModule;
  const renderer = new DrawPdf(getPdfProbeOptions(source.options), source.data, {
    loadDefaultFonts: false,
  });
  await renderer.defaultFontsLoadedPromise;
  renderer.render();
  const bytes = new Uint8Array(renderer.getPdf().output("arraybuffer"));
  if (bytes.length < 100 || bytes[0] !== 37 || bytes[1] !== 80
    || bytes[2] !== 68 || bytes[3] !== 70) {
    throw new Error("PDF-Kopf fehlt");
  }
  return { bytes, pageCount: renderer.getPdf().getNumberOfPages() };
}
