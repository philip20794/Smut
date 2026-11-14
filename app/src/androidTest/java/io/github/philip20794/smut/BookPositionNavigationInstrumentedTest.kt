package io.github.philip20794.smut

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookPositionNavigationInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = BookStore(context)
    private val device get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun create(name: String): String {
        val created = store.createBook(name)
        assertTrue(created is BookLibraryResult.Success)
        return (created as BookLibraryResult.Success).bookId
    }

    private fun visibleText(text: String) {
        if (!device.hasObject(By.text(text))) {
            repeat(9) {
                device.swipe(
                    device.displayWidth / 2, device.displayHeight * 4 / 5,
                    device.displayWidth / 2, device.displayHeight / 4, 22
                )
                if (device.hasObject(By.text(text))) return
            }
        }
        assertTrue("Nicht sichtbar: $text", device.hasObject(By.text(text)))
    }

    private fun scrollToTop() {
        repeat(8) {
            device.swipe(
                device.displayWidth / 2, device.displayHeight / 4,
                device.displayWidth / 2, device.displayHeight * 4 / 5, 18
            )
        }
    }

    @Test fun openBackReopenAndRecreateKeepCursorChapterAndScrollAnchor() {
        val name = "B03-UI-" + UUID.randomUUID().toString().take(8)
        val id = create(name)
        try {
            val original = store.load(id) as BookLoad.Found
            val chapter = original.position.chapterId
            val doc = JSONObject(original.document.toString())
            val text = "Anfang ".repeat(70) + "MARKIERTE_STELLE" + " Ende".repeat(70)
            val blockId = "bookmarkBlock"
            doc.getJSONArray("chapters").getJSONObject(0).getJSONArray("blocks")
                .put(JSONObject().put("id", blockId).put("type", "paragraph")
                    .put("runs", JSONArray().put(JSONObject().put("text", text))))
            val offset = text.indexOf("MARKIERTE_STELLE")
            val saved = BookPosition(chapter, blockId, offset, 86)
            assertEquals(BookSave.Saved(2), store.save(doc, 1, saved))

            ActivityScenario.launch(LibraryActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                device.findObject(By.text("Öffnen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buch geöffnet · Leseansicht")), 12_000))
                visibleText("Gespeicherte Textstelle: Zeichen $offset")
                assertTrue("Kapitel muss dieselbe Position behalten",
                    (store.load(id) as BookLoad.Found).position == saved)

                // Android system back navigates to the catalog instead of exiting.
                device.pressBack()
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                scrollToTop()
                device.findObject(By.text("Öffnen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buch geöffnet · Leseansicht")), 12_000))
                visibleText("Gespeicherte Textstelle: Zeichen $offset")
                assertEquals(saved, (store.load(id) as BookLoad.Found).position)

                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.text("Buch geöffnet · Leseansicht")), 12_000))
                assertEquals(saved, (store.load(id) as BookLoad.Found).position)
                scrollToTop()
                device.findObject(By.text("Buchmenü")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buch-Backup und Import")), 5_000))
                device.pressBack() // Dismiss the menu, not the open book.
                assertTrue(device.hasObject(By.text("Buch geöffnet · Leseansicht")))
                device.findObject(By.text("Zur Bücherliste")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                assertEquals(saved, (store.load(id) as BookLoad.Found).position)
            }
        } finally {
            store.deleteBook(id)
        }
    }

    @Test fun signedButStaleAnchorsRecoverDeterministicallyWithoutChangingManuscript() {
        val id = create("B03-Stale-" + UUID.randomUUID().toString().take(8))
        try {
            val initial = store.load(id) as BookLoad.Found
            val chapterId = initial.position.chapterId
            val document = JSONObject(initial.document.toString())
            document.getJSONArray("chapters").getJSONObject(0).getJSONArray("blocks")
                .put(JSONObject().put("id", "p1").put("type", "paragraph")
                    .put("runs", JSONArray().put(JSONObject().put("text", "A😀B"))))
            assertEquals(BookSave.Saved(2), store.save(document, 1,
                BookPosition(chapterId, "p1", 1, 77)))

            // An obsolete or mid-surrogate position may exist in a checksummed
            // historic file. The document and checksum still must be authentic.
            val primary = store.primaryFileForTest(id)
            val envelope = JSONObject(primary.readText())
            fun rewritePosition(position: BookPosition) {
                val text = position.toJson().toString()
                val rev = envelope.getLong("revision")
                val doc = envelope.getString("documentJson")
                val sum = MessageDigest.getInstance("SHA-256")
                    .digest("$rev\n$doc\n$text".toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it.toInt() and 0xff) }
                envelope.put("positionJson", text).put("sha256", sum)
                primary.writeText(envelope.toString())
            }

            rewritePosition(BookPosition(chapterId, "p1", 2, 77))
            val surrogate = store.load(id) as BookLoad.Found
            assertTrue(surrogate.positionRepaired)
            assertEquals(BookPosition(chapterId, "p1", 1, 77), surrogate.position)
            assertEquals("A😀B", surrogate.document.getJSONArray("chapters")
                .getJSONObject(0).getJSONArray("blocks").getJSONObject(0)
                .getJSONArray("runs").getJSONObject(0).getString("text"))

            rewritePosition(BookPosition(chapterId, "deletedBlock", 5, 123))
            val missingBlock = store.load(id) as BookLoad.Found
            assertTrue(missingBlock.positionRepaired)
            assertEquals(BookPosition(chapterId, null, 0, 0), missingBlock.position)

            rewritePosition(BookPosition("deletedChapter", null, 0, 499))
            val missingChapter = store.load(id) as BookLoad.Found
            assertTrue(missingChapter.positionRepaired)
            assertEquals(BookPosition(chapterId, null, 0, 0), missingChapter.position)

            // Invalid hash is corruption, NOT a recoverable old caret.
            envelope.put("sha256", "0".repeat(64))
            primary.writeText(envelope.toString())
            val fallback = store.load(id) as BookLoad.Found
            assertTrue(fallback.recovered)
            assertEquals(1L, fallback.revision)
        } finally {
            store.deleteBook(id)
        }
    }
}
