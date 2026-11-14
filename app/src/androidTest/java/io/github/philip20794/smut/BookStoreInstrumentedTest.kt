package io.github.philip20794.smut

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookStoreInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: BookIndexDatabase
    private lateinit var store: BookStore
    private val ids = mutableListOf<String>()
    private val databases = mutableListOf<String>()

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, BookIndexDatabase::class.java).build()
        store = BookStore(context, db)
    }
    @After fun cleanup() {
        db.close()
        for (id in ids) {
            for (suffix in listOf(".book.json", ".previous.json")) {
                val file = File(File(context.filesDir, "books-v1"), id + suffix)
                file.delete()
                File(file.path + ".bak").delete()
                File(file.path + ".new").delete()
            }
        }
        for (name in databases) context.deleteDatabase(name)
    }
    private fun id(): String = "d03" + UUID.randomUUID().toString().replace("-", "").also { ids.add("d03" + it) }
    private fun position() = BookPosition("chap1", "block1", 4, 123)
    private fun book(id: String, text: String = "Äpfel und Grüße"): JSONObject = JSONObject()
        .put("schemaVersion", 1).put("id", id).put("title", "Mein Buch")
        .put("createdAt", "2026-11-14T10:00:00.000Z").put("updatedAt", "2026-11-14T10:00:00.000Z")
        .put("layout", JSONObject().put("pageWidthMm", 148).put("pageHeightMm", 210)
            .put("marginsMm", JSONObject().put("top", 18).put("bottom", 18)
                .put("inside", 20).put("outside", 16))
            .put("fontFamily", "serif").put("fontSizePt", 11)
            .put("lineSpacing", 1.3).put("pageNumbers", true))
        .put("chapters", org.json.JSONArray().put(JSONObject().put("id", "chap1")
            .put("title", "Kapitel 1")
            .put("blocks", org.json.JSONArray().put(JSONObject().put("id", "block1")
                .put("type", "paragraph")
                .put("runs", org.json.JSONArray().put(JSONObject().put("text", text)))))))

    @Test fun atomicAutosavePreservesPositionAndRoomIndexAfterReopen() {
        val bookId = id()
        assertEquals(BookSave.Saved(1), store.save(book(bookId), 0, position()))
        val reopened = BookStore(context, db)
        val found = reopened.load(bookId) as BookLoad.Found
        assertEquals(1L, found.revision)
        assertEquals(4, found.position.offset)
        assertEquals(123, found.position.scrollY)
        assertEquals("Äpfel und Grüße", found.document.getJSONArray("chapters")
            .getJSONObject(0).getJSONArray("blocks").getJSONObject(0)
            .getJSONArray("runs").getJSONObject(0).getString("text"))
        assertFalse(found.recovered)
        assertEquals(1L, db.entries().find(bookId)?.revision)
        assertEquals("Mein Buch", db.entries().find(bookId)?.title)
    }

    @Test fun interruptedAtomicWriteKeepsLastGoodRevision() {
        val bookId = id()
        assertEquals(BookSave.Saved(1), store.save(book(bookId), 0, position()))
        val faulty = BookStore(context, db) { throw IOException("simulated process loss mid-write") }
        assertEquals(BookSave.Error, faulty.save(book(bookId, "Äpfel und neu"), 1, position()))
        val intact = store.load(bookId) as BookLoad.Found
        assertEquals(1L, intact.revision)
        assertEquals("Äpfel und Grüße", intact.document.getJSONArray("chapters")
            .getJSONObject(0).getJSONArray("blocks").getJSONObject(0)
            .getJSONArray("runs").getJSONObject(0).getString("text"))
    }

    @Test fun corruptedPrimaryRecoversValidatedPreviousRevisionAndRepairsIndex() {
        val bookId = id()
        assertEquals(BookSave.Saved(1), store.save(book(bookId), 0, position()))
        assertEquals(BookSave.Saved(2), store.save(book(bookId, "Äpfel und weiter"), 1, position()))
        store.primaryFileForTest(bookId).writeText("CORRUPTED")
        val recovered = store.load(bookId) as BookLoad.Found
        assertEquals(1L, recovered.revision)
        assertTrue(recovered.recovered)
        assertEquals(1L, db.entries().find(bookId)?.revision)
        assertEquals(BookSave.Saved(2), store.save(book(bookId, "gerettet"), 1, position()))
        assertFalse((store.load(bookId) as BookLoad.Found).recovered)
    }

    @Test fun rejectsStaleRevisionAndDoesNotOverwriteMalformedBook() {
        val bookId = id()
        assertEquals(BookSave.Saved(1), store.save(book(bookId), 0, position()))
        assertEquals(BookSave.Conflict(1), store.save(book(bookId, "veraltet"), 0, position()))
        assertEquals(BookSave.Error, store.save(book(bookId).put("schemaVersion", 7), 1, position()))
        assertEquals(1L, (store.load(bookId) as BookLoad.Found).revision)
        val badId = id()
        store.primaryFileForTest(badId).writeText("""{"broken":true}""")
        assertEquals(BookLoad.Corrupt, store.load(badId))
        assertEquals(BookSave.Error, store.save(book(badId), 0, position()))
    }

    @Test fun legacyPrototypeFileIsNeverChangedByNewPersistence() {
        val legacy = File(context.filesDir, "prototype-editor-v1.json")
        val existed = legacy.exists()
        val bytes = if (existed) legacy.readBytes() else null
        val bookId = id()
        assertEquals(BookSave.Saved(1), store.save(book(bookId), 0, position()))
        store.load(bookId)
        assertEquals(existed, legacy.exists())
        if (bytes != null) assertTrue(bytes.contentEquals(legacy.readBytes()))
    }

    @Test fun roomIndexSurvivesClosingAndReopeningDatabase() {
        val name = "smut-d03-test-" + UUID.randomUUID() + ".db"
        databases.add(name)
        val persistent = Room.databaseBuilder(context, BookIndexDatabase::class.java, name).build()
        val bookId = id()
        val diskStore = BookStore(context, persistent)
        assertEquals(BookSave.Saved(1), diskStore.save(book(bookId), 0, position()))
        persistent.close()
        val reopened = Room.databaseBuilder(context, BookIndexDatabase::class.java, name).build()
        try {
            val read = BookStore(context, reopened).load(bookId)
            assertTrue(read is BookLoad.Found)
            assertEquals(1L, reopened.entries().find(bookId)?.revision)
        } finally { reopened.close() }
    }
}
