/**
 * E-02 Machbarkeitsprototyp. Das vom Editor gelieferte Datenformat ist noch
 * KEIN endgültiges Smut-Dokumentmodell (siehe E-04 und D-01).
 */
export interface PrototypeData {
  readonly main: readonly { readonly value: string; [key: string]: unknown }[];
  readonly [key: string]: unknown;
}
export const INITIAL_DOCUMENT: PrototypeData = {
  main: [{
    value: "Dies ist meine erste A5-Buchseite. Hier kann ich direkt im Text schreiben: Äpfel, Öl, Grüße – „Hallo“.",
  }],
};
const MAX_DOCUMENT_LENGTH = 750_000;
const ID_PATTERN = /^[A-Za-z0-9_-]{1,40}$/;

export function isPrototypeData(value: unknown): value is PrototypeData {
  if (value === null || typeof value !== "object" || Array.isArray(value)) return false;
  const main = (value as Record<string, unknown>).main;
  return Array.isArray(main) &&
    main.length <= 50_000 &&
    main.every((item: unknown) =>
      item !== null &&
      typeof item === "object" &&
      !Array.isArray(item) &&
      typeof (item as Record<string, unknown>).value === "string"
    );
}

export function createDocumentRequest(
  type: "load" | "save",
  requestId: string,
  document?: PrototypeData
): string {
  if (!ID_PATTERN.test(requestId)) throw new Error("Ungültige Request-ID");
  if (type === "save" && (!document || !isPrototypeData(document))) {
    throw new Error("Ungültiges Dokument");
  }
  const request = type === "load"
    ? { version: 1, type, requestId }
    : { version: 1, type, requestId, document };
  const payload = JSON.stringify(request);
  if (payload.length > MAX_DOCUMENT_LENGTH) throw new Error("Dokument zu groß");
  return payload;
}

export type DocumentResponse =
  | { readonly version: 1; readonly type: "load-result"; readonly requestId: string; readonly document: PrototypeData | null }
  | { readonly version: 1; readonly type: "save-result"; readonly requestId: string; readonly saved: boolean };

export function parseDocumentResponse(value: unknown): DocumentResponse | null {
  if (typeof value !== "string" || value.length > MAX_DOCUMENT_LENGTH) return null;
  let decoded: unknown;
  try { decoded = JSON.parse(value); } catch { return null; }
  if (decoded === null || typeof decoded !== "object" || Array.isArray(decoded)) return null;
  const data = decoded as Record<string, unknown>;
  if (data.version !== 1 || typeof data.requestId !== "string" || !ID_PATTERN.test(data.requestId)) return null;
  if (data.type === "load-result" && Object.keys(data).length === 4 &&
    (data.document === null || isPrototypeData(data.document))) {
    return data as DocumentResponse;
  }
  if (data.type === "save-result" && Object.keys(data).length === 4 && typeof data.saved === "boolean") {
    return data as DocumentResponse;
  }
  return null;
}
