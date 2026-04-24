package app.pedallog.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PedalLogDarkColorScheme = darkColorScheme(
    primary = PedalYellow,
    onPrimary = PedalTextOnYellow,
    primaryContainer = PedalYellowBg,
    secondary = PedalYellowDark,
    onSecondary = PedalTextOnYellow,
    background = PedalBgDark,
    onBackground = PedalTextPrimary,
    surface = PedalBgCard,
    onSurface = PedalTextPrimary,
    surfaceVariant = PedalBgSection,
    onSurfaceVariant = PedalTextSecondary,
    outline = PedalDivider,
    outlineVariant = PedalBorder,
    error = PedalError,
    onError = PedalTextPrimary,
    errorContainer = PedalErrorBg
)

@Composable
fun PedalLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PedalLogDarkColorScheme,
        typography = PedalLogTypography,
        content = content
    )
}
