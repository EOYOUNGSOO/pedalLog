package app.pedallog.android.di

import app.pedallog.android.data.repository.NotionRepositoryImpl
import app.pedallog.android.data.repository.RidingRepositoryImpl
import app.pedallog.android.data.repository.TemplateRepositoryImpl
import app.pedallog.android.domain.repository.NotionRepository
import app.pedallog.android.domain.repository.RidingRepository
import app.pedallog.android.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRidingRepository(
        impl: RidingRepositoryImpl
    ): RidingRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        impl: TemplateRepositoryImpl
    ): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindNotionRepository(
        impl: NotionRepositoryImpl
    ): NotionRepository
}
