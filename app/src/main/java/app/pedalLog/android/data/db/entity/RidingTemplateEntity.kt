package app.pedalLog.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riding_templates")
data class RidingTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val courseName: String,
    val departure: String,
    val destination: String,
    val waypoints: List<String>,
    val bikeType: String,
    val defaultNote: String,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0
)
