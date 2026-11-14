package io.github.philip20794.smut

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream
import java.net.URI

internal const val EDITOR_URL = "https://appassets.androidplatform.net/assets/editor/index.html"
private const val EDITOR_PREFIX = "/assets/editor/"

/** Nur HTTPS-Assets aus dem APK werden angezeigt; keine fremden URLs oder Frames. */
internal fun isTrustedEditorAssetUrl(rawUrl: String): Boolean {
    val uri = try {
        URI(rawUrl)
    } catch (_: Exception) {
        return false
    }
    return uri.scheme == "https" &&
        uri.host == "appassets.androidplatform.net" &&
        uri.port == -1 &&
        uri.rawUserInfo == null &&
        uri.rawQuery == null &&
        uri.rawFragment == null &&
        uri.path?.startsWith(EDITOR_PREFIX) == true &&
        !uri.path.contains("..") &&
        !uri.rawPath.contains("%")
}

private fun blockedResource(): WebResourceResponse =
    WebResourceResponse(
        "text/plain",
        "UTF-8",
        403,
        "Forbidden",
        mapOf("Cache-Control" to "no-store"),
        ByteArrayInputStream(ByteArray(0))
    )

internal fun createEditorWebView(context: Context, bookId: String? = null): WebView {
    if (bookId != null) BookDocumentValidator.safeId(bookId)
    val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    return WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = false
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean = request.url.toString() != EDITOR_URL

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse {
                val url = request.url.toString()
                if (!isTrustedEditorAssetUrl(url)) return blockedResource()
                return assetLoader.shouldInterceptRequest(request.url) ?: blockedResource()
            }
        }
        addJavascriptInterface(EditorBridge(this, bookId), "SmutAndroid")
        loadUrl(EDITOR_URL)
    }
}
