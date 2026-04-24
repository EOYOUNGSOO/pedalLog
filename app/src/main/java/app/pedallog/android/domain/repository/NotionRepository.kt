package app.pedallog.android.domain.repository

import app.pedallog.android.data.notion.NotionRidingProperties
import java.io.File

interface NotionRepository {
    suspend fun registerRiding(
        sessionId: Long,
        properties: NotionRidingProperties,
        routeImageFile: File?
    ): Result<String>

    suspend fun validateConnection(): Result<String>
}
