/** D-01: editorunabhängiges, versioniertes Buchformat. PDF/Canvas sind keine Quelle. */
export const BOOK_SCHEMA_VERSION = 1 as const;
export const MAX_BOOK_BYTES = 8_000_000;

export interface InlineMarks {
  readonly bold?: true;
  readonly italic?: true;
  readonly fontFamily?: string;
  readonly fontSizePt?: number;
}
export interface TextRun {
  readonly text: string;
  readonly marks?: InlineMarks;
}
export type BookBlock =
  | { readonly id: string; readonly type: "paragraph"; readonly runs: readonly TextRun[] }
  | { readonly id: string; readonly type: "heading"; readonly level: 1 | 2; readonly runs: readonly TextRun[] }
  | { readonly id: string; readonly type: "sceneBreak" }
  | { readonly id: string; readonly type: "pageBreak" };
export interface BookChapter {
  readonly id: string;
  readonly title: string;
  /** Die Array-Position ist die verbindliche Kapitelreihenfolge. */
  readonly blocks: readonly BookBlock[];
}
export interface BookLayout {
  readonly pageWidthMm: 148;
  readonly pageHeightMm: 210;
  readonly marginsMm: {
    readonly top: number;
    readonly bottom: number;
    readonly inside: number;
    readonly outside: number;
  };
  readonly fontFamily: string;
  readonly fontSizePt: number;
  readonly lineSpacing: number;
  readonly pageNumbers: boolean;
}
export interface BookDocument {
  readonly schemaVersion: 1;
  readonly id: string;
  readonly title: string;
  readonly author?: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly layout: BookLayout;
  readonly chapters: readonly BookChapter[];
}

export const DEFAULT_BOOK_LAYOUT: BookLayout = {
  pageWidthMm: 148, pageHeightMm: 210,
  marginsMm: { top: 18, bottom: 18, inside: 20, outside: 16 },
  fontFamily: "serif", fontSizePt: 11, lineSpacing: 1.3, pageNumbers: true,
};

