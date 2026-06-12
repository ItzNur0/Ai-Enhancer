package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "enhancement_history")
data class EnhancementEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalImage: String, // Base64 representation or sample icon identifier
    val enhancedImage: String, // Base64 representation of enhanced image
    val toolType: String,      // "HD Upscale", "Background Removal", "Face Restoration", etc.
    val prompt: String,        // prompt or criteria
    val timestamp: Long = System.currentTimeMillis(),
    val parameters: String     // "1K", "2K", "4K", "Default"
)

@Dao
interface EnhancementDao {
    @Query("SELECT * FROM enhancement_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<EnhancementEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EnhancementEntry): Long

    @Delete
    suspend fun deleteEntry(entry: EnhancementEntry)

    @Query("DELETE FROM enhancement_history")
    suspend fun clearHistory()
}

@Database(entities = [EnhancementEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun enhancementDao(): EnhancementDao
}

// Simple Repository pattern implementation
class EnhancementRepository(private val dao: EnhancementDao) {
    val history: Flow<List<EnhancementEntry>> = dao.getAllHistory()

    suspend fun saveEntry(entry: EnhancementEntry) = dao.insertEntry(entry)

    suspend fun removeEntry(entry: EnhancementEntry) = dao.deleteEntry(entry)

    suspend fun clearAll() = dao.clearHistory()
}
