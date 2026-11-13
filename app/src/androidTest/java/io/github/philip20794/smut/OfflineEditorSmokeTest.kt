package io.github.philip20794.smut

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
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
        onWebView().withElement(findElement(Locator.ID, "recheck-button"))
            .perform(webClick())

        awaitJavaScriptValue(
            "document.getElementById('recheck-button')?.dataset.clicks",
            "\"1\"",
            exact = true
        )
        awaitJavaScriptValue(
            "document.getElementById('bridge-status')?.textContent",
            "Android-Verbindung aktiv"
        )

        onWebView().withElement(findElement(Locator.ID, "save-button"))
            .perform(webClick())
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
