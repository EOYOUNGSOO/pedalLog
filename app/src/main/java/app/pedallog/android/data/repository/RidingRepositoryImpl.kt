package app.pedallog.android.data.repository

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.domain.repository.RidingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RidingRepositoryImpl @Inject constructor(
    private val dao: RidingSessionDao
) : RidingRepository {

    override fun getAllSessions(): Flow<List<RidingSessionEntity>> = dao.getAllSessions()

    override suspend fun getSessionById(id: Long): RidingSessionEntity? = dao.getSessionById(id)

    override suspend fun saveSession(session: RidingSessionEntity): Long = dao.insert(session)

    override suspend fun updateNotionPageId(id: Long, pageId: String) =
        dao.updateNotionPageId(id, pageId)

    override suspend fun deleteSession(session: RidingSessionEntity) = dao.delete(session)
}
