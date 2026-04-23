package app.pedalLog.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WaypointsTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromWaypoints(waypoints: List<String>): String = gson.toJson(waypoints)

    @TypeConverter
    fun toWaypoints(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(raw, type)
    }
}
