package app.pedallog.android.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.db.dao.RidingSessionDao
import app.pedallog.android.data.db.dao.RidingTemplateDao
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.domain.usecase.RegisterToNotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    private val sessionDao: RidingSessionDao,
    private val templateDao: RidingTemplateDao,
    private val registerToNotionUseCase: RegisterToNotionUseCase
) : ViewModel() {

    data class ConfirmUiState(
        val session: RidingSessionEntity? = null,
        val templates: List<RidingTemplateEntity> = emptyList(),
        val selectedTemplate: RidingTemplateEntity? = null,
        val editableMemo: String = "",
        val isTemplateDropdownExpanded: Boolean = false,
        val registerState: RegisterState = RegisterState.IDLE,
        val notionPageId: String? = null,
        val isLoading: Boolean = true
    )

    sealed class RegisterState {
        data object IDLE : RegisterState()
        data object UPLOADING : RegisterState()
        data object CREATING : RegisterState()
        data object ATTACHING : RegisterState()
        data object SUCCESS : RegisterState()
        data class ERROR(val message: String) : RegisterState()
    }

    private val _uiState = MutableStateFlow(ConfirmUiState())
    val uiState: StateFlow<ConfirmUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getSessionById(sessionId)
            val templates = templateDao.getAllTemplates().first()

            val autoTemplate = templates.firstOrNull { it.isFavorite }
                ?: templates.firstOrNull()

            _uiState.update {
                it.copy(
                    session = session,
                    templates = templates,
                    selectedTemplate = autoTemplate,
                    editableMemo = session?.memo ?: "",
                    isLoading = false
                )
            }

            autoTemplate?.let { applyTemplate(it, sessionId) }
        }
    }

    fun selectTemplate(template: RidingTemplateEntity) {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            applyTemplate(template, sessionId)
            val merged = sessionDao.getSessionById(sessionId) ?: return@launch
            val withMemo = merged.copy(
                memo = template.defaultMemo?.takeIf { it.isNotBlank() } ?: merged.memo
            )
            sessionDao.update(withMemo)
            _uiState.update {
                it.copy(
                    session = withMemo,
                    selectedTemplate = template,
                    editableMemo = template.defaultMemo ?: "",
                    isTemplateDropdownExpanded = false
                )
            }
        }
    }

    private suspend fun applyTemplate(
        template: RidingTemplateEntity,
        sessionId: Long
    ) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val updated = session.copy(
            templateId = template.id,
            departure = template.departure,
            waypoints = template.waypoints,
            destination = template.destination,
            bikeType = template.bikeType
        )
        sessionDao.update(updated)
        _uiState.update { it.copy(session = updated) }
    }

    fun setDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isTemplateDropdownExpanded = expanded) }
    }

    fun closeDropdown() {
        _uiState.update { it.copy(isTemplateDropdownExpanded = false) }
    }

    fun updateMemo(memo: String) {
        _uiState.update { it.copy(editableMemo = memo) }
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            val session = sessionDao.getSessionById(sessionId) ?: return@launch
            sessionDao.update(session.copy(memo = memo.takeIf { it.isNotBlank() }))
        }
    }

    fun registerToNotion() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(registerState = RegisterState.UPLOADING) }

            val progressJob = launch {
                delay(800L)
                _uiState.update { it.copy(registerState = RegisterState.CREATING) }
                delay(1500L)
                _uiState.update { it.copy(registerState = RegisterState.ATTACHING) }
            }

            registerToNotionUseCase(sessionId)
                .onSuccess { pageId ->
                    progressJob.cancel()
                    _uiState.update {
                        it.copy(
                            registerState = RegisterState.SUCCESS,
                            notionPageId = pageId
                        )
                    }
                }
                .onFailure { error ->
                    progressJob.cancel()
                    _uiState.update {
                        it.copy(
                            registerState = RegisterState.ERROR(
                                error.message ?: "알 수 없는 오류"
                            )
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(registerState = RegisterState.IDLE) }
    }
}
