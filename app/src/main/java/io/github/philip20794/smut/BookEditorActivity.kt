package io.github.philip20794.smut

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmutStartScreen(bookId)
                }
            }
        }
    }
}
