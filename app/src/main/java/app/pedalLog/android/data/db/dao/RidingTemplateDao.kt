package app.pedalLog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RidingTemplateDao {
    @Query("SELECT * FROM riding_templates ORDER BY isFavorite DESC, sortOrder ASC")
    fun observeAll(): Flow<List<RidingTemplateEntity>>

    @Query("SELECT * FROM riding_templates ORDER BY isFavorite DESC, sortOrder ASC")
    suspend fun getAllOrdered(): List<RidingTemplateEntity>

    @Query("SELECT * FROM riding_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RidingTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: RidingTemplateEntity): Long

    @Update
    suspend fun update(template: RidingTemplateEntity)

    @Delete
    suspend fun delete(template: RidingTemplateEntity)

    @Query("UPDATE riding_templates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
