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
import java.util.UUID

/** All BookStore instances in this app process share the same atomic file namespace. */
private val BOOK_DISK_LOCK = Any()

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

internal sealed interface BookImport {
    data object Imported : BookImport
    data object AlreadyExists : BookImport
    data object Error : BookImport
}

/** Library commands report success only after an authoritative disk transaction. */
internal sealed interface BookLibraryResult {
    data class Success(val bookId: String) : BookLibraryResult
    data object Missing : BookLibraryResult
    data object InvalidTitle : BookLibraryResult
    data object Error : BookLibraryResult
}

/** P3 chapter changes are always optimistic, revision-checked transactions. */
internal sealed interface ChapterActionResult {
    data class Updated(val revision: Long) : ChapterActionResult
    data object Missing : ChapterActionResult
    data object NotFound : ChapterActionResult
    data object InvalidTitle : ChapterActionResult
    data object TooManyChapters : ChapterActionResult
    data object LastChapter : ChapterActionResult
    data object Boundary : ChapterActionResult
    data object Conflict : ChapterActionResult
    data object Error : ChapterActionResult
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
    private fun tombstone(id: String) = File(directory, "$id.deleted")
    private fun isDeleted(id: String) = exists(tombstone(id))
    private fun newId(prefix: String) = prefix + UUID.randomUUID().toString().replace("-", "")
    private fun validTitle(title: String): Boolean =
        title.isNotBlank() && title.length <= 240 && title.none { it == '\u0000' || it == '\n' || it == '\r' }
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
    fun load(id: String): BookLoad = synchronized(BOOK_DISK_LOCK) { loadUnderLock(id) }

