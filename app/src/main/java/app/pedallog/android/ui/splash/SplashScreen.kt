package app.pedallog.android.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.pedallog.android.ui.theme.PedalYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) { delay(1000); onFinished() }
    Box(Modifier.fillMaxSize().background(PedalYellow), contentAlignment = Alignment.Center) { Text("PedalLog") }
}
