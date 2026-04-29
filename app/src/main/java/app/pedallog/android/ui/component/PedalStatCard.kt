package app.pedallog.android.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalBgCard
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowDark

@Composable
fun PedalStatCard(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = PedalYellow
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PedalBgCard),
        border = BorderStroke(0.5.dp, PedalYellowDark)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = valueColor,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PedalTextMuted,
                    modifier = Modifier.padding(bottom = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PedalTextMuted
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalStatCardPreview() {
    PedalLogTheme { PedalStatCard(value = "70.1", unit = "km", label = "총 거리") }
}
