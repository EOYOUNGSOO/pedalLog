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

    @Query("SELECT * FROM bike_types ORDER BY sortOrder ASC")
    fun getAllBikeTypes(): Flow<List<BikeTypeEntity>>

    @Query("SELECT * FROM bike_types WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBikeType(): BikeTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<BikeTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(type: BikeTypeEntity): Long

    @Delete
    suspend fun delete(type: BikeTypeEntity)
}
