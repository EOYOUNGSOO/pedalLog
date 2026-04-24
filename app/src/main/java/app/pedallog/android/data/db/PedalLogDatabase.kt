package app.pedallog.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.pedallog.android.data.db.dao.BikeTypeDao
import app.pedallog.android.data.db.dao.DashboardDao
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.dao.TrackPointDao
import app.pedallog.android.data.db.entity.BikeTypeEntity
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.data.db.entity.TrackPointEntity

// Phase별 DB 버전 이력:
// v1: P1 초기 스키마 (4개 테이블)
// v2 예정: 스키마 변경 시 Migration (예: 신규 컬럼 — P2/P3 nullable 컬럼은 v1에 이미 포함)
// v3 예정: 필요 시 추가 Migration
@Database(
    entities = [
        RidingSessionEntity::class,
        TrackPointEntity::class,
        RidingTemplateEntity::class,
        BikeTypeEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PedalLogDatabase : RoomDatabase() {
    abstract fun ridingSessionDao(): RidingSessionDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun ridingTemplateDao(): RidingTemplateDao
    abstract fun bikeTypeDao(): BikeTypeDao
    abstract fun dashboardDao(): DashboardDao

    companion object {
        const val DB_NAME = "pedallog.db"
        const val DB_VERSION = 1
    }
}
