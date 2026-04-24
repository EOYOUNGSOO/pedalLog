package app.pedallog.android.di

import android.content.Context
import androidx.room.Room
import app.pedallog.android.data.db.PedalLogDatabase
import app.pedallog.android.data.db.dao.BikeTypeDao
import app.pedallog.android.data.db.dao.DashboardDao
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.dao.TrackPointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PedalLogDatabase =
        Room.databaseBuilder(
            context,
            PedalLogDatabase::class.java,
            PedalLogDatabase.DB_NAME
        )
            // 출시 전: .addMigrations(...) 로 교체하고 fallbackToDestructiveMigration 제거
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRidingSessionDao(db: PedalLogDatabase): RidingSessionDao = db.ridingSessionDao()

    @Provides
    fun provideTrackPointDao(db: PedalLogDatabase): TrackPointDao = db.trackPointDao()

    @Provides
    fun provideTemplateDao(db: PedalLogDatabase): RidingTemplateDao = db.ridingTemplateDao()

    @Provides
    fun provideBikeTypeDao(db: PedalLogDatabase): BikeTypeDao = db.bikeTypeDao()

    @Provides
    fun provideDashboardDao(db: PedalLogDatabase): DashboardDao = db.dashboardDao()
}
