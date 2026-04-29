package app.pedallog.android.data.model

data class ParseResult(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val totalDistanceM: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val elevationUp: Double,
    val calories: Int?,
    val avgCadence: Int?,
    val maxCadence: Int?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
    val trackPoints: List<TrackPointData>,
    val sourceFormat: String
) {
    val durationMin: Double
        get() = (endTime - startTime) / 60000.0

    val validTrackPoints: List<TrackPointData>
        get() = trackPoints.filter {
            it.latitude != 0.0 && it.longitude != 0.0
        }
}

data class TrackPointData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val speedKmh: Double?,
    val cadence: Int?,
    val heartRate: Int?,
    val timestamp: Long
)
