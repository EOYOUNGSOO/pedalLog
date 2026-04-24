package app.pedallog.android.ui.template

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
class TemplateListViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {
    data class TemplateListUiState(
        val templates: List<RidingTemplateEntity> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(TemplateListUiState())
    val uiState: StateFlow<TemplateListUiState> = _uiState.asStateFlow()

    init {
        observeTemplates()
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }

    fun upsertTemplate(template: RidingTemplateEntity) {
        viewModelScope.launch { templateRepository.upsertTemplate(template) }
    }

    fun deleteTemplate(template: RidingTemplateEntity) {
        viewModelScope.launch { templateRepository.deleteTemplate(template) }
    }
}
