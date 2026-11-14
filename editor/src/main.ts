import { DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, nextZoom, formatZoom } from "./zoom.js";
import { caretScrollDelta } from "./caret-visibility.js";
import { selectionEdgeScrollDelta } from "./selection-edge-scroll.js";
import { createReadyMessage, parseReadyAck } from "./bridge.js";
import {
  INITIAL_DOCUMENT, createDocumentRequest, isPrototypeData, parseDocumentResponse,
  type PrototypeData,
} from "./prototype.js";

interface AndroidBridge {
  postMessage(payload: string): void;
}
interface CanvasEditor {
  command: {
    getValue(): { data: PrototypeData };
    getRange(): { startIndex: number; endIndex: number } | null;
    getRangeText(): unknown;
    executeSelectAll(): void;
    executePageScale(scale: number): void;
    executeSetValue(data: Partial<PrototypeData>): void;
  };
  listener: { contentChange?: () => void };
}
interface CanvasEditorModule {
  default: new (
    container: HTMLElement,
    data: PrototypeData,
    options: Record<string, unknown>
  ) => CanvasEditor;
}
declare global {
  interface Window {
    SmutAndroid?: AndroidBridge;
    smutPrototype?: {
      getText(): string;
      getSavedStatus(): string | null;
      isLoaded(): boolean;
      getZoom(): number;
      getPageCount(): number;
      getSelection(): { startIndex: number; endIndex: number; hasSelection: boolean; text: string } | null;
      isSelectionDragActive(): boolean;
      selectAllTestFixture(): boolean;
      setPageTestFixture(enabled: boolean): void;
    };
  }
}

const status = document.getElementById("bridge-status");
const editorStatus = document.getElementById("editor-status");
const retryButton = document.getElementById("recheck-button");
const saveButton = document.getElementById("save-button");
const container = document.getElementById("canvas-editor");
const zoomOutButton = document.getElementById("zoom-out");
const zoomInButton = document.getElementById("zoom-in");
const zoomValue = document.getElementById("zoom-value");
let pendingRequestId = "";
let checkNumber = 0;
let saveNumber = 0;
let pendingSave = false;
let editor: CanvasEditor | null = null;
let isLoading = false;
let isSaving = false;
let currentZoom = DEFAULT_ZOOM;
let testDocumentBackup: PrototypeData | null = null;
let suppressTestAutoSave = false;
let caretVisibilityFrame = 0;

/**
 * Canvas Editor 1.1.0 positions its real caret in .ce-cursor. The proxy
 * textarea is NOT a suitable scroll target because its position can lag.
 * Keep the caret above the Android IME whenever the viewport shrinks,
 * an input is committed, or Canvas Editor updates its caret position.
 */
function keepCaretVisible(): void {
  if (!container || !editor || isNativeSelectionDrag || document.hidden) return;
  const input = container.querySelector<HTMLTextAreaElement>(".ce-inputarea");
  const caret = container.querySelector<HTMLElement>(".ce-cursor");
  if (!input || document.activeElement !== input || !caret
      || getComputedStyle(caret).display === "none") return;
  const caretRect = caret.getBoundingClientRect();
  if (caretRect.width === 0 && caretRect.height === 0) return;
  const visual = window.visualViewport;
  const viewportTop = Math.max(0, visual?.offsetTop ?? 0);
  const viewportBottom = Math.min(window.innerHeight,
    (visual?.offsetTop ?? 0) + (visual?.height ?? window.innerHeight));
  const delta = caretScrollDelta(caretRect.top, caretRect.bottom,
    viewportTop, viewportBottom, 28);
  if (delta) window.scrollBy(0, delta);
}

function scheduleCaretVisibility(): void {
  if (document.hidden) return;
  if (caretVisibilityFrame) cancelAnimationFrame(caretVisibilityFrame);
  caretVisibilityFrame = requestAnimationFrame(() => {
    // Canvas Editor updates its canvas/caret after its own IME event handler.
    caretVisibilityFrame = requestAnimationFrame(() => {
      caretVisibilityFrame = 0;
      keepCaretVisible();
    });
  });
}

function cancelCaretVisibility(): void {
  if (caretVisibilityFrame) cancelAnimationFrame(caretVisibilityFrame);
  caretVisibilityFrame = 0;
}

// Android can suspend the WebView while a delayed caret update is queued.
// Never let a stale animation frame scroll the document after app switching.
window.addEventListener("blur", cancelCaretVisibility);
window.addEventListener("pagehide", cancelCaretVisibility);
document.addEventListener("visibilitychange", () => {
  if (document.hidden) cancelCaretVisibility();
});

