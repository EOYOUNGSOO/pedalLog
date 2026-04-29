package app.pedallog.android.ui.template

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pedallog.android.data.db.dao.BikeTypeDao
import app.pedallog.android.data.db.entity.BikeTypeEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class TemplateEditViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val bikeTypeDao: BikeTypeDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class UiState(
        val templateName: String = "",
        val startPoint: String = "",
        val endPoint: String = "",
        val waypoints: List<String> = emptyList(),
        val bikeTypes: List<BikeTypeEntity> = emptyList(),
        val selectedBikeTypeId: Long? = null,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false,
        val errorMessage: String? = null,
        val isEditMode: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var editingTemplateId: Long? = null

    init {
        observeBikeTypes()
        val rawId = savedStateHandle.get<Long>("id")
        val id = rawId?.takeIf { it > 0L }
        if (id != null) {
            loadTemplate(id)
        }
    }

    private fun observeBikeTypes() {
        viewModelScope.launch {
            bikeTypeDao.getAllBikeTypes().collect { types ->
                _uiState.update { state ->
                    val selected = state.selectedBikeTypeId
                        ?: types.firstOrNull { it.isDefault }?.id
                        ?: types.firstOrNull()?.id
                    state.copy(
                        bikeTypes = types,
                        selectedBikeTypeId = selected
                    )
                }
            }
        }
    }

    fun loadTemplate(id: Long) {
        editingTemplateId = id
        viewModelScope.launch {
            val template = templateRepository.getTemplateById(id)
            if (template == null) {
                _uiState.update { it.copy(errorMessage = "템플릿을 찾을 수 없습니다.") }
                return@launch
            }
            val waypointList = parseWaypoints(template.waypoints)
            val selectedBikeId = _uiState.value.bikeTypes
                .firstOrNull { it.typeName == template.bikeType }
                ?.id
            _uiState.update {
                it.copy(
                    templateName = template.templateName,
                    startPoint = template.departure.orEmpty(),
                    endPoint = template.destination.orEmpty(),
                    waypoints = waypointList,
                    selectedBikeTypeId = selectedBikeId ?: it.selectedBikeTypeId,
                    isEditMode = true,
                    errorMessage = null
                )
            }
        }
    }

    fun onTemplateNameChanged(value: String) = _uiState.update {
        it.copy(templateName = value, errorMessage = null)
    }

    fun onStartPointChanged(value: String) = _uiState.update {
        it.copy(startPoint = value)
    }

    fun onEndPointChanged(value: String) = _uiState.update {
        it.copy(endPoint = value)
    }

    fun onBikeTypeSelected(id: Long?) = _uiState.update {
        it.copy(selectedBikeTypeId = id)
    }

    fun addWaypoint() {
        val current = _uiState.value.waypoints
        if (current.size >= 10) return
        _uiState.update { it.copy(waypoints = current + "") }
    }

    fun removeWaypoint(index: Int) {
        val current = _uiState.value.waypoints
        if (index !in current.indices) return
        _uiState.update { it.copy(waypoints = current.toMutableList().apply { removeAt(index) }) }
    }

    fun updateWaypoint(index: Int, value: String) {
        val current = _uiState.value.waypoints
        if (index !in current.indices) return
        val next = current.toMutableList().apply { set(index, value) }
        _uiState.update { it.copy(waypoints = next) }
    }

    fun saveTemplate() {
        val state = _uiState.value
        val name = state.templateName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "코스명은 필수 입력입니다.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val selectedBikeType = state.bikeTypes.firstOrNull { it.id == state.selectedBikeTypeId }?.typeName
            val entity = RidingTemplateEntity(
                id = editingTemplateId ?: 0L,
                templateName = name,
                departure = state.startPoint.trim().ifBlank { null },
                destination = state.endPoint.trim().ifBlank { null },
                waypoints = toWaypointsJson(state.waypoints),
                bikeType = selectedBikeType,
                sortOrder = if (editingTemplateId == null) Int.MAX_VALUE else templateRepository.getTemplateById(editingTemplateId!!)?.sortOrder ?: Int.MAX_VALUE,
                isFavorite = templateRepository.getTemplateById(editingTemplateId ?: -1L)?.isFavorite ?: false
            )
            templateRepository.upsertTemplate(entity)
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun deleteTemplate() {
        val id = editingTemplateId ?: return
        viewModelScope.launch {
            val entity = templateRepository.getTemplateById(id) ?: return@launch
            templateRepository.deleteTemplate(entity)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun parseWaypoints(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i -> arr.optString(i) }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toWaypointsJson(list: List<String>): String? {
        val normalized = list.map { it.trim() }.filter { it.isNotBlank() }
        if (normalized.isEmpty()) return null
        val arr = JSONArray()
        normalized.forEach { arr.put(it) }
        return arr.toString()
    }
}
