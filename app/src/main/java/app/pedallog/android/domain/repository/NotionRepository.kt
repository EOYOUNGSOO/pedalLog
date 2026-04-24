package app.pedallog.android.domain.repository

interface NotionRepository {
    suspend fun registerRidingToNotion(sessionId: Long): Result<String>
}
