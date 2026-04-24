package app.pedallog.android.domain.repository

import app.pedallog.android.data.db.entity.RidingTemplateEntity
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<RidingTemplateEntity>>
    suspend fun getTemplateById(id: Long): RidingTemplateEntity?
    suspend fun upsertTemplate(template: RidingTemplateEntity): Long
    suspend fun deleteTemplate(template: RidingTemplateEntity)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
    suspend fun updateSortOrder(id: Long, order: Int)
}
