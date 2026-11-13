package io.github.philip20794.smut

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject

/**
 * Nur der fest gebündelte Editor darf diese Schnittstelle verwenden.
 * Keine Datei-, Netzwerk- oder Datenzugriffsoperationen in F-03.
 */
internal class EditorBridge(private val webView: WebView) {
    @JavascriptInterface
    fun postMessage(payload: String) {
        val requestId = BridgeProtocol.parseReady(payload) ?: return
        webView.post {
            if (webView.url == EDITOR_URL) {
                // JSONObject.quote schützt die Übergabe vom JSON-String in JavaScript.
                val quotedPayload = JSONObject.quote(BridgeProtocol.ack(requestId))
                webView.evaluateJavascript(
                    "window.dispatchEvent(new CustomEvent('smut:native', { detail: $quotedPayload }));",
                    null
                )
            }
        }
    }
}

internal object BridgeProtocol {
    private const val MAX_PAYLOAD = 512
    private val idPattern = Regex("[A-Za-z0-9_-]{1,40}")

    fun parseReady(payload: String): String? {
        if (payload.length > MAX_PAYLOAD) return null
        val json = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return null
        }
        if (
            json.length() != 3 ||
            json.optInt("version", -1) != 1 ||
            json.optString("type") != "ready"
        ) return null
        val requestId = json.opt("requestId")
        if (requestId !is String || !idPattern.matches(requestId)) return null
        return requestId
    }

    fun ack(requestId: String): String {
        require(idPattern.matches(requestId))
        return JSONObject()
            .put("version", 1)
            .put("type", "ready-ack")
            .put("requestId", requestId)
            .toString()
    }
}
