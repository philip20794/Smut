package io.github.philip20794.smut

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.View
import android.view.KeyEvent
import android.view.WindowInsets
import android.content.Context
import android.view.inputmethod.InputMethodManager
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

    @Test
    fun zoomControlsChangeTheCanvasViewWithoutMutatingBookText() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        awaitJavaScriptValue("window.smutPrototype?.getZoom()", "0.6", exact = true)
        awaitJavaScriptValue(
            "window.smutPrototype?.getPageCount() >= 1",
            "true",
            exact = true
        )

        val originalText = evaluate("window.smutPrototype?.getText()")
        tapHtmlButton("zoom-in")
        awaitJavaScriptValue("window.smutPrototype?.getZoom()", "0.7", exact = true)
        awaitJavaScriptValue("document.getElementById('zoom-value')?.textContent", "70 %")

        tapHtmlButton("zoom-out")
        awaitJavaScriptValue("window.smutPrototype?.getZoom()", "0.6", exact = true)
        assertEquals("Zoom darf Manuskripttext nicht ändern", originalText,
            evaluate("window.smutPrototype?.getText()"))
    }

    @Test
    fun longPressOnBookPageSelectsTextWithoutMutatingManuscript() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        val originalText = evaluate("window.smutPrototype?.getText()")
        selectWordByNativeLongPress()
        assertEquals(
            "Long-press selection must not modify the saved manuscript",
            originalText, evaluate("window.smutPrototype?.getText()")
        )
    }

    @Test
    fun selectedWordCanBeReplacedThroughAndroidImeWithoutPersistingTestFixture() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        val savedManuscript = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue("window.smutPrototype?.getText()", "Absatz 75")
            val fixtureBefore = evaluate("window.smutPrototype?.getText()")
            selectWordByNativeLongPress()
            val selectionLength = evaluate(
                "window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex"
            ).toInt()
            assertTrue("Word selection must cover real document characters", selectionLength > 0)
            sendImeText("SmutErsatz")
            awaitJavaScriptValue("window.smutPrototype?.getText()", "SmutErsatz")
            assertTrue(
                "Android IME must replace selected content, not leave document unchanged",
                fixtureBefore != evaluate("window.smutPrototype?.getText()")
            )
        } finally {
            // This local test document is never written to app storage.
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals(
            "Replacing a test selection must leave the user's saved book untouched",
            savedManuscript, evaluate("window.smutPrototype?.getText()")
        )
    }

    /**
     * E-03: a real long-press must select text on the Canvas page. This is a
     * native Android touch gesture, not a JavaScript range injection.
     * If the candidate editor has no mobile selection gesture, keep this gate
     * red and evaluate an accessible alternative before claiming support.
     */
    /**
     * E-03 engine probe: after a genuine native touch focuses the editor,
     * select all across several A5 pages through the documented Canvas
     * command and replace through Android IME. The programmatic range is
     * deliberately NOT a substitute for testing cross-page selection handles.
     */
    @Test
    fun aMultiPageRangeCanBeReplacedViaAndroidImeAndRestoredWithoutDataLoss() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        val savedManuscript = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue(
                "window.smutPrototype?.getPageCount() >= 2",
                "true", exact = true
            )
            selectWordByNativeLongPress()
            assertEquals(
                "Select-all probe only permitted while isolated page fixture is active",
                "true", evaluate("window.smutPrototype?.selectAllTestFixture()")
            )
            val selectedLength = evaluate(
                "window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex"
            ).toInt()
            assertTrue("Range must span several thousand text characters", selectedLength > 1000)
            sendImeText("Ein neuer Absatz auf einer A5-Seite")
            awaitJavaScriptValue(
                "window.smutPrototype?.getText()?.includes('Ein neuer Absatz auf einer A5-Seite')",
                "true", exact = true
            )
            awaitJavaScriptValue(
                "window.smutPrototype?.getText()?.includes('Absatz 75')",
                "false", exact = true
            )
            awaitJavaScriptValue(
                "window.smutPrototype?.getPageCount() === 1",
                "true", exact = true
            )
        } finally {
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals(
            "Whole-document range probe must restore the real saved book",
            savedManuscript, evaluate("window.smutPrototype?.getText()")
        )
    }

    /**
     * E-03: a real long-press-and-drag gesture should extend the selection.
     * A plain swipe without a long press must remain available for scrolling.
     */
    @Test
    fun nativeLongPressDragExtendsWordSelectionWithoutChangingBook() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val original = evaluate("window.smutPrototype?.getText()")
        evaluate(
            """(() => {
                window.__smutDragEvents = {context:0, move:0};
                const c = document.querySelector('.ce-page-container canvas');
                c?.addEventListener('contextmenu', () => window.__smutDragEvents.context++, {capture:true});
                c?.addEventListener('touchmove', () => window.__smutDragEvents.move++, {capture:true});
                return !!c;
            })()"""
        )
        val cssX = evaluate(
            "(() => { const r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.left + Math.min(85, r.width*0.25) : -1; })()"
        ).toDouble()
        val cssY = evaluate(
            "(() => { const r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.top + Math.min(42, r.height*0.1) : -1; })()"
        ).toDouble()
        assertTrue("Canvas target must exist", cssX >= 0 && cssY >= 0)
        val viewportWidth = evaluate("window.innerWidth").toDouble()
        var startX = -1
        var startY = -1
        var endX = -1
        var endY = -1
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / viewportWidth
            startX = origin[0] + (cssX * ratio).toInt()
            startY = origin[1] + (cssY * ratio).toInt()
            endX = origin[0] + ((cssX + 90) * ratio).toInt()
            endY = origin[1] + ((cssY + 65) * ratio).toInt()
        }
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertTrue(
            "Native press-and-drag did not dispatch",
            device.drag(startX, startY, endX, endY, 85)
        )
        try {
            awaitJavaScriptValue(
                "(window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex) > 12",
                "true", exact = true
            )
        } catch (failure: AssertionError) {
            val diagnostic = evaluate(
                """JSON.stringify({
                    events:window.__smutDragEvents,
                    selection:window.smutPrototype?.getSelection(),
                    focused:document.hasFocus()
                })"""
            )
            throw AssertionError(
                "Native press-and-drag from $startX,$startY to $endX,$endY did not extend selection; $diagnostic; ${failure.message}",
                failure
            )
        }
        assertEquals(
            "Touch selection drag must not change book content",
            original, evaluate("window.smutPrototype?.getText()")
        )
    }

    /**
     * E-03: the actual Android finger must select across two distinct A5 page
     * canvases. No JavaScript range setter is allowed; the 75-paragraph fixture
     * must be restored even when a gesture assertion fails.
     */
    @Test
    fun nativeLongPressDragSelectsAcrossTwoA5PagesWithoutPersistingFixture() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val savedManuscript = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue("window.smutPrototype?.getPageCount() >= 2", "true", exact = true)

            // Center the seam between the first two real canvases in the
            // viewport, so both the origin and destination can be touched.
            evaluate(
                """(() => {
                    const pages = document.querySelectorAll('.ce-page-container canvas');
                    const a = pages[0]?.getBoundingClientRect();
                    const b = pages[1]?.getBoundingClientRect();
                    if (!a || !b) return false;
                    window.scrollBy(0, (a.bottom + b.top) / 2 - innerHeight * 0.5);
                    return true;
                })()"""
            )
            val cssStartX = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[0].getBoundingClientRect(); return r.left + Math.min(85,r.width*0.25); })()"
            ).toDouble()
            val cssStartY = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[0].getBoundingClientRect(); return r.bottom - Math.min(85,r.height*0.18); })()"
            ).toDouble()
            val cssEndX = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[1].getBoundingClientRect(); return r.left + Math.min(90,r.width*0.26); })()"
            ).toDouble()
            val cssEndY = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[1].getBoundingClientRect(); return r.top + Math.min(95,r.height*0.2); })()"
            ).toDouble()
            assertEquals(
                "Start must hit page 1 canvas", "true",
                evaluate("document.elementFromPoint($cssStartX,$cssStartY) === document.querySelectorAll('.ce-page-container canvas')[0]")
            )
            assertEquals(
                "End must hit a DIFFERENT page 2 canvas", "true",
                evaluate("document.elementFromPoint($cssEndX,$cssEndY) === document.querySelectorAll('.ce-page-container canvas')[1]")
            )
            val viewportWidth = evaluate("innerWidth").toDouble()
            var startX = -1
            var startY = -1
            var endX = -1
            var endY = -1
            scenarioRule.scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                val origin = IntArray(2)
                webView.getLocationOnScreen(origin)
                val ratio = webView.width / viewportWidth
                startX = origin[0] + (cssStartX * ratio).toInt()
                startY = origin[1] + (cssStartY * ratio).toInt()
                endX = origin[0] + (cssEndX * ratio).toInt()
                endY = origin[1] + (cssEndY * ratio).toInt()
            }
            assertTrue("Native cross-page drag must dispatch",
                UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                    .drag(startX, startY, endX, endY, 100))
            try {
                awaitJavaScriptValue(
                    "(window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex) > 85",
                    "true", exact = true
                )
            } catch (failure: AssertionError) {
                val diagnostic = evaluate(
                    """JSON.stringify({
                        range:window.smutPrototype?.getSelection(),
                        scroll:window.scrollY,
                        pages:[...document.querySelectorAll('.ce-page-container canvas')]
                            .slice(0,2).map(c => c.getBoundingClientRect().toJSON())
                    })"""
                )
                throw AssertionError(
                    "Cross-page native drag $startX,$startY -> $endX,$endY failed; $diagnostic; ${failure.message}",
                    failure
                )
            }
        } finally {
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals("Native multi-page touch selection must preserve real book",
            savedManuscript, evaluate("window.smutPrototype?.getText()"))
    }

    /**
     * E-03 regression: a real held Android long-press drag must keep extending
     * the native selection while the page scrolls near the lower WebView edge.
     * The fixture is never saved, and this test never injects a JS selection.
     */
    @Test
    fun nativeSelectionDragNearBottomEdgeScrollsAndExtendsRange() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val savedManuscript = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue("window.smutPrototype?.getPageCount() >= 3", "true", exact = true)
            evaluate(
                """(() => {
                    const c=document.querySelector('.ce-page-container canvas');
                    const r=c?.getBoundingClientRect();
                    if (!r) return false;
                    window.scrollBy(0, r.top - 110);
                    return true;
                })()"""
            )
            val startX = evaluate(
                "(() => { const r=document.querySelector('.ce-page-container canvas').getBoundingClientRect(); return r.left + Math.min(85,r.width*.25); })()"
            ).toDouble()
            val startY = evaluate(
                "(() => { const r=document.querySelector('.ce-page-container canvas').getBoundingClientRect(); return r.top + 65; })()"
            ).toDouble()
            val endY = evaluate(
                "(() => { const v=visualViewport; return Math.min(innerHeight-15,(v?.offsetTop??0)+(v?.height??innerHeight)-14); })()"
            ).toDouble()
            assertTrue("Start must be above bottom edge", startY + 100 < endY)
            assertEquals(
                "Start must hit the actual first canvas", "true",
                evaluate("document.elementFromPoint($startX,$startY) === document.querySelector('.ce-page-container canvas')")
            )
            val scrollBefore = evaluate("window.scrollY").toDouble()
            val viewportWidth = evaluate("innerWidth").toDouble()
            var x1 = -1
            var y1 = -1
            var x2 = -1
            var y2 = -1
            scenarioRule.scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                val origin = IntArray(2)
                webView.getLocationOnScreen(origin)
                val ratio = webView.width / viewportWidth
                x1 = origin[0] + (startX * ratio).toInt()
                y1 = origin[1] + (startY * ratio).toInt()
                x2 = x1
                y2 = origin[1] + (endY * ratio).toInt()
            }
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            assertTrue("Native held selection drag must dispatch", device.drag(x1,y1,x2,y2,180))
            val scrollAfter = evaluate("window.scrollY").toDouble()
            val selectionLength = evaluate(
                "(window.smutPrototype?.getSelection()?.endIndex ?? 0) - (window.smutPrototype?.getSelection()?.startIndex ?? 0)"
            ).toInt()
            assertTrue(
                "Native edge drag must scroll and extend range: scroll=$scrollBefore -> $scrollAfter; range=$selectionLength",
                scrollAfter > scrollBefore + 8 && selectionLength > 85
            )
            // Touch release must terminate the requestAnimationFrame loop.
            // A stray loop would keep moving the page after the finger lifts.
            awaitJavaScriptValue(
                "window.smutPrototype?.isSelectionDragActive()",
                "false", exact = true
            )
            val settledScroll = evaluate("window.scrollY").toDouble()
            SystemClock.sleep(350L)
            val scrollAfterRelease = evaluate("window.scrollY").toDouble()
            assertTrue(
                "Selection autoscroll must stop on native touchend: $settledScroll -> $scrollAfterRelease",
                kotlin.math.abs(scrollAfterRelease - settledScroll) < 2.0
            )
            assertEquals("Gesture must not mutate manuscript fixture", "true",
                evaluate("window.smutPrototype?.getText()?.includes('Absatz 75')"))
        } finally {
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals("Native edge drag must not persist fixture",
            savedManuscript, evaluate("window.smutPrototype?.getText()"))
    }

    /**
     * E-03: the real Android IME must not cover the active caret when writing
     * on a later A5 page. The test taps the canvas through UiDevice, opens the
     * native keyboard and commits text through Android InputConnection.
     */
    @Test
    fun laterA5PageCaretRemainsVisibleAfterAndroidKeyboardOpensAndTyping() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val savedManuscript = evaluate("window.smutPrototype?.getText()")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue("window.smutPrototype?.getPageCount() >= 3", "true", exact = true)
            device.executeShellCommand("settings put secure show_ime_with_hard_keyboard 1")

            // Bring a point WITHIN the second page's text into the lower
            // portion of the pre-keyboard viewport so IME insets must be
            // accounted for after the native focus/keyboard transition.
            evaluate(
                """(() => {
                    const page = document.querySelectorAll('.ce-page-container canvas')[1];
                    const r = page?.getBoundingClientRect();
                    if (!r) return false;
                    window.scrollBy(0, r.top + Math.min(100, r.height * 0.20) - innerHeight * 0.74);
                    return true;
                })()"""
            )
            val cssX = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[1].getBoundingClientRect(); return r.left + Math.min(85,r.width*.25); })()"
            ).toDouble()
            val cssY = evaluate(
                "(() => { const r=document.querySelectorAll('.ce-page-container canvas')[1].getBoundingClientRect(); return r.top + Math.min(100,r.height*.20); })()"
            ).toDouble()
            assertEquals(
                "Native tap must target A5 page 2", "true",
                evaluate("document.elementFromPoint($cssX,$cssY) === document.querySelectorAll('.ce-page-container canvas')[1]")
            )
            val viewportWidth = evaluate("innerWidth").toDouble()
            var screenX = -1
            var screenY = -1
            scenarioRule.scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                val origin = IntArray(2)
                webView.getLocationOnScreen(origin)
                val ratio = webView.width / viewportWidth
                screenX = origin[0] + (cssX * ratio).toInt()
                screenY = origin[1] + (cssY * ratio).toInt()
            }
            assertTrue("Tap on later canvas must dispatch", device.click(screenX, screenY))
            awaitJavaScriptValue(
                "document.activeElement?.classList?.contains('ce-inputarea')",
                "true", exact = true
            )
            scenarioRule.scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                val manager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT)
            }
            val imeDeadline = SystemClock.elapsedRealtime() + 12_000L
            var imeVisible = false
            while (SystemClock.elapsedRealtime() < imeDeadline) {
                scenarioRule.scenario.onActivity { activity ->
                    imeVisible = activity.window.decorView.rootWindowInsets
                        ?.isVisible(WindowInsets.Type.ime()) == true
                }
                if (imeVisible) break
                SystemClock.sleep(250)
            }
            assertTrue("Android keyboard must actually be visible for E-03 regression", imeVisible)

            sendImeText(" Tastaturtest Äöüß")
            awaitJavaScriptValue(
                "window.smutPrototype?.getText()?.includes('Tastaturtest Äöüß')",
                "true", exact = true
            )
            try {
                awaitJavaScriptValue(
                    """(() => {
                        const caret = document.querySelector('#canvas-editor .ce-cursor');
                        const input = document.querySelector('#canvas-editor .ce-inputarea');
                        if (!caret || document.activeElement !== input) return false;
                        const r = caret.getBoundingClientRect();
                        const v = window.visualViewport;
                        const top = Math.max(0, v?.offsetTop ?? 0);
                        const bottom = Math.min(innerHeight,
                            (v?.offsetTop ?? 0) + (v?.height ?? innerHeight));
                        return r.height > 0 && r.top >= top + 8 && r.bottom <= bottom - 8;
                    })()""",
                    "true", exact = true
                )
            } catch (failure: AssertionError) {
                val diagnostic = evaluate(
                    """(() => {
                        const c=document.querySelector('#canvas-editor .ce-cursor');
                        return JSON.stringify({
                            caret:c?.getBoundingClientRect().toJSON(),
                            viewport:[innerHeight,visualViewport?.height,visualViewport?.offsetTop],
                            scroll:scrollY,
                            pageCount:window.smutPrototype?.getPageCount(),
                            focus:document.activeElement?.className
                        });
                    })()"""
                )
                throw AssertionError("IME must not hide page-2 caret; $diagnostic; ${failure.message}", failure)
            }

            // E-03: changing the A5 page scale while the Android keyboard is
            // open must preserve focus, text and a visible caret. Trigger the
            // existing zoom control without tapping away from the IME field.
            assertEquals("Zoom starts at 60 %", "0.6",
                evaluate("window.smutPrototype?.getZoom()"))
            evaluate("document.getElementById('zoom-in')?.click()")
            awaitJavaScriptValue("window.smutPrototype?.getZoom()", "0.7", exact = true)
            awaitJavaScriptValue(
                """(() => {
                    const c=document.querySelector('#canvas-editor .ce-cursor');
                    const input=document.querySelector('#canvas-editor .ce-inputarea');
                    const r=c?.getBoundingClientRect();
                    const v=visualViewport;
                    const top=Math.max(0,v?.offsetTop??0);
                    const bottom=Math.min(innerHeight,(v?.offsetTop??0)+(v?.height??innerHeight));
                    return document.activeElement===input && !!r && r.height>0 &&
                        r.top>=top+8 && r.bottom<=bottom-8;
                })()""", "true", exact = true
            )
            var imeAfterZoom = false
            scenarioRule.scenario.onActivity { activity ->
                imeAfterZoom = activity.window.decorView.rootWindowInsets
                    ?.isVisible(WindowInsets.Type.ime()) == true
            }
            assertTrue("Zoom must not dismiss Android IME", imeAfterZoom)
            sendImeText(" Zoomprüfung")
            awaitJavaScriptValue(
                "window.smutPrototype?.getText()?.includes('Zoomprüfung')",
                "true", exact = true
            )
            evaluate("document.getElementById('zoom-out')?.click()")
            awaitJavaScriptValue("window.smutPrototype?.getZoom()", "0.6", exact = true)
            awaitJavaScriptValue(
                """(() => {
                    const c=document.querySelector('#canvas-editor .ce-cursor');
                    const r=c?.getBoundingClientRect();
                    const v=visualViewport;
                    const top=Math.max(0,v?.offsetTop??0);
                    const bottom=Math.min(innerHeight,(v?.offsetTop??0)+(v?.height??innerHeight));
                    return !!r && r.height>0 && r.top>=top+8 && r.bottom<=bottom-8;
                })()""", "true", exact = true
            )
        } finally {
            scenarioRule.scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                val manager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(webView.windowToken, 0)
            }
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals("IME visibility test must not persist the 75-paragraph manuscript",
            savedManuscript, evaluate("window.smutPrototype?.getText()"))
    }

    private fun selectWordByNativeLongPress() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        evaluate(
            """(() => {
                window.__smutSelectionTouches = {touch:0, pointer:0, context:0};
                const canvas = document.querySelector('.ce-page-container canvas');
                canvas?.addEventListener('touchstart', () => window.__smutSelectionTouches.touch++, {capture:true});
                canvas?.addEventListener('pointerdown', () => window.__smutSelectionTouches.pointer++, {capture:true});
                canvas?.addEventListener('contextmenu', () => window.__smutSelectionTouches.context++, {capture:true});
                return !!canvas;
            })()"""
        )
        val cssX = evaluate(
            "(() => { const r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.left + Math.min(85, r.width*0.25) : -1; })()"
        ).toDouble()
        val cssY = evaluate(
            "(() => { const r=document.querySelector('.ce-page-container canvas')?.getBoundingClientRect(); return r ? r.top + Math.min(42, r.height*0.1) : -1; })()"
        ).toDouble()
        assertTrue("Canvas text target must exist", cssX >= 0 && cssY >= 0)
        val viewportWidth = evaluate("window.innerWidth").toDouble()
        var x = -1
        var y = -1
        var target = ""
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / viewportWidth
            x = origin[0] + (cssX * ratio).toInt()
            y = origin[1] + (cssY * ratio).toInt()
            target = "origin=${origin.toList()}, webView=${webView.width}x${webView.height}, ratio=$ratio"
        }
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Stationary native swipe held past Android's long-press timeout.
        assertTrue("Native long-press at $x,$y must dispatch", device.swipe(x, y, x, y, 85))
        try {
            awaitJavaScriptValue(
                "window.smutPrototype?.getSelection()?.hasSelection",
                "true", exact = true
            )
        } catch (failure: AssertionError) {
            val diagnostic = evaluate(
                """JSON.stringify({
                    gesture:window.__smutSelectionTouches,
                    range:window.smutPrototype?.getSelection(),
                    active:document.activeElement?.className,
                    focused:document.hasFocus(),
                    canvas:document.querySelector('.ce-page-container canvas')?.getBoundingClientRect()?.toJSON()
                })"""
            )
            throw AssertionError(
                "Long-press did not select text at $x,$y ($target); state=$diagnostic; ${failure.message}",
                failure
            )
        }
    }

    /**
     * E-03: the user must be able to drag the visible END selection grip using
     * a real Android touch. The test neither injects a JS range nor weakens
     * existing long-press / multi-page coverage.
     */
    @Test
    fun nativeSelectionHandleDragExtendsWordSelectionWithoutModifyingText() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val original = evaluate("window.smutPrototype?.getText()")
        selectWordByNativeLongPress()
        awaitJavaScriptValue(
            "window.smutPrototype?.getSelectionHandles()?.end",
            "true", exact = true
        )
        val before = evaluate(
            "window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex"
        ).toInt()
        val x = evaluate(
            "(() => { const r=document.querySelector('.smut-selection-handle--end')?.getBoundingClientRect(); return r ? r.left + r.width/2 : -1; })()"
        ).toDouble()
        val y = evaluate(
            "(() => { const r=document.querySelector('.smut-selection-handle--end')?.getBoundingClientRect(); return r ? r.top + r.height/2 : -1; })()"
        ).toDouble()
        assertTrue("Selection end grip must be on screen", x >= 0 && y >= 0)
        assertEquals("Grip must own the native finger target", "true", evaluate(
            "document.elementFromPoint($x,$y)?.classList.contains('smut-selection-handle')"
        ))
        val viewportWidth = evaluate("window.innerWidth").toDouble()
        var startX = -1
        var startY = -1
        var endX = -1
        var endY = -1
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / viewportWidth
            startX = origin[0] + (x * ratio).toInt()
            startY = origin[1] + (y * ratio).toInt()
            endX = origin[0] + ((x + 70) * ratio).toInt()
            endY = origin[1] + ((y + 48) * ratio).toInt()
        }
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        assertTrue("Native selection grip touch must dispatch",
            device.drag(startX, startY, endX, endY, 70))
        val after = evaluate(
            "window.smutPrototype?.getSelection()?.endIndex - window.smutPrototype?.getSelection()?.startIndex"
        ).toInt()
        assertTrue("End grip should extend range: before=$before after=$after", after > before)
        assertEquals("Dragging a grip must not modify manuscript",
            original, evaluate("window.smutPrototype?.getText()"))
    }

    /** E-04: render a real PDF from the current, isolated editor document in
     * offline WebView. PdfRenderer independently checks the PDF header and A5.
     * Text content and searchable Unicode are inspected later by pypdf in CI.
     */
    @Test
    fun pdfProbeExportsMultipageA5DocumentOfflineWithoutChangingSavedManuscript() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        ensureEditorWindowFocused()
        val original = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPdfTestFixture(true)")
            awaitJavaScriptValue("window.smutPrototype?.getPageCount() >= 2", "true", exact = true)
            evaluate("window.smutPrototype?.generatePdfProbe()")
            awaitJavaScriptValue(
                "window.smutPrototype?.getPdfProbeStatus()?.state",
                "saved", exact = false
            )
            val pages = evaluate("window.smutPrototype?.getPdfProbeStatus()?.pdfPages").toInt()
            assertTrue("Exporter must create more than one PDF page", pages >= 2)
            scenarioRule.scenario.onActivity { activity ->
                val pdf = java.io.File(activity.filesDir, "e04-a5-probe.pdf")
                assertTrue("Locally saved PDF probe is missing", pdf.isFile && pdf.length() > 100)
                val descriptor = android.os.ParcelFileDescriptor.open(
                    pdf, android.os.ParcelFileDescriptor.MODE_READ_ONLY
                )
                android.graphics.pdf.PdfRenderer(descriptor).use { renderer ->
                    assertEquals("PDF page count must match the exporter", pages, renderer.pageCount)
                    renderer.openPage(0).use { page ->
                        assertTrue("A5 width in PDF points",
                            kotlin.math.abs(page.width - 419.53) < 1.0)
                        assertTrue("A5 height in PDF points",
                            kotlin.math.abs(page.height - 595.28) < 1.0)
                    }
                }
            }
        } finally {
            evaluate("window.smutPrototype?.setPdfTestFixture(false)")
        }
        assertEquals("PDF export must never overwrite the saved book",
            original, evaluate("window.smutPrototype?.getText()"))
    }

    @Test
    fun longGermanManuscriptFlowsAcrossA5PagesWithoutChangingSavedBook() {
        awaitJavaScriptValue("window.smutPrototype?.isLoaded()", "true", exact = true)
        val original = evaluate("window.smutPrototype?.getText()")
        try {
            evaluate("window.smutPrototype?.setPageTestFixture(true)")
            awaitJavaScriptValue(
                "window.smutPrototype?.getPageCount() >= 2",
                "true",
                exact = true
            )
            awaitJavaScriptValue("window.smutPrototype?.getText()", "Absatz 75")
            assertTrue(
                "A5 page flow must render multiple actual editor canvases",
                evaluate("window.smutPrototype?.getPageCount()").toInt() >= 2
            )
        } finally {
            evaluate("window.smutPrototype?.setPageTestFixture(false)")
        }
        assertEquals("Mehrseitiger Test darf Manuskript nicht überschreiben",
            original, evaluate("window.smutPrototype?.getText()"))
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
            if (it == 1) {
                // Some headless emulator boots leave a system surface above
                // the Activity despite am start reporting a successful launch.
                // Return to Home once, then bring the existing activity forward.
                device.pressHome()
                device.waitForIdle()
            }
        }
        val focusReport = device.executeShellCommand("dumpsys window windows")
            .lineSequence()
            .filter { it.contains("mCurrentFocus") || it.contains("mFocusedApp") || it.contains("mInputMethodTarget") }
            .take(6)
            .joinToString(" | ")
        val activityReport = device.executeShellCommand("dumpsys activity activities")
            .lineSequence()
            .filter { it.contains("topResumedActivity") || it.contains("mResumedActivity") || it.contains("ResumedActivity") }
            .take(5).joinToString(" | ")
        assertTrue(
            "Smut must own emulator window; wm=$focusReport; activity=$activityReport; launch=$launch",
            false
        )
    }

    private fun tapHtmlButton(id: String) {
        ensureEditorWindowFocused()
        // Inspect where a real Android touch will land, without substituting
        // JavaScript's HTMLElement.click() for the native input path.
        evaluate("document.getElementById('$id')?.scrollIntoView({block:'center'})")
        evaluate(
            """(() => {
                const button = document.getElementById('$id');
                if (!button) return false;
                button.dataset.smutNativeClicks = '0';
                button.dataset.smutNativePointers = '0';
                button.addEventListener('pointerdown', () => {
                    button.dataset.smutNativePointers = '1';
                }, {once:true});
                button.addEventListener('click', () => {
                    button.dataset.smutNativeClicks = '1';
                }, {once:true});
                return true;
            })()"""
        )
        val cssX = evaluate(
            "(() => { const r=document.getElementById('$id')?.getBoundingClientRect(); return r ? r.left + r.width/2 : -1; })()"
        ).toDouble()
        val cssY = evaluate(
            "(() => { const r=document.getElementById('$id')?.getBoundingClientRect(); return r ? r.top + r.height/2 : -1; })()"
        ).toDouble()
        val targetDiagnostic = evaluate(
            """(() => {
                const button = document.getElementById('$id');
                const rect = button?.getBoundingClientRect();
                if (!rect) return 'missing';
                const cx = rect.left + rect.width / 2;
                const cy = rect.top + rect.height / 2;
                const hit = document.elementFromPoint(cx, cy);
                return JSON.stringify({
                    center: [cx, cy],
                    hitButton: hit?.closest('button')?.id,
                    visible: [window.innerWidth, window.innerHeight],
                    pageScroll: [window.scrollX, window.scrollY],
                    disabled: button.disabled,
                    focused: document.hasFocus()
                });
            })()"""
        )
        assertTrue("Button $id must exist; target=$targetDiagnostic", cssX >= 0 && cssY >= 0)
        val cssViewportWidth = evaluate("window.innerWidth").toDouble()
        var x = 0
        var y = 0
        var webViewBounds = ""
        scenarioRule.scenario.onActivity { activity ->
            val webView = findWebView(activity.window.decorView)!!
            val origin = IntArray(2)
            webView.getLocationOnScreen(origin)
            val ratio = webView.width / cssViewportWidth
            x = origin[0] + (cssX * ratio).toInt()
            y = origin[1] + (cssY * ratio).toInt()
            webViewBounds = "origin=${origin.toList()},size=${webView.width}x${webView.height},ratio=$ratio"
        }
        assertTrue(
            "Android touch did not dispatch $id at $x,$y; $webViewBounds; DOM=$targetDiagnostic",
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).click(x, y)
        )
        try {
            awaitJavaScriptValue(
                "document.getElementById('$id')?.dataset.smutNativeClicks",
                "\"1\"",
                exact = true
            )
        } catch (failure: AssertionError) {
            val events = evaluate(
                """JSON.stringify({
                    pointer:document.getElementById('$id')?.dataset.smutNativePointers,
                    click:document.getElementById('$id')?.dataset.smutNativeClicks,
                    zoom:window.smutPrototype?.getZoom(),
                    status:document.getElementById('editor-status')?.textContent
                })"""
            )
            throw AssertionError(
                "Native Android tap missed $id at $x,$y; $webViewBounds; DOM=$targetDiagnostic; events=$events; ${failure.message}",
                failure
            )
        }
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
