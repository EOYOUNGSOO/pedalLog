package app.pedallog.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tokenInput by remember { mutableStateOf("") }
    var dbIdInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.notionToken, uiState.notionDbId) {
        if (tokenInput != uiState.notionToken) tokenInput = uiState.notionToken
        if (dbIdInput != uiState.notionDbId) dbIdInput = uiState.notionDbId
    }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = "설정",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = PedalBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("광고 제거: ${if (uiState.adsRemoved) "예" else "아니오"}", style = MaterialTheme.typography.bodyMedium)

            OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notion Token") }
            )
            Button(
                onClick = { viewModel.saveNotionToken(tokenInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Token 저장")
            }

            OutlinedTextField(
                value = dbIdInput,
                onValueChange = { dbIdInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notion Database ID") }
            )
            Button(
                onClick = { viewModel.saveNotionDbId(dbIdInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DB ID 저장")
            }
        }
    }
}
