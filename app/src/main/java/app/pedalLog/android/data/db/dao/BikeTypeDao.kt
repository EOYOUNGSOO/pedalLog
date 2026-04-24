package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedallog.android.data.db.entity.BikeTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeTypeDao {
    @Query("SELECT * FROM bike_types ORDER BY isDefault DESC, sortOrder ASC")
    fun getAllBikeTypes(): Flow<List<BikeTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bikeType: BikeTypeEntity): Long

    @Delete
    suspend fun delete(bikeType: BikeTypeEntity)
}
