package app.pedallog.android.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {
    data class ParsedRidePreview(
        val title: String = "",
        val distanceM: Double = 0.0,
        val startTime: Long = 0L,
        val endTime: Long = 0L
    )

    data class ConfirmUiState(
        val templates: List<RidingTemplateEntity> = emptyList(),
        val selectedTemplate: RidingTemplateEntity? = null,
        val parsedRidePreview: ParsedRidePreview? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(ConfirmUiState())
    val uiState: StateFlow<ConfirmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { current -> current.copy(templates = templates) }
            }
        }
    }

    fun selectTemplate(template: RidingTemplateEntity) {
        _uiState.update { it.copy(selectedTemplate = template) }
    }

    fun setParsedRidePreview(preview: ParsedRidePreview) {
        _uiState.update { it.copy(parsedRidePreview = preview) }
    }
}