container?.addEventListener("input", scheduleCaretVisibility, true);
container?.addEventListener("compositionend", scheduleCaretVisibility, true);
container?.addEventListener("keyup", scheduleCaretVisibility, true);
window.addEventListener("resize", scheduleCaretVisibility);
window.visualViewport?.addEventListener("resize", scheduleCaretVisibility);
window.visualViewport?.addEventListener("scroll", scheduleCaretVisibility);


function updateZoomControls(): void {
  if (zoomValue) zoomValue.textContent = formatZoom(currentZoom);
  if (zoomOutButton instanceof HTMLButtonElement) zoomOutButton.disabled = currentZoom <= MIN_ZOOM;
  if (zoomInButton instanceof HTMLButtonElement) zoomInButton.disabled = currentZoom >= MAX_ZOOM;
}

function adjustZoom(direction: -1 | 1): void {
  if (!editor) return;
  const zoom = nextZoom(currentZoom, direction);
  editor.command.executePageScale(zoom);
  currentZoom = zoom;
  updateZoomControls();
  scheduleCaretVisibility();
}


function report(message: string): void {
  if (editorStatus) editorStatus.textContent = message;
}

function postRequest(payload: string): void {
  window.SmutAndroid?.postMessage(payload);
}

function scheduleSave(): void {
  if (!editor || isLoading || suppressTestAutoSave) return;
  pendingSave = true;
  if (!isSaving) void saveCurrentDocument();
}
async function saveCurrentDocument(): Promise<void> {
  if (!editor || isSaving || !pendingSave) return;
  isSaving = true;
  pendingSave = false;
  saveNumber += 1;
  const requestId = `save-${saveNumber}`;
  try {
    const document = editor.command.getValue().data;
    if (!isPrototypeData(document)) throw new Error("Ungültige Editordaten");
    postRequest(createDocumentRequest("save", requestId, document));
    report("Änderungen werden lokal gespeichert …");
  } catch (error) {
    isSaving = false;
    report(`Speichern nicht möglich: ${String(error)}`);
  }
}
/**
 * The page-flow fixture is isolated from native persistence and restores the
 * exact original editor document. It is only used by the emulator test.
 */
function setPageTestFixture(enabled: boolean): void {
  if (!editor) return;
  if (enabled) {
    if (testDocumentBackup) return;
    testDocumentBackup = editor.command.getValue().data;
    suppressTestAutoSave = true;
    const prose = Array.from({ length: 75 }, (_, i) =>
      `Absatz ${i + 1}: Ein leiser Wind strich über die dunkle Straße. Äpfel, Öl und Grüße standen in einem Brief, während die Nacht langsam verging. `
    ).join("");
    editor.command.executeSetValue({ main: [{ value: prose }] });
  } else if (testDocumentBackup) {
    editor.command.executeSetValue(testDocumentBackup);
    testDocumentBackup = null;
    suppressTestAutoSave = false;
  }
}

/**
 * A native Android canvas long-press produces a contextmenu event. Canvas
 * Editor 1.1.0 handles mouse double-click word selection, but not touch
 * long-press. Forward only genuine, stationary Android touches to that
 * existing hit-testing/selection path. No Javascript test calls this handler.
 */
let lastCanvasTouch: { x: number; y: number; time: number } | null = null;
let isNativeSelectionDrag = false;
let selectionDragPoint: { x: number; y: number } | null = null;
let selectionScrollFrame = 0;

/**
 * A Canvas Editor page is a separate canvas. During an Android selection drag,
 * the finger can cross page gaps or the visible edge of the WebView; route each
 * move to the closest visible page, clamped to that page's actual drawing area.
 * This path is never used for ordinary swipes or multi-touch gestures.
 */
function forwardNativeSelectionDrag(x: number, y: number): void {
  if (!container || !isNativeSelectionDrag) return;
  let target: HTMLCanvasElement | null = null;
  let distance = Number.POSITIVE_INFINITY;
  for (const canvas of Array.from(container.querySelectorAll<HTMLCanvasElement>(".ce-page-container canvas"))) {
    const r = canvas.getBoundingClientRect();
    if (!r.width || !r.height || r.bottom < 0 || r.top > window.innerHeight) continue;
    const dx = Math.max(r.left - x, 0, x - r.right);
    const dy = Math.max(r.top - y, 0, y - r.bottom);
    const candidateDistance = Math.hypot(dx, dy);
    if (candidateDistance < distance) {
      target = canvas;
      distance = candidateDistance;
    }
  }
  // Avoid accidentally selecting pages while dragging over the app toolbar.
  if (!target || distance > 96) return;
  const r = target.getBoundingClientRect();
  target.dispatchEvent(new MouseEvent("mousemove", {
    bubbles: true,
    cancelable: true,
    view: window,
    clientX: Math.max(r.left + 1, Math.min(r.right - 1, x)),
    clientY: Math.max(r.top + 1, Math.min(r.bottom - 1, y)),
    buttons: 1,
  }));
}

