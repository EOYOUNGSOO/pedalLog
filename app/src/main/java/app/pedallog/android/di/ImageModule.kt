package app.pedallog.android.di

import app.pedallog.android.data.image.OsmDroidRouteImageGenerator
import app.pedallog.android.data.image.RouteImageGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageModule {

    @Binds
    @Singleton
    abstract fun bindRouteImageGenerator(
        impl: OsmDroidRouteImageGenerator
    ): RouteImageGenerator
}
