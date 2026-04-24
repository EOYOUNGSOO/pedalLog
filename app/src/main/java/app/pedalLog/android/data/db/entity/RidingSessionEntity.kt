package app.pedallog.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riding_sessions")
data class RidingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val totalDistanceM: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val avgCadence: Int? = null,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val elevationUp: Double? = null,
    val calories: Int? = null,
    val avgPower: Int? = null,
    val sourceFormat: String,
    val routeImagePath: String? = null,
    val notionPageId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
