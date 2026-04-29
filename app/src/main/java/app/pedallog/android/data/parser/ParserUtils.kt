package app.pedallog.android.data.parser

import app.pedallog.android.data.model.ParseResult
import app.pedallog.android.data.model.TrackPointData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ParserUtils {

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

    fun parseDate(dateStr: String): Long {
        for (format in dateFormats) {
            try {
                return format.parse(dateStr)?.time
                    ?: throw ParseException("날짜 파싱 실패: $dateStr")
            } catch (_: Exception) {
                continue
            }
        }
        throw ParseException("날짜 파싱 실패: $dateStr")
    }

    fun buildParseResult(
        title: String,
        startTimeStr: String?,
        trackPoints: List<TrackPointData>,
        format: String,
        totalDistanceM: Double? = null,
        movingTimeSec: Double? = null,
        calories: Int? = null,
        maxSpeedKmh: Double? = null,
        avgHeartRate: Int? = null,
        maxHeartRate: Int? = null,
        avgCadence: Int? = null
    ): ParseResult {
        val startTime = startTimeStr?.let { parseDate(it) } ?: trackPoints.first().timestamp
        val endTime = trackPoints.last().timestamp

        val distanceM = totalDistanceM ?: calcDistance(trackPoints)
        val durationSec = movingTimeSec ?: ((endTime - startTime) / 1000.0)
        val avgSpeed = if (durationSec > 0) (distanceM / durationSec) * 3.6 else 0.0
        val maxSpeed = maxSpeedKmh ?: trackPoints.mapNotNull { it.speedKmh }.maxOrNull() ?: 0.0

        val heartRates = trackPoints.mapNotNull { it.heartRate }
        val cadences = trackPoints.mapNotNull { it.cadence }

        return ParseResult(
            title = title,
            startTime = startTime,
            endTime = endTime,
            totalDistanceM = distanceM,
            avgSpeedKmh = "%.1f".format(Locale.US, avgSpeed).toDouble(),
            maxSpeedKmh = "%.1f".format(Locale.US, maxSpeed).toDouble(),
            elevationUp = calcElevation(trackPoints),
            calories = calories,
            avgCadence = avgCadence ?: cadences.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            maxCadence = cadences.takeIf { it.isNotEmpty() }?.max(),
            avgHeartRate = avgHeartRate ?: heartRates.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            maxHeartRate = maxHeartRate ?: heartRates.takeIf { it.isNotEmpty() }?.max(),
            trackPoints = trackPoints,
            sourceFormat = format
        )
    }

    fun calcDistance(points: List<TrackPointData>): Double {
        var total = 0.0
        for (i in 1 until points.size) {
            total += haversine(
                points[i - 1].latitude,
                points[i - 1].longitude,
                points[i].latitude,
                points[i].longitude
            )
        }
        return total
    }

    fun calcElevation(points: List<TrackPointData>): Double {
        var total = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1].altitude ?: continue
            val curr = points[i].altitude ?: continue
            val diff = curr - prev
            if (diff > 0.5) total += diff
        }
        return total
    }

    private fun haversine(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusM = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
        return earthRadiusM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

class ParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
