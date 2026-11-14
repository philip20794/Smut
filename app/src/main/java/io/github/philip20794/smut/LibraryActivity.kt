package io.github.philip20794.smut

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * B-01: launcher and native catalog for versioned books. The A5 legacy prototype
 * remains a separate opt-in screen until the W-01 model/editor migration.
 */
class LibraryActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val store by lazy { BookStore(applicationContext) }
    private var books by mutableStateOf<List<BookEntry>>(emptyList())
    private var selected by mutableStateOf<OpenedBook?>(null)
    private var status by mutableStateOf("Lokale Bücher werden geladen …")
    private var busy by mutableStateOf(false)

    private data class WorkResult(val message: String, val opened: OpenedBook? = null)
    private data class OpenedBook(
        val id: String, val title: String, val chapters: List<ChapterSummary>,
        val position: BookPosition, val revision: Long, val recovered: Boolean,
        val positionRepaired: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LibraryScreen(
                        books = books,
                        opened = selected?.let { book ->
                            BookSummary(book.id, book.title, book.chapters,
                                book.position, book.revision, book.recovered,
                                book.positionRepaired)
                        },
                        status = status,
                        busy = busy,
                        onBack = { selected = null },
                        onCreate = { title ->
                            perform("Buch wird angelegt …") {
                                val result = store.createBook(title)
                                val opened = (result as? BookLibraryResult.Success)?.let {
                                    (store.load(it.bookId) as? BookLoad.Found)?.let(::summary)
                                }
                                WorkResult(message(result, "Buch angelegt."), opened)
                            }
                        },
                        onOpen = { id ->
                            perform("Buch wird geöffnet …") {
                                when (val found = store.load(id)) {
                                    is BookLoad.Found -> WorkResult("Buch geöffnet.", summary(found))
                                    BookLoad.Missing -> WorkResult("Das Buch ist nicht mehr vorhanden.")
                                    BookLoad.Corrupt -> WorkResult("Buchdatei beschädigt. Bitte eine Sicherung prüfen.")
                                    BookLoad.Error -> WorkResult("Buch konnte nicht geöffnet werden.")
                                }
                            }
                        },
                        onRename = { id, title ->
                            perform("Buch wird umbenannt …") {
                                WorkResult(message(store.renameBook(id, title), "Buch umbenannt."))
                            }
                        },
                        onDuplicate = { id ->
                            perform("Buch wird dupliziert …") {
                                WorkResult(message(store.duplicateBook(id), "Kopie erstellt."))
                            }
                        },
                        onDelete = { id ->
                            perform("Buch wird gelöscht …") {
                                WorkResult(message(store.deleteBook(id), "Buch gelöscht."))
                            }
                        },
                        onCreateChapter = { id, revision, title ->
                            changeChapter(id, "Kapitel wird angelegt …", "Kapitel angelegt.") {
                                store.createChapter(id, revision, title)
                            }
                        },
                        onRenameChapter = { id, revision, chapterId, title ->
                            changeChapter(id, "Kapitel wird umbenannt …", "Kapitel umbenannt.") {
                                store.renameChapter(id, revision, chapterId, title)
                            }
                        },
                        onMoveChapter = { id, revision, chapterId, direction ->
                            changeChapter(id, "Kapitel wird verschoben …", "Reihenfolge gespeichert.") {
                                store.moveChapter(id, revision, chapterId, direction)
                            }
                        },
                        onDeleteChapter = { id, revision, chapterId ->
                            changeChapter(id, "Kapitel wird gelöscht …", "Kapitel gelöscht.") {
                                store.deleteChapter(id, revision, chapterId)
                            }
                        },
                        onOpenChapter = { id, revision, chapterId ->
                            changeChapter(id, "Kapitel wird geöffnet …", "Kapitel geöffnet.") {
                                store.openChapter(id, revision, chapterId)
                            }
                        },
                        onBackup = { startActivity(Intent(this, BackupActivity::class.java)) },
                        onPrototype = { startActivity(Intent(this, MainActivity::class.java)) },
                        onEdit = { id -> startActivity(BookEditorActivity.intent(this, id)) }
                    )
                }
            }
        }
        savedInstanceState?.getString("openedBookId")?.let { id ->
            perform("Buch wird wieder geöffnet …") {
                when (val found = store.load(id)) {
                    is BookLoad.Found -> WorkResult("Buch wieder geöffnet.", summary(found))
                    BookLoad.Missing -> WorkResult("Das Buch ist nicht mehr vorhanden.")
                    BookLoad.Corrupt -> WorkResult("Buchdatei beschädigt. Bitte die Sicherung prüfen.")
                    BookLoad.Error -> WorkResult("Buch konnte nicht geöffnet werden.")
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selected?.id?.let { outState.putString("openedBookId", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Show the saved caret's surrounding text in the read-only native view.
     * The actual editable A5 document is connected only in W-01.
     */
    private fun chapterPreview(chapter: JSONObject, focus: BookPosition?): String {
        val blocks = chapter.getJSONArray("blocks")
        val focusIndex = if (focus?.blockId == null) -1 else
            (0 until blocks.length()).firstOrNull {
                blocks.getJSONObject(it).optString("id") == focus.blockId
            } ?: -1
        return buildString {
            for (i in maxOf(0, focusIndex) until blocks.length()) {
                if (length >= 800) break
                val block = blocks.getJSONObject(i)
                val runs = block.optJSONArray("runs") ?: continue
                val fullText = buildString {
                    for (j in 0 until runs.length()) append(runs.getJSONObject(j).getString("text"))
                }
                if (fullText.isEmpty()) continue
                if (isNotEmpty()) append("\n")
                val cursor = if (i == focusIndex) focus!!.offset.coerceIn(0, fullText.length) else 0
                var from = if (i == focusIndex) (cursor - 160).coerceAtLeast(0) else 0
                if (from in 1 until fullText.length &&
                    Character.isHighSurrogate(fullText[from - 1]) &&
                    Character.isLowSurrogate(fullText[from])) from--
                var to = minOf(fullText.length, from + (800 - length).coerceAtLeast(0))
                if (to in 1 until fullText.length &&
                    Character.isHighSurrogate(fullText[to - 1]) &&
                    Character.isLowSurrogate(fullText[to])) to--
                if (from > 0) append("… ")
                if (i == focusIndex && cursor <= to) {
                    append(fullText.substring(from, cursor))
                    append("▏")
                    append(fullText.substring(cursor, to))
                } else append(fullText.substring(from, to))
                if (to < fullText.length) append(" …")
            }
        }
    }

    private fun summary(book: BookLoad.Found): OpenedBook {
        val json: JSONObject = book.document
        val chapters = json.getJSONArray("chapters")
        return OpenedBook(
            json.getString("id"), json.getString("title"),
            (0 until chapters.length()).map { index ->
                val chapter = chapters.getJSONObject(index)
                val blocks = chapter.getJSONArray("blocks")
                val focus = book.position.takeIf { it.chapterId == chapter.getString("id") }
                ChapterSummary(chapter.getString("id"), chapter.getString("title"),
                    blocks.length(), chapterPreview(chapter, focus))
            },
            book.position, book.revision, book.recovered, book.positionRepaired
        )
    }

    private fun message(result: BookLibraryResult, success: String): String = when (result) {
        is BookLibraryResult.Success -> success
        BookLibraryResult.InvalidTitle -> "Bitte einen Titel mit 1 bis 240 Zeichen eingeben."
        BookLibraryResult.Missing -> "Das Buch ist nicht mehr vorhanden."
        BookLibraryResult.Error -> "Vorgang fehlgeschlagen. Gespeicherte Daten wurden nicht als geändert bestätigt."
    }

    private fun chapterMessage(result: ChapterActionResult, success: String): String = when (result) {
        is ChapterActionResult.Updated -> success
        ChapterActionResult.InvalidTitle -> "Bitte einen Kapiteltitel mit 1 bis 240 Zeichen eingeben."
        ChapterActionResult.NotFound -> "Dieses Kapitel ist nicht mehr vorhanden."
        ChapterActionResult.Missing -> "Das Buch existiert nicht mehr."
        ChapterActionResult.Conflict -> "Buch wurde zwischenzeitlich geändert. Ansicht aktualisiert; bitte erneut versuchen."
        ChapterActionResult.LastChapter -> "Das letzte Kapitel kann nicht gelöscht werden."
        ChapterActionResult.TooManyChapters -> "Maximal 1.000 Kapitel pro Buch."
        ChapterActionResult.Boundary -> "Kapitel steht bereits am Anfang oder Ende."
        ChapterActionResult.Error -> "Kapiteländerung fehlgeschlagen. Bitte die Speicherung prüfen."
    }

    private fun changeChapter(
        id: String, progress: String, success: String, action: () -> ChapterActionResult
    ) {
        perform(progress) {
            val result = action()
            val fresh = (store.load(id) as? BookLoad.Found)?.let(::summary)
            WorkResult(chapterMessage(result, success), fresh)
        }
    }

    private fun refresh() {
        val openedId = selected?.id
        worker.execute {
            val result = runCatching { store.listEntries() }
            val latestOpened = openedId?.let { (store.load(it) as? BookLoad.Found)?.let(::summary) }
            runOnUiThread {
                if (!isFinishing && !isDestroyed && !busy) {
                    if (result.isSuccess) {
                        books = result.getOrThrow()
                        if (selected?.id == openedId && latestOpened != null) selected = latestOpened
                        if (status == "Lokale Bücher werden geladen …") status = "Alle Bücher bleiben offline auf diesem Gerät."
                    } else status = "Bücherliste konnte nicht geladen werden."
                }
            }
        }
    }

    private fun perform(progress: String, action: () -> WorkResult) {
        if (busy) return
        busy = true
        status = progress
        worker.execute {
            val result = runCatching { action() }
                .getOrElse { WorkResult("Vorgang fehlgeschlagen. Bitte die Speicherung prüfen.") }
            val latest = runCatching { store.listEntries() }
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    if (latest.isSuccess) books = latest.getOrThrow()
                    status = if (latest.isFailure) "Bücherliste konnte nicht aktualisiert werden."
                        else result.message
                    if (result.opened != null) selected = result.opened
                    busy = false
                }
            }
        }
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}

internal data class ChapterSummary(
    val id: String, val title: String, val blockCount: Int, val preview: String
)
internal data class BookSummary(
    val id: String, val title: String, val chapters: List<ChapterSummary>,
    val position: BookPosition, val revision: Long, val recovered: Boolean,
    val positionRepaired: Boolean
) {
    val lastChapterId: String get() = position.chapterId
}

@Composable
internal fun LibraryScreen(
    books: List<BookEntry>,
    opened: BookSummary?,
    status: String,
    busy: Boolean,
    onBack: () -> Unit,
    onCreate: (String) -> Unit,
    onOpen: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCreateChapter: (String, Long, String) -> Unit,
    onRenameChapter: (String, Long, String, String) -> Unit,
    onMoveChapter: (String, Long, String, Int) -> Unit,
    onDeleteChapter: (String, Long, String) -> Unit,
    onOpenChapter: (String, Long, String) -> Unit,
    onBackup: () -> Unit,
    onPrototype: () -> Unit,
    onEdit: (String) -> Unit
) {
    var createDialog by remember { mutableStateOf(false) }
    var renameEntry by remember { mutableStateOf<BookEntry?>(null) }
    var deleteEntry by remember { mutableStateOf<BookEntry?>(null) }
    var inputTitle by remember { mutableStateOf("") }
    var newChapterDialog by remember { mutableStateOf(false) }
    var renameChapter by remember { mutableStateOf<ChapterSummary?>(null) }
    var deleteChapter by remember { mutableStateOf<ChapterSummary?>(null) }
    var bookMenuOpen by remember { mutableStateOf(false) }
    val readingScroll = rememberScrollState()

    // Hardware/system back returns to the catalog instead of closing the app.
    BackHandler(enabled = opened != null) { if (!busy) onBack() }
    LaunchedEffect(opened?.id, opened?.lastChapterId) {
        if (opened == null) readingScroll.scrollTo(0)
        else {
            // Wait for the new book contents to be measured before restoring Y.
            withFrameNanos { }
            readingScroll.scrollTo(opened.position.scrollY)
        }
    }

    val editing = createDialog || renameEntry != null
    if (editing) {
        AlertDialog(
            onDismissRequest = { if (!busy) { createDialog = false; renameEntry = null } },
            title = { Text(if (createDialog) "Neues Buch" else "Buch umbenennen") },
            text = {
                OutlinedTextField(
                    value = inputTitle, onValueChange = { inputTitle = it },
                    label = { Text("Buchtitel") }, singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy && inputTitle.trim().isNotEmpty() &&
                        inputTitle.trim().length <= 240,
                    onClick = {
                        val target = renameEntry
                        val title = inputTitle
                        createDialog = false
                        renameEntry = null
                        if (target == null) onCreate(title) else onRename(target.id, title)
                    }
                ) { Text(if (createDialog) "Anlegen" else "Speichern") }
            },
            dismissButton = {
                TextButton(onClick = { createDialog = false; renameEntry = null }, enabled = !busy) {
                    Text("Abbrechen")
                }
            }
        )
    }
    deleteEntry?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!busy) deleteEntry = null },
            title = { Text("Buch löschen?") },
            text = { Text("„" + target.title + "“ mit allen Kapiteln dauerhaft löschen?") },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = { deleteEntry = null; onDelete(target.id) }
                ) { Text("Endgültig löschen") }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { deleteEntry = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (opened != null && (newChapterDialog || renameChapter != null)) {
        val target = renameChapter
        AlertDialog(
            onDismissRequest = { if (!busy) { newChapterDialog = false; renameChapter = null } },
            title = { Text(if (newChapterDialog) "Neues Kapitel" else "Kapitel umbenennen") },
            text = {
                OutlinedTextField(
                    value = inputTitle, onValueChange = { inputTitle = it },
                    label = { Text("Kapiteltitel") }, singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy && inputTitle.trim().isNotEmpty() && inputTitle.trim().length <= 240,
                    onClick = {
                        val title = inputTitle
                        newChapterDialog = false
                        renameChapter = null
                        if (target == null) onCreateChapter(opened.id, opened.revision, title)
                        else onRenameChapter(opened.id, opened.revision, target.id, title)
                    }
                ) { Text(if (target == null) "Anlegen" else "Speichern") }
            },
            dismissButton = {
                TextButton(
                    onClick = { newChapterDialog = false; renameChapter = null }, enabled = !busy
                ) { Text("Abbrechen") }
            }
        )
    }
    if (opened != null) {
        deleteChapter?.let { target ->
            AlertDialog(
                onDismissRequest = { if (!busy) deleteChapter = null },
                title = { Text("Kapitel löschen?") },
                text = { Text("„" + target.title + "“ mit allen Textblöcken dauerhaft löschen?") },
                confirmButton = {
                    TextButton(enabled = !busy, onClick = {
                        deleteChapter = null
                        onDeleteChapter(opened.id, opened.revision, target.id)
                    }) { Text("Kapitel endgültig löschen") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteChapter = null }, enabled = !busy) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
            .verticalScroll(readingScroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Smut", style = MaterialTheme.typography.headlineMedium)
        if (opened != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)
                ) { Text("Zur Bücherliste") }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { bookMenuOpen = true }, enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Buchmenü") }
                    DropdownMenu(
                        expanded = bookMenuOpen,
                        onDismissRequest = { bookMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Buch-Backup und Import") },
                            onClick = { bookMenuOpen = false; onBackup() }
                        )
                    }
                }
            }
            Text("Buch geöffnet · Leseansicht", style = MaterialTheme.typography.titleMedium)
            Text(opened.title, style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = { onEdit(opened.id) }, enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Im A5-Editor bearbeiten") }
            Text(status, style = MaterialTheme.typography.bodySmall)
            Text("Kapitel", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { inputTitle = ""; newChapterDialog = true },
                enabled = !busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Neues Kapitel") }
            opened.chapters.forEachIndexed { index, chapter ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text((index + 1).toString() + ". " + chapter.title,
                            style = MaterialTheme.typography.titleSmall)
                        if (chapter.id == opened.lastChapterId) {
                            Text("Aktuelles Kapitel", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpenChapter(opened.id, opened.revision, chapter.id) },
                                enabled = !busy,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Kapitel " + chapter.title + " öffnen"
                                }
                            ) { Text("Kapitel öffnen") }
                            OutlinedButton(
                                onClick = { inputTitle = chapter.title; renameChapter = chapter },
                                enabled = !busy,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Kapitel " + chapter.title + " umbenennen"
                                }
                            ) { Text("Umbenennen") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onMoveChapter(opened.id, opened.revision, chapter.id, -1)
                                },
                                enabled = !busy && index > 0,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Kapitel " + chapter.title + " nach oben"
                                }
                            ) { Text("Hoch") }
                            OutlinedButton(
                                onClick = {
                                    onMoveChapter(opened.id, opened.revision, chapter.id, 1)
                                },
                                enabled = !busy && index < opened.chapters.lastIndex,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Kapitel " + chapter.title + " nach unten"
                                }
                            ) { Text("Runter") }
                            OutlinedButton(
                                onClick = { deleteChapter = chapter },
                                enabled = !busy && opened.chapters.size > 1,
                                modifier = Modifier.weight(1f).semantics {
                                    contentDescription = "Kapitel " + chapter.title + " löschen"
                                }
                            ) { Text("Löschen") }
                        }
                    }
                }
            }
            opened.chapters.firstOrNull { it.id == opened.lastChapterId }?.let { chapter ->
                Text("Aktuelles Kapitel: " + chapter.title,
                    style = MaterialTheme.typography.titleMedium)
                Text(chapter.blockCount.toString() + " Textblöcke · Nur Lesevorschau",
                    style = MaterialTheme.typography.bodySmall)
                if (opened.position.blockId != null) {
                    Text("Gespeicherte Textstelle: Zeichen " + opened.position.offset,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (chapter.preview.isNotEmpty()) Text(chapter.preview)
                else Text("Dieses Kapitel enthält noch keinen Text.")
            }
            if (opened.positionRepaired) Text(
                "Eine veraltete Leseposition wurde auf einen gültigen Textanker gesetzt."
            )
            if (opened.recovered) Text("Buch wurde aus einer gültigen Vorversion wiederhergestellt.")
        } else {
            Button(
                onClick = { inputTitle = ""; createDialog = true },
                enabled = !busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Neues Buch") }
            Text(status, style = MaterialTheme.typography.bodyMedium)
            if (books.isEmpty()) {
                Text("Noch keine strukturierten Bücher vorhanden.")
            }
            books.forEach { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpen(entry.id) }, enabled = !busy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Öffnen") }
                            OutlinedButton(
                                onClick = { inputTitle = entry.title; renameEntry = entry },
                                enabled = !busy, modifier = Modifier.weight(1f)
                            ) { Text("Umbenennen") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onDuplicate(entry.id) }, enabled = !busy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Duplizieren") }
                            OutlinedButton(
                                onClick = { deleteEntry = entry }, enabled = !busy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Löschen") }
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onBackup, enabled = !busy, modifier = Modifier.fillMaxWidth()
            ) { Text("Buch-Backup und Import") }
            OutlinedButton(
                onClick = onPrototype, enabled = !busy, modifier = Modifier.fillMaxWidth()
            ) { Text("A5-Prototyp öffnen") }
            Text(
                "Der bisherige A5-Prototyp wird getrennt gespeichert. " +
                    "Er wird erst mit W-01 in die neue Buchverwaltung übernommen.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
