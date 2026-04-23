package app.pedalLog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedalLog.android.data.db.entity.BikeTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeTypeDao {
    @Query("SELECT * FROM bike_types ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<BikeTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<BikeTypeEntity>)
}
