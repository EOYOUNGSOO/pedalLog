package app.pedallog.android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.TrackPointDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.domain.calculator.RidingStatsCalculator
import app.pedallog.android.domain.repository.NotionRepository
import app.pedallog.android.domain.usecase.RegisterToNotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val sessionDao: RidingSessionDao,
    private val trackPointDao: TrackPointDao,
    private val notionRepository: NotionRepository,
    private val registerToNotionUseCase: RegisterToNotionUseCase
) : ViewModel() {

    data class DetailUiState(
        val session: RidingSessionEntity? = null,
        val isLoading: Boolean = true,
        val isEditing: Boolean = false,
        val editMemo: String = "",
        val editBikeType: String = "",
        val editDeparture: String = "",
        val editDestination: String = "",
        val isRetrying: Boolean = false,
        val isReparsing: Boolean = false,
        val retrySuccess: Boolean = false,
        val retrySuccessMsg: String? = null,
        val retryErrorMsg: String? = null,
        val isDeleting: Boolean = false,
        val deleteSuccess: Boolean = false,
        val deleteErrorMsg: String? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val saveSuccess: Boolean = false,
        val saveErrorMsg: String? = null
    )

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getSessionById(sessionId)
            _uiState.update {
                it.copy(
                    session = session,
                    isLoading = false,
                    editMemo = session?.memo ?: "",
                    editBikeType = session?.bikeType ?: "",
                    editDeparture = session?.departure ?: "",
                    editDestination = session?.destination ?: ""
                )
            }
        }
    }

    fun startEditing() {
        val s = _uiState.value.session ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editMemo = s.memo ?: "",
                editBikeType = s.bikeType ?: "",
                editDeparture = s.departure ?: "",
                editDestination = s.destination ?: ""
            )
        }
    }

    fun cancelEditing() = _uiState.update { it.copy(isEditing = false) }

    fun onEditMemoChange(v: String) = _uiState.update { it.copy(editMemo = v) }
    fun onEditBikeTypeChange(v: String) = _uiState.update { it.copy(editBikeType = v) }
    fun onEditDepartureChange(v: String) = _uiState.update { it.copy(editDeparture = v) }
    fun onEditDestinationChange(v: String) = _uiState.update { it.copy(editDestination = v) }

    fun saveEdits() {
        val session = _uiState.value.session ?: return
        val state = _uiState.value
        viewModelScope.launch {
            val updated = session.copy(
                memo = state.editMemo.trim().ifEmpty { null },
                bikeType = state.editBikeType.trim().ifEmpty { null },
                departure = state.editDeparture.trim().ifEmpty { null },
                destination = state.editDestination.trim().ifEmpty { null }
            )
            sessionDao.update(updated)

            registerToNotionUseCase(updated.id)
                .onSuccess {
                    val synced = sessionDao.getSessionById(updated.id) ?: updated
                    _uiState.update {
                        it.copy(
                            session = synced,
                            isEditing = false,
                            saveSuccess = true,
                            saveErrorMsg = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            session = updated,
                            isEditing = false,
                            saveSuccess = true,
                            saveErrorMsg = "로컬 저장 완료, Notion 반영 실패: ${error.message}"
                        )
                    }
                }
        }
    }

    fun retryRegister() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRetrying = true,
                    retryErrorMsg = null,
                    retrySuccess = false,
                    retrySuccessMsg = null
                )
            }
            registerToNotionUseCase(sessionId)
                .onSuccess {
                    val updated = sessionDao.getSessionById(sessionId)
                    _uiState.update {
                        it.copy(
                            session = updated,
                            isRetrying = false,
                            retrySuccess = true,
                            retrySuccessMsg = "✓ Notion 재등록 완료!"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isRetrying = false, retryErrorMsg = error.message ?: "재등록에 실패했습니다.")
                    }
                }
        }
    }

    fun reparseAndRetryRegister() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isReparsing = true,
                    isRetrying = true,
                    retryErrorMsg = null,
                    retrySuccess = false,
                    retrySuccessMsg = null
                )
            }

            var reparseSummaryMsg: String? = null
            runCatching {
                val points = trackPointDao.getTrackPointsBySession(session.id)
                if (points.size >= 2) {
                    val stats = RidingStatsCalculator.calculate(
                        points = points,
                        deviceMaxSpeedKmh = session.maxSpeedKmh.takeIf { it > 0 },
                        deviceDistanceM = session.totalDistanceM.takeIf { it > 0 }
                    )

                    val corrected = session.copy(
                        startTime = points.first().timestamp,
                        endTime = points.last().timestamp,
                        totalDistanceM = stats.totalDistanceM,
                        avgSpeedKmh = stats.avgSpeedKmh,
                        maxSpeedKmh = stats.maxSpeedKmh,
                        avgCadence = stats.avgCadenceRpm.takeIf { it > 0 } ?: session.avgCadence,
                        maxCadence = stats.maxCadenceRpm.takeIf { it > 0 } ?: session.maxCadence,
                        avgHeartRate = stats.avgHeartRateBpm.takeIf { it > 0 } ?: session.avgHeartRate,
                        maxHeartRate = stats.maxHeartRateBpm.takeIf { it > 0 } ?: session.maxHeartRate,
                        elevationUp = stats.elevationUpM.takeIf { it > 0 } ?: session.elevationUp
                    )
                    sessionDao.update(corrected)
                    _uiState.update { it.copy(session = corrected) }
                    reparseSummaryMsg = buildString {
                        append(
                            "거리 %.2fkm, 평균속도 %.1fkm/h, 이동시간 %d분".format(
                                corrected.totalDistanceM / 1000.0,
                                corrected.avgSpeedKmh,
                                (stats.movingTimeSec / 60)
                            )
                        )
                        corrected.elevationUp?.let {
                            append(", 상승고도 %.0fm".format(it))
                        }
                        corrected.avgHeartRate?.let {
                            append(", 평균심박 %dbpm".format(it))
                        }
                        corrected.maxHeartRate?.let {
                            append(", 최고심박 %dbpm".format(it))
                        }
                        append("로 재계산됨")
                    }
                }
            }.onFailure {
                // 재계산 실패해도 재전송은 시도하여 사용자 동선을 막지 않습니다.
            }

            registerToNotionUseCase(session.id)
                .onSuccess {
                    val updated = sessionDao.getSessionById(session.id)
                    _uiState.update {
                        it.copy(
                            session = updated,
                            isRetrying = false,
                            isReparsing = false,
                            retrySuccess = true,
                            retrySuccessMsg = if (reparseSummaryMsg != null) {
                                "✓ 정밀 재계산 후 Notion 재등록 완료\n$reparseSummaryMsg"
                            } else {
                                "✓ 정밀 재계산 후 Notion 재등록 완료"
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRetrying = false,
                            isReparsing = false,
                            retryErrorMsg = error.message ?: "재파싱 후 재등록에 실패했습니다."
                        )
                    }
                }
        }
    }

    fun clearRetryResult() = _uiState.update {
        it.copy(
            retrySuccess = false,
            retrySuccessMsg = null,
            retryErrorMsg = null
        )
    }

    fun requestDelete() = _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirmDialog = false) }

    fun confirmDelete(onDeleted: () -> Unit) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirmDialog = false) }
            if (!session.notionPageId.isNullOrBlank()) {
                notionRepository.deleteRidingPage(session.notionPageId)
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                deleteErrorMsg = "Notion 삭제 실패: ${error.message}\n로컬 데이터는 유지됩니다."
                            )
                        }
                        return@launch
                    }
            }
            trackPointDao.deleteBySession(session.id)
            sessionDao.delete(session)
            _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            onDeleted()
        }
    }

    fun clearDeleteError() = _uiState.update { it.copy(deleteErrorMsg = null) }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false, saveErrorMsg = null) }
}
