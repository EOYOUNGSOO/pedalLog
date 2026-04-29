package app.pedallog.android.ui.receive

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.model.ReceivedFile
import app.pedallog.android.data.receive.FileReceiveHandler
import app.pedallog.android.domain.usecase.DuplicateRidingException
import app.pedallog.android.domain.usecase.ParseProcessingStep
import app.pedallog.android.domain.usecase.ParseRidingFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val fileReceiveHandler: FileReceiveHandler,
    private val parseRidingFileUseCase: ParseRidingFileUseCase
) : ViewModel() {
    data class DuplicateSummary(
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val distanceM: Double,
        val avgSpeedKmh: Double,
        val sourceFormat: String
    )


    data class ReceiveUiState(
        val step: ReceiveStep = ReceiveStep.WAITING,
        val receivedFile: ReceivedFile? = null,
        val sessionId: Long? = null,
        val errorMessage: String? = null,
        val progress: Float = 0f
    )

    sealed class ReceiveStep {
        data object WAITING : ReceiveStep()
        data object DETECTING : ReceiveStep()
        data object COPYING : ReceiveStep()
        data object VALIDATING : ReceiveStep()
        data object PARSING : ReceiveStep()
        data object IMAGING : ReceiveStep()
        data class SUCCESS(val file: ReceivedFile) : ReceiveStep()
        data class DUPLICATE(
            val message: String,
            val existingSessionId: Long,
            val summary: DuplicateSummary
        ) : ReceiveStep()
        data class ERROR(val message: String) : ReceiveStep()
    }

    private val _uiState = MutableStateFlow(ReceiveUiState())
    val uiState: StateFlow<ReceiveUiState> = _uiState.asStateFlow()

    fun handleUri(uri: Uri?) {
        if (uri == null) {
            _uiState.update {
                it.copy(
                    step = ReceiveStep.ERROR(
                        "파일을 가져올 수 없습니다.\n다시 시도해주세요."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(step = ReceiveStep.DETECTING, progress = 0.1f)
            }

            _uiState.update {
                it.copy(step = ReceiveStep.COPYING, progress = 0.4f)
            }

            _uiState.update {
                it.copy(step = ReceiveStep.VALIDATING, progress = 0.7f)
            }

            fileReceiveHandler.handleUri(uri)
                .onSuccess { receivedFile ->
                    startParsing(receivedFile)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            step = ReceiveStep.ERROR(
                                error.message ?: "알 수 없는 오류가 발생했습니다."
                            ),
                            progress = 0f
                        )
                    }
                }
        }
    }

    private fun startParsing(receivedFile: ReceivedFile) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    receivedFile = receivedFile,
                    step = ReceiveStep.PARSING,
                    progress = 0.85f
                )
            }

            parseRidingFileUseCase(
                receivedFile = receivedFile,
                onStepChanged = { step ->
                    _uiState.update {
                        when (step) {
                            ParseProcessingStep.PARSING -> {
                                it.copy(step = ReceiveStep.PARSING, progress = 0.85f)
                            }

                            ParseProcessingStep.IMAGING -> {
                                it.copy(step = ReceiveStep.IMAGING, progress = 0.95f)
                            }
                        }
                    }
                }
            )
                .onSuccess { sessionId ->
                    _uiState.update {
                        it.copy(
                            step = ReceiveStep.SUCCESS(receivedFile),
                            receivedFile = receivedFile,
                            sessionId = sessionId,
                            progress = 1.0f
                        )
                    }
                }
                .onFailure { error ->
                    when (error) {
                        is DuplicateRidingException -> {
                            _uiState.update {
                                it.copy(
                                    step = ReceiveStep.DUPLICATE(
                                        message = error.message,
                                        existingSessionId = error.existingSessionId,
                                        summary = DuplicateSummary(
                                            title = error.existingTitle,
                                            startTime = error.existingStartTime,
                                            endTime = error.existingEndTime,
                                            distanceM = error.existingDistanceM,
                                            avgSpeedKmh = error.existingAvgSpeedKmh,
                                            sourceFormat = error.existingSourceFormat
                                        )
                                    ),
                                    progress = 0f
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    step = ReceiveStep.ERROR(
                                        error.message ?: "파싱 실패"
                                    ),
                                    progress = 0f
                                )
                            }
                        }
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                step = ReceiveStep.WAITING,
                receivedFile = null,
                sessionId = null,
                errorMessage = null,
                progress = 0f
            )
        }
    }
}
