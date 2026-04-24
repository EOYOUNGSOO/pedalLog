package app.pedallog.android.data.repository

import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepositoryImpl @Inject constructor(
    private val dao: RidingTemplateDao
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<RidingTemplateEntity>> = dao.getAllTemplates()

    override suspend fun getTemplateById(id: Long): RidingTemplateEntity? = dao.getTemplateById(id)

    override suspend fun upsertTemplate(template: RidingTemplateEntity): Long = dao.upsert(template)

    override suspend fun deleteTemplate(template: RidingTemplateEntity) = dao.delete(template)

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean) =
        dao.updateFavorite(id, isFavorite)

    override suspend fun updateSortOrder(id: Long, order: Int) = dao.updateSortOrder(id, order)
}
