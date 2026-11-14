package io.github.philip20794.smut

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BookBackupCodecTest {
    private fun book(): JSONObject = JSONObject(
        """{"schemaVersion":1,"id":"portable1","title":"Äpfel und Grüße","author":"Runa","createdAt":"2026-11-14T10:00:00.000Z","updatedAt":"2026-11-14T10:00:00.000Z","layout":{"pageWidthMm":148,"pageHeightMm":210,"marginsMm":{"top":18,"bottom":18,"inside":20,"outside":16},"fontFamily":"serif","fontSizePt":11,"lineSpacing":1.3,"pageNumbers":true},"chapters":[{"id":"chapter1","title":"Kapitel A","blocks":[{"id":"block1","type":"paragraph","runs":[{"text":"Äpfel, Öl, Grüße 😀","marks":{"italic":true}}]}]}]}"""
    )
    private fun sample(): BookLoad.Found = BookLoad.Found(
        book(), 7, BookPosition("chapter1", "block1", 5, 39), false
    )
    private fun editBackup(transform: (JSONObject) -> Unit): ByteArray {
        val data = JSONObject(String(BookBackupCodec.create(sample()), Charsets.UTF_8))
        transform(data)
        return data.toString().toByteArray(Charsets.UTF_8)
    }

    @Test fun fullBookAndCursorRoundTripPreservesStructuredContent() {
        val bytes = BookBackupCodec.create(sample())
        val result = BookBackupCodec.parse(bytes)
        assertEquals(sample().document.toString(), result.document.toString())
        assertEquals(sample().position, result.position)
        assertEquals(7L, result.revision)
        assertTrue(String(bytes, Charsets.UTF_8).contains("sha256"))
    }

    @Test fun corruptedAndUnsupportedBackupsAreRejected() {
        val cases = listOf(
            editBackup { it.put("backupVersion", 2) },
            editBackup { it.put("schemaVersion", 2) },
            editBackup { it.put("format", "zip") },
            editBackup { it.put("sha256", "0".repeat(64)) },
            editBackup { it.put("documentJson", it.getString("documentJson").replace("Äpfel", "Pferde")) },
            editBackup { it.put("positionJson", """{"chapterId":"chapter1","blockId":"block1","offset":999,"scrollY":0}""") },
            editBackup { it.put("extra", true) },
            BookBackupCodec.create(sample()).copyOf(40),
            ByteArray(BookBackupCodec.MAX_BACKUP_BYTES + 1),
            BookBackupCodec.create(sample()) + "malicious trailing data".toByteArray(),
            byteArrayOf(0xC3.toByte(), 0x28),
        )
        cases.forEach { bytes ->
            assertThrows(Exception::class.java) { BookBackupCodec.parse(bytes) }
        }
    }

    @Test fun malformedBookAndCursorCannotBeExported() {
        assertThrows(Exception::class.java) {
            BookBackupCodec.create(BookLoad.Found(
                book().put("schemaVersion", 999), 1, sample().position, false
            ))
        }
        assertThrows(Exception::class.java) {
            BookBackupCodec.create(BookLoad.Found(
                book(), 1, BookPosition("chapter1", "block1", 9999, 0), false
            ))
        }
        assertThrows(Exception::class.java) {
            BookBackupCodec.create(BookLoad.Found(book(), 0, sample().position, false))
        }
    }
}
