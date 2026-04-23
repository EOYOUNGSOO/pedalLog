package app.pedalLog.android.ui.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConfirmScreen(
    viewModel: ConfirmViewModel,
    onNavigateTemplateCreate: () -> Unit
) {
    val templates = viewModel.templates.collectAsStateWithLifecycle().value
    val form = viewModel.form.collectAsStateWithLifecycle().value
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("라이딩 확인") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { expanded = true }) {
                Text("코스 선택")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                templates.forEach {
                    DropdownMenuItem(
                        text = { Text(it.courseName) },
                        onClick = {
                            viewModel.onTemplateSelected(it.id)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("새 코스 추가") },
                    onClick = {
                        expanded = false
                        onNavigateTemplateCreate()
                    }
                )
            }

            OutlinedTextField(
                value = form.courseName,
                onValueChange = { viewModel.updateForm { old -> old.copy(courseName = it) } },
                label = { Text("코스명") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.departure,
                onValueChange = { viewModel.updateForm { old -> old.copy(departure = it) } },
                label = { Text("출발지") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.destination,
                onValueChange = { viewModel.updateForm { old -> old.copy(destination = it) } },
                label = { Text("목적지") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.waypoints.joinToString(", "),
                onValueChange = {
                    viewModel.updateForm { old ->
                        old.copy(waypoints = it.split(",").map(String::trim).filter(String::isNotBlank))
                    }
                },
                label = { Text("경유지(쉼표 구분)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.bikeType,
                onValueChange = { viewModel.updateForm { old -> old.copy(bikeType = it) } },
                label = { Text("자전거 종류") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.note,
                onValueChange = { viewModel.updateForm { old -> old.copy(note = it) } },
                label = { Text("비고") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onNavigateTemplateCreate) {
                    Text("새 코스 추가")
                }
            }
        }
    }
}
