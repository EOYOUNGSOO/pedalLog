package app.pedallog.android.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalTextMuted

@Composable
fun HistoryDetailScreen(
    sessionId: Long,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            PedalAppBar(
                title = "라이딩 상세",
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
                "HistoryDetailScreen (sessionId: $sessionId)\n— 이후 작업에서 구현 예정",
                color = PedalTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
