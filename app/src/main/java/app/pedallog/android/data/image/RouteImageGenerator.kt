package app.pedallog.android.data.image

import java.io.File

interface RouteImageGenerator {
    suspend fun generate(
        sessionId: Long,
        trackPoints: List<LatLng>
    ): File?
}

data class LatLng(
    val latitude: Double,
    val longitude: Double
)
