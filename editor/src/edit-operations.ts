import {
  serializeBookDocument, parseBookDocument,
  type BookBlock, type BookChapter, type BookDocument, type InlineMarks, type TextRun,
} from "./book-model.js";

/** Offsets use JavaScript UTF-16 code units; only grapheme boundaries are legal. */
export interface TextAnchor {
  readonly chapterId: string;
  readonly blockId: string;
  readonly offset: number;
}
export interface TextSelection {
  readonly anchor: TextAnchor;
  readonly focus: TextAnchor;
}
export type MarkPatch = {
  readonly bold?: boolean;
  readonly italic?: boolean;
  readonly fontFamily?: string | null;
  readonly fontSizePt?: number | null;
};
export type EditOperation =
  | { readonly type: "insert"; readonly at: TextAnchor; readonly text: string; readonly marks?: InlineMarks }
  | { readonly type: "delete"; readonly range: TextSelection }
  | { readonly type: "replace"; readonly range: TextSelection; readonly text: string; readonly marks?: InlineMarks }
  | { readonly type: "split"; readonly at: TextAnchor; readonly newBlockId: string }
  | { readonly type: "merge"; readonly chapterId: string; readonly firstBlockId: string; readonly secondBlockId: string }
  | { readonly type: "format"; readonly range: TextSelection; readonly patch: MarkPatch };

export interface EditResult {
  readonly document: BookDocument;
  readonly selection: TextSelection;
  /** Default caret after the command; may differ from a tracked selection. */
  readonly caret: TextAnchor;
  readonly changed: boolean;
}
export interface EditSnapshot {
  readonly document: BookDocument;
  readonly selection: TextSelection;
}
export interface EditHistory {
  readonly current: EditSnapshot;
  readonly past: readonly EditSnapshot[];
  readonly future: readonly EditSnapshot[];
  readonly maxDepth: number;
}
export const DEFAULT_HISTORY_DEPTH = 100;

type TextBlock = Extract<BookBlock, { readonly runs: readonly TextRun[] }>;
type Located = { chapterIndex: number; blockIndex: number; chapter: BookChapter; block: TextBlock; offset: number };
const segmenter = new Intl.Segmenter("de", { granularity: "grapheme" });

