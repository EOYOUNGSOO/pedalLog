package app.pedalLog.android.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pedalLog.android.data.db.entity.BikeTypeEntity
import app.pedalLog.android.data.db.entity.RidingTemplateEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(
    bikeTypes: List<BikeTypeEntity>,
    initialValue: RidingTemplateEntity? = null,
    onSave: (RidingTemplateEntity) -> Unit,
    onBack: () -> Unit
) {
    var courseName by remember { mutableStateOf(initialValue?.courseName.orEmpty()) }
    var departure by remember { mutableStateOf(initialValue?.departure.orEmpty()) }
    var destination by remember { mutableStateOf(initialValue?.destination.orEmpty()) }
    var note by remember { mutableStateOf(initialValue?.defaultNote.orEmpty()) }
    var selectedBikeType by remember { mutableStateOf(initialValue?.bikeType ?: "로드") }
    var expanded by remember { mutableStateOf(false) }
    var waypoints by remember { mutableStateOf(initialValue?.waypoints ?: listOf("")) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("템플릿 편집") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("코스명*") }
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = departure,
                    onValueChange = { departure = it },
                    label = { Text("출발지") }
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("목적지") }
                )
            }
            item {
                Column {
                    Button(onClick = { expanded = true }) {
                        Text("자전거 종류: $selectedBikeType")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        bikeTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedBikeType = type.name
                                    expanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("직접 입력") },
                            onClick = { expanded = false }
                        )
                    }
                }
            }
            itemsIndexed(waypoints) { index, value ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = value,
                        onValueChange = { changed ->
                            waypoints = waypoints.toMutableList().also { it[index] = changed }
                        },
                        label = { Text("경유지 ${index + 1}") }
                    )
                    IconButton(onClick = {
                        waypoints = waypoints.toMutableList().also {
                            if (it.size > 1) it.removeAt(index)
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "경유지 삭제")
                    }
                }
            }
            item {
                IconButton(onClick = { waypoints = waypoints + "" }) {
                    Icon(Icons.Default.Add, contentDescription = "경유지 추가")
                }
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("기본 비고") }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onBack) { Text("취소") }
                    Button(
                        onClick = {
                            if (courseName.isBlank()) return@Button
                            onSave(
                                RidingTemplateEntity(
                                    id = initialValue?.id ?: 0L,
                                    courseName = courseName,
                                    departure = departure,
                                    destination = destination,
                                    waypoints = waypoints.filter { it.isNotBlank() },
                                    bikeType = selectedBikeType,
                                    defaultNote = note,
                                    isFavorite = initialValue?.isFavorite ?: false,
                                    sortOrder = initialValue?.sortOrder ?: 0
                                )
                            )
                        }
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}
