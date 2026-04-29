package app.pedallog.android.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.data.db.entity.BikeTypeEntity
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalCard
import app.pedallog.android.ui.component.PedalDangerButton
import app.pedallog.android.ui.component.PedalPrimaryButton
import app.pedallog.android.ui.component.PedalTextField
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow
import androidx.compose.ui.window.PopupProperties

@Composable
fun TemplateEditScreen(
    templateId: Long?,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TemplateEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(templateId) {
        if (templateId != null && templateId > 0L) {
            viewModel.loadTemplate(templateId)
        }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = if (templateId == null) "템플릿 추가" else "템플릿 수정",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = PedalBgDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PedalTextField(
                    value = uiState.templateName,
                    onValueChange = viewModel::onTemplateNameChanged,
                    label = "코스명",
                    isRequired = true,
                    isError = uiState.errorMessage != null && uiState.templateName.isBlank(),
                    errorMessage = if (uiState.templateName.isBlank()) uiState.errorMessage else null
                )
            }
            item {
                PedalTextField(
                    value = uiState.startPoint,
                    onValueChange = viewModel::onStartPointChanged,
                    label = "출발지"
                )
            }
            item {
                PedalTextField(
                    value = uiState.endPoint,
                    onValueChange = viewModel::onEndPointChanged,
                    label = "목적지"
                )
            }
            item {
                WaypointSection(
                    waypoints = uiState.waypoints,
                    onAdd = viewModel::addWaypoint,
                    onRemove = viewModel::removeWaypoint,
                    onUpdate = viewModel::updateWaypoint
                )
            }
            item {
                BikeTypeDropdown(
                    bikeTypes = uiState.bikeTypes,
                    selectedBikeTypeId = uiState.selectedBikeTypeId,
                    onSelect = viewModel::onBikeTypeSelected
                )
            }
            item {
                if (uiState.isSaving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PedalYellow
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("저장 중...", color = PedalTextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                PedalPrimaryButton(
                    text = if (uiState.isSaving) "저장 중..." else "저장",
                    onClick = viewModel::saveTemplate,
                    enabled = !uiState.isSaving && uiState.templateName.isNotBlank()
                )
            }
            if (templateId != null) {
                item {
                    PedalDangerButton(text = "삭제", onClick = { showDeleteConfirm = true })
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("템플릿 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("이 템플릿을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteTemplate()
                    }
                ) { Text("삭제", color = PedalError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소", color = PedalTextMuted)
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun WaypointSection(
    waypoints: List<String>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, String) -> Unit
) {
    PedalCard {
        Text("경유지", color = PedalYellow, fontWeight = FontWeight.Bold)
        if (waypoints.isEmpty()) {
            Text("경유지가 없습니다.", color = PedalTextMuted, style = MaterialTheme.typography.bodySmall)
        }
        waypoints.forEachIndexed { index, value ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PedalTextField(
                    value = value,
                    onValueChange = { onUpdate(index, it) },
                    label = "경유지 ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onRemove(index) }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = "경유지 삭제",
                        tint = PedalError
                    )
                }
            }
        }
        TextButton(onClick = onAdd, enabled = waypoints.size < 10) {
            Text("+ 경유지 추가", color = PedalYellow)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BikeTypeDropdown(
    bikeTypes: List<BikeTypeEntity>,
    selectedBikeTypeId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = bikeTypes.firstOrNull { it.id == selectedBikeTypeId }?.typeName ?: "선택 안함"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.zIndex(2f)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("자전거 종류") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(
                focusable = true,
                clippingEnabled = false
            ),
            modifier = Modifier.exposedDropdownSize()
        ) {
            bikeTypes.forEach { bikeType ->
                DropdownMenuItem(
                    text = { Text(bikeType.typeName) },
                    onClick = {
                        onSelect(bikeType.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
