package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedallog.android.data.db.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getTrackPointsBySession(sessionId: Long): Flow<List<TrackPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trackPoints: List<TrackPointEntity>)

    @Query("DELETE FROM track_points WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)
}
