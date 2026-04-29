package app.pedallog.android.ui.template

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
import androidx.hilt.navigation.compose.hiltViewModel
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalTextMuted

@Composable
fun TemplateEditPlaceholderScreen(
    templateId: Long?,
    onSaved: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: TemplateEditViewModel = hiltViewModel()
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "TemplateEditScreen (id: ${templateId ?: "신규"})\n— 이후 작업에서 구현 예정",
                color = PedalTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
