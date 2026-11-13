package io.github.philip20794.smut

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.View
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class OfflineEditorSmokeTest {
    @get:Rule
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun embeddedEditorRunsOfflineAndButtonRechecksNativeHandshake() {
        scenarioRule.scenario.onActivity { activity ->
            assertEquals(
                "Die App darf keine Internet-Berechtigung verlangen",
                PackageManager.PERMISSION_DENIED,
                activity.checkSelfPermission(Manifest.permission.INTERNET)
            )
        }

        awaitJavaScriptValue(
            "document.getElementById('bridge-status')?.textContent",
            "Android-Verbindung aktiv"
        )

        // Echter Canvas Editor muss im lokalen HTML initialisiert sein.
        awaitJavaScriptValue(
            "window.smutPrototype?.isLoaded()",
            "true",
            exact = true
        )
        awaitJavaScriptValue(
            "window.smutPrototype?.getText()",
            "Äpfel"
        )

        // Tatsächlicher WebView-UI-Befehl statt bloßer Bridge-Mock.
        tapHtmlButton("recheck-button")

        awaitJavaScriptValue(
            "document.getElementById('recheck-button')?.dataset.clicks",
            "\"1\"",
            exact = true
        )
        awaitJavaScriptValue(
            "document.getElementById('bridge-status')?.textContent",
            "Android-Verbindung aktiv"
        )

        tapHtmlButton("save-button")
        awaitJavaScriptValue(
            "document.getElementById('editor-status')?.textContent",
            "Lokal gespeichert"
        )
        scenarioRule.scenario.recreate()
        awaitJavaScriptValue(
            "window.smutPrototype?.getText()",
            "Äpfel"
        )
    }


    /**
     * E-02: Native touch + Android InputConnection, not a JS-injected
     * document change. The actual Canvas Editor proxy textarea must receive
     * focus, then IME commitText and key events must edit the book data.
     */
    @Test
    fun touchingBookPageAndTypingThroughAndroidInputConnectionPersistsUnicode() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)

        ensureEditorWindowFocused()
        val cssX = evaluate(
            "(() => { const r = document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.left + Math.min(52, r.width * 0.2) : -1; })()"
        ).toDouble()
        val cssY = evaluate(
            "(() => { const r = document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.top + Math.min(40, r.height * 0.1) : -1; })()"
        ).toDouble()
        assertTrue("Canvas page must exist", cssX >= 0 && cssY >= 0)
        val viewportWidth = evaluate("window.innerWidth").toDouble()
        var tapX = 0
        var tapY = 0
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / viewportWidth
            tapX = origin[0] + (cssX * ratio).toInt()
            tapY = origin[1] + (cssY * ratio).toInt()
        }
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertTrue("Native page tap must succeed at $tapX,$tapY", device.click(tapX, tapY))
        val focusDiagnostic = evaluate(
            "JSON.stringify({active:document.activeElement?.className,tag:document.activeElement?.tagName,focused:document.hasFocus(),page:document.querySelector('.ce-page-container canvas')?.getBoundingClientRect()?.toJSON()})"
        )
        try {
            awaitJavaScriptValue(
                "document.activeElement?.classList?.contains('ce-inputarea')",
                "true",
                exact = true
            )
        } catch (failure: AssertionError) {
            throw AssertionError(
                "Touch at $tapX,$tapY did not focus editor; state=$focusDiagnostic; ${failure.message}",
                failure
            )
        }

        // Commit a real non-ASCII composition through Android's IME interface.
        sendImeText("Smut Äöüß")
        awaitJavaScriptValue("window.smutPrototype?.getText()", "Smut Äöüß")

        // Android IME composition updates the same word before committing it.
        // This covers the input path used by suggestions and autocorrection.
        sendImeComposition("Korrekter", "Korrektür")
        awaitJavaScriptValue("window.smutPrototype?.getText()", "Korrektür")
        awaitJavaScriptValue(
            "window.smutPrototype?.getText()?.includes('Korrekter')",
            "false",
            exact = true
        )

        sendImeKey(KeyEvent.KEYCODE_ENTER)
        sendImeText("Zweiter Absatz")
        awaitJavaScriptValue("window.smutPrototype?.getText()", "Zweiter Absatz")

        sendImeKey(KeyEvent.KEYCODE_DEL)
        awaitJavaScriptValue(
            "window.smutPrototype?.getText()?.includes('Zweiter Absatz')",
            "false",
            exact = true
        )
        awaitJavaScriptValue(
            "window.smutPrototype?.getText()?.includes('Zweiter Absat')",
            "true",
            exact = true
        )

        tapHtmlButton("save-button")
        awaitJavaScriptValue("document.getElementById('editor-status')?.textContent", "Lokal gespeichert")
        scenarioRule.scenario.recreate()
        awaitJavaScriptValue("window.smutPrototype?.getText()", "Smut Äöüß")
        awaitJavaScriptValue("window.smutPrototype?.getText()", "Zweiter Absat")
    }

    /**
     * Emulator setup and the Google keyboard may briefly steal window focus.
     * Reorder the ActivityScenario-owned task to the front instead of
     * injecting editor changes or accepting background-window clicks.
     */
    private fun ensureEditorWindowFocused() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wakeUp()
        device.executeShellCommand("wm dismiss-keyguard")
        var launch = ""
        repeat(4) {
            // CLEAR_TOP + SINGLE_TOP reuses the ActivityScenario-owned
            // MainActivity instead of opening a second editor instance.
            launch = device.executeShellCommand(
                "am start -W -n io.github.philip20794.smut.dev/io.github.philip20794.smut.MainActivity -f 0x24000000"
            )
            SystemClock.sleep(800L)
            var focused = false
            scenarioRule.scenario.onActivity { focused = it.hasWindowFocus() }
            if (focused) return
            device.waitForIdle()
        }
        val focusReport = device.executeShellCommand("dumpsys window windows")
            .lineSequence()
            .filter { it.contains("mCurrentFocus") || it.contains("mFocusedApp") || it.contains("mInputMethodTarget") }
            .take(6)
            .joinToString(" | ")
        assertTrue(
            "Smut must own emulator window; wm=$focusReport; launch=$launch",
            false
        )
    }

    private fun tapHtmlButton(id: String) {
        ensureEditorWindowFocused()
        // DOM determines the target coordinate, but the click itself is a
        // genuine Android touchscreen event (not HTMLElement.click()).
        evaluate("document.getElementById('$id')?.scrollIntoView({block:'center'})")
        val cssX = evaluate(
            "(() => { const r=document.getElementById('$id')?.getBoundingClientRect(); return r ? r.left + r.width/2 : -1; })()"
        ).toDouble()
        val cssY = evaluate(
            "(() => { const r=document.getElementById('$id')?.getBoundingClientRect(); return r ? r.top + r.height/2 : -1; })()"
        ).toDouble()
        assertTrue("Button $id must exist", cssX >= 0 && cssY >= 0)
        val cssViewportWidth = evaluate("window.innerWidth").toDouble()
        var x = 0
        var y = 0
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / cssViewportWidth
            x = origin[0] + (cssX * ratio).toInt()
            y = origin[1] + (cssY * ratio).toInt()
        }
        assertTrue("Android touch did not click $id at $x,$y",
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).click(x, y))
    }

    private fun sendImeText(value: String) {
        var accepted = false
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val connection = webView.onCreateInputConnection(EditorInfo())
            assertNotNull("Focused WebView needs an Android IME input connection", connection)
            accepted = connection!!.commitText(value, 1)
        }
        assertTrue("Android IME commitText must be accepted", accepted)
    }

    private fun sendImeComposition(draft: String, correction: String) {
        var accepted = false
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val connection = webView.onCreateInputConnection(EditorInfo())
            assertNotNull("Focused WebView needs an Android IME input connection", connection)
            accepted = connection!!.setComposingText(draft, 1) &&
                connection.setComposingText(correction, 1) &&
                connection.finishComposingText()
        }
        assertTrue("Android IME text composition must be accepted", accepted)
    }

    private fun sendImeKey(keyCode: Int) {
        var accepted = false
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val connection = webView.onCreateInputConnection(EditorInfo())
            assertNotNull("Focused WebView needs an Android IME input connection", connection)
            val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            accepted = connection!!.sendKeyEvent(down) && connection.sendKeyEvent(up)
        }
        assertTrue("IME key event must be accepted: $keyCode", accepted)
    }

    private fun awaitJavaScriptValue(
        script: String,
        expected: String,
        exact: Boolean = false
    ) {
        val deadline = SystemClock.elapsedRealtime() + 20_000L
        var last = ""
        while (SystemClock.elapsedRealtime() < deadline) {
            last = evaluate(script)
            if (if (exact) last == expected else last.contains(expected)) return
            Thread.sleep(250)
        }
        val editorError = evaluate("document.getElementById('editor-status')?.textContent")
        val bridgeState = evaluate("document.getElementById('bridge-status')?.textContent")
        assertTrue(
            "WebView $script: expected $expected, received $last; editor=$editorError; bridge=$bridgeState",
            false
        )
    }

    private fun evaluate(script: String): String {
        val answer = AtomicReference<String>()
        val latch = CountDownLatch(1)
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)
            assertNotNull("Android-View muss eine WebView enthalten", webView)
            webView!!.evaluateJavascript(script) { value ->
                answer.set(value)
                latch.countDown()
            }
        }
        assertTrue("WebView-Callback timeout", latch.await(8, TimeUnit.SECONDS))
        return answer.get() ?: "null"
    }

    private fun findWebView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val found = findWebView(view.getChildAt(index))
                if (found != null) return found
            }
        }
        return null
    }
}
