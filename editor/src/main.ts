import { DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, nextZoom, formatZoom } from "./zoom.js";
import { caretScrollDelta } from "./caret-visibility.js";
import { selectionEdgeScrollDelta } from "./selection-edge-scroll.js";
import { selectionGripPoint, type GripPoint, type SelectionEdge, type SelectionRect } from "./selection-handles.js";
import { createReadyMessage, parseReadyAck } from "./bridge.js";
import { generatePdfProbe, bytesToBase64 } from "./pdf-probe.js";
import {
  INITIAL_DOCUMENT, createDocumentRequest, isPrototypeData, parseDocumentResponse,
  type PrototypeData,
} from "./prototype.js";

interface AndroidBridge {
  postMessage(payload: string): void;
}
interface CanvasEditor {
  command: {
    getValue(): { data: PrototypeData; options: Record<string, unknown> };
    getRange(): { startIndex: number; endIndex: number } | null;
    getRangeText(): unknown;
    getRangeContext(): { startPageNo: number; endPageNo: number; rangeRects: SelectionRect[] } | null;
    getOptions(): { height: number; pageGap: number };
    executeSetRange(startIndex: number, endIndex: number): void;
    executeSelectAll(): void;
    executePageScale(scale: number): void;
    executeSetValue(data: Partial<PrototypeData>): void;
  };
  listener: { contentChange?: () => void; rangeStyleChange?: () => void };
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
      getSelectionHandles(): { start: boolean; end: boolean; dragging: SelectionEdge | null };
      selectAllTestFixture(): boolean;
      setPageTestFixture(enabled: boolean): void;
      setPdfTestFixture(enabled: boolean): void;
      generatePdfProbe(): Promise<void>;
      getPdfProbeStatus(): { state: string; editorPages: number; pdfPages: number; error: string };
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
let pdfProbeStatus = { state: "idle", editorPages: 0, pdfPages: 0, error: "" };

function setPdfTestFixture(enabled: boolean): void {
  if (!editor) return;
  if (enabled) {
    if (testDocumentBackup) return;
    testDocumentBackup = editor.command.getValue().data;
    suppressTestAutoSave = true;
    const prose = Array.from({ length: 24 }, (_, i) =>
      `Absatz ${i + 1}: Ein kühler Wind strich über die Straße. Äpfel, Öl und Grüße standen in dem Brief, während der Morgen näher rückte. `
    ).join("");
    editor.command.executeSetValue({ main: [{ value: prose }] });
  } else if (testDocumentBackup) {
    editor.command.executeSetValue(testDocumentBackup);
    testDocumentBackup = null;
    suppressTestAutoSave = false;
  }
}

async function generatePdfProbeForTest(): Promise<void> {
  if (!editor || !testDocumentBackup || !window.SmutAndroid) {
    pdfProbeStatus = { state: "failed", editorPages: 0, pdfPages: 0, error: "Nur als isolierte Android-Testprobe verfügbar" };
    return;
  }
  const editorPages = container?.querySelectorAll(".ce-page-container canvas").length ?? 0;
  pdfProbeStatus = { state: "rendering", editorPages, pdfPages: 0, error: "" };
  try {
    const { bytes, pageCount } = await generatePdfProbe(editor.command.getValue());
    pdfProbeStatus.pdfPages = pageCount;
    pdfProbeStatus.state = "saving";
    const base64 = bytesToBase64(bytes);
    if (base64.length > 2_400_000) throw new Error("PDF-Probe überschreitet das Größenlimit");
    postRequest(JSON.stringify({
      version: 1, type: "save-pdf-probe", requestId: "e04-pdf",
      base64, editorPages, pdfPages: pageCount,
    }));
  } catch (error) {
    pdfProbeStatus.state = "failed";
    pdfProbeStatus.error = String(error);
  }
}

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
  scheduleSelectionHandles();
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
let selectionReleaseFrame = 0;

type GripDrag = {
  side: SelectionEdge;
  fixedIndex: number;
  initialStart: number;
  initialEnd: number;
  fingerX: number;
  fingerY: number;
  origin: GripPoint;
  currentX: number;
  currentY: number;
  moved: boolean;
};
let activeGrip: GripDrag | null = null;
let selectionHandleFrame = 0;
const selectionHandles: Partial<Record<SelectionEdge, HTMLButtonElement>> = {};
const gripPositions: Partial<Record<SelectionEdge, GripPoint>> = {};

function hideSelectionHandles(): void {
  for (const side of ["start", "end"] as const) {
    if (selectionHandles[side]) selectionHandles[side]!.hidden = true;
    delete gripPositions[side];
  }
}

/**
 * Range geometry comes from the editor (not guessed character widths).
 * Keep the 44 px grip targets fixed in the WebView viewport and hide them
 * when their actual selected-text endpoint is scrolled off screen.
 */
function refreshSelectionHandles(): void {
  if (activeGrip) return;
  if (!editor || !window.SmutAndroid || document.hidden) {
    hideSelectionHandles();
    return;
  }
  const range = editor.command.getRange();
  const context = editor.command.getRangeContext();
  const rects = context?.rangeRects;
  if (!context || !rects?.length || !range || range.startIndex < 0 ||
      range.endIndex <= range.startIndex) {
    hideSelectionHandles();
    return;
  }
  ensureSelectionHandles();
  const pages = Array.from(container?.querySelectorAll<HTMLCanvasElement>(
    ".ce-page-container canvas") ?? []);
  const { height, pageGap } = editor.command.getOptions();
  const visual = window.visualViewport;
  const top = Math.max(0, visual?.offsetTop ?? 0);
  const bottom = Math.min(window.innerHeight, top + (visual?.height ?? window.innerHeight));
  for (const side of ["start", "end"] as const) {
    const pageNo = side === "start" ? context.startPageNo : context.endPageNo;
    const rect = side === "start" ? rects[0] : rects[rects.length - 1];
    const canvas = pages[pageNo];
    const grip = selectionHandles[side]!;
    const point = selectionGripPoint(rect, pageNo, side,
      canvas?.getBoundingClientRect(), height, pageGap);
    if (!point || point.y < top + 12 || point.y > bottom - 12 ||
        point.x < 12 || point.x > window.innerWidth - 12) {
      grip.hidden = true;
      delete gripPositions[side];
      continue;
    }
    gripPositions[side] = point;
    grip.style.left = `${point.x - 22}px`;
    grip.style.top = `${point.y - 22}px`;
    grip.hidden = false;
  }
}

function scheduleSelectionHandles(): void {
  if (selectionHandleFrame) return;
  selectionHandleFrame = requestAnimationFrame(() => {
    selectionHandleFrame = 0;
    refreshSelectionHandles();
  });
}

/** Nearest visible page to the finger; same guard as the native page drag. */
function nearestCanvas(x: number, y: number): { canvas: HTMLCanvasElement; rect: DOMRect } | null {
  if (!container) return null;
  let target: HTMLCanvasElement | null = null;
  let targetRect: DOMRect | null = null;
  let distance = Number.POSITIVE_INFINITY;
  for (const canvas of Array.from(container.querySelectorAll<HTMLCanvasElement>(
    ".ce-page-container canvas"))) {
    const r = canvas.getBoundingClientRect();
    if (!r.width || !r.height || r.bottom < 0 || r.top > window.innerHeight) continue;
    const dx = Math.max(r.left - x, 0, x - r.right);
    const dy = Math.max(r.top - y, 0, y - r.bottom);
    const candidate = Math.hypot(dx, dy);
    if (candidate < distance) {
      target = canvas;
      targetRect = r;
      distance = candidate;
    }
  }
  return target && targetRect && distance <= 96
    ? { canvas: target, rect: targetRect } : null;
}

function beginGripDrag(event: TouchEvent, side: SelectionEdge): void {
  if (!editor || event.touches.length !== 1 || activeGrip) return;
  const touch = event.touches.item(0);
  const point = gripPositions[side];
  if (!touch || !point) return;
  const target = nearestCanvas(point.textX, point.textY);
  if (!target) return;
  event.preventDefault();
  event.stopPropagation();
  const original = editor.command.getRange();
  if (!original) return;
  cancelSelectionReleaseGuard();
  stopNativeSelectionDrag();
  const fixedIndex = side === "start" ? original.endIndex : original.startIndex;
  activeGrip = {
    side,
    fixedIndex,
    initialStart: original.startIndex,
    initialEnd: original.endIndex,
    fingerX: touch.clientX,
    fingerY: touch.clientY,
    origin: point,
    currentX: point.textX,
    currentY: point.textY,
    moved: false,
  };
  // Collapse the old selection so Canvas Editor doesn't mistake a grip press
  // for dragging the highlighted text. Shift-mousedown retains the opposite
  // end as the anchor, then its existing mousemove logic owns all hit-testing.
  editor.command.executeSetRange(fixedIndex, fixedIndex);
  target.canvas.dispatchEvent(new MouseEvent("mousedown", {
    bubbles: true, cancelable: true, view: window, button: 0, buttons: 1,
    shiftKey: true, clientX: point.textX, clientY: point.textY,
  }));
  isNativeSelectionDrag = true;
}

function moveGripDrag(event: TouchEvent): void {
  if (!activeGrip) return;
  event.preventDefault();
  event.stopPropagation();
  if (event.touches.length !== 1) return;
  const touch = event.touches.item(0);
  if (!touch) return;
  activeGrip.currentX = activeGrip.origin.textX + touch.clientX - activeGrip.fingerX;
  activeGrip.currentY = activeGrip.origin.textY + touch.clientY - activeGrip.fingerY;
  activeGrip.moved = activeGrip.moved ||
    Math.hypot(touch.clientX - activeGrip.fingerX, touch.clientY - activeGrip.fingerY) > 5;
  selectionDragPoint = { x: activeGrip.currentX, y: activeGrip.currentY };
  forwardNativeSelectionDrag(activeGrip.currentX, activeGrip.currentY);
  if (!selectionScrollFrame) selectionScrollFrame = requestAnimationFrame(autoScrollSelection);
}

function finishGripDrag(event?: TouchEvent): void {
  if (!activeGrip) return;
  event?.preventDefault();
  event?.stopPropagation();
  const grip = activeGrip;
  activeGrip = null;
  const target = nearestCanvas(grip.currentX, grip.currentY);
  target?.canvas.dispatchEvent(new MouseEvent("mouseup", {
    bubbles: true, cancelable: true, view: window, button: 0,
    clientX: grip.currentX, clientY: grip.currentY,
  }));
  stopNativeSelectionDrag();
  if (!grip.moved) editor?.command.executeSetRange(grip.initialStart, grip.initialEnd);
  else settleSelectionAtRelease();
  scheduleSelectionHandles();
}

function ensureSelectionHandles(): void {
  for (const side of ["start", "end"] as const) {
    if (selectionHandles[side]) continue;
    const grip = document.createElement("button");
    grip.type = "button";
    grip.className = `smut-selection-handle smut-selection-handle--${side}`;
    grip.setAttribute("aria-label",
      side === "start" ? "Beginn der Textauswahl verschieben" : "Ende der Textauswahl verschieben");
    grip.hidden = true;
    grip.addEventListener("touchstart", event => beginGripDrag(event, side), { passive: false });
    grip.addEventListener("touchmove", moveGripDrag, { passive: false });
    grip.addEventListener("touchend", finishGripDrag, { passive: false });
    grip.addEventListener("touchcancel", finishGripDrag, { passive: false });
    grip.addEventListener("click", event => event.preventDefault());
    document.body.append(grip);
    selectionHandles[side] = grip;
  }
}
window.addEventListener("scroll", scheduleSelectionHandles, { passive: true });
window.addEventListener("resize", scheduleSelectionHandles);
window.visualViewport?.addEventListener("resize", scheduleSelectionHandles);
window.visualViewport?.addEventListener("scroll", scheduleSelectionHandles);
window.addEventListener("blur", () => { activeGrip = null; hideSelectionHandles(); });
window.addEventListener("pagehide", () => { activeGrip = null; hideSelectionHandles(); });
document.addEventListener("visibilitychange", () => {
  if (document.hidden) { activeGrip = null; hideSelectionHandles(); }
});


/**
 * Android WebView may continue a compositor-driven fling after the final
 * touchend even though our selection RAF is cancelled. Hold the release
 * position while that fling decays, for *selection drags only* and never
 * for ordinary swipes. New gestures, keyboard input and IME composition
 * immediately cancel the guard so normal editing and scrolling remain usable.
 */
function cancelSelectionReleaseGuard(): void {
  if (selectionReleaseFrame) cancelAnimationFrame(selectionReleaseFrame);
  selectionReleaseFrame = 0;
}

function settleSelectionAtRelease(): void {
  cancelSelectionReleaseGuard();
  const releaseY = window.scrollY;
  const releaseUntil = performance.now() + 1800;
  const hold = (): void => {
    selectionReleaseFrame = 0;
    if (window.scrollY !== releaseY) window.scrollTo(window.scrollX, releaseY);
    if (performance.now() < releaseUntil) {
      selectionReleaseFrame = requestAnimationFrame(hold);
    }
  };
  selectionReleaseFrame = requestAnimationFrame(hold);
}

window.addEventListener("touchstart", cancelSelectionReleaseGuard, { passive: true });
window.addEventListener("wheel", cancelSelectionReleaseGuard, { passive: true });
window.addEventListener("keydown", cancelSelectionReleaseGuard, true);
container?.addEventListener("beforeinput", cancelSelectionReleaseGuard, true);
container?.addEventListener("compositionstart", cancelSelectionReleaseGuard, true);
window.addEventListener("blur", cancelSelectionReleaseGuard);
window.addEventListener("pagehide", cancelSelectionReleaseGuard);

/**
 * A Canvas Editor page is a separate canvas. During an Android selection drag,
 * the finger can cross page gaps or the visible edge of the WebView; route each
 * move to the closest visible page, clamped to that page's actual drawing area.
 * This path is never used for ordinary swipes or multi-touch gestures.
 */
function forwardNativeSelectionDrag(x: number, y: number): void {
  if (!isNativeSelectionDrag) return;
  const target = nearestCanvas(x, y);
  if (!target) return;
  const { rect: r } = target;
  target.canvas.dispatchEvent(new MouseEvent("mousemove", {
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
// A drag may finish over the toolbar or outside the editor container.
// Listen at window level so a released finger always stops the RAF loop.
function finishNativeSelectionTouch(): void {
  const wasSelecting = isNativeSelectionDrag;
  stopNativeSelectionDrag();
  if (wasSelecting) settleSelectionAtRelease();
  scheduleSelectionHandles();
}
window.addEventListener("touchend", event => {
  if (event.touches.length === 0) finishNativeSelectionTouch();
});
window.addEventListener("touchcancel", finishNativeSelectionTouch);
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
  if (range && range.endIndex > range.startIndex) {
    scheduleSelectionHandles();
    return;
  }
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
  scheduleSelectionHandles();
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
      scheduleSelectionHandles();
    };
    editor.listener.rangeStyleChange = scheduleSelectionHandles;
    // The editor repositions .ce-cursor after key presses and page reflow.
    // Observe only this one node to avoid re-render loops on the canvas.
    const caret = container.querySelector(".ce-cursor");
    if (caret) {
      new MutationObserver(scheduleCaretVisibility).observe(caret, {
        attributes: true, attributeFilter: ["style"],
      });
    }
    updateZoomControls();
    scheduleSelectionHandles();
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
  if (response.type === "pdf-probe-result" && response.requestId === "e04-pdf") {
    pdfProbeStatus.state = response.saved ? "saved" : "failed";
    if (!response.saved) pdfProbeStatus.error = "PDF konnte nicht gespeichert werden";
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
  // Native gesture tests inspect handle visibility, never mutate a range.
  getSelectionHandles: () => ({
    start: selectionHandles.start?.hidden === false,
    end: selectionHandles.end?.hidden === false,
    dragging: activeGrip?.side ?? null,
  }),
  // Test-only proof that the editor can select a range spanning A5 pages.
  // Never expose this operation for a real saved manuscript.
  selectAllTestFixture: () => {
    if (!editor || !testDocumentBackup || !suppressTestAutoSave) return false;
    editor.command.executeSelectAll();
    return true;
  },
  setPageTestFixture,
  setPdfTestFixture,
  generatePdfProbe: generatePdfProbeForTest,
  getPdfProbeStatus: () => ({ ...pdfProbeStatus }),
};
updateZoomControls();
requestHandshake("startup");
