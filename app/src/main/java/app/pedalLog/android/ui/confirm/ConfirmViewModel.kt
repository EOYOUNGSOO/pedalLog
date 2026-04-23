package app.pedalLog.android.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import app.pedalLog.android.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfirmFormState(
    val selectedTemplateId: Long? = null,
    val courseName: String = "",
    val departure: String = "",
    val destination: String = "",
    val waypoints: List<String> = emptyList(),
    val bikeType: String = "",
    val note: String = ""
)

class ConfirmViewModel(
    private val templateRepository: TemplateRepository
) : ViewModel() {
    val templates = templateRepository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(ConfirmFormState())
    val form: StateFlow<ConfirmFormState> = _form.asStateFlow()

    fun onTemplateSelected(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            val template = templateRepository.getById(id) ?: return@launch
            _form.value = _form.value.copy(
                selectedTemplateId = id,
                courseName = template.courseName,
                departure = template.departure,
                destination = template.destination,
                waypoints = template.waypoints,
                bikeType = template.bikeType,
                note = template.defaultNote
            )
        }
    }

    fun updateForm(transform: (ConfirmFormState) -> ConfirmFormState) {
        _form.value = transform(_form.value)
    }

    fun toTemplateEntity(sortOrder: Int): RidingTemplateEntity {
        val value = _form.value
        return RidingTemplateEntity(
            id = value.selectedTemplateId ?: 0L,
            courseName = value.courseName,
            departure = value.departure,
            destination = value.destination,
            waypoints = value.waypoints,
            bikeType = value.bikeType,
            defaultNote = value.note,
            sortOrder = sortOrder
        )
    }
}

class ConfirmViewModelFactory(
    private val repository: TemplateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConfirmViewModel(repository) as T
    }
}
