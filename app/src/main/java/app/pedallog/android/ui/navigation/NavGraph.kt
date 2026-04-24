package app.pedallog.android.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.pedallog.android.ui.component.PedalBottomNavBar
import app.pedallog.android.ui.dashboard.DashboardScreen
import app.pedallog.android.ui.history.HistoryScreen
import app.pedallog.android.ui.home.HomeScreen
import app.pedallog.android.ui.settings.SettingsScreen
import app.pedallog.android.ui.splash.SplashScreen
import app.pedallog.android.ui.template.TemplateListScreen
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun PedalLogNavGraph(intentUri: Uri?) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    Scaffold(bottomBar = {
        Column {
            PedalBottomNavBar(currentRoute = currentRoute, onNavigate = { route -> nav.navigate(route) })
            AdBanner()
        }
    }) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } } }
            composable("home") { HomeScreen(intentUri) }
            composable("templateList") { TemplateListScreen() }
            composable("history") { HistoryScreen() }
            composable("dashboard") { DashboardScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
private fun AdBanner() {
    AndroidView(modifier = Modifier.fillMaxWidth(), factory = { c -> AdView(c).apply { setAdSize(AdSize.BANNER); adUnitId = "ca-app-pub-3940256099942544/6300978111"; loadAd(AdRequest.Builder().build()) } })
}
