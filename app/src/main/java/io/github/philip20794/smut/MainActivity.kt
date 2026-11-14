package io.github.philip20794.smut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmutStartScreen()
                }
            }
        }
    }
}

@Composable
internal fun SmutStartScreen(bookId: String? = null) {
    val context = LocalContext.current
    val webView = remember(context, bookId) { createEditorWebView(context, bookId) }
    DisposableEffect(webView) {
        onDispose {
            webView.removeJavascriptInterface("SmutAndroid")
            webView.stopLoading()
            webView.destroy()
        }
    }
    // Android 15+ draws edge-to-edge. Keep WebView controls clear of the
    // status/navigation bars and shrink the editor above the soft keyboard.
    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()
    )
}
