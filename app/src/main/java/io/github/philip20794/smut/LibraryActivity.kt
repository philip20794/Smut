package io.github.philip20794.smut

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        val id: String, val title: String, val chapters: List<String>,
        val position: BookPosition, val recovered: Boolean
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
                                book.position.chapterId, book.recovered)
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
                        onBackup = { startActivity(Intent(this, BackupActivity::class.java)) },
                        onPrototype = { startActivity(Intent(this, MainActivity::class.java)) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun summary(book: BookLoad.Found): OpenedBook {
        val json: JSONObject = book.document
        val chapters = json.getJSONArray("chapters")
        return OpenedBook(
            json.getString("id"), json.getString("title"),
            (0 until chapters.length()).map { chapters.getJSONObject(it).getString("title") },
            book.position, book.recovered
        )
    }

    private fun message(result: BookLibraryResult, success: String): String = when (result) {
        is BookLibraryResult.Success -> success
        BookLibraryResult.InvalidTitle -> "Bitte einen Titel mit 1 bis 240 Zeichen eingeben."
        BookLibraryResult.Missing -> "Das Buch ist nicht mehr vorhanden."
        BookLibraryResult.Error -> "Vorgang fehlgeschlagen. Gespeicherte Daten wurden nicht als geändert bestätigt."
    }

    private fun refresh() {
        worker.execute {
            val result = runCatching { store.listEntries() }
            runOnUiThread {
                if (!isFinishing && !isDestroyed && !busy) {
                    if (result.isSuccess) {
                        books = result.getOrThrow()
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

internal data class BookSummary(
    val id: String, val title: String, val chapterTitles: List<String>,
    val lastChapterId: String, val recovered: Boolean
)

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
    onBackup: () -> Unit,
    onPrototype: () -> Unit
) {
    var createDialog by remember { mutableStateOf(false) }
    var renameEntry by remember { mutableStateOf<BookEntry?>(null) }
    var deleteEntry by remember { mutableStateOf<BookEntry?>(null) }
    var inputTitle by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Smut", style = MaterialTheme.typography.headlineMedium)
        if (opened != null) {
            Text("Buch geöffnet · Leseansicht", style = MaterialTheme.typography.titleMedium)
            Text(opened.title, style = MaterialTheme.typography.headlineSmall)
            Text("Die direkte Bearbeitung dieses Buchformats folgt in W-01.",
                style = MaterialTheme.typography.bodyMedium)
            Text("Kapitel", style = MaterialTheme.typography.titleMedium)
            if (opened.chapterTitles.isEmpty()) Text("Keine Kapitel.")
            opened.chapterTitles.forEachIndexed { index, title ->
                Text((index + 1).toString() + ". " + title)
            }
            if (opened.recovered) Text("Buch wurde aus einer gültigen Vorversion wiederhergestellt.")
            Button(onClick = onBack, enabled = !busy) { Text("Zur Bücherliste") }
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
