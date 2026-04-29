package app.pedallog.android.ui.confirm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.data.db.entity.RidingSessionEntity
import app.pedallog.android.data.db.entity.RidingTemplateEntity
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalAutoFillField
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.component.PedalOutlineButton
import app.pedallog.android.ui.component.PedalPrimaryButton
import app.pedallog.android.ui.component.PedalStatCard
import app.pedallog.android.ui.component.PedalTextField
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
import app.pedallog.android.ui.theme.PedalTextSecondary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg
import app.pedallog.android.ui.theme.PedalYellowDark
import coil.compose.AsyncImage
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun ConfirmScreen(
    sessionId: Long,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(uiState.registerState) {
        // 자동 이동 없음 — 사용자가 확인 버튼을 눌러서 이동
    }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = "라이딩 정보 확인",
                showBackButton = true,
                onBackClick = onBackClick
            )
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RouteImageCard(routeImagePath = uiState.session?.routeImagePath)

            CourseDropdownSection(
                templates = uiState.templates,
                selectedTemplate = uiState.selectedTemplate,
                customTitleInput = uiState.customTitleInput,
                isExpanded = uiState.isTemplateDropdownExpanded,
                onExpandedChange = viewModel::setDropdownExpanded,
                onSelect = viewModel::selectTemplate,
                onCustomTitleChanged = viewModel::updateCustomTitleInput,
                onDismiss = viewModel::closeDropdown
            )

            AutoFillSection(session = uiState.session)

            PedalTextField(
                value = uiState.editableMemo,
                onValueChange = viewModel::updateMemo,
                label = "비고",
                placeholder = "메모를 입력하세요 (선택)"
            )

            PedalDivider()

            ParsedDataSection(session = uiState.session)

            Spacer(Modifier.height(4.dp))

            RegisterSection(
                state = uiState.registerState,
                onRegist = viewModel::registerToNotion,
                onRetry = viewModel::registerToNotion,
                onCancel = viewModel::clearError,
                onSuccess = onSuccess
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RouteImageCard(routeImagePath: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = PedalYellow,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "경로 이미지 생성 중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextMuted
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CourseDropdownSection(
    templates: List<RidingTemplateEntity>,
    selectedTemplate: RidingTemplateEntity?,
    customTitleInput: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (RidingTemplateEntity) -> Unit,
    onCustomTitleChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "라이딩명 선택 (필수)",
            style = MaterialTheme.typography.labelMedium,
            color = PedalYellow
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = selectedTemplate?.let {
                    if (it.isFavorite) "⭐  ${it.templateName}" else it.templateName
                } ?: "코스를 선택해주세요",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PedalYellow,
                    unfocusedBorderColor = PedalYellow,
                    focusedContainerColor = PedalYellowBg,
                    unfocusedContainerColor = PedalYellowBg,
                    focusedTextColor = PedalYellow,
                    unfocusedTextColor = PedalYellow
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                trailingIcon = {
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = PedalYellow
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = onDismiss,
                modifier = Modifier.background(PedalBgCard)
            ) {
                if (templates.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "등록된 코스가 없습니다\n템플릿 탭에서 코스를 추가해주세요",
                                color = PedalTextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = onDismiss
                    )
                } else {
                    val favorites = templates.filter { it.isFavorite }
                    val normals = templates.filter { !it.isFavorite }

                    if (favorites.isNotEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "⭐ 즐겨찾기",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PedalYellow
                                )
                            },
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.background(PedalYellowBg)
                        )
                        favorites.forEach { tmpl ->
                            CourseDropdownItem(
                                template = tmpl,
                                isSelected = tmpl.id == selectedTemplate?.id,
                                onSelect = onSelect
                            )
                        }
                        if (normals.isNotEmpty()) {
                            PedalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "전체 코스",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PedalTextMuted
                                    )
                                },
                                onClick = { },
                                enabled = false
                            )
                        }
                    }
                    normals.forEach { tmpl ->
                        CourseDropdownItem(
                            template = tmpl,
                            isSelected = tmpl.id == selectedTemplate?.id,
                            onSelect = onSelect
                        )
                    }
                }
            }
        }

        PedalTextField(
            value = customTitleInput,
            onValueChange = onCustomTitleChanged,
            label = "라이딩명 직접 입력",
            placeholder = "목록에 없으면 입력하세요 (자동 템플릿 등록)"
        )
    }
}

@Composable
private fun CourseDropdownItem(
    template: RidingTemplateEntity,
    isSelected: Boolean,
    onSelect: (RidingTemplateEntity) -> Unit
) {
    DropdownMenuItem(
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.templateName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PedalYellow else PedalTextPrimary
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PedalYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val route = listOfNotNull(template.departure, template.destination)
                    .joinToString(" → ")
                if (route.isNotEmpty()) {
                    Text(
                        text = route,
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextMuted
                    )
                }
            }
        },
        onClick = { onSelect(template) }
    )
}

