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

    @Query(
        """
        UPDATE riding_sessions
        SET notionPageId = :pageId,
            notionRegisteredAt = :registeredAt
        WHERE id = :id
        """
    )
    suspend fun updateNotionResult(
        id: Long,
        pageId: String,
        registeredAt: Long
    )

    @Query(
        """
        SELECT * FROM riding_sessions
        WHERE notionPageId IS NULL
        ORDER BY startTime DESC
        """
    )
    fun getUnregisteredSessions(): Flow<List<RidingSessionEntity>>

    @Query("SELECT COUNT(*) FROM riding_sessions")
    suspend fun getTotalCount(): Int
}
