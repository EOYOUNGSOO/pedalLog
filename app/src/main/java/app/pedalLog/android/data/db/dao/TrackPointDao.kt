package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedallog.android.data.db.entity.TrackPointEntity

@Dao
interface TrackPointDao {

    @Query(
        """
        SELECT * FROM track_points
        WHERE sessionId = :sessionId
        ORDER BY timestamp ASC
        """
    )
    suspend fun getTrackPointsBySession(sessionId: Long): List<TrackPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<TrackPointEntity>)

    @Query("DELETE FROM track_points WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM track_points WHERE sessionId = :sessionId")
    suspend fun getCountBySession(sessionId: Long): Int
}
