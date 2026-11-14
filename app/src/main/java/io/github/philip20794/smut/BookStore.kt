package io.github.philip20794.smut

import android.content.Context
import android.util.AtomicFile
import androidx.room.Room
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

internal sealed interface BookLoad {
    data object Missing : BookLoad
    data object Corrupt : BookLoad
    data class Found(
        val document: JSONObject,
        val revision: Long,
        val position: BookPosition,
        val recovered: Boolean
    ) : BookLoad
    data object Error : BookLoad
}
internal sealed interface BookSave {
    data class Saved(val revision: Long) : BookSave
    data class Conflict(val revision: Long) : BookSave
    data object Error : BookSave
}

/**
 * A book's checksummed AtomicFile is authoritative. Room is a reparable index.
 * A second atomic file retains the last valid revision across disk corruption.
 * The E-02 file prototype-editor-v1.json is NEVER read, overwritten or deleted.
 *
 * Only call the synchronous API from a worker / JavascriptInterface thread.
 */
internal class BookStore(
    context: Context,
    private val database: BookIndexDatabase = Room.databaseBuilder(
        context.applicationContext, BookIndexDatabase::class.java, "smut-books-index-v1.db"
    ).build(),
    private val interruptPrimaryWrite: (() -> Unit)? = null,
) {
    private val directory = File(context.applicationContext.filesDir, "books-v1").apply {
        if (!isDirectory && !mkdirs()) throw IOException("Buchordner kann nicht erstellt werden")
    }
    private val dao get() = database.entries()
    private val iso = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
        .withZone(ZoneOffset.UTC)
    private val maxSnapshotBytes = 16_500_000

    private data class Snapshot(
        val book: JSONObject,
        val info: BookInfo,
        val position: BookPosition,
        val revision: Long,
        val encoded: ByteArray,
    )
    private sealed interface DiskState {
        data object Missing : DiskState
        data object Corrupt : DiskState
        data class Valid(val snapshot: Snapshot, val recovered: Boolean, val primary: Snapshot?) : DiskState
    }

    private fun primary(id: String) = File(directory, "$id.book.json")
    private fun previous(id: String) = File(directory, "$id.previous.json")
    private fun exists(file: File) = file.exists() || File(file.path + ".bak").exists()

    private fun hash(revision: Long, document: String, position: String): String {
        val input = "$revision\n$document\n$position".toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256").digest(input)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }
    private fun bytesFor(document: JSONObject, revision: Long, position: BookPosition): ByteArray {
        val content = document.toString()
        val location = position.toJson().toString()
        return JSONObject()
            .put("formatVersion", 1)
            .put("revision", revision)
            .put("documentJson", content)
            .put("positionJson", location)
            .put("sha256", hash(revision, content, location))
            .toString().toByteArray(Charsets.UTF_8).also {
                require(it.size <= maxSnapshotBytes) { "Speicherdatei zu groß" }
            }
    }
    private fun decode(bytes: ByteArray, expectedId: String): Snapshot {
        require(bytes.size <= maxSnapshotBytes)
        val envelope = JSONObject(String(bytes, Charsets.UTF_8))
        BookDocumentValidator.exactKeys(
            envelope, setOf("formatVersion", "revision", "documentJson", "positionJson", "sha256")
        )
        BookDocumentValidator.integer(envelope.opt("formatVersion"), 1, 1)
        val revRaw = envelope.opt("revision")
        require(revRaw is Number && revRaw.toDouble().isFinite() &&
            revRaw.toDouble() % 1.0 == 0.0 && revRaw.toLong() in 1..Long.MAX_VALUE)
        val revision = revRaw.toLong()
        val content = envelope.opt("documentJson") as? String ?: throw IllegalArgumentException("Kein Buchtext")
        val location = envelope.opt("positionJson") as? String ?: throw IllegalArgumentException("Keine Cursorposition")
        require(content.toByteArray(Charsets.UTF_8).size <= BookDocumentValidator.MAX_BOOK_BYTES)
        val checksum = envelope.opt("sha256") as? String
        require(checksum != null && checksum == hash(revision, content, location)) { "Prüfsumme ungültig" }
        val book = JSONObject(content)
        val info = BookDocumentValidator.inspect(book)
        require(info.id == expectedId) { "Buch-ID stimmt nicht mit Dateiname überein" }
        val position = BookPosition.parse(JSONObject(location))
        BookDocumentValidator.validatePosition(book, position)
        return Snapshot(book, info, position, revision, bytes)
    }
    private fun readBounded(file: File): ByteArray {
        val output = ByteArrayOutputStream()
        AtomicFile(file).openRead().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() + count > maxSnapshotBytes) throw IOException("Buchdatei zu groß")
                output.write(buffer, 0, count)
            }
        }
        return output.toByteArray()
    }
    private fun readValid(file: File, id: String): Snapshot? =
        if (!exists(file)) null else runCatching { decode(readBounded(file), id) }.getOrNull()

    private fun readDisk(id: String): DiskState {
        val source = primary(id)
        val backup = previous(id)
        if (!exists(source) && !exists(backup)) return DiskState.Missing
        val a = readValid(source, id)
        val b = readValid(backup, id)
        if (a == null && b == null) return DiskState.Corrupt
        return if (a != null && (b == null || a.revision >= b.revision)) {
            DiskState.Valid(a, false, a)
        } else {
            DiskState.Valid(requireNotNull(b), true, a)
        }
    }
    private fun writeAtomic(file: File, bytes: ByteArray, injectFailure: Boolean = false) {
        val atomic = AtomicFile(file)
        var stream: FileOutputStream? = null
        try {
            stream = atomic.startWrite()
            if (injectFailure) {
                val middle = bytes.size / 2
                stream.write(bytes, 0, middle)
                interruptPrimaryWrite?.invoke()
                stream.write(bytes, middle, bytes.size - middle)
            } else stream.write(bytes)
            atomic.finishWrite(stream)
        } catch (error: Exception) {
            if (stream != null) atomic.failWrite(stream)
            throw error
        }
    }
    private fun index(snapshot: Snapshot): BookEntry = BookEntry(
        snapshot.info.id, snapshot.info.title, snapshot.info.updatedAt, snapshot.revision,
        snapshot.position.chapterId, snapshot.position.blockId,
        snapshot.position.offset, snapshot.position.scrollY
    )
    private fun syncIndex(snapshot: Snapshot) {
        val expected = index(snapshot)
        if (dao.find(snapshot.info.id) != expected) dao.upsert(expected)
    }

    @Synchronized
    fun load(id: String): BookLoad {
        if (runCatching { BookDocumentValidator.safeId(id) }.isFailure) return BookLoad.Error
        return try {
            when (val state = readDisk(id)) {
                DiskState.Missing -> BookLoad.Missing
                DiskState.Corrupt -> BookLoad.Corrupt
                is DiskState.Valid -> {
                    syncIndex(state.snapshot)
                    BookLoad.Found(state.snapshot.book, state.snapshot.revision,
                        state.snapshot.position, state.recovered)
                }
            }
        } catch (_: Exception) {
            BookLoad.Error
        }
    }

    @Synchronized
    fun save(document: JSONObject, expectedRevision: Long, position: BookPosition): BookSave {
        if (expectedRevision < 0 || expectedRevision == Long.MAX_VALUE) return BookSave.Error
        return try {
            // Copy the untrusted input before any disk write; reject malformed data atomically.
            val working = JSONObject(document.toString())
            val info = BookDocumentValidator.inspect(working)
            BookDocumentValidator.validatePosition(working, position)
            val state = readDisk(info.id)
            if (state == DiskState.Corrupt) return BookSave.Error
            val revision = when (state) {
                DiskState.Missing -> 0L
                is DiskState.Valid -> state.snapshot.revision
                DiskState.Corrupt -> error("Already rejected")
            }
            if (revision != expectedRevision) return BookSave.Conflict(revision)
            val savedAt = iso.format(Instant.ofEpochMilli(System.currentTimeMillis()))
            working.put("updatedAt", maxOf(info.createdAt, info.updatedAt, savedAt))
            BookDocumentValidator.inspect(working)
            val next = revision + 1
            val encoded = bytesFor(working, next, position)
            if (state is DiskState.Valid && state.primary != null) {
                // Preserve a verified previous revision, not an already-corrupt primary.
                writeAtomic(previous(info.id), state.primary.encoded)
            }
            writeAtomic(primary(info.id), encoded, injectFailure = true)
            syncIndex(decode(encoded, info.id))
            BookSave.Saved(next)
        } catch (_: Exception) {
            BookSave.Error
        }
    }

    /** Non-destructive, bounded test hook. The E-02 file is never touched. */
    internal fun primaryFileForTest(id: String): File = primary(BookDocumentValidator.safeId(id))
    internal fun previousFileForTest(id: String): File = previous(BookDocumentValidator.safeId(id))
}
