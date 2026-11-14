package io.github.philip20794.smut

import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class StructuredBookEditorInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = BookStore(context)
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun findWebView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) {
            val found = findWebView(view.getChildAt(i))
            if (found != null) return found
        }
        return null
    }
    private fun evaluate(scenario: ActivityScenario<BookEditorActivity>, script: String): String {
        val result = AtomicReference<String>()
        val latch = CountDownLatch(1)
        scenario.onActivity { activity ->
            val web = findWebView(activity.window.decorView)!!
            web.evaluateJavascript(script) { value ->
                result.set(value)
                latch.countDown()
            }
        }
        assertTrue("WebView callback timed out: $script", latch.await(8, TimeUnit.SECONDS))
        return result.get() ?: "null"
    }
    private fun await(scenario: ActivityScenario<BookEditorActivity>, script: String, contains: String) {
        val deadline = SystemClock.elapsedRealtime() + 25_000
        var last = "null"
        while (SystemClock.elapsedRealtime() < deadline) {
            last = evaluate(scenario, script)
            if (last.contains(contains)) return
            SystemClock.sleep(250)
        }
        val state = evaluate(scenario, "document.getElementById('editor-status')?.textContent")
        throw AssertionError("$script: expected $contains got $last; editor=$state")
    }
    private fun nativeTapCanvas(scenario: ActivityScenario<BookEditorActivity>) {
        val x = evaluate(scenario,
            "(() => {let r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.left+Math.min(45,r.width*.2) : -1})()").toDouble()
        val y = evaluate(scenario,
            "(() => {let r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.top+Math.min(35,r.height*.1) : -1})()").toDouble()
        val viewport = evaluate(scenario, "window.innerWidth").toDouble()
        assertTrue("Canvas must be present and visible", x >= 0 && y >= 0)
        var screenX = -1
        var screenY = -1
        scenario.onActivity { activity ->
            val web = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            web.getLocationOnScreen(origin)
            val scale = web.width / viewport
            screenX = origin[0] + (x * scale).toInt()
            screenY = origin[1] + (y * scale).toInt()
        }
        assertTrue("Android must tap the real Canvas page", device.click(screenX, screenY))
        await(scenario, "document.activeElement?.classList?.contains('ce-inputarea')", "true")
    }
    private fun ime(scenario: ActivityScenario<BookEditorActivity>, text: String) {
        var applied = false
        scenario.onActivity { activity ->
            val web = findWebView(activity.window.decorView)!!
            val connection = web.onCreateInputConnection(EditorInfo())
            applied = connection?.commitText(text, 1) == true
        }
        assertTrue("Native Android IME must commit into A5 Canvas", applied)
    }
    private fun textOf(id: String): String {
        val chapters = (store.load(id) as BookLoad.Found).document.getJSONArray("chapters")
        val blocks = chapters.getJSONObject(0).getJSONArray("blocks")
        return (0 until blocks.length()).joinToString("\n") { i ->
            val runs = blocks.getJSONObject(i).getJSONArray("runs")
            (0 until runs.length()).joinToString("") { j -> runs.getJSONObject(j).getString("text") }
        }
    }
    @Test fun realAndroidImeWritesRevisionedManuscriptAndRestoresItAfterReopen() {
        val created = store.createBook("W01-E2E-" + UUID.randomUUID())
        assertTrue(created is BookLibraryResult.Success)
        val id = (created as BookLibraryResult.Success).bookId
        val legacy = File(context.filesDir, "prototype-editor-v1.json")
        val legacyBefore = if (legacy.exists()) legacy.readBytes() else null
        try {
            ActivityScenario.launch<BookEditorActivity>(BookEditorActivity.intent(context, id)).use { scenario ->
                await(scenario, "window.smutBook?.getBookId()", id)
                await(scenario, "window.smutPrototype?.isLoaded()", "true")
                assertEquals("", textOf(id))
                nativeTapCanvas(scenario)
                ime(scenario, "W01 Äöüß 😀")
                await(scenario, "window.smutBook?.getText()", "W01")
                await(scenario, "window.smutBook?.getRevision() >= 2", "true")
                assertTrue("Persisted structured manuscript must contain native IME text",
                    textOf(id).contains("W01 Äöüß"))
                await(scenario, "window.smutBook?.isDirty()", "false")
            }
            ActivityScenario.launch<BookEditorActivity>(BookEditorActivity.intent(context, id)).use { reopened ->
                await(reopened, "window.smutBook?.getBookId()", id)
                await(reopened, "window.smutBook?.getText()", "W01 Äöüß")
                assertTrue((store.load(id) as BookLoad.Found).position.blockId != null)
                // The legacy E-02 file cannot be overwritten by structured edits.
                if (legacyBefore == null) assertTrue(!legacy.exists())
                else assertTrue(legacyBefore.contentEquals(legacy.readBytes()))
            }
        } finally { store.deleteBook(id) }
    }

    @Test fun staleBookRevisionIsRejectedInsteadOfOverwritingNewerContent() {
        val created = store.createBook("W01-Conflict-" + UUID.randomUUID())
        val id = (created as BookLibraryResult.Success).bookId
        try {
            ActivityScenario.launch<BookEditorActivity>(BookEditorActivity.intent(context, id)).use { scenario ->
                await(scenario, "window.smutBook?.getBookId()", id)
                await(scenario, "window.smutPrototype?.isLoaded()", "true")
                assertTrue(store.renameBook(id, "Von anderer Instanz geändert") is BookLibraryResult.Success)
                nativeTapCanvas(scenario)
                ime(scenario, "Konkurrierende Änderungen")
                await(scenario, "window.smutBook?.getText()", "Konkurrierende")
                await(scenario, "document.getElementById('editor-status')?.textContent", "Speicherkonflikt")
                assertTrue("Failed save must remain dirty", evaluate(scenario,
                    "window.smutBook?.isDirty()").contains("true"))
                assertEquals("", textOf(id))
                assertEquals("Von anderer Instanz geändert",
                    (store.load(id) as BookLoad.Found).document.getString("title"))
            }
        } finally { store.deleteBook(id) }
    }
}
