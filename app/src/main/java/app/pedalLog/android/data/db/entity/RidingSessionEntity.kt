package app.pedallog.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "riding_sessions",
    indices = [
        Index("startTime"),
        Index("title"),
        Index("notionPageId"),
    ]
)
data class RidingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val startTime: Long,
    val endTime: Long,

    val totalDistanceM: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val elevationUp: Double? = null,
    val calories: Int? = null,
    val avgCadence: Int? = null,
    val maxCadence: Int? = null,

    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,

    val avgPower: Int? = null,
    val maxPower: Int? = null,

    val sourceFormat: String,

    val templateId: Long? = null,
    val departure: String? = null,
    val waypoints: String? = null,
    val destination: String? = null,
    val bikeType: String? = null,
    val memo: String? = null,

    val routeImagePath: String? = null,

    val notionPageId: String? = null,
    val notionRegisteredAt: Long? = null,

    val createdAt: Long = System.currentTimeMillis()
)
