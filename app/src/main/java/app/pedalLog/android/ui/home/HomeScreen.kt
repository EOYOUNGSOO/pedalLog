package app.pedallog.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.ui.component.PedalAdBanner
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalCard
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalInfo
import app.pedallog.android.ui.theme.PedalInfoBg
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { PedalAppBar(title = "PedalLog") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReceive,
                containerColor = PedalYellow,
                contentColor = PedalTextOnYellow
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "라이딩 추가"
                )
            }
        },
        bottomBar = {
            PedalAdBanner(modifier = Modifier.fillMaxWidth())
        },
        containerColor = PedalBgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "최근 라이딩 ${uiState.recentSessions.size}건",
                    color = PedalTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (uiState.recentSessions.isEmpty()) {
                    Text(
                        "아직 저장된 라이딩이 없습니다.\n우측 하단 + 버튼으로 파일을 가져오세요.",
                        color = PedalTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    uiState.recentSessions.take(5).forEach { session ->
                        PedalCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToDetail(session.id) }
                        ) {
                            val dateText = SimpleDateFormat(
                                "yyyy.MM.dd",
                                Locale.getDefault()
                            ).format(Date(session.startTime))

                            Surface(
                                color = when (session.sourceFormat) {
                                    "GPX" -> PedalSuccessBg
                                    "TCX" -> PedalYellowBg
                                    else -> PedalInfoBg
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$dateText · ${session.sourceFormat}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (session.sourceFormat) {
                                        "GPX" -> PedalSuccess
                                        "TCX" -> PedalYellow
                                        else -> PedalInfo
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = PedalTextPrimary
                            )
                            val durationMin = TimeUnit.MILLISECONDS.toMinutes(
                                session.endTime - session.startTime
                            )
                            Text(
                                text = "%.1fkm · %.1fkm/h".format(
                                    session.totalDistanceM / 1000.0,
                                    session.avgSpeedKmh
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = PedalTextMuted
                            )
                            Text(
                                text = "소요시간 ${durationMin}분",
                                style = MaterialTheme.typography.bodySmall,
                                color = PedalTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
