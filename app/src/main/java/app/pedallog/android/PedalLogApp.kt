package app.pedallog.android

import android.app.Application
import app.pedallog.android.data.db.PedalLogDatabase
import app.pedallog.android.data.db.entity.BikeTypeEntity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PedalLogApp : Application() {

    @Inject
    lateinit var database: PedalLogDatabase

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        val requestConfiguration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
            )
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        GlobalScope.launch(Dispatchers.IO) {
            val count = database.bikeTypeDao().getAllBikeTypes().first().size
            if (count == 0) {
                database.bikeTypeDao().insertAll(
                    listOf(
                        BikeTypeEntity(typeName = "로드자전거", isDefault = true, sortOrder = 0),
                        BikeTypeEntity(typeName = "그래블자전거", isDefault = false, sortOrder = 1),
                        BikeTypeEntity(typeName = "MTB", isDefault = false, sortOrder = 2),
                        BikeTypeEntity(typeName = "미니벨로", isDefault = false, sortOrder = 3),
                        BikeTypeEntity(typeName = "하이브리드", isDefault = false, sortOrder = 4),
                    )
                )
            }
        }
    }
}
