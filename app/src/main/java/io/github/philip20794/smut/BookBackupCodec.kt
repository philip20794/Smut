package io.github.philip20794.smut

import org.json.JSONObject
import org.json.JSONTokener
import java.security.MessageDigest

/** Portable, uncompressed JSON v1: no paths, entries, executable data or ZIP-bomb surface. */
internal object BookBackupCodec {
    const val MAX_BACKUP_BYTES = 20_000_000
    const val MIME_TYPE = "application/json"
    private const val MAGIC = "smut-book-backup"
    private val CHECKSUM = Regex("[0-9a-f]{64}")

    data class Payload(
        val document: JSONObject,
        val position: BookPosition,
        val revision: Long
    )

    private fun digest(documentJson: String, positionJson: String, revision: Long): String {
        val value = "$MAGIC\n1\n$revision\n$documentJson\n$positionJson"
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun checkedRevision(value: Any?): Long {
        require(value is Number && value.toString().matches(Regex("[1-9][0-9]{0,14}")))
        val revision = value.toLong()
        require(revision in 1..9_007_199_254_740_991L)
        return revision
    }

    fun create(found: BookLoad.Found): ByteArray {
        BookDocumentValidator.inspect(found.document)
        BookDocumentValidator.validatePosition(found.document, found.position)
        val revision = checkedRevision(found.revision)
        val document = found.document.toString()
        val position = found.position.toJson().toString()
        val backup = JSONObject()
            .put("format", MAGIC)
            .put("backupVersion", 1)
            .put("schemaVersion", 1)
            .put("revision", revision)
            .put("documentJson", document)
            .put("positionJson", position)
            .put("sha256", digest(document, position, revision))
            .toString().toByteArray(Charsets.UTF_8)
        require(backup.size <= MAX_BACKUP_BYTES) { "Backupdatei zu groß" }
        return backup
    }

    fun parse(bytes: ByteArray): Payload {
        require(bytes.isNotEmpty() && bytes.size <= MAX_BACKUP_BYTES) { "Backupdatei leer oder zu groß" }
        val decoded = Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        val tokenizer = JSONTokener(decoded)
        val envelope = tokenizer.nextValue() as? JSONObject ?: error("Kein JSON-Objekt")
        require(tokenizer.nextClean() == 0.toChar()) { "Angehängte Daten" }
        BookDocumentValidator.exactKeys(envelope, setOf(
            "format", "backupVersion", "schemaVersion", "revision", "documentJson", "positionJson", "sha256"
        ))
        require(envelope.opt("format") == MAGIC) { "Unbekanntes Backupformat" }
        require(envelope.opt("backupVersion") is Int && envelope.getInt("backupVersion") == 1) { "Backupversion nicht unterstützt" }
        require(envelope.opt("schemaVersion") is Int && envelope.getInt("schemaVersion") == 1) { "Buchversion nicht unterstützt" }
        val revision = checkedRevision(envelope.opt("revision"))
        val documentString = envelope.opt("documentJson") as? String ?: error("Buchdaten fehlen")
        val positionString = envelope.opt("positionJson") as? String ?: error("Positionsdaten fehlen")
        require(documentString.toByteArray(Charsets.UTF_8).size <= BookDocumentValidator.MAX_BOOK_BYTES)
        val expected = envelope.opt("sha256") as? String ?: error("Prüfsumme fehlt")
        require(CHECKSUM.matches(expected))
        val actual = digest(documentString, positionString, revision)
        require(MessageDigest.isEqual(
            actual.toByteArray(Charsets.US_ASCII), expected.toByteArray(Charsets.US_ASCII)
        )) { "Backup-Prüfsumme stimmt nicht" }
        val document = JSONObject(documentString)
        BookDocumentValidator.inspect(document)
        val position = BookPosition.parse(JSONObject(positionString))
        BookDocumentValidator.validatePosition(document, position)
        return Payload(document, position, revision)
    }
}
