package app.pedallog.android.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.dashboard.annual.AnnualScreen
import app.pedallog.android.ui.dashboard.calorie.CalorieScreen
import app.pedallog.android.ui.dashboard.course.CourseScreen
import app.pedallog.android.ui.dashboard.intensity.IntensityScreen
import app.pedallog.android.ui.dashboard.monthly.MonthlyScreen
import app.pedallog.android.ui.dashboard.time.TimeScreen
import app.pedallog.android.ui.navigation.NavRoutes
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgCard
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalYellow

@Composable
fun DashboardScreen() {
    val nestedNavController = rememberNavController()
    val nestedEntry by nestedNavController.currentBackStackEntryAsState()
    val currentNestedRoute = nestedEntry?.destination?.route

    val tabs = listOf(
        "연간" to NavRoutes.DashboardNested.ANNUAL,
        "월별" to NavRoutes.DashboardNested.MONTHLY,
        "코스" to NavRoutes.DashboardNested.COURSE,
        "칼로리" to NavRoutes.DashboardNested.CALORIE,
        "시간" to NavRoutes.DashboardNested.TIME,
        "강도" to NavRoutes.DashboardNested.INTENSITY
    )

    val selectedTabIndex = tabs.indexOfFirst { it.second == currentNestedRoute }
        .coerceAtLeast(0)

    Scaffold(
        topBar = { PedalAppBar(title = "대시보드") },
        containerColor = PedalBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PedalBgCard,
                contentColor = PedalYellow,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, (label, route) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            nestedNavController.navigate(route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        text = {
                            Text(
                                text = label,
                                color = if (selectedTabIndex == index) {
                                    PedalYellow
                                } else {
                                    PedalTextMuted
                                },
                                fontWeight = if (selectedTabIndex == index) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }

            NavHost(
                navController = nestedNavController,
                startDestination = NavRoutes.DashboardNested.ANNUAL,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavRoutes.DashboardNested.ANNUAL) { AnnualScreen() }
                composable(NavRoutes.DashboardNested.MONTHLY) { MonthlyScreen() }
                composable(NavRoutes.DashboardNested.COURSE) { CourseScreen() }
                composable(NavRoutes.DashboardNested.CALORIE) { CalorieScreen() }
                composable(NavRoutes.DashboardNested.TIME) { TimeScreen() }
                composable(NavRoutes.DashboardNested.INTENSITY) { IntensityScreen() }
            }
        }
    }
}
