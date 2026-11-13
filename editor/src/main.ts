import { createReadyMessage, parseReadyAck } from "./bridge.js";

interface AndroidBridge {
  postMessage(payload: string): void;
}

declare global {
  interface Window {
    SmutAndroid?: AndroidBridge;
  }
}

const status = document.getElementById("bridge-status");
const retryButton = document.getElementById("recheck-button");
let pendingRequestId = "";
let checkNumber = 0;

function requestHandshake(requestId: string): void {
  pendingRequestId = requestId;
  if (status) {
    status.textContent = "Android-Verbindung wird geprüft …";
  }
  if (window.SmutAndroid) {
    window.SmutAndroid.postMessage(createReadyMessage(requestId));
  } else if (status) {
    status.textContent = "Vorschau im Browser · keine Android-Verbindung";
  }
}

window.addEventListener("smut:native", (event: Event) => {
  const ack = parseReadyAck((event as CustomEvent<unknown>).detail);
  if (ack?.requestId === pendingRequestId && status) {
    status.textContent = "Android-Verbindung aktiv · vollständig offline";
  }
});

retryButton?.addEventListener("click", () => {
  checkNumber += 1;
  retryButton.dataset.clicks = String(checkNumber);
  requestHandshake(`recheck-${checkNumber}`);
});

requestHandshake("startup");
