package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.TrackPointDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.TrackPointEntity
import app.pedallog.android.data.model.ReceivedFile
import app.pedallog.android.data.parser.RidingFileParser
import app.pedallog.android.domain.calculator.RidingStatsCalculator
import javax.inject.Inject

enum class ParseProcessingStep {
    PARSING,
    IMAGING
}

class ParseRidingFileUseCase @Inject constructor(
    private val parser: RidingFileParser,
    private val sessionDao: RidingSessionDao,
    private val trackPointDao: TrackPointDao,
    private val generateRouteImageUseCase: GenerateRouteImageUseCase
) {
    companion object {
        private const val DUPLICATE_DISTANCE_TOLERANCE_M = 50.0
    }

    suspend operator fun invoke(
        receivedFile: ReceivedFile,
        onStepChanged: ((ParseProcessingStep) -> Unit)? = null
    ): Result<Long> {
        onStepChanged?.invoke(ParseProcessingStep.PARSING)
        val parseResult = parser.parse(receivedFile).getOrElse {
            return Result.failure(it)
        }

        // 중복 감지 시 기존 세션을 덮어씁니다 (기존 notionPageId 유지)
        val duplicate = sessionDao.findDuplicateSession(
            startTime = parseResult.startTime,
            endTime = parseResult.endTime,
            sourceFormat = parseResult.sourceFormat,
            distanceM = parseResult.totalDistanceM,
            distanceToleranceM = DUPLICATE_DISTANCE_TOLERANCE_M
        )

        val calculatedStats = RidingStatsCalculator.calculate(
            points = parseResult.validTrackPoints.map { tp ->
                TrackPointEntity(
                    sessionId = 0L,
                    latitude = tp.latitude,
                    longitude = tp.longitude,
                    altitude = tp.altitude,
                    speedKmh = tp.speedKmh,
                    cadence = tp.cadence,
                    heartRate = tp.heartRate,
                    timestamp = tp.timestamp
                )
            },
            deviceMaxSpeedKmh = parseResult.maxSpeedKmh.takeIf { it > 0 },
            deviceDistanceM = parseResult.totalDistanceM.takeIf { it > 0 }
        )

        val newSession = RidingSessionEntity(
            title = parseResult.title,
            startTime = parseResult.startTime,
            endTime = parseResult.endTime,
            totalDistanceM = calculatedStats.totalDistanceM,
            avgSpeedKmh = calculatedStats.avgSpeedKmh,
            maxSpeedKmh = calculatedStats.maxSpeedKmh,
            elevationUp = calculatedStats.elevationUpM.takeIf { it > 0 } ?: parseResult.elevationUp,
            calories = parseResult.calories,
            avgCadence = calculatedStats.avgCadenceRpm.takeIf { it > 0 } ?: parseResult.avgCadence,
            maxCadence = calculatedStats.maxCadenceRpm.takeIf { it > 0 } ?: parseResult.maxCadence,
            avgHeartRate = calculatedStats.avgHeartRateBpm.takeIf { it > 0 } ?: parseResult.avgHeartRate,
            maxHeartRate = calculatedStats.maxHeartRateBpm.takeIf { it > 0 } ?: parseResult.maxHeartRate,
            sourceFormat = parseResult.sourceFormat,
            // 기존 notionPageId / 등록일시 / 템플릿 정보 승계
            notionPageId = duplicate?.notionPageId,
            notionRegisteredAt = duplicate?.notionRegisteredAt,
            templateId = duplicate?.templateId,
            departure = duplicate?.departure,
            waypoints = duplicate?.waypoints,
            destination = duplicate?.destination,
            bikeType = duplicate?.bikeType,
            memo = duplicate?.memo
        )

        val sessionId: Long
        if (duplicate != null) {
            // 기존 Row ID 유지하여 update
            val updated = newSession.copy(id = duplicate.id, createdAt = duplicate.createdAt)
            sessionDao.update(updated)
            // 기존 트랙 포인트 삭제 후 재삽입
            trackPointDao.deleteBySession(duplicate.id)
            sessionId = duplicate.id
        } else {
            sessionId = sessionDao.insert(newSession)
        }

        val trackPointEntities = parseResult.validTrackPoints.map { tp ->
            TrackPointEntity(
                sessionId = sessionId,
                latitude = tp.latitude,
                longitude = tp.longitude,
                altitude = tp.altitude,
                speedKmh = tp.speedKmh,
                cadence = tp.cadence,
                heartRate = tp.heartRate,
                timestamp = tp.timestamp
            )
        }

        trackPointDao.insertAll(trackPointEntities)
        onStepChanged?.invoke(ParseProcessingStep.IMAGING)
        generateRouteImageUseCase(sessionId)
        return Result.success(sessionId)
    }
}

class DuplicateRidingException(
    val existingSessionId: Long,
    val existingTitle: String,
    val existingStartTime: Long,
    val existingEndTime: Long,
    val existingDistanceM: Double,
    val existingAvgSpeedKmh: Double,
    val existingSourceFormat: String,
    override val message: String
) : Exception(message)
