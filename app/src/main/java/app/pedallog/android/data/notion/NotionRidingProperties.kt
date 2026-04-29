package app.pedallog.android.data.notion

data class NotionRidingProperties(
    val title: String,
    val date: String,
    val startTimeStr: String,    // 라이딩 시작시간 (HH:mm:ss 형식)
    val endTimeStr: String,      // 라이딩 종료시간 (HH:mm:ss 형식)
    val distanceKm: Double,
    val durationMin: Double,
    val avgSpeedKmh: Double,
    val sourceFormat: String,
    val sourceApp: String = "trimm Cycling",

    val maxSpeedKmh: Double? = null,
    val avgCadence: Int? = null,
    val elevationUp: Double? = null,
    val calories: Int? = null,

    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,

    val avgPower: Int? = null,

    val departure: String? = null,
    val waypoints: String? = null,
    val destination: String? = null,
    val bikeType: String? = null,
    val memo: String? = null
)
