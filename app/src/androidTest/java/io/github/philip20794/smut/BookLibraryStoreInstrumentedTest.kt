package io.github.philip20794.smut

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookLibraryStoreInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: BookIndexDatabase
    private lateinit var store: BookStore
    private lateinit var dbName: String
    private val ids = mutableListOf<String>()

    @Before fun setup() {
        dbName = "b01-" + UUID.randomUUID() + ".db"
        database = Room.databaseBuilder(context, BookIndexDatabase::class.java, dbName).build()
        store = BookStore(context, database)
    }

    @After fun cleanup() {
        database.close()
        context.deleteDatabase(dbName)
        ids.forEach { id ->
            listOf(".book.json", ".previous.json", ".deleted").forEach { suffix ->
                val path = File(context.filesDir, "books-v1/" + id + suffix)
                path.delete()
                File(path.path + ".bak").delete()
                File(path.path + ".new").delete()
            }
        }
    }

    private fun created(result: BookLibraryResult): String {
        assertTrue(result is BookLibraryResult.Success)
        val id = (result as BookLibraryResult.Success).bookId
        ids.add(id)
        return id
    }

    @Test fun createOpenRenameDuplicateAndDeletePersistAcrossDatabaseRestart() {
        val id = created(store.createBook("  Mein Buch  "))
        val fresh = store.load(id) as BookLoad.Found
        assertEquals("Mein Buch", fresh.document.getString("title"))
        assertEquals(1, fresh.document.getJSONArray("chapters").length())
        assertEquals(1, fresh.revision)
        assertNull(fresh.position.blockId)
        assertEquals(0, fresh.position.offset)
        val chapter = fresh.position.chapterId

        val working = JSONObject(fresh.document.toString())
        val block = JSONObject().put("id", "paragraph1").put("type", "paragraph")
            .put("runs", JSONArray().put(JSONObject().put("text", "Äpfel, Grüße 😀")
                .put("marks", JSONObject().put("italic", true))))
        working.getJSONArray("chapters").getJSONObject(0).getJSONArray("blocks").put(block)
        assertEquals(BookSave.Saved(2), store.save(working, 1, BookPosition(chapter, "paragraph1", 6, 84)))

        assertEquals(BookLibraryResult.Success(id), store.renameBook(id, "Neuer Titel"))
        val renamed = store.load(id) as BookLoad.Found
        assertEquals("Neuer Titel", renamed.document.getString("title"))
        assertEquals("paragraph1", renamed.position.blockId)
        assertEquals(6, renamed.position.offset)

        val copyId = created(store.duplicateBook(id))
        val copy = store.load(copyId) as BookLoad.Found
        assertEquals("Neuer Titel (Kopie)", copy.document.getString("title"))
        assertNotEquals(id, copyId)
        assertNotEquals(chapter, copy.position.chapterId)
        assertNotEquals("paragraph1", copy.position.blockId)
        assertEquals(6, copy.position.offset)
        assertEquals(84, copy.position.scrollY)
        assertEquals("Äpfel, Grüße 😀", copy.document.getJSONArray("chapters")
            .getJSONObject(0).getJSONArray("blocks").getJSONObject(0)
            .getJSONArray("runs").getJSONObject(0).getString("text"))
        assertTrue(copy.document.getJSONArray("chapters").getJSONObject(0)
            .getJSONArray("blocks").getJSONObject(0).getJSONArray("runs")
            .getJSONObject(0).getJSONObject("marks").getBoolean("italic"))

        database.close()
        database = Room.databaseBuilder(context, BookIndexDatabase::class.java, dbName).build()
        store = BookStore(context, database)
        assertEquals(setOf(id, copyId), store.listEntries().map { it.id }.toSet())
        assertEquals("Neuer Titel", (store.load(id) as BookLoad.Found).document.getString("title"))
        assertEquals(BookLibraryResult.Success(id), store.deleteBook(id))
        assertEquals(BookLoad.Missing, store.load(id))
        assertEquals(listOf(copyId), store.listEntries().map { it.id })
        assertTrue(store.load(copyId) is BookLoad.Found)
        assertEquals(BookLibraryResult.Missing, store.deleteBook(id))
        assertEquals(BookLibraryResult.Missing, store.renameBook(id, "Nicht möglich"))
    }

    @Test fun invalidTitlesAndDamagedSourcesCannotBeSilentlyChanged() {
        assertEquals(BookLibraryResult.InvalidTitle, store.createBook("  "))
        assertEquals(BookLibraryResult.InvalidTitle, store.createBook("a".repeat(241)))
        assertEquals(emptyList<BookEntry>(), store.listEntries())
        val id = created(store.createBook("Original"))
        assertEquals(BookLibraryResult.InvalidTitle, store.renameBook(id, "\n"))
        assertEquals("Original", (store.load(id) as BookLoad.Found).document.getString("title"))
        store.primaryFileForTest(id).writeText("BESCHÄDIGT")
        assertEquals(BookLibraryResult.Error, store.renameBook(id, "Geändert"))
        assertEquals(BookLibraryResult.Error, store.duplicateBook(id))
        assertEquals(BookLibraryResult.Error, store.deleteBook(id))
        assertTrue(store.listEntries().any { it.id == id })
    }

    @Test fun deletionMarkerRecoversAfterSimulatedCrashWithoutResurrectingBook() {
        val id = created(store.createBook("Zum Löschen"))
        val marker = File(context.filesDir, "books-v1/" + id + ".deleted")
        marker.writeBytes(byteArrayOf(1)) // Simulate process death after durable delete intent
        assertEquals(BookLoad.Missing, store.load(id))
        assertFalse(store.listEntries().any { it.id == id })
        assertFalse(store.primaryFileForTest(id).exists())
        assertFalse(marker.exists())
        assertEquals(BookLoad.Missing, BookStore(context, database).load(id))
    }
}
