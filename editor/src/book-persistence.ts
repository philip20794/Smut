import {
  parseBookDocument, serializeBookDocument, type BookDocument,
} from "./book-model.js";

/** D-03 transport for the structured book model. Canvas E-02 uses its own v1 messages. */
export interface BookPosition {
  readonly chapterId: string;
  readonly blockId: string | null;
  readonly offset: number;
  readonly scrollY: number;
}
export type BookLoadReply =
  | { readonly state: "ok"; readonly revision: number; readonly document: BookDocument; readonly position: BookPosition; readonly recovered: boolean }
  | { readonly state: "missing" | "corrupt" | "error" };
export type BookSaveReply =
  | { readonly state: "saved" | "conflict"; readonly revision: number }
  | { readonly state: "error" };

const ID = /^[A-Za-z0-9_-]{1,40}$/;
const BOOK_ID = /^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$/;
const MAX_MESSAGE_BYTES = 10_500_000;
function checkRequest(id: string): void {
  if (!ID.test(id)) throw new Error("Ungültige Request-ID");
}
function checkRevision(revision: number): void {
  if (!Number.isSafeInteger(revision) || revision < 0) throw new Error("Ungültige Revision");
}
function positionValid(value: unknown): value is BookPosition {
  if (value === null || typeof value !== "object" || Array.isArray(value)) return false;
  const p = value as Record<string, unknown>;
  return Object.keys(p).length === 4 && BOOK_ID.test(String(p.chapterId)) &&
    (p.blockId === null || (typeof p.blockId === "string" && BOOK_ID.test(p.blockId))) &&
    typeof p.offset === "number" && Number.isInteger(p.offset) && p.offset >= 0 && p.offset <= 1_000_000 &&
    typeof p.scrollY === "number" && Number.isInteger(p.scrollY) && p.scrollY >= 0 && p.scrollY <= 10_000_000;
}
function serializeRequest(request: unknown): string {
  const payload = JSON.stringify(request);
  if (new TextEncoder().encode(payload).byteLength > MAX_MESSAGE_BYTES) throw new Error("Bridge-Nachricht zu groß");
  return payload;
}
export function createBookLoadRequest(requestId: string, bookId: string): string {
  checkRequest(requestId);
  if (!BOOK_ID.test(bookId)) throw new Error("Ungültige Buch-ID");
  return serializeRequest({ version: 1, type: "book-load", requestId, bookId });
}
export function createBookSaveRequest(
  requestId: string, document: BookDocument, expectedRevision: number, position: BookPosition
): string {
  checkRequest(requestId);
  checkRevision(expectedRevision);
  const clean = parseBookDocument(serializeBookDocument(document));
  if (!positionValid(position)) throw new Error("Ungültige Cursorposition");
  return serializeRequest({ version: 1, type: "book-save", requestId, expectedRevision, document: clean, position });
}
export function parseBookReply(payload: unknown, expectedRequestId: string): BookLoadReply | BookSaveReply | null {
  if (typeof payload !== "string" || payload.length > MAX_MESSAGE_BYTES || !ID.test(expectedRequestId)) return null;
  let input: unknown;
  try { input = JSON.parse(payload) as unknown; } catch { return null; }
  if (input === null || typeof input !== "object" || Array.isArray(input)) return null;
  const data = input as Record<string, unknown>;
  if (data.version !== 1 || data.requestId !== expectedRequestId) return null;
  if (data.type === "book-save-result") {
    if (data.state === "error" && Object.keys(data).length === 4) return { state: "error" };
    if ((data.state === "saved" || data.state === "conflict") &&
      Object.keys(data).length === 5 && typeof data.revision === "number" &&
      Number.isSafeInteger(data.revision) && data.revision >= 0) {
      return { state: data.state, revision: data.revision };
    }
  }
  if (data.type === "book-load-result") {
    if ((data.state === "missing" || data.state === "corrupt" || data.state === "error") &&
      Object.keys(data).length === 4) return { state: data.state };
    if (data.state === "ok" && Object.keys(data).length === 8 &&
      typeof data.revision === "number" && Number.isSafeInteger(data.revision) && data.revision >= 1 &&
      typeof data.recovered === "boolean" && positionValid(data.position)) {
      try {
        const document = parseBookDocument(JSON.stringify(data.document));
        return { state: "ok", document, revision: data.revision, recovered: data.recovered, position: data.position };
      } catch { return null; }
    }
  }
  return null;
}

/**
 * Autosave coalesces rapid edits, commits only one revision at a time and
 * keeps dirty data on I/O failure. The WebView/BookModel binding is W-01.
 */
export class BookAutosaveQueue {
  private pending: { book: BookDocument; position: BookPosition } | null = null;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private running: Promise<boolean> | null = null;
  private revision: number;
  private disposed = false;
  constructor(
    revision: number,
    private readonly persist: (book: BookDocument, position: BookPosition, expectedRevision: number) => Promise<number>,
    private readonly delayMs = 350
  ) { checkRevision(revision); this.revision = revision; }

  get currentRevision(): number { return this.revision; }
  get dirty(): boolean { return this.pending !== null || this.running !== null; }

  changed(book: BookDocument, position: BookPosition): void {
    if (this.disposed) throw new Error("Autosave bereits beendet");
    if (!positionValid(position)) throw new Error("Ungültige Cursorposition");
    this.pending = { book: parseBookDocument(serializeBookDocument(book)), position: { ...position } };
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = setTimeout(() => { this.timer = null; void this.flush(); }, this.delayMs);
  }
  async flush(): Promise<boolean> {
    if (this.timer !== null) { clearTimeout(this.timer); this.timer = null; }
    if (this.running !== null) {
      const priorSucceeded = await this.running;
      if (!priorSucceeded) return false;
      if (!this.pending) return true;
    }
    const snapshot = this.pending;
    if (!snapshot) return true;
    this.pending = null;
    const task = Promise.resolve().then(() => this.persist(snapshot.book, snapshot.position, this.revision))
      .then((next) => {
        checkRevision(next);
        if (next !== this.revision + 1) throw new Error("Ungültige Autosave-Revision");
        this.revision = next;
        return true;
      }).catch(() => {
        // A later user edit takes precedence over a failed older save.
        if (!this.pending) this.pending = snapshot;
        return false;
      });
    this.running = task;
    const success = await task;
    if (this.running === task) this.running = null;
    if (success && this.pending) return this.flush();
    return success;
  }
  /** Call flush() explicitly before disposal; never silently discard dirty edits. */
  dispose(): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
    this.disposed = true;
  }
}
