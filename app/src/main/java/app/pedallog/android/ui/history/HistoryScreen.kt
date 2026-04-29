package app.pedallog.android.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.ui.component.PedalAdBanner
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.theme.PedalBgCard
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalBorder
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalErrorBg
import app.pedallog.android.ui.theme.PedalInfo
import app.pedallog.android.ui.theme.PedalInfoBg
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg
import app.pedallog.android.ui.theme.PedalYellowDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HistoryScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.retrySuccessId, uiState.retryErrorMsg) {
        when {
            uiState.retrySuccessId != null -> {
                snackbarHostState.showSnackbar("✓ Notion 재등록 완료!")
                viewModel.clearRetryResult()
            }

            uiState.retryErrorMsg != null -> {
                snackbarHostState.showSnackbar("✗ 재전송 실패: ${uiState.retryErrorMsg}")
                viewModel.clearRetryResult()
            }
        }
    }

    Scaffold(
        topBar = { PedalAppBar(title = "전송 이력") },
        bottomBar = {
            PedalAdBanner(modifier = Modifier.fillMaxWidth())
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PedalBgCard,
                    contentColor = PedalTextPrimary,
                    actionColor = PedalYellow
                )
            }
        },
        containerColor = PedalBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HistoryFilterChips(
                currentFilter = uiState.currentFilter,
                allCount = uiState.allSessions.size,
                successCount = uiState.allSessions.count { it.notionPageId != null },
                failedCount = uiState.allSessions.count { it.notionPageId == null },
                onFilterChange = viewModel::setFilter
            )

            PedalDivider()

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PedalYellow)
                    }
                }

                uiState.filteredSessions.isEmpty() -> {
                    EmptyHistoryContent(filter = uiState.currentFilter)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredSessions, key = { it.id }) { session ->
                            HistoryItem(
                                session = session,
                                isRetrying = uiState.retryingId == session.id,
                                onTap = { onNavigateToDetail(session.id) },
                                onRetry = { viewModel.retryRegister(session.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterChips(
    currentFilter: HistoryViewModel.FilterType,
    allCount: Int,
    successCount: Int,
    failedCount: Int,
    onFilterChange: (HistoryViewModel.FilterType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple(HistoryViewModel.FilterType.ALL, "전체 $allCount", PedalYellow),
            Triple(HistoryViewModel.FilterType.SUCCESS, "성공 $successCount", PedalSuccess),
            Triple(HistoryViewModel.FilterType.FAILED, "실패 $failedCount", PedalError)
        ).forEach { (filter, label, color) ->
            val selected = currentFilter == filter
            FilterChip(
                selected = selected,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (color) {
                        PedalYellow -> PedalYellowBg
                        PedalSuccess -> PedalSuccessBg
                        else -> PedalErrorBg
                    },
                    selectedLabelColor = color,
                    labelColor = PedalTextMuted,
                    containerColor = PedalBgSection
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = color,
                    borderColor = PedalBorder,
                    borderWidth = 0.5.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
    }
}

@Composable
private fun HistoryItem(
    session: RidingSessionEntity,
    isRetrying: Boolean,
    onTap: () -> Unit,
    onRetry: () -> Unit
) {
    val isSuccess = session.notionPageId != null
    val accentColor = if (isSuccess) PedalYellow else PedalError
    val bgColor = if (isSuccess) PedalYellowBg else PedalErrorBg
    val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(session.startTime))
    val distKm = "%.1fkm".format(session.totalDistanceM / 1000.0)
    val speed = "%.1fkm/h".format(session.avgSpeedKmh)
    val durationMin = TimeUnit.MILLISECONDS.toMinutes(session.endTime - session.startTime)

    Card(
        onClick = onTap,
        colors = CardDefaults.cardColors(containerColor = PedalBgCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, if (isSuccess) PedalYellowDark else PedalError)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PedalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, accentColor)
                    ) {
                        Text(
                            text = if (isSuccess) "✓  성공" else "✗  실패",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "$dateStr  ·  $distKm  ·  $speed",
                    style = MaterialTheme.typography.bodySmall,
                    color = PedalTextMuted
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (session.sourceFormat) {
                            "GPX" -> PedalSuccessBg
                            "TCX" -> PedalYellowBg
                            else -> PedalInfoBg
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = session.sourceFormat,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (session.sourceFormat) {
                                "GPX" -> PedalSuccess
                                "TCX" -> PedalYellow
                                else -> PedalInfo
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${durationMin}분",
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextMuted
                    )
                }
            }

            if (!isSuccess) {
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    if (isRetrying) {
                        CircularProgressIndicator(
                            color = PedalYellow,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Surface(
                                color = PedalYellowBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "재전송",
                                    tint = PedalYellow,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent(filter: HistoryViewModel.FilterType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (filter == HistoryViewModel.FilterType.FAILED) "🎉" else "🚴",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = when (filter) {
                    HistoryViewModel.FilterType.ALL -> "아직 등록된 라이딩이 없습니다"
                    HistoryViewModel.FilterType.SUCCESS -> "성공한 라이딩이 없습니다"
                    HistoryViewModel.FilterType.FAILED -> "실패한 라이딩이 없습니다 🎉"
                },
                style = MaterialTheme.typography.titleMedium,
                color = PedalTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (filter) {
                    HistoryViewModel.FilterType.ALL -> "trimm Cycling에서 라이딩 파일을\n공유하면 자동으로 등록됩니다"
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = PedalTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