    private fun loadUnderLock(id: String): BookLoad {
        if (runCatching { BookDocumentValidator.safeId(id) }.isFailure) return BookLoad.Error
        if (isDeleted(id)) return BookLoad.Missing
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
    fun save(document: JSONObject, expectedRevision: Long, position: BookPosition): BookSave =
        synchronized(BOOK_DISK_LOCK) { saveUnderLock(document, expectedRevision, position) }

    private fun saveUnderLock(document: JSONObject, expectedRevision: Long, position: BookPosition): BookSave {
        if (expectedRevision < 0 || expectedRevision == Long.MAX_VALUE) return BookSave.Error
        return try {
            // Copy the untrusted input before any disk write; reject malformed data atomically.
            val working = JSONObject(document.toString())
            val info = BookDocumentValidator.inspect(working)
            BookDocumentValidator.validatePosition(working, position)
            if (isDeleted(info.id)) return BookSave.Error
            val state = readDisk(info.id)
            if (state == DiskState.Corrupt) return BookSave.Error
            val revision = when (state) {
                DiskState.Missing -> 0L
                is DiskState.Valid -> state.snapshot.revision
                DiskState.Corrupt -> error("Already rejected")
            }
            if (revision != expectedRevision) return BookSave.Conflict(revision)
            if (state is DiskState.Valid) {
                require(state.snapshot.info.createdAt == info.createdAt) { "Erstellungsdatum darf nicht geändert werden" }
            }
            val savedAt = iso.format(Instant.ofEpochMilli(System.currentTimeMillis()))
            val previousUpdatedAt = if (state is DiskState.Valid) state.snapshot.info.updatedAt else info.updatedAt
            working.put("updatedAt", maxOf(info.createdAt, info.updatedAt, previousUpdatedAt, savedAt))
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

    /** A crash after the deletion marker cannot make a deleted book visible again. */
    private fun finalizeDelete(id: String) {
        if (!isDeleted(id)) return
        val artifacts = listOf(primary(id), previous(id)).flatMap { file ->
            listOf(file, File(file.path + ".bak"), File(file.path + ".new"))
        }
        if (artifacts.any { it.exists() && !it.delete() }) return
        if (runCatching { dao.delete(id) }.isFailure) return
        val marker = tombstone(id)
        listOf(marker, File(marker.path + ".bak"), File(marker.path + ".new")).forEach {
            if (it.exists()) it.delete()
        }
    }

    private fun cleanupPendingDeletes() {
        directory.listFiles()?.mapNotNull { file ->
            when {
                file.name.endsWith(".deleted") -> file.name.removeSuffix(".deleted")
                file.name.endsWith(".deleted.bak") -> file.name.removeSuffix(".deleted.bak")
                else -> null
            }
        }?.distinct()?.forEach { id ->
            if (runCatching { BookDocumentValidator.safeId(id) }.isSuccess) finalizeDelete(id)
        }
    }

    /** Room is only a catalog; deletion markers take precedence during recovery. */
    @Synchronized
    fun listEntries(): List<BookEntry> = synchronized(BOOK_DISK_LOCK) {
        cleanupPendingDeletes()
        dao.all().filterNot { isDeleted(it.id) }
    }

    /** New books start with one empty chapter and a valid, non-text cursor position. */
    @Synchronized
    fun createBook(rawTitle: String): BookLibraryResult = synchronized(BOOK_DISK_LOCK) {
        val title = rawTitle.trim()
        if (!validTitle(title)) return@synchronized BookLibraryResult.InvalidTitle
        val id = newId("b")
        val chapterId = newId("c")
        val now = iso.format(Instant.now())
        val document = JSONObject().put("schemaVersion", 1)
            .put("id", id).put("title", title).put("createdAt", now).put("updatedAt", now)
            .put("layout", JSONObject().put("pageWidthMm", 148).put("pageHeightMm", 210)
                .put("marginsMm", JSONObject().put("top", 18).put("bottom", 18)
                    .put("inside", 20).put("outside", 16))
                .put("fontFamily", "serif").put("fontSizePt", 11)
                .put("lineSpacing", 1.3).put("pageNumbers", true))
            .put("chapters", org.json.JSONArray().put(JSONObject().put("id", chapterId)
                .put("title", "Kapitel 1").put("blocks", org.json.JSONArray())))
        when (saveUnderLock(document, 0, BookPosition(chapterId, null, 0, 0))) {
            is BookSave.Saved -> BookLibraryResult.Success(id)
            else -> BookLibraryResult.Error
        }
    }

    /** A rename changes only the title; the original manuscript and position stay intact. */
    @Synchronized
    fun renameBook(id: String, rawTitle: String): BookLibraryResult = synchronized(BOOK_DISK_LOCK) {
        val title = rawTitle.trim()
        if (!validTitle(title)) return@synchronized BookLibraryResult.InvalidTitle
        when (val loaded = loadUnderLock(id)) {
            BookLoad.Missing -> BookLibraryResult.Missing
            is BookLoad.Found -> {
                val changed = JSONObject(loaded.document.toString()).put("title", title)
                when (saveUnderLock(changed, loaded.revision, loaded.position)) {
                    is BookSave.Saved -> BookLibraryResult.Success(id)
                    else -> BookLibraryResult.Error
                }
            }
            else -> BookLibraryResult.Error
        }
    }

    /** Duplicate all content and layout under independent book/chapter/block IDs. */
    @Synchronized
    fun duplicateBook(id: String): BookLibraryResult = synchronized(BOOK_DISK_LOCK) {
        when (val loaded = loadUnderLock(id)) {
            BookLoad.Missing -> BookLibraryResult.Missing
            is BookLoad.Found -> {
                try {
                    val copied = JSONObject(loaded.document.toString())
                    val newBookId = newId("b")
                    val now = iso.format(Instant.now())
                    val originalTitle = copied.getString("title")
                    val title = originalTitle.take(232) + " (Kopie)"
                    val ids = mutableMapOf<String, String>()
                    val chapters = copied.getJSONArray("chapters")
                    for (i in 0 until chapters.length()) {
                        val chapter = chapters.getJSONObject(i)
                        val oldId = chapter.getString("id")
                        val freshId = newId("c")
                        ids[oldId] = freshId
                        chapter.put("id", freshId)
                        val blocks = chapter.getJSONArray("blocks")
                        for (j in 0 until blocks.length()) {
                            val block = blocks.getJSONObject(j)
                            val prior = block.getString("id")
                            val next = newId("p")
                            ids[prior] = next
                            block.put("id", next)
                        }
                    }
                    val position = loaded.position
                    val clonedPosition = BookPosition(
                        ids.getValue(position.chapterId),
                        position.blockId?.let { ids.getValue(it) },
                        position.offset, position.scrollY
                    )
                    copied.put("id", newBookId).put("title", title)
                        .put("createdAt", now).put("updatedAt", now)
                    when (saveUnderLock(copied, 0, clonedPosition)) {
                        is BookSave.Saved -> BookLibraryResult.Success(newBookId)
                        else -> BookLibraryResult.Error
                    }
                } catch (_: Exception) {
                    BookLibraryResult.Error
                }
            }
            else -> BookLibraryResult.Error
        }
    }

    /**
     * Commit a persistent deletion marker before removing any document bytes.
     * If the process dies during cleanup, the next catalog read finishes it.
     * The separate E-02 prototype is never touched.
     */
    @Synchronized
    fun deleteBook(id: String): BookLibraryResult = synchronized(BOOK_DISK_LOCK) {
        if (runCatching { BookDocumentValidator.safeId(id) }.isFailure) {
            return@synchronized BookLibraryResult.Error
        }
        when (readDisk(id)) {
            DiskState.Missing -> BookLibraryResult.Missing
            DiskState.Corrupt -> BookLibraryResult.Error
            is DiskState.Valid -> {
                if (!isDeleted(id)) {
                    try {
                        writeAtomic(tombstone(id), byteArrayOf(1))
                    } catch (_: Exception) {
                        return@synchronized BookLibraryResult.Error
                    }
                }
                finalizeDelete(id)
                BookLibraryResult.Success(id)
            }
        }
    }

    /**
     * D-04: restore a validated portable backup as a NEW book only.
     * A matching ID, even when its local files are damaged, is never overwritten.
     * Original timestamps, chapter IDs and layout are preserved, unlike normal saves.
     */
    @Synchronized
    fun importNew(source: BookBackupCodec.Payload): BookImport =
        synchronized(BOOK_DISK_LOCK) { importNewUnderLock(source) }

    private fun importNewUnderLock(source: BookBackupCodec.Payload): BookImport = try {
        val book = JSONObject(source.document.toString())
        val info = BookDocumentValidator.inspect(book)
        BookDocumentValidator.validatePosition(book, source.position)
        require(source.revision in 1..9_007_199_254_740_991L)
        val a = primary(info.id)
        val b = previous(info.id)
        fun anyArtifact(file: File): Boolean =
            exists(file) || File(file.path + ".new").exists()
        if (isDeleted(info.id) || dao.find(info.id) != null || anyArtifact(a) || anyArtifact(b)) {
            BookImport.AlreadyExists
        } else {
            val bytes = bytesFor(book, source.revision, source.position)
            writeAtomic(a, bytes)
            // If indexing fails, keep the valid book file; the next load repairs Room.
            syncIndex(decode(bytes, info.id))
            BookImport.Imported
        }
    } catch (_: Exception) {
        BookImport.Error
    }

    /** B-02: every chapter action reads and saves under the same process-wide lock. */
    private sealed interface ChapterEdit {
        data class Apply(val position: BookPosition) : ChapterEdit
        data object NotFound : ChapterEdit
        data object TooMany : ChapterEdit
        data object Last : ChapterEdit
        data object Boundary : ChapterEdit
    }

    private fun editChapters(
        bookId: String,
        expectedRevision: Long,
        change: (org.json.JSONArray, BookPosition) -> ChapterEdit
    ): ChapterActionResult = synchronized(BOOK_DISK_LOCK) {
        if (expectedRevision < 1 || runCatching {
                BookDocumentValidator.safeId(bookId)
            }.isFailure) return@synchronized ChapterActionResult.Error
        when (val found = loadUnderLock(bookId)) {
            BookLoad.Missing -> ChapterActionResult.Missing
            is BookLoad.Found -> {
                if (found.revision != expectedRevision) return@synchronized ChapterActionResult.Conflict
                try {
                    val document = JSONObject(found.document.toString())
                    val chapters = document.getJSONArray("chapters")
                    when (val outcome = change(chapters, found.position)) {
                        ChapterEdit.NotFound -> ChapterActionResult.NotFound
                        ChapterEdit.TooMany -> ChapterActionResult.TooManyChapters
                        ChapterEdit.Last -> ChapterActionResult.LastChapter
                        ChapterEdit.Boundary -> ChapterActionResult.Boundary
                        is ChapterEdit.Apply -> when (val saved =
                            saveUnderLock(document, expectedRevision, outcome.position)) {
                            is BookSave.Saved -> ChapterActionResult.Updated(saved.revision)
                            is BookSave.Conflict -> ChapterActionResult.Conflict
                            BookSave.Error -> ChapterActionResult.Error
                        }
                    }
                } catch (_: Exception) {
                    ChapterActionResult.Error
                }
            }
            else -> ChapterActionResult.Error
        }
    }

    private fun chapterIndex(chapters: org.json.JSONArray, chapterId: String): Int {
        if (runCatching { BookDocumentValidator.safeId(chapterId) }.isFailure) return -1
        for (index in 0 until chapters.length()) {
            if (chapters.getJSONObject(index).getString("id") == chapterId) return index
        }
        return -1
    }

    /** New chapter becomes active; it begins with an empty valid cursor anchor. */
    fun createChapter(bookId: String, revision: Long, rawTitle: String): ChapterActionResult {
        val title = rawTitle.trim()
        if (!validTitle(title)) return ChapterActionResult.InvalidTitle
        return editChapters(bookId, revision) { chapters, _ ->
            if (chapters.length() >= 1_000) ChapterEdit.TooMany
            else {
                val id = newId("c")
                chapters.put(JSONObject().put("id", id).put("title", title)
                    .put("blocks", org.json.JSONArray()))
                ChapterEdit.Apply(BookPosition(id, null, 0, 0))
            }
        }
    }

    /** Names are metadata only: leave blocks and the current cursor untouched. */
    fun renameChapter(bookId: String, revision: Long, chapterId: String, rawTitle: String): ChapterActionResult {
        val title = rawTitle.trim()
        if (!validTitle(title)) return ChapterActionResult.InvalidTitle
        return editChapters(bookId, revision) { chapters, position ->
            val index = chapterIndex(chapters, chapterId)
            if (index < 0) ChapterEdit.NotFound
            else {
                chapters.getJSONObject(index).put("title", title)
                ChapterEdit.Apply(position)
            }
        }
    }

    /** Reordering swaps entire chapter objects. IDs, text and cursor stay stable. */
    fun moveChapter(bookId: String, revision: Long, chapterId: String, direction: Int): ChapterActionResult =
        editChapters(bookId, revision) { chapters, position ->
            val index = chapterIndex(chapters, chapterId)
            val target = index + direction
            when {
                index < 0 -> ChapterEdit.NotFound
                direction !in setOf(-1, 1) || target !in 0 until chapters.length() ->
                    ChapterEdit.Boundary
                else -> {
                    val current = chapters.getJSONObject(index)
                    val other = chapters.getJSONObject(target)
                    chapters.put(index, other)
                    chapters.put(target, current)
                    ChapterEdit.Apply(position)
                }
            }
        }

    /** Never silently delete the last chapter of a structured manuscript. */
    fun deleteChapter(bookId: String, revision: Long, chapterId: String): ChapterActionResult =
        editChapters(bookId, revision) { chapters, position ->
            val index = chapterIndex(chapters, chapterId)
            when {
                index < 0 -> ChapterEdit.NotFound
                chapters.length() == 1 -> ChapterEdit.Last
                else -> {
                    chapters.remove(index)
                    val updatedPosition = if (position.chapterId != chapterId) position else
                        BookPosition(chapters.getJSONObject(minOf(index, chapters.length() - 1))
                            .getString("id"), null, 0, 0)
                    ChapterEdit.Apply(updatedPosition)
                }
            }
        }

    /** Explicitly navigate to a chapter without reading or changing the old E-02 document. */
    fun openChapter(bookId: String, revision: Long, chapterId: String): ChapterActionResult =
        editChapters(bookId, revision) { chapters, _ ->
            if (chapterIndex(chapters, chapterId) < 0) ChapterEdit.NotFound
            else ChapterEdit.Apply(BookPosition(chapterId, null, 0, 0))
        }

    /** Non-destructive, bounded test hook. The E-02 file is never touched. */
    internal fun primaryFileForTest(id: String): File = primary(BookDocumentValidator.safeId(id))
    internal fun previousFileForTest(id: String): File = previous(BookDocumentValidator.safeId(id))
}
