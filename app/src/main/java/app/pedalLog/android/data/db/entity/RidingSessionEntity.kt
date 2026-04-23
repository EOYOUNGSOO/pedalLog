package app.pedalLog.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riding_sessions")
data class RidingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val courseName: String,
    val departure: String,
    val destination: String,
    val waypoints: List<String>,
    val bikeType: String,
    val note: String,
    val startedAt: Long
)
