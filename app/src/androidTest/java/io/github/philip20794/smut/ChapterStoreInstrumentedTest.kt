package io.github.philip20794.smut

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ChapterStoreInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: BookIndexDatabase
    private lateinit var store: BookStore
    private lateinit var name: String
    private val bookIds = mutableListOf<String>()

    @Before fun setup() {
        name = "b02-" + UUID.randomUUID() + ".db"
        db = Room.databaseBuilder(context, BookIndexDatabase::class.java, name).build()
        store = BookStore(context, db)
    }

    @After fun cleanup() {
        db.close()
        context.deleteDatabase(name)
        for (id in bookIds) {
            for (suffix in listOf(".book.json", ".previous.json", ".deleted")) {
                val file = File(context.filesDir, "books-v1/" + id + suffix)
                file.delete()
                File(file.path + ".bak").delete()
                File(file.path + ".new").delete()
            }
        }
    }

    private fun createdBook(): String {
        val result = store.createBook("B02-" + UUID.randomUUID().toString().take(8))
        assertTrue(result is BookLibraryResult.Success)
        return (result as BookLibraryResult.Success).bookId.also(bookIds::add)
    }

    private fun read(id: String): BookLoad.Found = store.load(id) as BookLoad.Found
    private fun chapters(id: String): JSONArray = read(id).document.getJSONArray("chapters")
    private fun updated(result: ChapterActionResult): Long {
        assertTrue("Expected persisted chapter change but got " + result,
            result is ChapterActionResult.Updated)
        return (result as ChapterActionResult.Updated).revision
    }

    @Test fun chapterLifecyclePreservesContentOrderAndPositionAcrossRestart() {
        val id = createdBook()
        val initial = read(id)
        val firstId = initial.position.chapterId
        assertEquals(1, initial.revision)
        assertEquals(2L, updated(store.createChapter(id, 1, "Zweites Kapitel")))
        val secondId = read(id).position.chapterId
        assertEquals(3L, updated(store.createChapter(id, 2, "Drittes Kapitel")))
        val thirdId = read(id).position.chapterId
        assertEquals(3, chapters(id).length())
        assertEquals(4L, updated(store.renameChapter(id, 3, secondId, "Überschrift Äöü 😀")))
        assertEquals(5L, updated(store.moveChapter(id, 4, thirdId, -1)))
        assertEquals(listOf(firstId, thirdId, secondId),
            (0 until 3).map { chapters(id).getJSONObject(it).getString("id") })

        // Store actual long manuscript content and a caret in the second chapter.
        val current = read(id)
        val document = JSONObject(current.document.toString())
        val chapter = document.getJSONArray("chapters").getJSONObject(2)
        val veryLongText = "Grüße 😀 und Äpfel ".repeat(800)
        chapter.getJSONArray("blocks").put(JSONObject().put("id", "longParagraph")
            .put("type", "paragraph")
            .put("runs", JSONArray().put(JSONObject().put("text", veryLongText)
                .put("marks", JSONObject().put("bold", true)))))
        assertEquals(BookSave.Saved(6), store.save(document, 5,
            BookPosition(secondId, "longParagraph", 8, 254)))
        assertEquals(7L, updated(store.moveChapter(id, 6, secondId, -1)))
        assertEquals(secondId, read(id).position.chapterId)
        assertEquals("longParagraph", read(id).position.blockId)
        assertEquals(8, read(id).position.offset)
        assertEquals(254, read(id).position.scrollY)
        assertEquals(8L, updated(store.openChapter(id, 7, firstId)))
        assertEquals(BookPosition(firstId, null, 0, 0), read(id).position)
        assertEquals(9L, updated(store.openChapter(id, 8, secondId)))
        assertEquals(BookPosition(secondId, null, 0, 0), read(id).position)

        db.close()
        db = Room.databaseBuilder(context, BookIndexDatabase::class.java, name).build()
        store = BookStore(context, db)
        assertEquals(listOf(firstId, secondId, thirdId),
            (0 until 3).map { chapters(id).getJSONObject(it).getString("id") })
        val savedText = chapters(id).getJSONObject(1).getJSONArray("blocks")
            .getJSONObject(0).getJSONArray("runs").getJSONObject(0).getString("text")
        assertEquals(veryLongText, savedText)
        assertEquals("Überschrift Äöü 😀", chapters(id).getJSONObject(1).getString("title"))
        assertEquals(10L, updated(store.deleteChapter(id, 9, secondId)))
        assertEquals(BookPosition(thirdId, null, 0, 0), read(id).position)
        assertEquals(2, chapters(id).length())
        assertEquals(11L, updated(store.deleteChapter(id, 10, thirdId)))
        assertEquals(BookPosition(firstId, null, 0, 0), read(id).position)
        assertEquals(ChapterActionResult.LastChapter, store.deleteChapter(id, 11, firstId))
        assertEquals(11L, read(id).revision)
    }

    @Test fun conflictsAndInvalidCommandsCannotOverwriteNewerManuscript() {
        val id = createdBook()
        val firstId = read(id).position.chapterId
        assertEquals(ChapterActionResult.InvalidTitle, store.createChapter(id, 1, "\n"))
        assertEquals(ChapterActionResult.InvalidTitle, store.createChapter(id, 1, "A".repeat(241)))
        assertEquals(ChapterActionResult.NotFound, store.renameChapter(id, 1, "missing", "Test"))
        assertEquals(ChapterActionResult.Boundary, store.moveChapter(id, 1, firstId, -1))
        assertEquals(1L, read(id).revision)

        val other = BookStore(context, db)
        assertEquals(2L, updated(other.createChapter(id, 1, "Konkurrenz")))
        assertEquals(ChapterActionResult.Conflict, store.renameChapter(id, 1, firstId, "Veraltet"))
        assertEquals(ChapterActionResult.Conflict, store.deleteChapter(id, 1, firstId))
        assertEquals(ChapterActionResult.Conflict, store.openChapter(id, 1, firstId))
        assertEquals("Kapitel 1", chapters(id).getJSONObject(0).getString("title"))
        assertEquals(2, chapters(id).length())
        assertEquals(2L, read(id).revision)
        val legacy = File(context.filesDir, "prototype-editor-v1.json")
        val before = if (legacy.exists()) legacy.readBytes() else null
        assertEquals(3L, updated(store.renameChapter(id, 2, firstId, "Neu")))
        if (before == null) assertFalse(legacy.exists())
        else assertTrue(before.contentEquals(legacy.readBytes()))
    }
}
