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
        val customTitleInput: String = "",
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
            val selected = session?.templateId?.let { id ->
                templates.firstOrNull { it.id == id }
            }

            _uiState.update {
                it.copy(
                    session = session,
                    templates = templates,
                    selectedTemplate = selected,
                    editableMemo = session?.memo ?: "",
                    isLoading = false
                )
            }
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
                    customTitleInput = "",
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
            title = template.templateName,
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

    fun updateCustomTitleInput(title: String) {
        _uiState.update {
            it.copy(
                customTitleInput = title,
                selectedTemplate = if (title.isNotBlank()) null else it.selectedTemplate
            )
        }
    }

    private suspend fun ensureTitleTemplate(sessionId: Long): Result<Unit> {
        val state = _uiState.value
        val selectedTemplate = state.selectedTemplate
        if (selectedTemplate != null) {
            applyTemplate(selectedTemplate, sessionId)
            return Result.success(Unit)
        }

        val customTitle = state.customTitleInput.trim()
        if (customTitle.isBlank()) {
            return Result.failure(Exception("라이딩명 선택은 필수입니다. 목록에서 선택하거나 직접 입력해주세요."))
        }

        val session = sessionDao.getSessionById(sessionId)
            ?: return Result.failure(Exception("세션을 찾을 수 없습니다."))

        val existing = state.templates.firstOrNull { it.templateName.equals(customTitle, ignoreCase = true) }
        val template = if (existing != null) {
            existing
        } else {
            val newTemplate = RidingTemplateEntity(
                templateName = customTitle,
                departure = session.departure,
                waypoints = session.waypoints,
                destination = session.destination,
                bikeType = session.bikeType,
                defaultMemo = state.editableMemo.takeIf { it.isNotBlank() },
                sortOrder = templateDao.getTotalCount()
            )
            val newId = templateDao.upsert(newTemplate)
            newTemplate.copy(id = newId)
        }

        applyTemplate(template, sessionId)
        val templates = templateDao.getAllTemplates().first()
        _uiState.update {
            it.copy(
                templates = templates,
                selectedTemplate = template,
                customTitleInput = ""
            )
        }
        return Result.success(Unit)
    }

    fun registerToNotion() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            ensureTitleTemplate(sessionId).onFailure { error ->
                _uiState.update {
                    it.copy(
                        registerState = RegisterState.ERROR(error.message ?: "라이딩명 선택이 필요합니다.")
                    )
                }
                return@launch
            }

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
