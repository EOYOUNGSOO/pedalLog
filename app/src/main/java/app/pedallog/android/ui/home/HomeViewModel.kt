package app.pedallog.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.datastore.PreferencesDataStore
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.domain.repository.RidingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ridingRepository: RidingRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {
    data class HomeUiState(
        val recentSessions: List<RidingSessionEntity> = emptyList(),
        val isNotionConfigured: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecentSessions()
        checkNotionConfig()
    }

    private fun loadRecentSessions() {
        viewModelScope.launch {
            ridingRepository.getAllSessions()
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .collect { sessions ->
                    _uiState.update { it.copy(recentSessions = sessions) }
                }
        }
    }

    private fun checkNotionConfig() {
        viewModelScope.launch {
            preferencesDataStore.notionToken.collect { token ->
                _uiState.update { it.copy(isNotionConfigured = !token.isNullOrBlank()) }
            }
        }
    }
}
