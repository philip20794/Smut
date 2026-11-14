package io.github.philip20794.smut

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

/**
 * D-03: Room is a rebuildable index, never the authoritative manuscript store.
 * Each validated, checksummed revision lives in an AtomicFile under filesDir.
 */
@Entity(tableName = "book_entries")
internal data class BookEntry(
    @PrimaryKey val id: String,
    val title: String,
    val updatedAt: String,
    val revision: Long,
    val chapterId: String,
    val blockId: String?,
    val caretOffset: Int,
    val scrollY: Int,
)

@Dao
internal interface BookEntryDao {
    @Query("SELECT * FROM book_entries WHERE id = :id LIMIT 1")
    fun find(id: String): BookEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(entry: BookEntry)

    @Query("SELECT * FROM book_entries ORDER BY updatedAt DESC")
    fun all(): List<BookEntry>
}

@Database(entities = [BookEntry::class], version = 1, exportSchema = false)
internal abstract class BookIndexDatabase : RoomDatabase() {
    abstract fun entries(): BookEntryDao
}
