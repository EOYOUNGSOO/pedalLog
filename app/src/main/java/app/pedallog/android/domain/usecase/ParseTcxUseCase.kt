package app.pedallog.android.domain.usecase

import android.net.Uri
import app.pedallog.android.domain.repository.RidingRepository
import javax.inject.Inject

class ParseTcxUseCase @Inject constructor(
    private val ridingRepository: RidingRepository
) {
    suspend operator fun invoke(fileUri: Uri): Result<Long> {
        return Result.failure(NotImplementedError("Phase 1 task #8"))
    }
}