function error(message: string): never { throw new Error("Editieroperation: " + message); }
function isTextBlock(block: BookBlock | undefined): block is TextBlock {
  return block?.type === "paragraph" || block?.type === "heading";
}
function blockText(block: TextBlock): string {
  return block.runs.map(run => run.text).join("");
}
function boundary(text: string, offset: number): boolean {
  if (!Number.isSafeInteger(offset) || offset < 0 || offset > text.length) return false;
  if (offset === 0 || offset === text.length) return true;
  for (const part of segmenter.segment(text)) if (part.index === offset) return true;
  return false;
}
function snapForward(text: string, offset: number): number {
  const clamped = Math.max(0, Math.min(text.length, offset));
  if (boundary(text, clamped)) return clamped;
  for (const part of segmenter.segment(text)) if (part.index > clamped) return part.index;
  return text.length;
}
function locate(doc: BookDocument, anchor: TextAnchor): Located {
  if (!anchor || typeof anchor.chapterId !== "string" || typeof anchor.blockId !== "string") error("ungültiger Textanker");
  const chapterIndex = doc.chapters.findIndex(ch => ch.id === anchor.chapterId);
  if (chapterIndex < 0) error("Kapitel-ID nicht gefunden");
  const chapter = doc.chapters[chapterIndex]!;
  const blockIndex = chapter.blocks.findIndex(block => block.id === anchor.blockId);
  const block = chapter.blocks[blockIndex];
  if (!isTextBlock(block)) error("Anker verweist nicht auf einen Textblock");
  if (!boundary(blockText(block), anchor.offset)) error("Offset liegt nicht auf einer Unicode-Graphemgrenze");
  return { chapterIndex, blockIndex, chapter, block, offset: anchor.offset };
}
function pair(doc: BookDocument, range: TextSelection): { first: Located; last: Located; start: TextAnchor; end: TextAnchor } {
  if (!range) error("Textbereich fehlt");
  const a = locate(doc, range.anchor);
  const b = locate(doc, range.focus);
  if (a.chapterIndex !== b.chapterIndex) error("Kapitelübergreifende Auswahl ist nicht unterstützt");
  const firstIsA = a.blockIndex < b.blockIndex || (a.blockIndex === b.blockIndex && a.offset <= b.offset);
  const first = firstIsA ? a : b;
  const last = firstIsA ? b : a;
  for (let i = first.blockIndex; i <= last.blockIndex; i++) {
    if (!isTextBlock(first.chapter.blocks[i])) error("Strukturblock liegt im Textbereich");
  }
  const start = { chapterId: first.chapter.id, blockId: first.block.id, offset: first.offset };
  const end = { chapterId: last.chapter.id, blockId: last.block.id, offset: last.offset };
  return { first, last, start, end };
}
function anchor(chapterId: string, blockId: string, offset: number): TextAnchor {
  return { chapterId, blockId, offset };
}
function collapsed(at: TextAnchor): TextSelection { return { anchor: at, focus: at }; }
function marksKey(marks: InlineMarks | undefined): string {
  return JSON.stringify([marks?.bold === true, marks?.italic === true, marks?.fontFamily ?? null, marks?.fontSizePt ?? null]);
}
function compact(runs: readonly TextRun[]): TextRun[] {
  const result: TextRun[] = [];
  for (const run of runs) {
    if (!run.text) continue;
    const previous = result[result.length - 1];
    if (previous && marksKey(previous.marks) === marksKey(run.marks)) {
      result[result.length - 1] = previous.marks
        ? { text: previous.text + run.text, marks: previous.marks }
        : { text: previous.text + run.text };
    } else {
      result.push(run.marks && Object.keys(run.marks).length ? { text: run.text, marks: { ...run.marks } } : { text: run.text });
    }
  }
  return result;
}
/** Take a UTF-16 range, preserving formatting at its original offsets. */
function sliceRuns(runs: readonly TextRun[], start: number, end: number): TextRun[] {
  const result: TextRun[] = [];
  let cursor = 0;
  for (const run of runs) {
    const a = Math.max(start, cursor);
    const b = Math.min(end, cursor + run.text.length);
    if (b > a) result.push({ ...run, text: run.text.slice(a - cursor, b - cursor) });
    cursor += run.text.length;
  }
  return result;
}
function inheritedMarks(block: TextBlock, offset: number): InlineMarks | undefined {
  let count = 0;
  for (const run of block.runs) {
    count += run.text.length;
    if (count > offset || (offset > 0 && count === offset)) return run.marks;
  }
  return undefined;
}
function insertedRun(text: string, marks: InlineMarks | undefined): TextRun[] {
  return text ? [marks && Object.keys(marks).length ? { text, marks } : { text }] : [];
}
function validatePlainText(text: string): void {
  if (typeof text !== "string" || text.length > 1_000_000 || /[\r\n\u0000]/.test(text)) {
    error("Text darf nur eine Zeile ohne NUL enthalten; Absätze mit split anlegen");
  }
}
function validateMarks(marks: InlineMarks | undefined): void {
  if (marks === undefined) return;
  if (!marks || typeof marks !== "object" || Array.isArray(marks)) error("ungültige Einfügeformatierung");
  const keys = Object.keys(marks);
  if (keys.some(key => !["bold", "italic", "fontFamily", "fontSizePt"].includes(key))) error("unbekannte Formatierung");
  if (marks.bold !== undefined && marks.bold !== true) error("bold muss true sein");
  if (marks.italic !== undefined && marks.italic !== true) error("italic muss true sein");
  if (marks.fontFamily !== undefined && (typeof marks.fontFamily !== "string" || marks.fontFamily.length === 0 || marks.fontFamily.length > 80)) error("ungültige Schrift");
  if (marks.fontSizePt !== undefined && (typeof marks.fontSizePt !== "number" || !Number.isFinite(marks.fontSizePt) || marks.fontSizePt < 6 || marks.fontSizePt > 72)) error("ungültige Schriftgröße");
}
function validatePatch(patch: MarkPatch): void {
  if (!patch || typeof patch !== "object" || Array.isArray(patch) || !Object.keys(patch).length) error("leere Formatänderung");
  if (Object.keys(patch).some(key => !["bold", "italic", "fontFamily", "fontSizePt"].includes(key))) error("unbekannte Formatänderung");
  for (const key of ["bold", "italic"] as const) {
    if (patch[key] !== undefined && typeof patch[key] !== "boolean") error("ungültige Markierungsänderung");
  }
  if (patch.fontFamily !== undefined && patch.fontFamily !== null && (typeof patch.fontFamily !== "string" || !patch.fontFamily || patch.fontFamily.length > 80)) error("ungültige Schrift");
  if (patch.fontSizePt !== undefined && patch.fontSizePt !== null && (typeof patch.fontSizePt !== "number" || !Number.isFinite(patch.fontSizePt) || patch.fontSizePt < 6 || patch.fontSizePt > 72)) error("ungültige Schriftgröße");
}
function patched(marks: InlineMarks | undefined, patch: MarkPatch): InlineMarks | undefined {
  const out: { bold?: true; italic?: true; fontFamily?: string; fontSizePt?: number } = { ...marks };
  for (const key of ["bold", "italic"] as const) {
    if (patch[key] === true) out[key] = true;
    if (patch[key] === false) delete out[key];
  }
  for (const key of ["fontFamily", "fontSizePt"] as const) {
    const value = patch[key];
    if (value === null) delete out[key];
    else if (value !== undefined) {
      // The key-specific branches keep the TypeScript value types narrow.
      if (key === "fontFamily" && typeof value === "string") out.fontFamily = value;
      if (key === "fontSizePt" && typeof value === "number") out.fontSizePt = value;
    }
  }
  return Object.keys(out).length ? out : undefined;
}
function formatRuns(runs: readonly TextRun[], from: number, to: number, patch: MarkPatch): TextRun[] {
  const updated: TextRun[] = [];
  let index = 0;
  for (const run of runs) {
    const runEnd = index + run.text.length;
    const start = Math.max(index, from);
    const end = Math.min(runEnd, to);
    if (start >= end) updated.push(run);
    else {
      updated.push(...sliceRuns([run], 0, start - index));
      const middle = run.text.slice(start - index, end - index);
      const marks = patched(run.marks, patch);
      updated.push(...insertedRun(middle, marks));
      updated.push(...sliceRuns([run], end - index, run.text.length));
    }
    index = runEnd;
  }
  return compact(updated);
}
function replaceChapter(doc: BookDocument, chapterIndex: number, blocks: readonly BookBlock[]): BookDocument {
  const chapters = doc.chapters.map((chapter, i) => i === chapterIndex ? { ...chapter, blocks } : chapter);
  // Checks complete structure and the maximum JSON size, before returning an editable value.
  const next = { ...doc, chapters };
  serializeBookDocument(next);
  return next;
}
function moveAnchorAfterReplace(
  doc: BookDocument, original: TextAnchor, first: Located, last: Located,
  deletedStart: number, deletedEnd: number, insertedLength: number, next: BookDocument,
): TextAnchor {
  if (original.chapterId !== first.chapter.id) return original;
  const old = locate(doc, original);
  if (old.blockIndex < first.blockIndex || old.blockIndex > last.blockIndex) return original;
  const target = anchor(first.chapter.id, first.block.id, deletedStart + insertedLength);
  let offset = target.offset;
  if (first.blockIndex === last.blockIndex) {
    if (old.offset < deletedStart) offset = old.offset;
    else if (old.offset >= deletedEnd) offset = old.offset + insertedLength - (deletedEnd - deletedStart);
  } else {
    if (old.blockIndex === first.blockIndex && old.offset < deletedStart) offset = old.offset;
    if (old.blockIndex === last.blockIndex && old.offset >= deletedEnd) {
      offset = deletedStart + insertedLength + old.offset - deletedEnd;
    }
  }
  const resultBlock = next.chapters[first.chapterIndex]!.blocks[first.blockIndex];
  if (!isTextBlock(resultBlock)) error("Zielblock verloren");
  return anchor(first.chapter.id, first.block.id, snapForward(blockText(resultBlock), offset));
}
function mappedSelection(doc: BookDocument, selection: TextSelection | undefined, mapper: (a: TextAnchor) => TextAnchor, fallback: TextAnchor): TextSelection {
  if (!selection) return collapsed(fallback);
  locate(doc, selection.anchor);
  locate(doc, selection.focus);
  return { anchor: mapper(selection.anchor), focus: mapper(selection.focus) };
}
function checkOp(op: EditOperation): void {
  if (!op || typeof op !== "object" || !["insert", "delete", "replace", "split", "merge", "format"].includes(op.type)) {
    error("unbekannte Operation");
  }
}

