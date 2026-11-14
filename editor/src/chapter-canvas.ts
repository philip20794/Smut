import {
  serializeBookDocument, type BookDocument, type BookChapter, type BookBlock,
  type InlineMarks, type TextRun,
} from "./book-model.js";
import type { BookPosition } from "./book-persistence.js";
import type { PrototypeData } from "./prototype.js";

type Paragraph = Extract<BookBlock, { type: "paragraph" }>;
type Span = { text: string; marks?: InlineMarks; sourceId?: string; nextId?: string };
export type ChangedChapter = {
  readonly document: BookDocument;
  readonly position: BookPosition;
  readonly changed: boolean;
};

/**
 * W-01, safe subset: normal paragraphs with arbitrary runs/inline formatting.
 * Unsupported structural block types are rejected before editing, rather than
 * silently flattened to plain text. Scene/page breaks are mapped in W-03.
 */
export function textForChapter(document: BookDocument, chapterId: string): string {
  const chapter = document.chapters.find(c => c.id === chapterId);
  if (!chapter) throw new Error("Kapitel fehlt");
  if (chapter.blocks.some(b => b.type !== "paragraph")) {
    throw new Error("Kapitel enthält Strukturelemente, die erst ab W-03 bearbeitet werden");
  }
  return chapter.blocks.map(b => (b as Paragraph).runs.map(r => r.text).join("")).join("\n");
}
export function canvasForChapter(document: BookDocument, chapterId: string): PrototypeData {
  const chapter = document.chapters.find(c => c.id === chapterId);
  if (!chapter) throw new Error("Kapitel fehlt");
  textForChapter(document, chapterId); // fail closed on unsupported content
  const main: { value: string; bold?: true; italic?: true; font?: string; size?: number }[] = [];
  chapter.blocks.forEach((block, i) => {
    if (i) main.push({ value: "\n" });
    for (const run of (block as Paragraph).runs) {
      main.push({
        value: run.text,
        ...(run.marks?.bold ? { bold: true as const } : {}),
        ...(run.marks?.italic ? { italic: true as const } : {}),
        ...(run.marks?.fontFamily ? { font: run.marks.fontFamily } : {}),
        ...(run.marks?.fontSizePt ? { size: run.marks.fontSizePt } : {}),
      });
    }
  });
  if (!main.length) main.push({ value: "" });
  return { main };
}
function canvasText(data: PrototypeData): string {
  if (!Array.isArray(data.main) || data.main.length > 50_000) throw new Error("Ungültige Canvas-Daten");
  let result = "";
  for (const entry of data.main) {
    if (typeof entry.value !== "string" || (entry.type !== undefined && entry.type !== "text")) {
      throw new Error("Canvas enthält einen nicht unterstützten Blocktyp");
    }
    if (entry.value.includes("\r") || entry.value.includes("\u0000")) throw new Error("Ungültiges Zeilenende");
    result += entry.value;
    if (result.length > 2_000_000) throw new Error("Kapitel überschreitet das Textlimit");
  }
  return result;
}
function sameMarks(a?: InlineMarks, b?: InlineMarks): boolean {
  return JSON.stringify(a ?? {}) === JSON.stringify(b ?? {});
}
function slices(spans: readonly Span[], from: number, until: number): Span[] {
  const out: Span[] = [];
  let cursor = 0;
  for (const span of spans) {
    const end = cursor + span.text.length;
    if (from < end && until > cursor) {
      out.push({ ...span, text: span.text.slice(Math.max(from, cursor) - cursor,
        Math.min(until, end) - cursor) });
    }
    cursor = end;
  }
  return out;
}
function appendRun(runs: TextRun[], run: TextRun): void {
  if (!run.text) return;
  const prev = runs[runs.length - 1];
  if (prev && sameMarks(prev.marks, run.marks) && prev.text.length + run.text.length <= 1_000_000) {
    runs[runs.length - 1] = prev.marks
      ? { text: prev.text + run.text, marks: prev.marks }
      : { text: prev.text + run.text };
  } else {
    runs.push(run.marks ? { text: run.text, marks: run.marks } : { text: run.text });
  }
}
function safePrefix(oldText: string, text: string): number {
  let n = 0;
  while (n < oldText.length && n < text.length && oldText[n] === text[n]) n++;
  if (n > 0 && n < oldText.length && n < text.length &&
    /[\uDC00-\uDFFF]/.test(oldText[n]!) && /[\uD800-\uDBFF]/.test(oldText[n - 1]!)) n--;
  return n;
}
function safeSuffix(oldText: string, text: string, prefix: number): number {
  let n = 0;
  while (n < oldText.length - prefix && n < text.length - prefix &&
    oldText[oldText.length - 1 - n] === text[text.length - 1 - n]) n++;
  if (n > 0 && n < oldText.length - prefix && n < text.length - prefix &&
    /[\uD800-\uDBFF]/.test(oldText[oldText.length - n - 1]!) &&
    /[\uDC00-\uDFFF]/.test(oldText[oldText.length - n]!)) n--;
  return n;
}
function positionAt(chapterId: string, blocks: readonly Paragraph[], offset: number, scrollY: number): BookPosition {
  let remaining = Math.max(0, offset);
  for (const block of blocks) {
    const length = block.runs.reduce((sum, run) => sum + run.text.length, 0);
    if (remaining <= length) return { chapterId, blockId: block.id,
      offset: remaining, scrollY: Math.min(10_000_000, Math.max(0, Math.trunc(scrollY))) };
    remaining -= length + 1;
  }
  const last = blocks[blocks.length - 1];
  return last
    ? { chapterId, blockId: last.id,
      offset: last.runs.reduce((s, run) => s + run.text.length, 0),
      scrollY: Math.min(10_000_000, Math.max(0, Math.trunc(scrollY))) }
    : { chapterId, blockId: null, offset: 0, scrollY: 0 };
}

