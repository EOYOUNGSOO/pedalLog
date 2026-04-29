package app.pedallog.android.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.domain.usecase.RegisterToNotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionDao: RidingSessionDao,
    private val registerToNotionUseCase: RegisterToNotionUseCase
) : ViewModel() {

    enum class FilterType { ALL, SUCCESS, FAILED }

    data class HistoryUiState(
        val allSessions: List<RidingSessionEntity> = emptyList(),
        val filteredSessions: List<RidingSessionEntity> = emptyList(),
        val currentFilter: FilterType = FilterType.ALL,
        val isLoading: Boolean = true,
        val retryingId: Long? = null,
        val retrySuccessId: Long? = null,
        val retryErrorMsg: String? = null
    )

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            sessionDao.getAllSessions().collect { sessions ->
                val filter = _uiState.value.currentFilter
                _uiState.update {
                    it.copy(
                        allSessions = sessions,
                        filteredSessions = applyFilter(sessions, filter),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update {
            it.copy(
                currentFilter = filter,
                filteredSessions = applyFilter(it.allSessions, filter)
            )
        }
    }

    private fun applyFilter(
        sessions: List<RidingSessionEntity>,
        filter: FilterType
    ): List<RidingSessionEntity> = when (filter) {
        FilterType.ALL -> sessions
        FilterType.SUCCESS -> sessions.filter { it.notionPageId != null }
        FilterType.FAILED -> sessions.filter { it.notionPageId == null }
    }

    fun retryRegister(sessionId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    retryingId = sessionId,
                    retryErrorMsg = null,
                    retrySuccessId = null
                )
            }

            registerToNotionUseCase(sessionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            retryingId = null,
                            retrySuccessId = sessionId
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            retryingId = null,
                            retryErrorMsg = error.message ?: "재전송에 실패했습니다."
                        )
                    }
                }
        }
    }

    fun clearRetryResult() {
        _uiState.update {
            it.copy(
                retrySuccessId = null,
                retryErrorMsg = null
            )
        }
    }
}
