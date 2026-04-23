package app.pedalLog.android.data.repository

import app.pedalLog.android.data.db.dao.BikeTypeDao
import app.pedalLog.android.data.db.dao.RidingTemplateDao
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import kotlinx.coroutines.flow.Flow

class TemplateRepository(
    private val templateDao: RidingTemplateDao,
    private val bikeTypeDao: BikeTypeDao
) {
    fun observeTemplates(): Flow<List<RidingTemplateEntity>> = templateDao.observeAll()

    fun observeBikeTypes() = bikeTypeDao.observeAll()

    suspend fun upsert(template: RidingTemplateEntity) {
        if (template.id == 0L) {
            templateDao.insert(template)
        } else {
            templateDao.update(template)
        }
    }

    suspend fun delete(template: RidingTemplateEntity) = templateDao.delete(template)

    suspend fun toggleFavorite(template: RidingTemplateEntity) {
        templateDao.updateFavorite(template.id, !template.isFavorite)
    }

    suspend fun reorder(templates: List<RidingTemplateEntity>) {
        templates.forEachIndexed { index, item ->
            if (item.sortOrder != index) {
                templateDao.update(item.copy(sortOrder = index))
            }
        }
    }

    suspend fun getById(id: Long) = templateDao.getById(id)
    suspend fun getAllOrdered() = templateDao.getAllOrdered()
}
