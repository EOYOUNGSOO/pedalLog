package app.pedalLog.android.data.repository

import app.pedalLog.android.data.db.dao.RidingSessionDao
import app.pedalLog.android.data.db.dao.TrackPointDao
import app.pedalLog.android.data.db.entity.RidingSessionEntity
import app.pedalLog.android.data.db.entity.TrackPointEntity

class RidingRepository(
    private val sessionDao: RidingSessionDao,
    private val trackPointDao: TrackPointDao
) {
    suspend fun saveSession(session: RidingSessionEntity, points: List<TrackPointEntity>) {
        val sessionId = sessionDao.insert(session)
        trackPointDao.insertAll(points.map { it.copy(sessionId = sessionId) })
    }
}
