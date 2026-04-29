package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.notion.NotionRidingProperties
import app.pedallog.android.domain.repository.NotionRepository
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class RegisterToNotionUseCase @Inject constructor(
    private val notionRepository: NotionRepository,
    private val sessionDao: RidingSessionDao
) {
    companion object {
        private const val DUPLICATE_DISTANCE_TOLERANCE_M = 50.0
    }

    suspend operator fun invoke(sessionId: Long): Result<String> {
        val session = sessionDao.getSessionById(sessionId)
            ?: return Result.failure(Exception("세션을 찾을 수 없습니다 (ID: $sessionId)"))

        if (session.notionPageId != null) {
            return Result.failure(
                Exception("이미 Notion에 등록된 라이딩입니다.\n(PageID: ${session.notionPageId})")
            )
        }

        val alreadyRegisteredDuplicate = sessionDao.findRegisteredDuplicate(
            currentSessionId = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            sourceFormat = session.sourceFormat,
            distanceM = session.totalDistanceM,
            distanceToleranceM = DUPLICATE_DISTANCE_TOLERANCE_M
        )
        if (alreadyRegisteredDuplicate != null) {
            return Result.failure(
                Exception(
                    "동일한 라이딩이 이미 Notion에 등록되어 있습니다.\n" +
                        "(기존 PageID: ${alreadyRegisteredDuplicate.notionPageId})"
                )
            )
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(session.startTime))
        val durationMin = (session.endTime - session.startTime) / 60000.0

        val properties = NotionRidingProperties(
            title = session.title,
            date = date,
            distanceKm = Math.round(session.totalDistanceM / 1000.0 * 10) / 10.0,
            durationMin = Math.round(durationMin * 10) / 10.0,
            avgSpeedKmh = Math.round(session.avgSpeedKmh * 10) / 10.0,
            maxSpeedKmh = session.maxSpeedKmh
                .takeIf { it > 0 }
                ?.let { Math.round(it * 10) / 10.0 },
            avgCadence = session.avgCadence,
            elevationUp = session.elevationUp?.let { Math.round(it * 10) / 10.0 },
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
