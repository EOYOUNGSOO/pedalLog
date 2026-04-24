package app.pedallog.android.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalErrorBg
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow

@Composable
fun PedalPhaseBadge(phase: String) {
    val (bg, fc) = when (phase) {
        "P1" -> PedalYellow to PedalTextOnYellow
        "P2" -> PedalSuccess to PedalTextPrimary
        "P3" -> Color(0xFF9B59B6) to PedalTextPrimary
        else -> PedalBgSection to PedalTextMuted
    }
    Surface(color = bg) {
        Text(
            phase,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fc,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PedalStatusChip(isSuccess: Boolean) {
    val bg = if (isSuccess) PedalSuccessBg else PedalErrorBg
    val border = if (isSuccess) PedalSuccess else PedalError
    val text = if (isSuccess) "성공" else "실패"
    val color = if (isSuccess) PedalSuccess else PedalError

    Surface(
        color = bg,
        border = BorderStroke(0.5.dp, border)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalBadgePreview() {
    PedalLogTheme {
        Row {
            PedalPhaseBadge("P1")
            PedalStatusChip(isSuccess = true)
        }
    }
}
