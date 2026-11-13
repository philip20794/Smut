import { DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, nextZoom, formatZoom } from "./zoom.js";
import { createReadyMessage, parseReadyAck } from "./bridge.js";
import {
  INITIAL_DOCUMENT, createDocumentRequest, isPrototypeData, parseDocumentResponse,
  type PrototypeData,
} from "./prototype.js";

interface AndroidBridge {
  postMessage(payload: string): void;
}
interface CanvasEditor {
  command: { getValue(): { data: PrototypeData }; executePageScale(scale: number): void };
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
}


function report(message: string): void {
  if (editorStatus) editorStatus.textContent = message;
}

function postRequest(payload: string): void {
  window.SmutAndroid?.postMessage(payload);
}

function scheduleSave(): void {
  if (!editor || isLoading) return;
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
    editor.listener.contentChange = scheduleSave;
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
};
updateZoomControls();
requestHandshake("startup");
