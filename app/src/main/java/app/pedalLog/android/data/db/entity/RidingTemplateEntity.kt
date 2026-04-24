package app.pedallog.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riding_templates")
data class RidingTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateName: String,
    val departure: String? = null,
    val waypoints: String? = null,
    val destination: String? = null,
    val bikeType: String? = null,
    val defaultMemo: String? = null,
    val sortOrder: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
