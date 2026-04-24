package app.pedallog.android.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.pedallog.android.data.db.entity.RidingTemplateEntity

@Composable
fun TemplateListScreen(
    viewModel: TemplateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Template List", style = MaterialTheme.typography.headlineSmall)
        Button(
            onClick = {
                viewModel.upsertTemplate(
                    RidingTemplateEntity(templateName = "샘플 코스 ${System.currentTimeMillis() % 10000}")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("샘플 템플릿 추가")
        }

        if (uiState.templates.isEmpty()) {
            Text("템플릿이 없습니다.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.templates, key = { it.id }) { template ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(template.templateName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "출발: ${template.departure ?: "-"} / 도착: ${template.destination ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(onClick = { viewModel.deleteTemplate(template) }) {
                                Text("삭제")
                            }
                        }
                    }
                }
            }
        }
    }
}
