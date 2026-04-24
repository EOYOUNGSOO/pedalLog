package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RidingTemplateDao {
    @Query(
        """
        SELECT * FROM riding_templates
        ORDER BY isFavorite DESC, sortOrder ASC
        """
    )
    fun getAllTemplates(): Flow<List<RidingTemplateEntity>>

    @Query("SELECT * FROM riding_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): RidingTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: RidingTemplateEntity): Long

    @Delete
    suspend fun delete(template: RidingTemplateEntity)

    @Query("UPDATE riding_templates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE riding_templates SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)
}