const ID = /^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$/;
function fail(path: string, reason: string): never {
  throw new Error(`Ungültiges Buchformat bei ${path}: ${reason}`);
}
function record(value: unknown, path: string): Record<string, unknown> {
  if (value === null || typeof value !== "object" || Array.isArray(value)) fail(path, "Objekt erwartet");
  return value as Record<string, unknown>;
}
function keys(value: Record<string, unknown>, path: string, required: readonly string[], optional: readonly string[] = []): void {
  for (const key of required) if (!Object.hasOwn(value, key)) fail(`${path}.${key}`, "Pflichtfeld fehlt");
  for (const key of Object.keys(value)) if (!required.includes(key) && !optional.includes(key)) fail(`${path}.${key}`, "unbekanntes Feld");
}
function text(value: unknown, path: string, max: number, allowEmpty = false): void {
  if (typeof value !== "string" || value.length > max || (!allowEmpty && value.length === 0) || /\u0000/.test(value)) {
    fail(path, `Text erwartet (maximal ${max} Zeichen, ohne NUL)`);
  }
}
function id(value: unknown, path: string, used: Set<string>): void {
  if (typeof value !== "string" || !ID.test(value)) fail(path, "ID muss 1–64 erlaubte Zeichen haben");
  if (used.has(value)) fail(path, "ID mehrfach verwendet");
  used.add(value);
}
function number(value: unknown, path: string, min: number, max: number): void {
  if (typeof value !== "number" || !Number.isFinite(value) || value < min || value > max) fail(path, `Zahl zwischen ${min} und ${max} erwartet`);
}
function timestamp(value: unknown, path: string): void {
  if (typeof value !== "string" || !/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d\.\d{3}Z$/.test(value) ||
      !Number.isFinite(Date.parse(value)) || new Date(value).toISOString() !== value) fail(path, "kanonischer ISO-UTC-Zeitstempel erwartet");
}
function runs(value: unknown, path: string): void {
  if (!Array.isArray(value) || value.length > 20_000) fail(path, "Textlauf-Liste erwartet");
  for (const [i, item] of value.entries()) {
    const p = `${path}[${i}]`;
    const run = record(item, p);
    keys(run, p, ["text"], ["marks"]);
    text(run.text, `${p}.text`, 1_000_000);
    if ((run.text as string).includes("\r") || (run.text as string).includes("\n")) fail(`${p}.text`, "Zeilenumbrüche strukturell speichern");
    if (run.marks !== undefined) {
      const m = record(run.marks, `${p}.marks`);
      keys(m, `${p}.marks`, [], ["bold", "italic", "fontFamily", "fontSizePt"]);
      if (!Object.keys(m).length) fail(`${p}.marks`, "leere Markierung entfernen");
      for (const flag of ["bold", "italic"]) if (m[flag] !== undefined && m[flag] !== true) fail(`${p}.marks.${flag}`, "nur true erlaubt");
      if (m.fontFamily !== undefined) text(m.fontFamily, `${p}.marks.fontFamily`, 80);
      if (m.fontSizePt !== undefined) number(m.fontSizePt, `${p}.marks.fontSizePt`, 6, 72);
    }
  }
}
/** Prüft die komplette externe Struktur, bevor D-02/D-03 sie bearbeiten dürfen. */
export function validateBookDocument(input: unknown): asserts input is BookDocument {
  const book = record(input, "book");
  keys(book, "book", ["schemaVersion", "id", "title", "createdAt", "updatedAt", "layout", "chapters"], ["author"]);
  if (book.schemaVersion !== BOOK_SCHEMA_VERSION) fail("book.schemaVersion", "nicht unterstützte Version");
  const used = new Set<string>();
  id(book.id, "book.id", used);
  text(book.title, "book.title", 240);
  if (book.author !== undefined) text(book.author, "book.author", 240);
  timestamp(book.createdAt, "book.createdAt");
  timestamp(book.updatedAt, "book.updatedAt");
  if ((book.updatedAt as string) < (book.createdAt as string)) fail("book.updatedAt", "vor Erstellungsdatum");
  const layout = record(book.layout, "book.layout");
  keys(layout, "book.layout", ["pageWidthMm", "pageHeightMm", "marginsMm", "fontFamily", "fontSizePt", "lineSpacing", "pageNumbers"]);
  if (layout.pageWidthMm !== 148 || layout.pageHeightMm !== 210) fail("book.layout", "Version 1 ist auf A5 festgelegt");
  const margins = record(layout.marginsMm, "book.layout.marginsMm");
  keys(margins, "book.layout.marginsMm", ["top", "bottom", "inside", "outside"]);
  for (const side of ["top", "bottom", "inside", "outside"]) number(margins[side], `book.layout.marginsMm.${side}`, 0, 50);
  if ((margins.inside as number) + (margins.outside as number) >= 130 ||
      (margins.top as number) + (margins.bottom as number) >= 190) fail("book.layout.marginsMm", "Satzfläche zu klein");
  text(layout.fontFamily, "book.layout.fontFamily", 80);
  number(layout.fontSizePt, "book.layout.fontSizePt", 6, 32);
  number(layout.lineSpacing, "book.layout.lineSpacing", 1, 3);
  if (typeof layout.pageNumbers !== "boolean") fail("book.layout.pageNumbers", "Boolean erwartet");
  if (!Array.isArray(book.chapters) || book.chapters.length > 1000) fail("book.chapters", "Kapitel-Liste erwartet");
  for (const [i, ch] of book.chapters.entries()) {
    const p = `book.chapters[${i}]`;
    const chapter = record(ch, p);
    keys(chapter, p, ["id", "title", "blocks"]);
    id(chapter.id, `${p}.id`, used);
    text(chapter.title, `${p}.title`, 240);
    if (!Array.isArray(chapter.blocks) || chapter.blocks.length > 25_000) fail(`${p}.blocks`, "Block-Liste erwartet");
    for (const [j, b] of chapter.blocks.entries()) {
      const bp = `${p}.blocks[${j}]`;
      const block = record(b, bp);
      id(block.id, `${bp}.id`, used);
      switch (block.type) {
        case "paragraph":
          keys(block, bp, ["id", "type", "runs"]);
          runs(block.runs, `${bp}.runs`);
          break;
        case "heading":
          keys(block, bp, ["id", "type", "level", "runs"]);
          if (block.level !== 1 && block.level !== 2) fail(`${bp}.level`, "Überschriftenebene 1 oder 2 erwartet");
          runs(block.runs, `${bp}.runs`);
          if ((block.runs as unknown[]).length === 0) fail(`${bp}.runs`, "Überschrift benötigt Text");
          break;
        case "sceneBreak":
        case "pageBreak":
          keys(block, bp, ["id", "type"]);
          break;
        default:
          fail(`${bp}.type`, "unbekannter Blocktyp");
      }
    }
  }
}
/** 8 MB sind eine Schutzgrenze für Modell- und Migrationsdaten, keine Speicherzusage. */
function encodedSize(data: string): number {
  return new TextEncoder().encode(data).byteLength;
}
export function serializeBookDocument(input: unknown): string {
  validateBookDocument(input);
  const encoded = JSON.stringify(input);
  if (encodedSize(encoded) > MAX_BOOK_BYTES) fail("book", "Dokument überschreitet das Größenlimit");
  return encoded;
}
export function parseBookDocument(json: string): BookDocument {
  if (typeof json !== "string" || encodedSize(json) > MAX_BOOK_BYTES) fail("book", "JSON zu groß oder nicht vorhanden");
  let decoded: unknown;
  try { decoded = JSON.parse(json) as unknown; } catch { fail("book", "ungültiges JSON"); }
  validateBookDocument(decoded);
  return decoded;
}
/** Unveränderliches, deterministisch initialisiertes Leerbuch. Zeit/ID sind Eingaben, keine Zufallsgeneratoren. */
export function createEmptyBook(idValue: string, titleValue: string, timestampValue: string, chapterId: string): BookDocument {
  const book: BookDocument = {
    schemaVersion: BOOK_SCHEMA_VERSION, id: idValue, title: titleValue,
    createdAt: timestampValue, updatedAt: timestampValue,
    layout: { ...DEFAULT_BOOK_LAYOUT, marginsMm: { ...DEFAULT_BOOK_LAYOUT.marginsMm } }, chapters: [{ id: chapterId, title: "Kapitel 1", blocks: [] }],
  };
  validateBookDocument(book);
  return book;
}
