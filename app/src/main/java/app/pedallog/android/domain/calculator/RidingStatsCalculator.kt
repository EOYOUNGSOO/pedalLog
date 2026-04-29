package app.pedallog.android.domain.calculator

import app.pedallog.android.data.db.entity.TrackPointEntity
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * TrackPoint RAW 데이터 기반 라이딩 통계 계산기.
 *
 * - 거리: Haversine
 * - 이동시간: 속도 임계 + 시간 갭 복합 기준
 * - 노이즈: 비정상 고속 구간 제거
 * - 고도: 미세 노이즈 임계값 적용 후 누적
 */
object RidingStatsCalculator {
    private const val MOVING_SPEED_THRESHOLD_KMH = 1.5
    private const val STOPPED_GAP_SECONDS = 20L
    private const val GPS_NOISE_SPEED_KMH = 100.0
    private const val ELEVATION_NOISE_THRESHOLD_M = 2.0
    private const val EARTH_RADIUS_M = 6_371_000.0

    data class RidingStats(
        val movingTimeSec: Long,
        val elapsedTimeSec: Long,
        val totalDistanceM: Double,
        val avgSpeedKmh: Double,
        val maxSpeedKmh: Double,
        val elevationUpM: Double,
        val elevationDownM: Double,
        val avgCadenceRpm: Int,
        val maxCadenceRpm: Int,
        val avgHeartRateBpm: Int,
        val maxHeartRateBpm: Int,
        val validPointCount: Int,
        val filteredPointCount: Int
    )

    fun calculate(
        points: List<TrackPointEntity>,
        deviceMaxSpeedKmh: Double? = null,
        deviceDistanceM: Double? = null
    ): RidingStats {
        if (points.size < 2) return emptyStats()

        val (cleanPoints, filteredCount) = filterGpsNoise(points)
        if (cleanPoints.size < 2) return emptyStats(filteredCount)

        var totalDistM = 0.0
        var movingTimeSec = 0L
        var maxSpeedKmh = 0.0

        var elevationUp = 0.0
        var elevationDown = 0.0
        var prevValidAlt: Double? = cleanPoints.firstOrNull { it.altitude != null }?.altitude

        var heartRateSum = 0L
        var heartRateCount = 0
        var maxHeartRate = 0
        var cadenceSum = 0L
        var cadenceCount = 0
        var maxCadence = 0

        for (i in 1 until cleanPoints.size) {
            val prev = cleanPoints[i - 1]
            val curr = cleanPoints[i]

            val dtSec = (curr.timestamp - prev.timestamp) / 1000L
            if (dtSec <= 0) continue

            val segDist = haversineM(prev.latitude, prev.longitude, curr.latitude, curr.longitude)
            val segSpeedKmh = if (dtSec > 0) (segDist / dtSec) * 3.6 else 0.0
            val isStopped = segSpeedKmh < MOVING_SPEED_THRESHOLD_KMH || dtSec >= STOPPED_GAP_SECONDS

            if (!isStopped) {
                totalDistM += segDist
                movingTimeSec += dtSec
                if (segSpeedKmh > maxSpeedKmh) maxSpeedKmh = segSpeedKmh
            }

            val currAlt = curr.altitude
            if (currAlt != null && prevValidAlt != null) {
                val diff = currAlt - prevValidAlt
                if (abs(diff) >= ELEVATION_NOISE_THRESHOLD_M) {
                    if (diff > 0) elevationUp += diff else elevationDown += abs(diff)
                    prevValidAlt = currAlt
                }
            } else if (currAlt != null) {
                prevValidAlt = currAlt
            }

            val hr = curr.heartRate
            if (hr != null && hr > 0) {
                heartRateSum += hr
                heartRateCount++
                if (hr > maxHeartRate) maxHeartRate = hr
            }

            val cad = curr.cadence
            if (!isStopped && cad != null && cad > 0) {
                cadenceSum += cad
                cadenceCount++
                if (cad > maxCadence) maxCadence = cad
            }
        }

        val finalDistM = deviceDistanceM ?: totalDistM
        val finalMaxSpeed = deviceMaxSpeedKmh ?: maxSpeedKmh
        val avgSpeedKmh = if (movingTimeSec > 0) (finalDistM / movingTimeSec) * 3.6 else 0.0

        val totalElapsed = (cleanPoints.last().timestamp - cleanPoints.first().timestamp) / 1000L

        return RidingStats(
            movingTimeSec = movingTimeSec,
            elapsedTimeSec = totalElapsed,
            totalDistanceM = finalDistM.roundTo(1),
            avgSpeedKmh = avgSpeedKmh.roundTo(1),
            maxSpeedKmh = finalMaxSpeed.roundTo(1),
            elevationUpM = elevationUp.roundTo(1),
            elevationDownM = elevationDown.roundTo(1),
            avgCadenceRpm = if (cadenceCount > 0) (cadenceSum / cadenceCount).toInt() else 0,
            maxCadenceRpm = maxCadence,
            avgHeartRateBpm = if (heartRateCount > 0) (heartRateSum / heartRateCount).toInt() else 0,
            maxHeartRateBpm = maxHeartRate,
            validPointCount = cleanPoints.size,
            filteredPointCount = filteredCount
        )
    }

    private fun filterGpsNoise(points: List<TrackPointEntity>): Pair<List<TrackPointEntity>, Int> {
        if (points.size < 3) return Pair(points, 0)

        val result = mutableListOf(points.first())
        var removed = 0

        for (i in 1 until points.size - 1) {
            val prev = result.last()
            val curr = points[i]
            val next = points[i + 1]

            val dtPrev = (curr.timestamp - prev.timestamp) / 1000.0
            val dtNext = (next.timestamp - curr.timestamp) / 1000.0

            if (dtPrev <= 0 || dtNext <= 0) {
                result.add(curr)
                continue
            }

            val speedPrev = haversineM(prev.latitude, prev.longitude, curr.latitude, curr.longitude) / dtPrev * 3.6
            val speedNext = haversineM(curr.latitude, curr.longitude, next.latitude, next.longitude) / dtNext * 3.6

            if (speedPrev > GPS_NOISE_SPEED_KMH && speedNext > GPS_NOISE_SPEED_KMH) {
                removed++
            } else {
                result.add(curr)
            }
        }
        result.add(points.last())
        return Pair(result, removed)
    }

    fun haversineM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return round(this * factor) / factor
    }

    private fun emptyStats(filteredCount: Int = 0) = RidingStats(
        movingTimeSec = 0L,
        elapsedTimeSec = 0L,
        totalDistanceM = 0.0,
        avgSpeedKmh = 0.0,
        maxSpeedKmh = 0.0,
        elevationUpM = 0.0,
        elevationDownM = 0.0,
        avgCadenceRpm = 0,
        maxCadenceRpm = 0,
        avgHeartRateBpm = 0,
        maxHeartRateBpm = 0,
        validPointCount = 0,
        filteredPointCount = filteredCount
    )
}