/**
 * Pure reconciliation: preserve every unchanged run including formatting and
 * every untouched chapter. A single changed span is spliced into source runs.
 * Each existing paragraph ID is retained if its text survives the edit.
 */
export function reconcileCanvasChapter(
  book: BookDocument, chapterId: string, data: PrototypeData,
  newId: () => string, scrollY = 0
): ChangedChapter {
  const index = book.chapters.findIndex(c => c.id === chapterId);
  if (index < 0) throw new Error("Kapitel fehlt");
  const chapter: BookChapter = book.chapters[index]!;
  const oldText = textForChapter(book, chapterId);
  const nextText = canvasText(data);
  if (oldText === nextText) {
    return { document: book, changed: false,
      position: { chapterId, blockId: chapter.blocks[0]?.id ?? null, offset: 0,
        scrollY: Math.min(10_000_000, Math.max(0, Math.trunc(scrollY))) } };
  }
  const spans: Span[] = [];
  chapter.blocks.forEach((block, i) => {
    if (i) spans.push({ text: "\n", nextId: block.id });
    for (const run of (block as Paragraph).runs) spans.push({
      text: run.text, marks: run.marks, sourceId: block.id,
    });
  });
  const prefix = safePrefix(oldText, nextText);
  const suffix = safeSuffix(oldText, nextText, prefix);
  const insertion = nextText.slice(prefix, nextText.length - suffix);
  const inherited = slices(spans, prefix > 0 ? prefix - 1 : prefix, prefix + 1)
    .find(s => s.sourceId)?.marks;
  const replacement: Span[] = [
    ...slices(spans, 0, prefix),
    ...(insertion ? [{ text: insertion, marks: inherited }] : []),
    ...slices(spans, oldText.length - suffix, oldText.length),
  ];
  const used = new Set<string>();
  const paragraphs: Paragraph[] = [];
  let parts: Span[] = [];
  let hint: string | undefined = chapter.blocks[0]?.id;
  const emit = (): void => {
    let id = parts.find(s => s.sourceId && !used.has(s.sourceId))?.sourceId;
    if (!id && hint && !used.has(hint)) id = hint;
    if (!id) id = newId();
    if (!/^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$/.test(id) || used.has(id)) {
      throw new Error("Ungültige oder doppelte Block-ID");
    }
    used.add(id);
    const runs: TextRun[] = [];
    for (const p of parts) appendRun(runs, { text: p.text, ...(p.marks ? { marks: p.marks } : {}) });
    paragraphs.push({ id, type: "paragraph", runs });
    parts = [];
    hint = undefined;
  };
  for (const piece of replacement) {
    let from = 0;
    for (let j = 0; j < piece.text.length; j++) {
      if (piece.text[j] !== "\n") continue;
      if (j > from) parts.push({ ...piece, text: piece.text.slice(from, j) });
      emit();
      hint = j === 0 && piece.nextId ? piece.nextId : undefined;
      from = j + 1;
    }
    if (from < piece.text.length) parts.push({ ...piece, text: piece.text.slice(from) });
  }
  emit();
  const next = { ...book, chapters: book.chapters.map((ch, i) =>
    i === index ? { ...ch, blocks: paragraphs } : ch) };
  serializeBookDocument(next);
  const caretAt = prefix + insertion.length;
  return { document: next, changed: true, position: positionAt(chapterId, paragraphs, caretAt, scrollY) };
}