/** Pure transformation. No I/O and no mutation of the source document. */
export function applyEditOperation(document: BookDocument, op: EditOperation, trackedSelection?: TextSelection): EditResult {
  serializeBookDocument(document);
  checkOp(op);
  if (trackedSelection) {
    locate(document, trackedSelection.anchor);
    locate(document, trackedSelection.focus);
  }
  let next: BookDocument = document;
  let caret: TextAnchor;
  let mapped: TextSelection;
  switch (op.type) {
    case "insert":
    case "delete":
    case "replace": {
      const range = op.type === "insert" ? collapsed(op.at) : op.range;
      const { first, last, start, end } = pair(document, range);
      const value = op.type === "delete" ? "" : op.text;
      validatePlainText(value);
      if (op.type !== "delete") validateMarks(op.marks);
      const noChange = first.blockIndex === last.blockIndex && start.offset === end.offset && value.length === 0;
      if (noChange) {
        caret = start;
        mapped = trackedSelection ?? collapsed(caret);
        return { document, selection: mapped, caret, changed: false };
      }
      const source = first.chapter.blocks;
      const selectedMarks = op.type === "delete" ? undefined : op.marks ?? inheritedMarks(first.block, start.offset);
      const before = sliceRuns(first.block.runs, 0, start.offset);
      const after = sliceRuns(last.block.runs, end.offset, blockText(last.block).length);
      const runs = compact([...before, ...insertedRun(value, selectedMarks), ...after]);
      const merged = { ...first.block, runs } as TextBlock;
      const updatedBlocks = [
        ...source.slice(0, first.blockIndex), merged, ...source.slice(last.blockIndex + 1),
      ];
      next = replaceChapter(document, first.chapterIndex, updatedBlocks);
      caret = anchor(first.chapter.id, first.block.id, snapForward(blockText(merged), start.offset + value.length));
      mapped = mappedSelection(document, trackedSelection, old =>
        moveAnchorAfterReplace(document, old, first, last, start.offset, end.offset, value.length, next), caret);
      break;
    }
    case "split": {
      const located = locate(document, op.at);
      if (typeof op.newBlockId !== "string") error("neue Block-ID fehlt");
      const before = compact(sliceRuns(located.block.runs, 0, located.offset));
      const after = compact(sliceRuns(located.block.runs, located.offset, blockText(located.block).length));
      const left = { ...located.block, runs: before } as TextBlock;
      const right: TextBlock = { id: op.newBlockId, type: "paragraph", runs: after };
      const updated = [...located.chapter.blocks.slice(0, located.blockIndex),
        left, right, ...located.chapter.blocks.slice(located.blockIndex + 1)];
      next = replaceChapter(document, located.chapterIndex, updated);
      caret = anchor(located.chapter.id, right.id, 0);
      mapped = mappedSelection(document, trackedSelection, old => {
        if (old.chapterId !== located.chapter.id || old.blockId !== located.block.id || old.offset < located.offset) return old;
        return anchor(located.chapter.id, right.id, old.offset - located.offset);
      }, caret);
      break;
    }
    case "merge": {
      const chapterIndex = document.chapters.findIndex(ch => ch.id === op.chapterId);
      if (chapterIndex < 0) error("Kapitel-ID nicht gefunden");
      const chapter = document.chapters[chapterIndex]!;
      const firstIndex = chapter.blocks.findIndex(b => b.id === op.firstBlockId);
      if (firstIndex < 0 || chapter.blocks[firstIndex + 1]?.id !== op.secondBlockId) error("Blöcke müssen unmittelbar benachbart sein");
      const first = chapter.blocks[firstIndex];
      const second = chapter.blocks[firstIndex + 1];
      if (!isTextBlock(first) || !isTextBlock(second)) error("Strukturblöcke können nicht verbunden werden");
      const joinOffset = blockText(first).length;
      const merged = { ...first, runs: compact([...first.runs, ...second.runs]) } as TextBlock;
      next = replaceChapter(document, chapterIndex, [
        ...chapter.blocks.slice(0, firstIndex), merged, ...chapter.blocks.slice(firstIndex + 2),
      ]);
      caret = anchor(chapter.id, first.id, snapForward(blockText(merged), joinOffset));
      mapped = mappedSelection(document, trackedSelection, old => {
        if (old.chapterId !== chapter.id || old.blockId !== second.id) return old;
        return anchor(chapter.id, first.id, snapForward(blockText(merged), joinOffset + old.offset));
      }, caret);
      break;
    }
    case "format": {
      validatePatch(op.patch);
      const { first, last, start, end } = pair(document, op.range);
      caret = end;
      if (first.blockIndex === last.blockIndex && first.offset === last.offset) {
        mapped = trackedSelection ?? collapsed(caret);
        return { document, selection: mapped, caret, changed: false };
      }
      const updatedBlocks = first.chapter.blocks.map((block, index) => {
        if (index < first.blockIndex || index > last.blockIndex) return block;
        if (!isTextBlock(block)) error("Formatieren eines Strukturblocks ist nicht erlaubt");
        const from = index === first.blockIndex ? start.offset : 0;
        const to = index === last.blockIndex ? end.offset : blockText(block).length;
        return { ...block, runs: formatRuns(block.runs, from, to, op.patch) };
      });
      next = replaceChapter(document, first.chapterIndex, updatedBlocks);
      mapped = trackedSelection ?? collapsed(caret);
      break;
    }
  }
  const changed = serializeBookDocument(document) !== serializeBookDocument(next);
  return { document: changed ? next : document, selection: mapped, caret, changed };
}

