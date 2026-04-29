package app.pedallog.android.ui.history

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.component.PedalOutlineButton
import app.pedallog.android.ui.component.PedalPrimaryButton
import app.pedallog.android.ui.component.PedalStatCard
import app.pedallog.android.ui.theme.PedalBgCard
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalBorder
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalErrorBg
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalTextSecondary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg
import app.pedallog.android.ui.theme.PedalYellowDark
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import org.json.JSONArray

@Composable
fun HistoryDetailScreen(
    sessionId: Long,
    onBackClick: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(uiState.retrySuccess, uiState.retryErrorMsg, uiState.saveSuccess, uiState.saveErrorMsg, uiState.deleteErrorMsg) {
        when {
            uiState.retrySuccess -> {
                snackbarHostState.showSnackbar(uiState.retrySuccessMsg ?: "✓ Notion 재등록 완료!")
                viewModel.clearRetryResult()
            }
            uiState.retryErrorMsg != null -> {
                snackbarHostState.showSnackbar("✗ ${uiState.retryErrorMsg}")
                viewModel.clearRetryResult()
            }
            uiState.saveSuccess -> {
                val message = uiState.saveErrorMsg ?: "✓ 수정 내용 저장 및 Notion 반영 완료"
                snackbarHostState.showSnackbar(message)
                viewModel.clearSaveSuccess()
            }
            uiState.deleteErrorMsg != null -> {
                snackbarHostState.showSnackbar("✗ ${uiState.deleteErrorMsg}")
                viewModel.clearDeleteError()
            }
        }
    }

    // 삭제 확인 다이얼로그
    if (uiState.showDeleteConfirmDialog) {
        val notionMsg = if (!uiState.session?.notionPageId.isNullOrBlank()) {
            "\n\nNotion에 등록된 페이지도 함께 삭제됩니다."
        } else {
            ""
        }

        Dialog(
            onDismissRequest = viewModel::cancelDelete,
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PedalBgCard),
                    border = BorderStroke(1.dp, PedalBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "라이딩 삭제",
                            color = PedalTextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "이 라이딩 기록을 삭제하시겠습니까?$notionMsg",
                            color = PedalTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(color = PedalBorder, thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = viewModel::cancelDelete) {
                                Text("취소", color = PedalTextSecondary)
                            }
                            TextButton(onClick = { viewModel.confirmDelete(onBackClick) }) {
                                Text("삭제", color = PedalError, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = if (uiState.isEditing) "라이딩 수정" else "라이딩 상세",
                showBackButton = true,
                onBackClick = if (uiState.isEditing) viewModel::cancelEditing else onBackClick,
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(Icons.Default.Edit, contentDescription = "수정", tint = PedalTextOnYellow)
                        }
                        IconButton(onClick = viewModel::requestDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = PedalTextOnYellow)
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PedalBgCard,
                    contentColor = PedalTextPrimary
                )
            }
        },
        containerColor = PedalBgDark
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PedalYellow)
            }
            return@Scaffold
        }

        val session = uiState.session ?: return@Scaffold

        if (uiState.isDeleting) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = PedalYellow)
                    Text("삭제 중...", color = PedalTextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            return@Scaffold
        }

        if (uiState.isEditing) {
            // ── 편집 모드 폼 ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("수정 가능한 항목", style = MaterialTheme.typography.titleSmall, color = PedalYellow, fontWeight = FontWeight.Bold)

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PedalYellow,
                    unfocusedBorderColor = PedalBorder,
                    focusedLabelColor = PedalYellow,
                    unfocusedLabelColor = PedalTextMuted,
                    cursorColor = PedalYellow,
                    focusedTextColor = PedalTextPrimary,
                    unfocusedTextColor = PedalTextPrimary
                )

                OutlinedTextField(
                    value = uiState.editDeparture,
                    onValueChange = viewModel::onEditDepartureChange,
                    label = { Text("출발지") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.editDestination,
                    onValueChange = viewModel::onEditDestinationChange,
                    label = { Text("목적지") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.editBikeType,
                    onValueChange = viewModel::onEditBikeTypeChange,
                    label = { Text("자전거 종류") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.editMemo,
                    onValueChange = viewModel::onEditMemoChange,
                    label = { Text("비고") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    minLines = 3,
                    maxLines = 5
                )

                PedalPrimaryButton(
                    text = "저장(노션에 반영)",
                    onClick = viewModel::saveEdits,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RouteImageSection(routeImagePath = session.routeImagePath)
            RidingHeaderSection(session = session)
            PedalDivider()
            RidingStatsSection(session = session)
            PedalDivider()
            RouteInfoSection(session = session)
            PedalDivider()
            ActionButtonSection(
                session = session,
                isRetrying = uiState.isRetrying,
                isReparsing = uiState.isReparsing,
                onNotion = {
                    session.notionPageId?.let { pageId ->
                        val url = "https://notion.so/${pageId.replace("-", "")}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                onRetry = viewModel::retryRegister,
                onReparseRetry = viewModel::reparseAndRetryRegister
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RouteImageSection(routeImagePath: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = PedalBgCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, PedalYellowDark)
    ) {
        if (routeImagePath != null && File(routeImagePath).exists()) {
            AsyncImage(
                model = File(routeImagePath),
                contentDescription = "라이딩 경로",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = PedalTextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "경로 이미지 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RidingHeaderSection(session: RidingSessionEntity) {
    val isSuccess = session.notionPageId != null
    val dateStr = SimpleDateFormat("yyyy.MM.dd  EEE", Locale.KOREAN).format(Date(session.startTime))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.headlineMedium,
                color = PedalYellow,
                fontWeight = FontWeight.Black
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextMuted
            )
            session.bikeType?.takeIf { it.isNotBlank() }?.let { bikeType ->
                Surface(color = PedalBgSection, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = bikeType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = PedalTextMuted
                    )
                }
            }
        }

        Surface(
            color = if (isSuccess) PedalSuccessBg else PedalErrorBg,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, if (isSuccess) PedalSuccess else PedalError)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) PedalSuccess else PedalError,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isSuccess) "등록 완료" else "미등록",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSuccess) PedalSuccess else PedalError,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RidingStatsSection(session: RidingSessionEntity) {
    val durationMin = TimeUnit.MILLISECONDS.toMinutes(session.endTime - session.startTime)
    val mainStats = buildList {
        add(Triple("%.1f".format(session.totalDistanceM / 1000.0), "km", "거리"))
        add(Triple("$durationMin", "분", "시간"))
        add(Triple("%.1f".format(session.avgSpeedKmh), "km/h", "평균속도"))
        session.calories?.let { add(Triple("$it", "kcal", "칼로리")) }
    }
    val subStats = buildList {
        session.avgCadence?.let { add(Triple("$it", "rpm", "케이던스")) }
        session.elevationUp?.let { add(Triple("%.0f".format(it), "m", "상승고도")) }
        session.maxSpeedKmh.takeIf { it > 0 }?.let { add(Triple("%.1f".format(it), "km/h", "최고속도")) }
        session.avgHeartRate?.let { add(Triple("$it", "bpm", "평균심박수")) }
        session.maxHeartRate?.let { add(Triple("$it", "bpm", "최고심박수")) }
        session.avgPower?.let { add(Triple("$it", "W", "평균파워")) }
        session.maxPower?.let { add(Triple("$it", "W", "최고파워")) }
        session.maxCadence?.let { add(Triple("$it", "rpm", "최고케이던스")) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        mainStats.chunked(4).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowStats.forEach { (v, u, l) ->
                    PedalStatCard(
                        value = v,
                        unit = u,
                        label = l,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowStats.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }

        subStats.chunked(4).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowStats.forEach { (v, u, l) ->
                    PedalStatCard(
                        value = v,
                        unit = u,
                        label = l,
                        modifier = Modifier.weight(1f),
                        valueColor = PedalTextSecondary
                    )
                }
                repeat(4 - rowStats.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RouteInfoSection(session: RidingSessionEntity) {
    val hasRouteInfo = listOf(session.departure, session.waypoints, session.destination).any { !it.isNullOrBlank() }
    if (!hasRouteInfo && session.memo.isNullOrBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PedalBgCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, PedalBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "경로 정보",
                style = MaterialTheme.typography.labelMedium,
                color = PedalTextMuted,
                fontWeight = FontWeight.Bold
            )

            session.departure?.takeIf { it.isNotBlank() }?.let {
                RouteInfoRow(icon = Icons.Default.TripOrigin, label = "출발지", value = it, color = PedalSuccess)
            }

            session.waypoints?.takeIf { it.isNotBlank() }?.let { json ->
                val list = try {
                    val arr = JSONArray(json)
                    (0 until arr.length()).map { i -> arr.getString(i) }.filter { it.isNotBlank() }
                } catch (_: Exception) {
                    emptyList()
                }

                list.forEachIndexed { index, waypoint ->
                    RouteInfoRow(
                        icon = Icons.Default.RadioButtonChecked,
                        label = if (list.size > 1) "경유지 ${index + 1}" else "경유지",
                        value = waypoint,
                        color = PedalYellow
                    )
                }
            }

            session.destination?.takeIf { it.isNotBlank() }?.let {
                RouteInfoRow(icon = Icons.Default.Place, label = "목적지", value = it, color = PedalError)
            }

            session.memo?.takeIf { it.isNotBlank() }?.let {
                PedalDivider()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = PedalTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PedalTextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtonSection(
    session: RidingSessionEntity,
    isRetrying: Boolean,
    isReparsing: Boolean,
    onNotion: () -> Unit,
    onRetry: () -> Unit,
    onReparseRetry: () -> Unit
) {
    val isSuccess = session.notionPageId != null

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isSuccess) {
            PedalPrimaryButton(
                text = "📋  Notion에서 보기",
                onClick = onNotion,
                icon = Icons.Default.OpenInBrowser
            )
        }

        if (!isSuccess) {
            if (isRetrying) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PedalYellowBg),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, PedalYellow)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = PedalYellow,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (isReparsing) "재계산 후 Notion 재등록 중..." else "Notion 재등록 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PedalYellow
                        )
                    }
                }
            } else {
                PedalPrimaryButton(
                    text = "🔄  Notion에 재등록",
                    onClick = onRetry,
                    icon = Icons.Default.Refresh
                )
                PedalOutlineButton(
                    text = "정밀 재계산 후 재등록",
                    onClick = onReparseRetry
                )
            }
        }
    }
}
