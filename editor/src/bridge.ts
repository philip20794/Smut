/** Versionierte, absichtlich schmale Kommunikation mit der Android-Hülle. */
export const BRIDGE_VERSION = 1;
const REQUEST_ID = /^[A-Za-z0-9_-]{1,40}$/;
const MAX_MESSAGE_LENGTH = 512;

export interface ReadyAck {
  readonly version: 1;
  readonly type: "ready-ack";
  readonly requestId: string;
}

export function createReadyMessage(requestId: string): string {
  if (!REQUEST_ID.test(requestId)) {
    throw new Error("Ungültige Bridge-Request-ID");
  }
  return JSON.stringify({
    version: BRIDGE_VERSION,
    type: "ready",
    requestId,
  });
}

export function parseReadyAck(payload: unknown): ReadyAck | null {
  if (typeof payload !== "string" || payload.length > MAX_MESSAGE_LENGTH) {
    return null;
  }
  let decoded: unknown;
  try {
    decoded = JSON.parse(payload);
  } catch {
    return null;
  }
  if (typeof decoded !== "object" || decoded === null || Array.isArray(decoded)) {
    return null;
  }
  const data = decoded as Record<string, unknown>;
  if (
    Object.keys(data).length !== 3 ||
    data.version !== BRIDGE_VERSION ||
    data.type !== "ready-ack" ||
    typeof data.requestId !== "string" ||
    !REQUEST_ID.test(data.requestId)
  ) {
    return null;
  }
  return { version: 1, type: "ready-ack", requestId: data.requestId };
}
