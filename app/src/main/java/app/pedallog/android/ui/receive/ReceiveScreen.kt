package app.pedallog.android.ui.receive

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalTextMuted

@Composable
fun ReceiveScreen(
    intentUri: Uri?,
    onParsed: (Long) -> Unit,
    onError: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ReceiveViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            PedalAppBar(
                title = "파일 수신 중",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = PedalBgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ReceiveScreen — 작업 #7, #8에서 구현 예정\nURI: ${intentUri ?: "없음"}",
                color = PedalTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
