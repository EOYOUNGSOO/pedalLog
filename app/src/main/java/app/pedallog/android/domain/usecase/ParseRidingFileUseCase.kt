package app.pedallog.android.domain.usecase

import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.TrackPointDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.TrackPointEntity
import app.pedallog.android.data.model.ReceivedFile
import app.pedallog.android.data.parser.RidingFileParser
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

        val duplicate = sessionDao.findDuplicateSession(
            startTime = parseResult.startTime,
            endTime = parseResult.endTime,
            sourceFormat = parseResult.sourceFormat,
            distanceM = parseResult.totalDistanceM,
            distanceToleranceM = DUPLICATE_DISTANCE_TOLERANCE_M
        )
        if (duplicate != null) {
            return Result.failure(
                DuplicateRidingException(
                    existingSessionId = duplicate.id,
                    existingTitle = duplicate.title,
                    existingStartTime = duplicate.startTime,
                    existingEndTime = duplicate.endTime,
                    existingDistanceM = duplicate.totalDistanceM,
                    existingAvgSpeedKmh = duplicate.avgSpeedKmh,
                    existingSourceFormat = duplicate.sourceFormat,
                    message = "이미 같은 라이딩이 등록되어 있습니다.\n중복 등록을 건너뜁니다."
                )
            )
        }

        val sessionId = sessionDao.insert(
            RidingSessionEntity(
                title = parseResult.title,
                startTime = parseResult.startTime,
                endTime = parseResult.endTime,
                totalDistanceM = parseResult.totalDistanceM,
                avgSpeedKmh = parseResult.avgSpeedKmh,
                maxSpeedKmh = parseResult.maxSpeedKmh,
                elevationUp = parseResult.elevationUp,
                calories = parseResult.calories,
                avgCadence = parseResult.avgCadence,
                maxCadence = parseResult.maxCadence,
                avgHeartRate = parseResult.avgHeartRate,
                maxHeartRate = parseResult.maxHeartRate,
                sourceFormat = parseResult.sourceFormat
            )
        )

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
