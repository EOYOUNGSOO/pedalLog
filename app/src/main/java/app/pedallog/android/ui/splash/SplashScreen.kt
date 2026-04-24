package app.pedallog.android.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.R
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalYellow

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PedalBgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = "PedalLog",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "PedalLog",
                style = MaterialTheme.typography.displayMedium,
                color = PedalYellow,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "자전거 라이딩 기록 허브",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextMuted
            )
            Spacer(Modifier.height(48.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(200.dp),
                color = PedalYellow,
                trackColor = PedalBgSection
            )
        }
    }

    LaunchedEffect(uiState.isReady) {
        if (uiState.isReady) {
            if (uiState.isNotionConfigured) {
                onNavigateToHome()
            } else {
                onNavigateToSettings()
            }
        }
    }
}
