package app.pedallog.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.datastore.PreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {
    data class SettingsUiState(
        val notionToken: String = "",
        val notionDbId: String = "",
        val adsRemoved: Boolean = false
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePrefs()
    }

    private fun observePrefs() {
        viewModelScope.launch {
            preferencesDataStore.notionToken.collect { token ->
                _uiState.update { it.copy(notionToken = token.orEmpty()) }
            }
        }
        viewModelScope.launch {
            preferencesDataStore.notionDbId.collect { dbId ->
                _uiState.update { it.copy(notionDbId = dbId.orEmpty()) }
            }
        }
        viewModelScope.launch {
            preferencesDataStore.adsRemoved.collect { removed ->
                _uiState.update { it.copy(adsRemoved = removed) }
            }
        }
    }

    fun saveNotionToken(token: String) {
        viewModelScope.launch { preferencesDataStore.saveNotionToken(token) }
    }

    fun saveNotionDbId(dbId: String) {
        viewModelScope.launch { preferencesDataStore.saveNotionDbId(dbId) }
    }
}
