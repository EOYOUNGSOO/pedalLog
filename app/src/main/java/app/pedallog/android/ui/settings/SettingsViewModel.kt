package app.pedallog.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.datastore.PreferencesDataStore
import app.pedallog.android.data.notion.NotionApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: PreferencesDataStore,
    private val notionApiClient: NotionApiClient
) : ViewModel() {

    data class SettingsUiState(
        val tokenInput: String = "",
        val savedToken: String? = null,
        val isTokenVisible: Boolean = false,
        val tokenSaveResult: SaveResult = SaveResult.IDLE,
        val dbIdInput: String = "",
        val savedDbId: String? = null,
        val dbIdSaveResult: SaveResult = SaveResult.IDLE,
        val testState: TestState = TestState.IDLE,
        val adsRemoved: Boolean = false,
        val isLoading: Boolean = true,
        val isFirstLaunch: Boolean = false
    )

    sealed class SaveResult {
        data object IDLE : SaveResult()
        data object SUCCESS : SaveResult()
        data class ERROR(val message: String) : SaveResult()
    }

    sealed class TestState {
        data object IDLE : TestState()
        data object LOADING : TestState()
        data class SUCCESS(val message: String) : TestState()
        data class ERROR(val message: String) : TestState()
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            combine(
                dataStore.notionToken,
                dataStore.notionDbId,
                dataStore.adsRemoved,
                dataStore.isFirstLaunch
            ) { token, dbId, adsRemoved, isFirst ->
                _uiState.update {
                    it.copy(
                        savedToken = token,
                        savedDbId = dbId,
                        tokenInput = "",
                        dbIdInput = dbId ?: "",
                        adsRemoved = adsRemoved,
                        isFirstLaunch = isFirst,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun updateTokenInput(value: String) {
        _uiState.update {
            it.copy(
                tokenInput = value,
                tokenSaveResult = SaveResult.IDLE
            )
        }
    }

    fun toggleTokenVisibility() {
        _uiState.update {
            it.copy(isTokenVisible = !it.isTokenVisible)
        }
    }

    fun saveToken() {
        val token = _uiState.value.tokenInput.trim()
        if (!dataStore.isValidToken(token)) {
            _uiState.update {
                it.copy(
                    tokenSaveResult = SaveResult.ERROR(
                        "올바른 Token 형식이 아닙니다.\nntn_ (신형) 또는 secret_ (구형)으로 시작하는 토큰을 입력해주세요."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            dataStore.saveNotionToken(token)
            _uiState.update {
                it.copy(
                    savedToken = token,
                    tokenInput = "",
                    tokenSaveResult = SaveResult.SUCCESS,
                    isTokenVisible = false
                )
            }
        }
    }

    fun updateDbIdInput(value: String) {
        _uiState.update {
            it.copy(
                dbIdInput = value,
                dbIdSaveResult = SaveResult.IDLE
            )
        }
    }

    fun saveDbId() {
        val raw = _uiState.value.dbIdInput.trim()
        val dbId = if (raw.startsWith("http")) {
            dataStore.extractDbIdFromUrl(raw)
        } else {
            raw.replace("-", "").let { clean ->
                if (clean.length == 32) {
                    "${clean.substring(0, 8)}-" +
                        "${clean.substring(8, 12)}-" +
                        "${clean.substring(12, 16)}-" +
                        "${clean.substring(16, 20)}-" +
                        clean.substring(20)
                } else {
                    raw
                }
            }
        }

        if (!dataStore.isValidDbId(dbId)) {
            _uiState.update {
                it.copy(
                    dbIdSaveResult = SaveResult.ERROR(
                        "올바른 Database ID 형식이 아닙니다.\nNotion DB 페이지 URL을 붙여넣어도 됩니다."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            dataStore.saveNotionDbId(dbId)
            _uiState.update {
                it.copy(
                    savedDbId = dbId,
                    dbIdInput = dbId,
                    dbIdSaveResult = SaveResult.SUCCESS
                )
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(testState = TestState.LOADING) }

            val token = dataStore.notionToken.first()
            val dbId = dataStore.notionDbId.first()

            when {
                token.isNullOrBlank() -> {
                    _uiState.update {
                        it.copy(testState = TestState.ERROR("Notion Token을 먼저 저장해주세요."))
                    }
                }

                dbId.isNullOrBlank() -> {
                    _uiState.update {
                        it.copy(testState = TestState.ERROR("Database ID를 먼저 저장해주세요."))
                    }
                }

                else -> {
                    val tokenResult = notionApiClient.validateToken(token)
                    if (tokenResult.isFailure) {
                        _uiState.update {
                            it.copy(
                                testState = TestState.ERROR(
                                    tokenResult.exceptionOrNull()?.message ?: "Token 검증 실패"
                                )
                            )
                        }
                        return@launch
                    }

                    val dbResult = notionApiClient.validateDatabase(dbId, token)
                    if (dbResult.isFailure) {
                        _uiState.update {
                            it.copy(
                                testState = TestState.ERROR(
                                    dbResult.exceptionOrNull()?.message ?: "Database 검증 실패"
                                )
                            )
                        }
                        return@launch
                    }

                    _uiState.update {
                        it.copy(
                            testState = TestState.SUCCESS(
                                "연결 성공\nDB: ${dbResult.getOrDefault("라이딩 기록부")}"
                            )
                        )
                    }

                    if (_uiState.value.isFirstLaunch) {
                        dataStore.setFirstLaunchDone()
                    }
                }
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testState = TestState.IDLE) }
    }

    fun clearNotionSettings() {
        viewModelScope.launch {
            dataStore.clearNotionSettings()
            _uiState.update {
                it.copy(
                    savedToken = null,
                    savedDbId = null,
                    tokenInput = "",
                    dbIdInput = "",
                    tokenSaveResult = SaveResult.IDLE,
                    dbIdSaveResult = SaveResult.IDLE,
                    testState = TestState.IDLE
                )
            }
        }
    }
}
