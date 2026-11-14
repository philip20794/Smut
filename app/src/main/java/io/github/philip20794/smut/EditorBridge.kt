package io.github.philip20794.smut

import android.content.Context
import android.util.AtomicFile
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * E-02: ausschließlich versionierte und begrenzte Nachrichten aus der lokalen,
 * streng per URL-Allowlist kontrollierten WebView. Das Dateiformat ist ein
 * temporäres Testformat, noch keine finale Buch- oder Kapitelpersistenz.
 */
internal class EditorBridge(private val webView: WebView) {
    private val prototypeFile = AtomicFile(File(webView.context.filesDir, "prototype-editor-v1.json"))
    private val pdfProbeFile = AtomicFile(File(webView.context.filesDir, "e04-a5-probe.pdf"))

    @JavascriptInterface
    fun postMessage(payload: String) {
        val request = BridgeProtocol.parseRequest(payload) ?: return
        val response = when (request) {
            is BridgeRequest.Ready -> BridgeProtocol.ack(request.requestId)
            is BridgeRequest.Load -> {
                val content = try {
                    val bytes = prototypeFile.openRead().use { it.readBytes() }
                    if (bytes.size > BridgeProtocol.MAX_DOCUMENT_LENGTH) null
                    else BridgeProtocol.parseDocument(String(bytes, StandardCharsets.UTF_8))
                } catch (_: IOException) {
                    null
                }
                BridgeProtocol.loadResult(request.requestId, content)
            }
            is BridgeRequest.PdfProbe -> {
                var stream: java.io.FileOutputStream? = null
                val saved = try {
                    val bytes = Base64.decode(request.base64, Base64.NO_WRAP)
                    require(bytes.size in 100..1_800_000)
                    require(bytes.copyOfRange(0, 4).contentEquals("%PDF".toByteArray(StandardCharsets.US_ASCII)))
                    stream = pdfProbeFile.startWrite()
                    stream.write(bytes)
                    pdfProbeFile.finishWrite(stream)
                    true
                } catch (_: Exception) {
                    if (stream != null) pdfProbeFile.failWrite(stream)
                    false
                }
                BridgeProtocol.pdfProbeResult(request.requestId, saved)
            }
            is BridgeRequest.Save -> {
                val content = request.document.toString().toByteArray(StandardCharsets.UTF_8)
                var stream: java.io.FileOutputStream? = null
                val saved = try {
                    stream = prototypeFile.startWrite()
                    stream.write(content)
                    prototypeFile.finishWrite(stream)
                    true
                } catch (_: IOException) {
                    if (stream != null) prototypeFile.failWrite(stream)
                    false
                }
                BridgeProtocol.saveResult(request.requestId, saved)
            }
        }
        webView.post {
            if (webView.url == EDITOR_URL) {
                val quoted = JSONObject.quote(response)
                webView.evaluateJavascript(
                    "window.dispatchEvent(new CustomEvent('smut:native', { detail: $quoted }));",
                    null
                )
            }
        }
    }
}

internal sealed class BridgeRequest(open val requestId: String) {
    data class Ready(override val requestId: String) : BridgeRequest(requestId)
    data class Load(override val requestId: String) : BridgeRequest(requestId)
    data class Save(override val requestId: String, val document: JSONObject) : BridgeRequest(requestId)
    data class PdfProbe(override val requestId: String, val base64: String) : BridgeRequest(requestId)
}

internal object BridgeProtocol {
    const val MAX_DOCUMENT_LENGTH = 750_000
    const val MAX_PDF_PROBE_LENGTH = 2_500_000
    private val base64Pattern = Regex("[A-Za-z0-9+/]+={0,2}")
    private val idPattern = Regex("[A-Za-z0-9_-]{1,40}")

    fun parseReady(payload: String): String? =
        (parseRequest(payload) as? BridgeRequest.Ready)?.requestId

    fun parseRequest(payload: String): BridgeRequest? {
        if (payload.length > MAX_PDF_PROBE_LENGTH) return null
        val json = try { JSONObject(payload) } catch (_: Exception) { return null }
        if (json.optInt("version", -1) != 1) return null
        val requestId = json.opt("requestId")
        if (requestId !is String || !idPattern.matches(requestId)) return null
        return when (json.optString("type")) {
            "ready" -> if (json.length() == 3) BridgeRequest.Ready(requestId) else null
            "load" -> if (json.length() == 3) BridgeRequest.Load(requestId) else null
            "save-pdf-probe" -> {
                if (json.length() != 6) return null
                val base64 = json.opt("base64")
                val editorPages = json.opt("editorPages")
                val pdfPages = json.opt("pdfPages")
                if (base64 !is String || base64.length > 2_400_000 || !base64Pattern.matches(base64)) return null
                if (editorPages !is Int || pdfPages !is Int || editorPages !in 2..100 || pdfPages !in 2..100) return null
                BridgeRequest.PdfProbe(requestId, base64)
            }
            "save" -> {
                if (json.length() != 4) return null
                val document = json.optJSONObject("document") ?: return null
                if (parseDocument(document.toString()) == null) return null
                BridgeRequest.Save(requestId, document)
            }
            else -> null
        }
    }

    fun parseDocument(serialized: String): JSONObject? {
        if (serialized.length > MAX_DOCUMENT_LENGTH) return null
        val document = try { JSONObject(serialized) } catch (_: Exception) { return null }
        val main = document.optJSONArray("main") ?: return null
        if (main.length() > 50_000) return null
        for (index in 0 until main.length()) {
            val element = main.optJSONObject(index) ?: return null
            if (element.opt("value") !is String) return null
        }
        return document
    }

    fun ack(requestId: String): String {
        require(idPattern.matches(requestId))
        return JSONObject()
            .put("version", 1)
            .put("type", "ready-ack")
            .put("requestId", requestId)
            .toString()
    }

    fun loadResult(requestId: String, document: JSONObject?): String {
        require(idPattern.matches(requestId))
        return JSONObject()
            .put("version", 1)
            .put("type", "load-result")
            .put("requestId", requestId)
            .put("document", document ?: JSONObject.NULL)
            .toString()
    }

    fun pdfProbeResult(requestId: String, saved: Boolean): String {
        require(idPattern.matches(requestId))
        return JSONObject()
            .put("version", 1)
            .put("type", "pdf-probe-result")
            .put("requestId", requestId)
            .put("saved", saved)
            .toString()
    }

    fun saveResult(requestId: String, saved: Boolean): String {
        require(idPattern.matches(requestId))
        return JSONObject()
            .put("version", 1)
            .put("type", "save-result")
            .put("requestId", requestId)
            .put("saved", saved)
            .toString()
    }
}
