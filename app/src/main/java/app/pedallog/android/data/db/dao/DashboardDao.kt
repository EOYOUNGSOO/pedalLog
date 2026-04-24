package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    @Query(
        """
        SELECT
            strftime('%Y', datetime(startTime/1000,'unixepoch','localtime'))
                AS year,
            COUNT(*)                              AS rideCount,
            SUM(totalDistanceM) / 1000.0          AS totalDistKm,
            SUM((endTime - startTime) / 60000)    AS totalMinutes,
            SUM(COALESCE(calories, 0))            AS totalCalories,
            AVG(avgSpeedKmh)                      AS avgSpeed
        FROM riding_sessions
        GROUP BY year
        ORDER BY year ASC
        """
    )
    fun getAnnualStats(): Flow<List<AnnualStatResult>>

    @Query(
        """
        SELECT
            strftime('%m', datetime(startTime/1000,'unixepoch','localtime'))
                AS month,
            COUNT(*)                              AS rideCount,
            SUM(totalDistanceM) / 1000.0          AS totalDistKm,
            SUM(COALESCE(calories, 0))            AS totalCalories
        FROM riding_sessions
        WHERE strftime('%Y', datetime(startTime/1000,'unixepoch','localtime'))
              = :year
        GROUP BY month
        ORDER BY month ASC
        """
    )
    fun getMonthlyStats(year: String): Flow<List<MonthlyStatResult>>

    @Query(
        """
        SELECT
            strftime('%Y', datetime(startTime/1000,'unixepoch','localtime'))
                AS year,
            strftime('%m', datetime(startTime/1000,'unixepoch','localtime'))
                AS month,
            COUNT(*) AS rideCount
        FROM riding_sessions
        GROUP BY year, month
        ORDER BY year ASC, month ASC
        """
    )
    fun getHeatmapData(): Flow<List<HeatmapResult>>

    @Query(
        """
        SELECT
            title,
            COUNT(*)                              AS rideCount,
            AVG(avgSpeedKmh)                      AS avgSpeed,
            MAX(maxSpeedKmh)                      AS bestSpeed,
            AVG(totalDistanceM) / 1000.0          AS avgDistKm,
            AVG(COALESCE(calories, 0))            AS avgCalories,
            AVG(COALESCE(elevationUp, 0))         AS avgElevation
        FROM riding_sessions
        GROUP BY title
        ORDER BY avgSpeed DESC
        """
    )
    fun getCourseStats(): Flow<List<CourseStatResult>>

    @Query(
        """
        SELECT
            strftime('%m', datetime(startTime/1000,'unixepoch','localtime'))
                AS month,
            SUM(COALESCE(calories, 0))    AS totalCalories,
            AVG(COALESCE(calories, 0))    AS avgCalories,
            AVG(COALESCE(
                CAST(calories AS REAL) /
                CAST((endTime - startTime) / 60000 AS REAL)
            , 0)) AS avgCalPerMin
        FROM riding_sessions
        WHERE strftime('%Y', datetime(startTime/1000,'unixepoch','localtime'))
              = :year
        GROUP BY month
        ORDER BY month ASC
        """
    )
    fun getCalorieStatsByYear(year: String): Flow<List<CalorieStatResult>>

    @Query(
        """
        SELECT
            strftime('%Y', datetime(startTime/1000,'unixepoch','localtime'))
                AS year,
            SUM((endTime - startTime) / 60000)    AS totalMinutes,
            AVG((endTime - startTime) / 60000)    AS avgMinutes,
            COUNT(*)                              AS rideCount
        FROM riding_sessions
        GROUP BY year
        ORDER BY year ASC
        """
    )
    fun getTimeStatsByYear(): Flow<List<TimeStatResult>>

    @Query(
        """
        SELECT
            title,
            AVG(avgSpeedKmh)                      AS avgSpeed,
            AVG(COALESCE(avgHeartRate, 0))        AS avgHeartRate,
            AVG(COALESCE(elevationUp, 0))         AS avgElevation,
            AVG(COALESCE(
                CAST(calories AS REAL) /
                CAST((endTime - startTime) / 60000 AS REAL)
            , 0)) AS avgCalPerMin
        FROM riding_sessions
        WHERE avgHeartRate IS NOT NULL
        GROUP BY title
        ORDER BY avgHeartRate DESC
        """
    )
    fun getIntensityStats(): Flow<List<IntensityStatResult>>

    @Query(
        """
        SELECT
            COUNT(*)                              AS totalRides,
            SUM(totalDistanceM) / 1000.0          AS totalDistKm,
            SUM((endTime - startTime) / 60000)    AS totalMinutes,
            SUM(COALESCE(calories, 0))            AS totalCalories
        FROM riding_sessions
        """
    )
    fun getTotalStats(): Flow<TotalStatResult>

    @Query(
        """
        SELECT
            COUNT(*)                              AS totalRides,
            SUM(totalDistanceM) / 1000.0          AS totalDistKm,
            SUM((endTime - startTime) / 60000)    AS totalMinutes,
            SUM(COALESCE(calories, 0))            AS totalCalories
        FROM riding_sessions
        WHERE strftime('%Y-%m', datetime(startTime/1000,'unixepoch','localtime'))
              = strftime('%Y-%m', 'now', 'localtime')
        """
    )
    fun getCurrentMonthStats(): Flow<TotalStatResult>
}

data class AnnualStatResult(
    val year: String,
    val rideCount: Int,
    val totalDistKm: Double,
    val totalMinutes: Long,
    val totalCalories: Int,
    val avgSpeed: Double
)

data class MonthlyStatResult(
    val month: String,
    val rideCount: Int,
    val totalDistKm: Double,
    val totalCalories: Int
)

data class HeatmapResult(
    val year: String,
    val month: String,
    val rideCount: Int
)

data class CourseStatResult(
    val title: String,
    val rideCount: Int,
    val avgSpeed: Double,
    val bestSpeed: Double,
    val avgDistKm: Double,
    val avgCalories: Double,
    val avgElevation: Double
)

data class CalorieStatResult(
    val month: String,
    val totalCalories: Int,
    val avgCalories: Double,
    val avgCalPerMin: Double
)

data class TimeStatResult(
    val year: String,
    val totalMinutes: Long,
    val avgMinutes: Double,
    val rideCount: Int
)

data class IntensityStatResult(
    val title: String,
    val avgSpeed: Double,
    val avgHeartRate: Double,
    val avgElevation: Double,
    val avgCalPerMin: Double
)

data class TotalStatResult(
    val totalRides: Int,
    val totalDistKm: Double,
    val totalMinutes: Long,
    val totalCalories: Int
)
