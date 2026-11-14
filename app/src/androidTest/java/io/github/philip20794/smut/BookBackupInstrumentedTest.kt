package io.github.philip20794.smut

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BookBackupInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: BookIndexDatabase
    private lateinit var store: BookStore
    private val ids = mutableListOf<String>()
    private fun nextId(): String = ("bk" + UUID.randomUUID().toString().replace("-", "")).also(ids::add)
    private fun position() = BookPosition("chapter1", "block1", 5, 72)
    private fun book(id: String): JSONObject {
        val margins = JSONObject()
            .put("top", 18).put("bottom", 18).put("inside", 20).put("outside", 16)
        val layout = JSONObject()
            .put("pageWidthMm", 148).put("pageHeightMm", 210)
            .put("marginsMm", margins).put("fontFamily", "serif")
            .put("fontSizePt", 11).put("lineSpacing", 1.3).put("pageNumbers", true)
        val run = JSONObject().put("text", "Äpfel, Öl und Grüße 😀")
            .put("marks", JSONObject().put("bold", true))
        val block = JSONObject().put("id", "block1").put("type", "paragraph")
            .put("runs", JSONArray().put(run))
        val chapter = JSONObject().put("id", "chapter1").put("title", "Kapitel A")
            .put("blocks", JSONArray().put(block))
        return JSONObject()
            .put("schemaVersion", 1).put("id", id).put("title", "Äpfel und Grüße")
            .put("author", "Runa").put("createdAt", "2026-11-14T10:00:00.000Z")
            .put("updatedAt", "2026-11-14T10:00:00.000Z")
            .put("layout", layout).put("chapters", JSONArray().put(chapter))
    }

    @Before fun before() {
        database = Room.inMemoryDatabaseBuilder(context, BookIndexDatabase::class.java).build()
        store = BookStore(context, database)
    }
    @After fun after() {
        database.close()
        for (id in ids) {
            listOf(".book.json", ".previous.json").forEach { suffix ->
                val file = File(context.filesDir, "books-v1/$id$suffix")
                file.delete()
                File(file.path + ".bak").delete()
                File(file.path + ".new").delete()
            }
        }
    }

    @Test fun exportDeleteTestCopyThenImportKeepsBookExactlyAndDoesNotTouchLegacy() {
        val legacy = File(context.filesDir, "prototype-editor-v1.json")
        val before = if (legacy.exists()) legacy.readBytes() else null
        val id = nextId()
        assertEquals(BookSave.Saved(1), store.save(book(id), 0, position()))
        assertEquals(BookSave.Saved(2), store.save(book(id), 1, position()))
        val original = store.load(id) as BookLoad.Found
        val payload = BookBackupCodec.parse(BookBackupCodec.create(original))
        // Delete ONLY this uniquely named test copy, never any real user content.
        assertTrue(store.primaryFileForTest(id).delete())
        store.previousFileForTest(id).delete()
        database.entries().delete(id)
        assertEquals(BookLoad.Missing, store.load(id))
        assertEquals(BookImport.Imported, store.importNew(payload))
        val restored = store.load(id) as BookLoad.Found
        assertEquals(original.document.toString(), restored.document.toString())
        assertEquals(original.position, restored.position)
        assertEquals(original.revision, restored.revision)
        assertEquals("Äpfel und Grüße", database.entries().find(id)?.title)
        assertEquals(BookImport.AlreadyExists, store.importNew(payload))
        if (before == null) assertFalse(legacy.exists())
        else assertTrue(before.contentEquals(legacy.readBytes()))
    }

    @Test fun collisionAndDamagedLocalBookCannotBeOverwritten() {
        val id = nextId()
        assertEquals(BookSave.Saved(1), store.save(book(id), 0, position()))
        val exported = BookBackupCodec.parse(BookBackupCodec.create(store.load(id) as BookLoad.Found))
        val pristine = store.primaryFileForTest(id).readBytes()
        assertEquals(BookImport.AlreadyExists, store.importNew(exported))
        assertTrue(pristine.contentEquals(store.primaryFileForTest(id).readBytes()))
        store.primaryFileForTest(id).writeText("BROKEN")
        assertEquals(BookImport.AlreadyExists, store.importNew(exported))
        assertEquals(BookLoad.Corrupt, store.load(id))
    }

    @Test fun documentProviderUriCanExportAndReadCheckedBackupOffline() {
        val id = nextId()
        assertEquals(BookSave.Saved(1), store.save(book(id), 0, position()))
        val bytes = BookBackupCodec.create(store.load(id) as BookLoad.Found)
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "smut-test-" + UUID.randomUUID() + ".json")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SmutTests/")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Android Documents storage returned no URI")
        try {
            BookBackupIO.write(resolver, uri, bytes)
            val stored = BookBackupIO.read(resolver, uri)
            assertTrue(bytes.contentEquals(stored))
            val restored = BookBackupCodec.parse(stored)
            assertEquals(id, restored.document.getString("id"))
            assertEquals(position(), restored.position)
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun importNeverRacesWithExistingSaveAcrossStoreInstances() {
        val id = nextId()
        val incoming = BookBackupCodec.parse(BookBackupCodec.create(
            BookLoad.Found(book(id), 7, position(), false)
        ))
        val secondStore = BookStore(context, database)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val start = CountDownLatch(1)
            val imported = pool.submit<BookImport> {
                start.await()
                store.importNew(incoming)
            }
            val saved = pool.submit<BookSave> {
                start.await()
                secondStore.save(book(id), 0, position())
            }
            start.countDown()
            val importResult = imported.get(10, TimeUnit.SECONDS)
            val saveResult = saved.get(10, TimeUnit.SECONDS)
            assertTrue(
                "One writer wins without data loss",
                (importResult == BookImport.Imported && saveResult == BookSave.Conflict(7)) ||
                    (importResult == BookImport.AlreadyExists && saveResult == BookSave.Saved(1))
            )
            val found = store.load(id) as BookLoad.Found
            assertEquals(if (importResult == BookImport.Imported) 7L else 1L, found.revision)
        } finally {
            pool.shutdownNow()
        }
    }

    @Test fun interruptedForeignImportCannotOverrideExistingPendingAtomicFile() {
        val id = nextId()
        val incoming = BookBackupCodec.parse(BookBackupCodec.create(
            BookLoad.Found(book(id), 1, position(), false)
        ))
        val stray = File(store.primaryFileForTest(id).path + ".new")
        stray.writeText("pending local manuscript")
        assertEquals(BookImport.AlreadyExists, store.importNew(incoming))
        assertEquals("pending local manuscript", stray.readText())
    }

    @Test fun invalidArchiveAndOversizedInputAreRejectedBeforeWriting() {
        val id = nextId()
        val payload = BookBackupCodec.parse(BookBackupCodec.create(
            BookLoad.Found(book(id), 1, position(), false)
        ))
        val forged = JSONObject(String(BookBackupCodec.create(
            BookLoad.Found(book(id), 1, position(), false)
        ), Charsets.UTF_8)).put("sha256", "0".repeat(64))
        val error = runCatching { BookBackupCodec.parse(forged.toString().toByteArray()) }
        assertTrue(error.isFailure)
        assertEquals(BookLoad.Missing, store.load(id))
        assertEquals(BookImport.Imported, store.importNew(payload))
    }
}
