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
const requestId = "startup";
const bridge = window.SmutAndroid;

window.addEventListener("smut:native", (event: Event) => {
  const ack = parseReadyAck((event as CustomEvent<unknown>).detail);
  if (ack?.requestId === requestId && status) {
    status.textContent = "Android-Verbindung aktiv · vollständig offline";
  }
});

if (bridge && status) {
  bridge.postMessage(createReadyMessage(requestId));
} else if (status) {
  status.textContent = "Vorschau im Browser · keine Android-Verbindung";
}
