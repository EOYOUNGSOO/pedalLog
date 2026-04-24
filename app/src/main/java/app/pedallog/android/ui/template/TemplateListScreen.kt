package app.pedallog.android.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalTextMuted

@Composable
fun TemplateListScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { PedalAppBar(title = "코스 템플릿") },
        containerColor = PedalBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "TemplateListScreen — 목록·편집 UI는 이후 작업",
                color = PedalTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onNavigateToAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("새 템플릿")
            }
            uiState.templates.forEach { template ->
                Button(
                    onClick = { onNavigateToEdit(template.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(template.templateName)
                }
            }
        }
    }
}
