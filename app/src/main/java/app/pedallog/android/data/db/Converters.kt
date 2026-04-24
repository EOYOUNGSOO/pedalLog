package app.pedallog.android.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> = value?.split("|") ?: emptyList()

    @TypeConverter
    fun toString(value: List<String>?): String = value?.joinToString("|") ?: ""
}
