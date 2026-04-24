package app.pedallog.android.domain.repository

import app.pedallog.android.data.db.entity.RidingSessionEntity
import kotlinx.coroutines.flow.Flow

interface RidingRepository {
    fun getAllSessions(): Flow<List<RidingSessionEntity>>
    suspend fun getSessionById(id: Long): RidingSessionEntity?
    suspend fun saveSession(session: RidingSessionEntity): Long
    suspend fun updateNotionPageId(id: Long, pageId: String)
    suspend fun deleteSession(session: RidingSessionEntity)
}
