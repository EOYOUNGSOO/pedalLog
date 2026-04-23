package app.pedalLog.android.ui.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.pedalLog.android.data.db.entity.RidingTemplateEntity
import app.pedalLog.android.data.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateViewModel(
    private val repository: TemplateRepository
) : ViewModel() {
    val templates: StateFlow<List<RidingTemplateEntity>> = repository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bikeTypes = repository.observeBikeTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavorite(item: RidingTemplateEntity) {
        viewModelScope.launch { repository.toggleFavorite(item) }
    }

    fun delete(item: RidingTemplateEntity) {
        viewModelScope.launch { repository.delete(item) }
    }

    fun save(item: RidingTemplateEntity) {
        viewModelScope.launch { repository.upsert(item) }
    }

    fun reorder(newOrder: List<RidingTemplateEntity>) {
        viewModelScope.launch { repository.reorder(newOrder) }
    }
}

class TemplateViewModelFactory(
    private val repository: TemplateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplateViewModel(repository) as T
    }
}
