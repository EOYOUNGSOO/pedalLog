package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.TrackPointDao
import app.pedallog.android.data.image.LatLng
import app.pedallog.android.data.image.RouteImageGenerator
import java.io.File
import javax.inject.Inject

class GenerateRouteImageUseCase @Inject constructor(
    private val trackPointDao: TrackPointDao,
    private val sessionDao: RidingSessionDao,
    private val generator: RouteImageGenerator
) {
    suspend operator fun invoke(sessionId: Long): File? {
        val trackPoints = trackPointDao.getTrackPointsBySession(sessionId)
        if (trackPoints.size < 2) return null

        val latLngs = trackPoints.map { tp ->
            LatLng(
                latitude = tp.latitude,
                longitude = tp.longitude
            )
        }

        val imageFile = generator.generate(
            sessionId = sessionId,
            trackPoints = latLngs
        ) ?: return null

        sessionDao.getSessionById(sessionId)?.let { session ->
            sessionDao.update(session.copy(routeImagePath = imageFile.absolutePath))
        }
        return imageFile
    }
}
