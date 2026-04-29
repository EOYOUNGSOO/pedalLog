package app.pedallog.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    data object Home : BottomNavItem(
        route = NavRoutes.HOME,
        label = "홈",
        icon = Icons.Default.Home,
        contentDescription = "홈 화면"
    )

    data object Dashboard : BottomNavItem(
        route = NavRoutes.DASHBOARD,
        label = "대시보드",
        icon = Icons.Default.BarChart,
        contentDescription = "라이딩 대시보드"
    )

    data object Template : BottomNavItem(
        route = NavRoutes.TEMPLATE,
        label = "템플릿",
        icon = Icons.AutoMirrored.Filled.List,
        contentDescription = "코스 템플릿"
    )

    data object Settings : BottomNavItem(
        route = NavRoutes.SETTINGS,
        label = "설정",
        icon = Icons.Default.Settings,
        contentDescription = "앱 설정"
    )

    companion object {
        val items = listOf(Home, Dashboard, Template, Settings)
    }
}
