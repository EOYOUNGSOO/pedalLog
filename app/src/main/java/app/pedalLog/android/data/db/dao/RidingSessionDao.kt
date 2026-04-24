package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.pedallog.android.data.db.entity.RidingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RidingSessionDao {
    @Query("SELECT * FROM riding_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<RidingSessionEntity>>

    @Query("SELECT * FROM riding_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): RidingSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RidingSessionEntity): Long

    @Update
    suspend fun update(session: RidingSessionEntity)

    @Delete
    suspend fun delete(session: RidingSessionEntity)

    @Query("UPDATE riding_sessions SET notionPageId = :pageId WHERE id = :id")
    suspend fun updateNotionPageId(id: Long, pageId: String)
}
