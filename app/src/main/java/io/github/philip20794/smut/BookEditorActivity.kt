package io.github.philip20794.smut

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * W-01: native book identity comes only from an explicit in-app Intent.
 * Never infer it from a remote URL or the legacy prototype state.
 */
class BookEditorActivity : ComponentActivity() {
    private var bookWebView: WebView? = null
    companion object {
        private const val EXTRA_BOOK_ID = "io.github.philip20794.smut.BOOK_ID"

        fun intent(context: Context, bookId: String): Intent {
            BookDocumentValidator.safeId(bookId)
            return Intent(context, BookEditorActivity::class.java)
                .putExtra(EXTRA_BOOK_ID, bookId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID)?.let {
            runCatching { BookDocumentValidator.safeId(it) }.getOrNull()
        }
        if (bookId == null) {
            finish()
            return
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // The WebView finishes the Activity only after a successful flush.
                bookWebView?.evaluateJavascript("window.smutBook?.close()", null)
            }
        })
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmutStartScreen(bookId) { bookWebView = it }
                }
            }
        }
    }
}
