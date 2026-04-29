package app.pedallog.android.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalBorder
import app.pedallog.android.ui.theme.PedalDimen
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow

@Composable
fun PedalPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(PedalDimen.ButtonHeight),
        shape = RoundedCornerShape(PedalDimen.RadiusButton),
        border = BorderStroke(1.dp, PedalBorder),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = PedalYellow,
            contentColor = PedalTextOnYellow,
            disabledContainerColor = PedalBgSection,
            disabledContentColor = PedalTextMuted
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(PedalDimen.IconSmall))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PedalOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(PedalDimen.ButtonHeight),
        shape = RoundedCornerShape(PedalDimen.RadiusButton),
        border = BorderStroke(1.2.dp, PedalBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PedalYellow)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = PedalYellow, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PedalDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(PedalDimen.ButtonHeight),
        shape = RoundedCornerShape(PedalDimen.RadiusButton),
        border = BorderStroke(1.dp, PedalBorder),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = PedalError,
            contentColor = PedalTextPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalButtonPreview() {
    PedalLogTheme { PedalPrimaryButton(text = "확인", onClick = {}) }
}
