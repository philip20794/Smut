import {
  BOOK_SCHEMA_VERSION, DEFAULT_BOOK_LAYOUT, MAX_BOOK_BYTES,
  validateBookDocument, type BookBlock, type BookDocument, type InlineMarks, type TextRun,
} from "./book-model.js";

/** D-01: einmaliger, NICHT speichernder Importpfad aus der bisherigen E-02-Canvas-Probe. */
export interface PrototypeMigrationOptions {
  readonly bookId: string;
  readonly chapterId: string;
  readonly title: string;
  readonly chapterTitle: string;
  readonly createdAt: string;
}
export interface PrototypeMigrationResult {
  readonly book: BookDocument;
  /** Quellsnapshot für verlustfreie Rückkehr und spätere Migrationsprüfung aufbewahren. */
  readonly originalJson: string;
  /** Nicht abgebildete Canvas-Felder sind damit ausdrücklich erkennbar. */
  readonly warnings: readonly string[];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}
function invalid(reason: string): never { throw new Error(`Canvas-Migration: ${reason}`); }

export function migratePrototypeSnapshot(input: unknown, options: PrototypeMigrationOptions): PrototypeMigrationResult {
  let originalJson: string;
  try { originalJson = JSON.stringify(input); } catch { return invalid("Snapshot ist nicht als JSON speicherbar"); }
  if (!originalJson || new TextEncoder().encode(originalJson).byteLength > MAX_BOOK_BYTES) invalid("Snapshot ist leer oder zu groß");
  const source: unknown = JSON.parse(originalJson);
  if (!isRecord(source) || !Array.isArray(source.main) || source.main.length > 50_000) invalid("main-Elementliste fehlt oder ist zu groß");
  const warnings = new Set<string>();
  for (const key of Object.keys(source)) {
    if (key !== "main") warnings.add("Canvas-Wurzelfeld '" + key + "' wird nicht semantisch abgebildet; Original bleibt erhalten");
  }
  const blocks: BookBlock[] = [];
  let currentRuns: TextRun[] = [];
  const nextId = (): string => `legacy-block-${String(blocks.length + 1).padStart(6, "0")}`;
  const finishParagraph = (): void => {
    blocks.push({ id: nextId(), type: "paragraph", runs: currentRuns });
    currentRuns = [];
  };
  const addText = (value: string, marks: InlineMarks): void => {
    if (!value) return;
    currentRuns.push(Object.keys(marks).length ? { text: value, marks } : { text: value });
  };
  for (const [index, item] of source.main.entries()) {
    if (!isRecord(item) || typeof item.value !== "string") invalid(`Element ${index}: value muss Text sein`);
    if (item.value.includes("\u0000")) invalid(`Element ${index}: NUL-Zeichen`);
    for (const key of Object.keys(item)) {
      if (!["value", "type", "bold", "italic", "font", "size"].includes(key)) warnings.add(`Canvas-Feld '${key}' wird noch nicht semantisch abgebildet; Original bleibt erhalten`);
    }
    if (item.type !== undefined && item.type !== "text") {
      // Nicht verstandene Strukturelemente dürfen nicht als gewöhnlicher Text verschwinden.
      invalid(`Element ${index}: Canvas-Typ '${String(item.type)}' braucht eigenen Adapter`);
    }
    const marks: { bold?: true; italic?: true; fontFamily?: string; fontSizePt?: number } = {};
    if (item.bold !== undefined) {
      if (typeof item.bold !== "boolean") invalid(`Element ${index}: bold ist kein Boolean`);
      if (item.bold) marks.bold = true;
    }
    if (item.italic !== undefined) {
      if (typeof item.italic !== "boolean") invalid(`Element ${index}: italic ist kein Boolean`);
      if (item.italic) marks.italic = true;
    }
    if (item.font !== undefined) {
      if (typeof item.font !== "string") invalid(`Element ${index}: font ist kein Text`);
      marks.fontFamily = item.font;
    }
    if (item.size !== undefined) {
      if (typeof item.size !== "number") invalid(`Element ${index}: size ist keine Zahl`);
      marks.fontSizePt = item.size;
    }
    // Canvas speichert Absatzwechsel als newline innerhalb seiner Elementwerte.
    const normalized = item.value.replace(/\r\n/g, "\n");
    if (normalized.includes("\r")) invalid(`Element ${index}: einzelnes CR ohne LF`);
    if (normalized !== item.value) warnings.add("CRLF in kanonischen Absatzwechsel umgewandelt; Original bleibt erhalten");
    const segments = normalized.split("\n");
    segments.forEach((piece, i) => {
      if (i > 0) finishParagraph();
      addText(piece, marks);
    });
  }
  finishParagraph();
  const book: BookDocument = {
    schemaVersion: BOOK_SCHEMA_VERSION, id: options.bookId, title: options.title,
    createdAt: options.createdAt, updatedAt: options.createdAt,
    layout: DEFAULT_BOOK_LAYOUT,
    chapters: [{ id: options.chapterId, title: options.chapterTitle, blocks }],
  };
  validateBookDocument(book);
  return { book, originalJson, warnings: Array.from(warnings) };
}
