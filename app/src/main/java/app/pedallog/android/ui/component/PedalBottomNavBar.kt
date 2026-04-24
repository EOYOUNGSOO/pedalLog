package app.pedallog.android.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalDimen
import app.pedallog.android.ui.theme.PedalLogTheme
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg

@Composable
fun PedalBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("home", "홈", Icons.Default.Home),
        BottomNavItem("dashboard", "대시보드", Icons.Default.Settings),
        BottomNavItem("templateList", "템플릿", Icons.Default.List),
        BottomNavItem("settings", "설정", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = PedalBgSection,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(PedalDimen.IconMedium)
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PedalYellow,
                    selectedTextColor = PedalYellow,
                    unselectedIconColor = PedalTextMuted,
                    unselectedTextColor = PedalTextMuted,
                    indicatorColor = PedalYellowBg
                )
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PedalBottomNavBarPreview() {
    PedalLogTheme { PedalBottomNavBar(currentRoute = "home", onNavigate = {}) }
}