export function createEditHistory(document: BookDocument, selection: TextSelection, maxDepth = DEFAULT_HISTORY_DEPTH): EditHistory {
  if (!Number.isSafeInteger(maxDepth) || maxDepth < 1 || maxDepth > 1000) error("ungültige History-Tiefe");
  const snapshot = parseBookDocument(serializeBookDocument(document));
  locate(snapshot, selection.anchor);
  locate(snapshot, selection.focus);
  return { current: { document: snapshot, selection: structuredClone(selection) }, past: [], future: [], maxDepth };
}
export function applyHistoryOperation(history: EditHistory, operation: EditOperation): EditHistory {
  const result = applyEditOperation(history.current.document, operation);
  if (!result.changed) return history;
  return {
    ...history,
    current: { document: result.document, selection: collapsed(result.caret) },
    past: [...history.past, history.current].slice(-history.maxDepth),
    future: [],
  };
}
export function undoHistory(history: EditHistory): EditHistory {
  if (!history.past.length) return history;
  const previous = history.past[history.past.length - 1]!;
  return { ...history, current: previous, past: history.past.slice(0, -1), future: [history.current, ...history.future] };
}
export function redoHistory(history: EditHistory): EditHistory {
  if (!history.future.length) return history;
  const next = history.future[0]!;
  return { ...history, current: next, past: [...history.past, history.current].slice(-history.maxDepth), future: history.future.slice(1) };
}
