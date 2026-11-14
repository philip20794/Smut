package io.github.philip20794.smut

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException

/** SAF returns scoped content URIs; this code never accepts filesystem paths. */
internal object BookBackupIO {
    fun read(resolver: ContentResolver, uri: Uri): ByteArray {
        val input = resolver.openInputStream(uri) ?: throw IOException("Backup-Datei kann nicht geöffnet werden")
        return input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(16_384)
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                if (read == 0) continue
                if (output.size() > BookBackupCodec.MAX_BACKUP_BYTES - read) {
                    throw IOException("Backup überschreitet das Größenlimit")
                }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        }
    }

    fun write(resolver: ContentResolver, uri: Uri, bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= BookBackupCodec.MAX_BACKUP_BYTES)
        val output = resolver.openOutputStream(uri, "wt") ?: throw IOException("Backup-Ziel nicht beschreibbar")
        output.use { stream ->
            stream.write(bytes)
            stream.flush()
        }
    }
}
