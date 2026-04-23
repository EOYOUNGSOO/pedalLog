package app.pedalLog.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import app.pedalLog.android.data.db.dao.BikeTypeDao
import app.pedalLog.android.data.db.dao.RidingSessionDao
import app.pedalLog.android.data.db.dao.RidingTemplateDao
import app.pedalLog.android.data.db.dao.TrackPointDao
import app.pedalLog.android.data.db.entity.BikeTypeEntity
import app.pedalLog.android.data.db.entity.RidingSessionEntity
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import app.pedalLog.android.data.db.entity.TrackPointEntity
import app.pedalLog.android.data.db.prepopulate.DefaultTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        RidingSessionEntity::class,
        TrackPointEntity::class,
        RidingTemplateEntity::class,
        BikeTypeEntity::class
    ],
    version = 1
)
@TypeConverters(WaypointsTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ridingSessionDao(): RidingSessionDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun ridingTemplateDao(): RidingTemplateDao
    abstract fun bikeTypeDao(): BikeTypeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pedal_log.db"
                ).addCallback(prepopulateCallback)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val prepopulateCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                scope.launch {
                    val instance = INSTANCE ?: return@launch
                    instance.ridingTemplateDao().apply {
                        DefaultTemplates.templates().forEach { insert(it) }
                    }
                    instance.bikeTypeDao().insertAll(DefaultTemplates.bikeTypes())
                }
            }
        }
    }
}
