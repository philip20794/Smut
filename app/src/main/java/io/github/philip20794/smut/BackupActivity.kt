package io.github.philip20794.smut

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.Executors

/** D-04: separate native SAF screen. The E-02 Canvas layout stays untouched. */
class BackupActivity : ComponentActivity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val store by lazy { BookStore(applicationContext) }
    private var entries by mutableStateOf<List<BookEntry>>(emptyList())
    private var status by mutableStateOf("Bücher werden geladen …")
    private var busy by mutableStateOf(false)
    private var pendingExportId: String? = null

    private val createDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument(BookBackupCodec.MIME_TYPE)) { uri: Uri? ->
            val id = pendingExportId
            pendingExportId = null
            if (uri != null && id != null) perform("Sicherung wird geschrieben …") {
                val found = store.load(id) as? BookLoad.Found
                    ?: return@perform "Buch nicht lesbar. Keine Sicherung geschrieben."
                BookBackupIO.write(contentResolver, uri, BookBackupCodec.create(found))
                "Sicherung für „${found.document.getString("title")}“ erstellt."
            }
        }

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) perform("Backup wird geprüft …") {
                val backup = BookBackupCodec.parse(BookBackupIO.read(contentResolver, uri))
                when (store.importNew(backup)) {
                    BookImport.Imported -> "Buch sicher importiert."
                    BookImport.AlreadyExists -> "Buch ist bereits vorhanden. Keine bestehenden Daten verändert."
                    BookImport.Error -> "Import fehlgeschlagen. Keine vorhandenen Bücher verändert."
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingExportId = savedInstanceState?.getString("pendingExportId")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BackupScreen(
                        books = entries, status = status, busy = busy,
                        onBack = { finish() },
                        onImport = { openDocument.launch(arrayOf("application/json", "application/octet-stream", "text/plain")) },
                        onExport = { id ->
                            pendingExportId = id
                            createDocument.launch("smut-$id.smut.json")
                        }
                    )
                }
            }
        }
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingExportId", pendingExportId)
        super.onSaveInstanceState(outState)
    }

    private fun refresh() {
        worker.execute {
            val result = runCatching { store.listEntries() }
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    entries = result.getOrDefault(emptyList())
                    if (result.isFailure) status = "Bücher konnten nicht gelesen werden."
                    else if (!busy) status = "Nur strukturierte Smut-Bücher (ab D-03)."
                }
            }
        }
    }

    private fun perform(progress: String, task: () -> String) {
        if (busy) return
        busy = true
        status = progress
        worker.execute {
            val result = runCatching { task() }.getOrElse {
                "Datei ungültig oder Dateizugriff fehlgeschlagen. Keine vorhandenen Bücher verändert."
            }
            val list = runCatching { store.listEntries() }.getOrDefault(emptyList())
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    entries = list
                    status = result
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

@Composable
private fun BackupScreen(
    books: List<BookEntry>,
    status: String,
    busy: Boolean,
    onBack: () -> Unit,
    onImport: () -> Unit,
    onExport: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Buch-Backup", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Sicherungen enthalten ein vollständiges Buch mit Kapiteln, Formatierungen und letzter Position. " +
                "Die aktuelle A5-Prototypseite ist noch nicht Teil des neuen Buchformats.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onImport, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text("Buch aus Datei importieren")
        }
        Text(status, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text("Gespeicherte Bücher", style = MaterialTheme.typography.titleMedium)
        if (books.isEmpty()) {
            Text("Noch keine Bücher im neuen Format vorhanden.")
        }
        books.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(entry.title, style = MaterialTheme.typography.titleSmall)
                    Text(entry.id, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { onExport(entry.id) }, enabled = !busy) {
                    Text("Sichern")
                }
            }
        }
        OutlinedButton(onClick = onBack) { Text("Zurück zum Editor") }
    }
}
