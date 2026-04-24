package app.pedallog.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bike_types")
data class BikeTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val typeName: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
