package app.pedallog.android.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.pedallog.android.ui.confirm.ConfirmScreen
import app.pedallog.android.ui.dashboard.DashboardScreen
import app.pedallog.android.ui.history.HistoryDetailScreen
import app.pedallog.android.ui.home.HomeScreen
import app.pedallog.android.ui.receive.ReceiveScreen
import app.pedallog.android.ui.settings.SettingsScreen
import app.pedallog.android.ui.splash.SplashScreen
import app.pedallog.android.ui.template.TemplateEditScreen
import app.pedallog.android.ui.template.TemplateListScreen

@Composable
fun PedalLogNavGraph(intentUri: Uri? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = when {
        currentRoute == null -> false
        currentRoute == NavRoutes.SPLASH -> false
        currentRoute == NavRoutes.RECEIVE -> false
        currentRoute.startsWith("confirm/") -> false
        currentRoute.startsWith("history_detail/") -> false
        currentRoute.startsWith("template_edit") -> false
        currentRoute == NavRoutes.HOME ||
            currentRoute == NavRoutes.DASHBOARD ||
            currentRoute == NavRoutes.TEMPLATE ||
            currentRoute == NavRoutes.SETTINGS ||
            currentRoute.startsWith("dashboard/") -> true
        else -> false
    }

    LaunchedEffect(intentUri, currentRoute) {
        if (intentUri != null &&
            currentRoute != null &&
            currentRoute != NavRoutes.SPLASH &&
            currentRoute != NavRoutes.RECEIVE &&
            !currentRoute.startsWith("confirm/")
        ) {
            navController.navigate(NavRoutes.RECEIVE) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PedalBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.SPLASH) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavRoutes.SETTINGS) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.HOME) {
                HomeScreen(
                    onNavigateToDetail = { sessionId ->
                        navController.navigate(NavRoutes.historyDetail(sessionId))
                    },
                    onNavigateToReceive = {
                        navController.navigate(NavRoutes.RECEIVE)
                    }
                )
            }

            composable(
                route = NavRoutes.HISTORY_DETAIL,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { stack ->
                val sessionId = stack.arguments?.getLong("sessionId") ?: 0L
                HistoryDetailScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.RECEIVE) {
                ReceiveScreen(
                    intentUri = intentUri,
                    onParsed = { sessionId ->
                        navController.navigate(NavRoutes.confirm(sessionId)) {
                            popUpTo(NavRoutes.RECEIVE) { inclusive = true }
                        }
                    },
                    onError = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.CONFIRM,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType }
                )
            ) { stack ->
                val sessionId = stack.arguments?.getLong("sessionId") ?: 0L
                ConfirmScreen(
                    sessionId = sessionId,
                    onSuccess = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.DASHBOARD) {
                DashboardScreen()
            }

            composable(NavRoutes.TEMPLATE) {
                TemplateListScreen(
                    onNavigateToEdit = { id ->
                        navController.navigate(NavRoutes.templateEdit(id))
                    },
                    onNavigateToAdd = {
                        navController.navigate(NavRoutes.templateEdit(null))
                    }
                )
            }

            composable(
                route = NavRoutes.TEMPLATE_EDIT,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { stack ->
                val rawId = stack.arguments?.getLong("id") ?: -1L
                val templateId = rawId.takeIf { it != -1L }
                TemplateEditScreen(
                    templateId = templateId,
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
