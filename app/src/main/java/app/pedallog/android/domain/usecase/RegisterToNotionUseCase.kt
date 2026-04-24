package app.pedallog.android.domain.usecase

import app.pedallog.android.domain.repository.NotionRepository
import app.pedallog.android.domain.repository.RidingRepository
import javax.inject.Inject

class RegisterToNotionUseCase @Inject constructor(
    private val notionRepository: NotionRepository,
    private val ridingRepository: RidingRepository
) {
    suspend operator fun invoke(sessionId: Long): Result<String> {
        return Result.failure(NotImplementedError("Phase 1 task #10"))
    }
}
