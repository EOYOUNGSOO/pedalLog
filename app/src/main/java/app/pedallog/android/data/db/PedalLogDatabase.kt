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
}
