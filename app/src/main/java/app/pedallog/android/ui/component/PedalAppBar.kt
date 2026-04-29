package app.pedallog.android.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedalAppBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PedalTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = if (showBackButton) {
            {
                IconButton(onClick = { onBackClick?.invoke() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = PedalTextPrimary
                    )
                }
            }
        } else {
            {}
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PedalBgDark,
            titleContentColor = PedalTextPrimary,
            actionIconContentColor = PedalYellow
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalAppBarPreview() {
    PedalLogTheme { PedalAppBar(title = "PedalLog") }
}
