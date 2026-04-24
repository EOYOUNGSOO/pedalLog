package app.pedallog.android.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    intentUri: Uri?,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Home", style = MaterialTheme.typography.headlineSmall)
        Text("공유된 파일: ${intentUri ?: "없음"}")
        Text("최근 라이딩 개수: ${uiState.recentSessions.size}")
        Text("Notion 설정 여부: ${if (uiState.isNotionConfigured) "설정됨" else "미설정"}")
        uiState.errorMessage?.let { message ->
            Text("오류: $message", color = MaterialTheme.colorScheme.error)
        }
    }
}
