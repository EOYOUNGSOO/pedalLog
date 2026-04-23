package app.pedalLog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedalLog.android.data.db.entity.RidingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RidingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RidingSessionEntity): Long

    @Query("SELECT * FROM riding_sessions ORDER BY startedAt DESC")
    fun observeRecent(): Flow<List<RidingSessionEntity>>
}
