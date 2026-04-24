package app.pedallog.android.data.repository

import app.pedallog.android.data.notion.NotionApiClient
import app.pedallog.android.domain.repository.NotionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionRepositoryImpl @Inject constructor(
    private val notionApiClient: NotionApiClient
) : NotionRepository {
    override suspend fun registerRidingToNotion(sessionId: Long): Result<String> {
        return Result.failure(NotImplementedError("Phase 1 task #10"))
    }
}