function stopNativeSelectionDrag(): void {
  lastCanvasTouch = null;
  isNativeSelectionDrag = false;
  selectionDragPoint = null;
  if (selectionScrollFrame) cancelAnimationFrame(selectionScrollFrame);
  selectionScrollFrame = 0;
}

/** Keep extending the range when the finger reaches the WebView's vertical edge. */
function autoScrollSelection(): void {
  selectionScrollFrame = 0;
  if (!isNativeSelectionDrag || !selectionDragPoint) return;
  const { x, y } = selectionDragPoint;
  const visual = window.visualViewport;
  const viewportTop = Math.max(0, visual?.offsetTop ?? 0);
  const viewportBottom = Math.min(window.innerHeight,
    (visual?.offsetTop ?? 0) + (visual?.height ?? window.innerHeight));
  const delta = selectionEdgeScrollDelta(y, viewportTop, viewportBottom);
  let didScroll = false;
  if (delta) {
    const before = window.scrollY;
    window.scrollBy(0, delta);
    didScroll = window.scrollY !== before;
    if (didScroll) forwardNativeSelectionDrag(x, y);
  }
  // Stop at the document boundary as well as outside the edge zone.
  // Otherwise a held finger at the first/last page schedules RAF forever.
  // A later touchmove can restart scrolling when the viewport can move again.
  if (didScroll) selectionScrollFrame = requestAnimationFrame(autoScrollSelection);
}

container?.addEventListener("touchstart", event => {
  stopNativeSelectionDrag();
  const target = event.target;
  const touch = event.touches.item(0);
  if (event.touches.length !== 1 || !touch || !(target instanceof HTMLCanvasElement)
      || !target.closest(".ce-page-container")) return;
  lastCanvasTouch = { x: touch.clientX, y: touch.clientY, time: Date.now() };
}, { passive: true });
container?.addEventListener("touchmove", event => {
  const touch = event.touches.item(0);
  if (event.touches.length !== 1 || !touch) {
    stopNativeSelectionDrag();
    return;
  }
  if (isNativeSelectionDrag) {
    // Only confirmed long-press selection prevents the browser's normal swipe.
    event.preventDefault();
    selectionDragPoint = { x: touch.clientX, y: touch.clientY };
    forwardNativeSelectionDrag(touch.clientX, touch.clientY);
    if (!selectionScrollFrame) selectionScrollFrame = requestAnimationFrame(autoScrollSelection);
    return;
  }
  if (lastCanvasTouch &&
      Math.hypot(touch.clientX - lastCanvasTouch.x, touch.clientY - lastCanvasTouch.y) > 14) {
    lastCanvasTouch = null;
  }
}, { passive: false });
container?.addEventListener("touchend", stopNativeSelectionDrag);
container?.addEventListener("touchcancel", stopNativeSelectionDrag);
// A native selection must never keep scrolling after the Android WebView
// loses focus (Home/Recents, another app, or a hidden document). Android
// does not guarantee a touchcancel event in these cases.
window.addEventListener("blur", stopNativeSelectionDrag);
window.addEventListener("pagehide", stopNativeSelectionDrag);
document.addEventListener("visibilitychange", () => {
  if (document.hidden) stopNativeSelectionDrag();
});
container?.addEventListener("contextmenu", event => {
  if (!editor || !window.SmutAndroid || !lastCanvasTouch) return;
  if (Date.now() - lastCanvasTouch.time > 3500) return;
  const target = event.target;
  if (!(target instanceof HTMLCanvasElement) || !target.closest(".ce-page-container")) return;
  event.preventDefault();
  const range = editor.command.getRange();
  if (range && range.endIndex > range.startIndex) return;
  const { x, y } = lastCanvasTouch;
  for (const type of ["mousedown", "dblclick"] as const) {
    target.dispatchEvent(new MouseEvent(type, {
      bubbles: true,
      cancelable: true,
      view: window,
      clientX: x,
      clientY: y,
      button: 0,
      detail: type === "dblclick" ? 2 : 1,
    }));
  }
  const selection = editor.command.getRange();
  isNativeSelectionDrag = Boolean(selection && selection.endIndex > selection.startIndex);
});

