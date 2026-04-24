package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.notion.NotionRidingProperties
import app.pedallog.android.domain.repository.NotionRepository
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round

class RegisterToNotionUseCase @Inject constructor(
    private val notionRepository: NotionRepository,
    private val sessionDao: RidingSessionDao
) {
    suspend operator fun invoke(sessionId: Long): Result<String> {
        val session = sessionDao.getSessionById(sessionId)
            ?: return Result.failure(Exception("세션을 찾을 수 없습니다. ID: $sessionId"))

        if (session.notionPageId != null) {
            return Result.failure(Exception("이미 Notion에 등록된 라이딩입니다."))
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date(session.startTime))
        val durationMin = (session.endTime - session.startTime) / 60000.0

        val properties = NotionRidingProperties(
            title = session.title,
            date = date,
            distanceKm = round(session.totalDistanceM / 1000.0 * 100) / 100.0,
            durationMin = round(durationMin * 10) / 10.0,
            avgSpeedKmh = round(session.avgSpeedKmh * 10) / 10.0,
            maxSpeedKmh = session.maxSpeedKmh
                .takeIf { it > 0 }
                ?.let { round(it * 10) / 10.0 },
            avgCadence = session.avgCadence,
            elevationUp = session.elevationUp?.let { round(it) },
            calories = session.calories,
            avgHeartRate = session.avgHeartRate,
            maxHeartRate = session.maxHeartRate,
            avgPower = session.avgPower,
            departure = session.departure,
            waypoints = session.waypoints?.let { formatWaypoints(it) },
            destination = session.destination,
            bikeType = session.bikeType,
            memo = session.memo,
            sourceFormat = session.sourceFormat,
            sourceApp = "trimm Cycling"
        )

        val routeImageFile = session.routeImagePath
            ?.let { File(it) }
            ?.takeIf { it.exists() }

        return notionRepository.registerRiding(
            sessionId = sessionId,
            properties = properties,
            routeImageFile = routeImageFile
        )
    }

    private fun formatWaypoints(waypointsJson: String): String? {
        return try {
            val arr = JSONArray(waypointsJson)
            (0 until arr.length()).map { arr.getString(it) }
                .joinToString(", ")
                .takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }
}
