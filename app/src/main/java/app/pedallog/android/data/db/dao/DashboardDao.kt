package app.pedallog.android.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query(
        """
        SELECT strftime('%Y', datetime(startTime/1000,'unixepoch','localtime')) AS year,
               SUM(totalDistanceM)/1000.0 AS totalDistKm,
               COUNT(*) AS rideCount,
               SUM((endTime - startTime)/60000) AS totalMinutes,
               SUM(COALESCE(calories, 0)) AS totalCalories
        FROM riding_sessions
        GROUP BY year
        ORDER BY year ASC
        """
    )
    fun getAnnualStats(): Flow<List<AnnualStatResult>>

    @Query(
        """
        SELECT strftime('%m', datetime(startTime/1000,'unixepoch','localtime')) AS month,
               COUNT(*) AS rideCount,
               SUM(totalDistanceM)/1000.0 AS totalDistKm
        FROM riding_sessions
        WHERE strftime('%Y', datetime(startTime/1000,'unixepoch','localtime')) = :year
        GROUP BY month
        ORDER BY month ASC
        """
    )
    fun getMonthlyStats(year: String): Flow<List<MonthlyStatResult>>

    @Query(
        """
        SELECT title,
               AVG(avgSpeedKmh) AS avgSpeed,
               COUNT(*) AS rideCount,
               AVG(COALESCE(calories, 0)) AS avgCalories,
               AVG(COALESCE(totalDistanceM, 0)/1000.0) AS avgDistKm
        FROM riding_sessions
        GROUP BY title
        ORDER BY avgSpeed DESC
        """
    )
    fun getCourseStats(): Flow<List<CourseStatResult>>

    @Query(
        """
        SELECT SUM(totalDistanceM)/1000.0 AS totalDistKm,
               SUM((endTime - startTime)/60000) AS totalMinutes,
               COUNT(*) AS totalRides,
               SUM(COALESCE(calories, 0)) AS totalCalories
        FROM riding_sessions
        """
    )
    fun getTotalStats(): Flow<TotalStatResult>
}

data class AnnualStatResult(
    val year: String,
    val totalDistKm: Double,
    val rideCount: Int,
    val totalMinutes: Long,
    val totalCalories: Int
)

data class MonthlyStatResult(
    val month: String,
    val rideCount: Int,
    val totalDistKm: Double
)

data class CourseStatResult(
    val title: String,
    val avgSpeed: Double,
    val rideCount: Int,
    val avgCalories: Double,
    val avgDistKm: Double
)

data class TotalStatResult(
    val totalDistKm: Double,
    val totalMinutes: Long,
    val totalRides: Int,
    val totalCalories: Int
)