function requestHandshake(requestId: string): void {
  pendingRequestId = requestId;
  if (status) status.textContent = "Android-Verbindung wird geprüft …";
  if (window.SmutAndroid) {
    postRequest(createReadyMessage(requestId));
  } else if (status) {
    status.textContent = "Vorschau im Browser · keine Android-Verbindung";
    void initializeEditor(INITIAL_DOCUMENT);
  }
}

async function initializeEditor(document: PrototypeData): Promise<void> {
  if (editor || isLoading || !container) return;
  isLoading = true;
  report("Buchseite wird geladen …");
  try {
    const url = new URL("./vendor/canvas-editor.js", import.meta.url);
    const module = await import(url.href) as CanvasEditorModule;
    editor = new module.default(container, document, {
      width: 559,
      height: 794,
      margins: [54, 50, 54, 50],
      scale: DEFAULT_ZOOM,
      defaultFont: "serif",
      defaultSize: 15,
      pageMode: "paging",
    });
    editor.listener.contentChange = () => {
      scheduleSave();
      scheduleCaretVisibility();
    };
    // The editor repositions .ce-cursor after key presses and page reflow.
    // Observe only this one node to avoid re-render loops on the canvas.
    const caret = container.querySelector(".ce-cursor");
    if (caret) {
      new MutationObserver(scheduleCaretVisibility).observe(caret, {
        attributes: true, attributeFilter: ["style"],
      });
    }
    updateZoomControls();
    report("A5-Buchseite · Tippen direkt im Text");
  } catch (error) {
    report(`Editor konnte nicht gestartet werden: ${String(error)}`);
  } finally {
    isLoading = false;
  }
}

window.addEventListener("smut:native", (event: Event) => {
  const payload: unknown = (event as CustomEvent<unknown>).detail;
  const ack = parseReadyAck(payload);
  if (ack?.requestId === pendingRequestId && status) {
    status.textContent = "Android-Verbindung aktiv · vollständig offline";
    if (!editor && !isLoading) postRequest(createDocumentRequest("load", "load-startup"));
    return;
  }
  const response = parseDocumentResponse(payload);
  if (!response) return;
  if (response.type === "load-result" && response.requestId === "load-startup") {
    void initializeEditor(response.document ?? INITIAL_DOCUMENT);
  }
  if (response.type === "save-result" && response.requestId === `save-${saveNumber}`) {
    report(response.saved ? "Lokal gespeichert" : "Speichern fehlgeschlagen");
    isSaving = false;
    if (pendingSave) void saveCurrentDocument();
  }
});

retryButton?.addEventListener("click", () => {
  checkNumber += 1;
  retryButton.dataset.clicks = String(checkNumber);
  requestHandshake(`recheck-${checkNumber}`);
});
zoomOutButton?.addEventListener("click", () => adjustZoom(-1));
zoomInButton?.addEventListener("click", () => adjustZoom(1));
saveButton?.addEventListener("click", () => {
  pendingSave = true;
  void saveCurrentDocument();
});
window.smutPrototype = {
  getText: () => editor?.command.getValue().data.main.map(x => x.value).join("") ?? "",
  getSavedStatus: () => editorStatus?.textContent ?? null,
  isLoaded: () => editor !== null,
  getZoom: () => currentZoom,
  getPageCount: () => container?.querySelectorAll(".ce-page-container canvas").length ?? 0,
  // Read-only diagnostic for instrumented *native* touch selection tests.
  // The test must generate the touch; it never sets an editor range through JS.
  getSelection: () => {
    const range = editor?.command.getRange();
    if (!range) return null;
    const selectedText = editor?.command.getRangeText();
    return {
      startIndex: range.startIndex,
      endIndex: range.endIndex,
      hasSelection: range.startIndex >= 0 && range.endIndex > range.startIndex,
      text: typeof selectedText === "string" ? selectedText : JSON.stringify(selectedText ?? null),
    };
  },
  // Read-only lifecycle diagnostic for native gesture regression tests.
  isSelectionDragActive: () => isNativeSelectionDrag || selectionScrollFrame !== 0,
  // Test-only proof that the editor can select a range spanning A5 pages.
  // Never expose this operation for a real saved manuscript.
  selectAllTestFixture: () => {
    if (!editor || !testDocumentBackup || !suppressTestAutoSave) return false;
    editor.command.executeSelectAll();
    return true;
  },
  setPageTestFixture,
};
updateZoomControls();
requestHandshake("startup");
