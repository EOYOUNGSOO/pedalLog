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
class HistoryDetailViewModel @Inject constructor(
    private val sessionDao: RidingSessionDao,
    private val registerToNotionUseCase: RegisterToNotionUseCase
) : ViewModel() {

    data class DetailUiState(
        val session: RidingSessionEntity? = null,
        val isLoading: Boolean = true,
        val isRetrying: Boolean = false,
        val retrySuccess: Boolean = false,
        val retryErrorMsg: String? = null
    )

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getSessionById(sessionId)
            _uiState.update {
                it.copy(session = session, isLoading = false)
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
                    retrySuccess = false
                )
            }
            registerToNotionUseCase(sessionId)
                .onSuccess {
                    val updated = sessionDao.getSessionById(sessionId)
                    _uiState.update {
                        it.copy(
                            session = updated,
                            isRetrying = false,
                            retrySuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRetrying = false,
                            retryErrorMsg = error.message ?: "재등록에 실패했습니다."
                        )
                    }
                }
        }
    }

    fun clearRetryResult() {
        _uiState.update {
            it.copy(
                retrySuccess = false,
                retryErrorMsg = null
            )
        }
    }
}