@Composable
private fun AutoFillSection(session: RidingSessionEntity?) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        session?.departure?.takeIf { it.isNotBlank() }?.let {
            PedalAutoFillField(label = "출발지", value = it)
        }

        session?.waypoints?.takeIf { it.isNotBlank() }?.let { json ->
            val waypointsText = try {
                val arr = org.json.JSONArray(json)
                (0 until arr.length()).map { i -> arr.getString(i) }.joinToString(", ")
            } catch (_: Exception) {
                json
            }
            if (waypointsText.isNotBlank()) {
                PedalAutoFillField(label = "경유지", value = waypointsText)
            }
        }

        session?.destination?.takeIf { it.isNotBlank() }?.let {
            PedalAutoFillField(label = "목적지", value = it)
        }

        session?.bikeType?.takeIf { it.isNotBlank() }?.let {
            PedalAutoFillField(label = "자전거 종류", value = it)
        }

        if (session?.departure == null && session?.destination == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PedalBgSection),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, PedalBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PedalTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "코스를 선택하면 출발지·목적지가 자동으로 채워집니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedDataSection(session: RidingSessionEntity?) {
    if (session == null) return

    val durationMin = TimeUnit.MILLISECONDS.toMinutes(session.endTime - session.startTime)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "자동 추출 데이터",
                style = MaterialTheme.typography.labelMedium,
                color = PedalTextMuted
            )
            Surface(
                color = when (session.sourceFormat) {
                    "GPX" -> PedalSuccessBg
                    "TCX" -> PedalYellowBg
                    "FIT" -> PedalInfoBg
                    else -> PedalBgSection
                },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(
                    0.5.dp,
                    when (session.sourceFormat) {
                        "GPX" -> PedalSuccess
                        "TCX" -> PedalYellow
                        "FIT" -> PedalInfo
                        else -> PedalBorder
                    }
                )
            ) {
                Text(
                    text = session.sourceFormat,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (session.sourceFormat) {
                        "GPX" -> PedalSuccess
                        "TCX" -> PedalYellow
                        "FIT" -> PedalInfo
                        else -> PedalTextMuted
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PedalStatCard(
                value = "%.1f".format(session.totalDistanceM / 1000.0),
                unit = "km",
                label = "거리",
                modifier = Modifier.weight(1f)
            )
            PedalStatCard(
                value = "$durationMin",
                unit = "분",
                label = "시간",
                modifier = Modifier.weight(1f)
            )
            PedalStatCard(
                value = "%.1f".format(session.avgSpeedKmh),
                unit = "km/h",
                label = "평균속도",
                modifier = Modifier.weight(1f)
            )
            session.calories?.let {
                PedalStatCard(
                    value = "$it",
                    unit = "kcal",
                    label = "칼로리",
                    modifier = Modifier.weight(1f)
                )
            } ?: Spacer(Modifier.weight(1f))
        }

        val subStats = buildList {
            session.avgCadence?.let { add(Triple("$it", "rpm", "케이던스")) }
            session.elevationUp?.let { add(Triple("%.1f".format(it), "m", "상승고도")) }
            session.maxSpeedKmh.takeIf { it > 0 }?.let {
                add(Triple("%.1f".format(it), "km/h", "최고속도"))
            }
            session.avgHeartRate?.let { add(Triple("$it", "bpm", "평균심박수")) }
        }

        subStats.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (v, u, l) ->
                    PedalStatCard(
                        value = v,
                        unit = u,
                        label = l,
                        modifier = Modifier.weight(1f),
                        valueColor = PedalTextSecondary
                    )
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RegisterSection(
    state: ConfirmViewModel.RegisterState,
    onRegist: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "RegisterStateTransition"
    ) { currentState ->
        when (currentState) {
            is ConfirmViewModel.RegisterState.IDLE -> {
                PedalPrimaryButton(
                    text = "🚀  Notion에 등록",
                    onClick = onRegist,
                    icon = Icons.Default.Upload
                )
            }

            is ConfirmViewModel.RegisterState.UPLOADING,
            is ConfirmViewModel.RegisterState.CREATING,
            is ConfirmViewModel.RegisterState.ATTACHING -> {
                val (stepLabel, stepNumber, progress) = when (currentState) {
                    is ConfirmViewModel.RegisterState.UPLOADING ->
                        Triple("경로 이미지 업로드 중...", 1, 0.33f)
                    is ConfirmViewModel.RegisterState.CREATING ->
                        Triple("Notion 페이지 생성 중...", 2, 0.66f)
                    else ->
                        Triple("이미지 블록 첨부 중...", 3, 0.90f)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PedalBgCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, PedalYellowDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = PedalYellow,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = stepLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PedalTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PedalYellow,
                            trackColor = PedalBgSection
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labels = listOf("이미지 업로드", "페이지 생성", "이미지 첨부")
                            labels.forEachIndexed { index, label ->
                                val done = index < stepNumber - 1
                                val current = index == stepNumber - 1
                                Text(
                                    text = if (done) "✓ $label" else label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        done -> PedalSuccess
                                        current -> PedalYellow
                                        else -> PedalTextMuted
                                    },
                                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            is ConfirmViewModel.RegisterState.SUCCESS -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PedalSuccessBg),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PedalSuccess)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PedalSuccess,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Notion 등록 완료!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PedalSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "확인 버튼을 눌러 홈으로 이동하세요.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PedalTextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PedalPrimaryButton(
                            text = "확인(닫기)",
                            onClick = onSuccess,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            is ConfirmViewModel.RegisterState.ERROR -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PedalErrorBg),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PedalError)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = PedalError,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "등록 실패",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PedalError,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PedalTextSecondary
                                )
                            }
                        }
                    }
                    PedalPrimaryButton(
                        text = "재시도",
                        onClick = onRetry,
                        icon = Icons.Default.Refresh
                    )
                    PedalOutlineButton(text = "취소", onClick = onCancel)
                }
            }
        }
    }
}
