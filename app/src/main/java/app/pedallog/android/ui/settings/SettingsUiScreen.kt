package app.pedallog.android.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.BuildConfig
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.component.PedalOutlineButton
import app.pedallog.android.ui.component.PedalPrimaryButton
import app.pedallog.android.ui.component.PedalStatusCard
import app.pedallog.android.ui.theme.PedalBgCard
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgInput
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalBorder
import app.pedallog.android.ui.theme.PedalDimen
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalTextSecondary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg

private const val PRIVACY_POLICY_URL = "https://leaguemind-c3a4c.web.app/privacy-pedalLog.html"

@Composable
fun SettingsScreen(
    onBackClick: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showTokenGuideSheet by remember { mutableStateOf(false) }
    var showDeviceGuideSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = "설정",
                showBackButton = onBackClick != null,
                onBackClick = onBackClick ?: {}
            )
        },
        containerColor = PedalBgDark
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.isFirstLaunch) FirstLaunchBanner()
            NotionSection(
                uiState = uiState,
                onTokenInput = viewModel::updateTokenInput,
                onTokenVisible = viewModel::toggleTokenVisibility,
                onTokenSave = viewModel::saveToken,
                onDbIdInput = viewModel::updateDbIdInput,
                onDbIdSave = viewModel::saveDbId,
                onTestConnection = viewModel::testConnection,
                onClearSettings = viewModel::clearNotionSettings
            )
            AdsSection(adsRemoved = uiState.adsRemoved, onPurchase = {})
            AppInfoSection(
                onPrivacyPolicy = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                },
                onTokenGuide = { showTokenGuideSheet = true },
                onDeviceGuide = { showDeviceGuideSheet = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showTokenGuideSheet) {
        TokenGuideBottomSheet(onDismiss = { showTokenGuideSheet = false })
    }
    if (showDeviceGuideSheet) {
        DeviceGuideBottomSheet(onDismiss = { showDeviceGuideSheet = false })
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TokenGuideBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PedalBgCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Notion Integration Token 확인 방법",
                style = MaterialTheme.typography.titleMedium,
                color = PedalYellow,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "1단계 — Notion 개발자 페이지 접속\nhttps://www.notion.so/my-integrations",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary
            )
            Text(
                text = "2단계 — Integration 선택\n접속하면 본인이 만든 Integration 목록이 보입니다.\nPedalLog용으로 만든 Integration이 없다면 \"New integration\" 버튼으로 새로 생성합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary
            )
            Text(
                text = "3단계 — Token 확인\nIntegration 클릭 → \"Secrets\" 탭 선택\n\"Show\" 클릭 → 토큰 전체 표시 → \"복사\" 버튼으로 복사",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary
            )
            Text(
                text = "4단계 — 페달로그 앱에 입력\n앱 하단 탭 설정 → Notion Integration Token 입력란에 붙여넣기\nsecret_xxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary
            )

            PedalDivider()

            Text(
                text = "Integration이 없는 경우 — 신규 생성 방법",
                style = MaterialTheme.typography.titleSmall,
                color = PedalYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "1. https://www.notion.so/my-integrations 접속\n2. + New integration 클릭\n3. 이름 입력: PedalLog\n4. Workspace 선택 (본인 워크스페이스)\n5. Submit 클릭\n6. 생성된 Token 복사",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextPrimary
            )
            Text(
                text = "생성 후 반드시 라이딩 기록 DB 페이지에 Integration 연결이 필요합니다.\n노션 DB 페이지 우측 상단 → ··· (더보기) → Connections → + Add connections → PedalLog 검색 후 선택\n이 연결이 빠지면 Token이 맞아도 404 오류가 발생합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = PedalTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AppInfoSection(
    onPrivacyPolicy: () -> Unit,
    onTokenGuide: () -> Unit,
    onDeviceGuide: () -> Unit
) {
    SettingsSectionCard("앱 정보") {
        SettingsRow(icon = Icons.Default.Description, label = "개인정보처리방침", onClick = onPrivacyPolicy)
        PedalDivider()
        SettingsRow(icon = Icons.AutoMirrored.Filled.DirectionsBike, label = "기기별 노션 항목 정의", onClick = onDeviceGuide)
        PedalDivider()
        SettingsRow(icon = Icons.Default.Info, label = "Notion Integration Token 확인 방법", onClick = onTokenGuide)
        PedalDivider()
        SettingsRow(icon = Icons.Default.Info, label = "버전", value = BuildConfig.VERSION_NAME, onClick = null)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DeviceGuideBottomSheet(onDismiss: () -> Unit) {
    data class DeviceGuide(
        val name: String,
        val fileFormat: String,
        val notionMapping: String,
        val note: String,
        val shareMethod: String
    )

    val guides = listOf(
        DeviceGuide(
            name = "trimm",
            fileFormat = "TCX/GPX/FIT",
            notionMapping = "P1 기준 대부분 자동 매핑\n(날짜, 시작/종료시간, 거리, 시간, 속도, 상승고도, 소비칼로리)",
            note = "하강고도/경로이미지는 Notion 속성 추가 권장",
            shareMethod = "1) trimm Cycling 실행\n2) 하단 기록 탭 → 라이딩 선택\n3) 우상단 공유 아이콘\n4) 파일 형식 선택(TCX 권장)\n5) 공유 대상에서 pedalLog 선택"
        ),
        DeviceGuide(
            name = "Garmin",
            fileFormat = "FIT(주) / TCX / GPX",
            notionMapping = "P1에서는 TCX 권장, P3에서 FIT 확장 지원",
            note = "가민앱에서 TCX 내보내기 선택 시 즉시 호환",
            shareMethod = "Garmin Connect 앱: 활동 선택 → ··· → 파일 내보내기(TCX) → 공유 → pedalLog\n또는 웹(connect.garmin.com)에서 원본 내보내기 후 공유"
        ),
        DeviceGuide(
            name = "Wahoo",
            fileFormat = "FIT / GPX",
            notionMapping = "GPX 중심 매핑, FIT은 확장 단계",
            note = "웹/앱 모두 Export 후 pedalLog 공유 가능",
            shareMethod = "Wahoo Fitness 앱: 운동 선택 → 공유 아이콘 → .fit 파일 → pedalLog\n또는 Wahoo 웹 대시보드에서 Export .gpx 후 공유"
        ),
        DeviceGuide(
            name = "Bryton",
            fileFormat = "GPX / TCX / FIT",
            notionMapping = "GPX/TCX 기반 기본 매핑 가능",
            note = "활동 내보내기에서 GPX 선택 권장",
            shareMethod = "Bryton Active 앱: 활동 선택 → 우상단 ··· → 내보내기 → GPX 선택 → 공유 → pedalLog"
        ),
        DeviceGuide(
            name = "iGPSPORT",
            fileFormat = "FIT / GPX",
            notionMapping = "GPX 기본 매핑, FIT 확장 단계",
            note = "앱 공유 아이콘에서 파일 공유",
            shareMethod = "iGPSPORT 앱: 활동 선택 → 공유 아이콘 → FIT 또는 GPX 선택 → pedalLog"
        ),
        DeviceGuide(
            name = "Polar/Suunto",
            fileFormat = "GPX / TCX(일부)",
            notionMapping = "GPX 기준 기본 항목 매핑 가능",
            note = "훈련 내보내기 후 pedalLog 공유",
            shareMethod = "Polar Flow 또는 Suunto 앱: 훈련 선택 → 내보내기 → GPX 선택 → pedalLog 공유"
        ),
        DeviceGuide(
            name = "CatEye",
            fileFormat = "직접 출력 없음",
            notionMapping = "직접 매핑 불가",
            note = "Strava/Komoot 우회 후 GPX 공유 필요",
            shareMethod = "1) Strava/Komoot 앱으로 기록\n2) Strava 웹에서 활동 원본 .gpx 내보내기\n3) 파일 앱에서 .gpx를 pedalLog로 공유"
        )
    )
    var selected by remember { mutableStateOf(0) }
    val current = guides[selected]

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PedalBgCard) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "기기별 노션 항목 정의",
                style = MaterialTheme.typography.titleMedium,
                color = PedalYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "상단 기기를 선택하면 파일 형식과 Notion 매핑 기준을 확인할 수 있습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = PedalTextSecondary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                guides.forEachIndexed { index, item ->
                    TextButton(
                        onClick = { selected = index },
                        modifier = Modifier
                            .background(
                                if (selected == index) PedalYellowBg else PedalBgSection,
                                RoundedCornerShape(PedalDimen.RadiusChip)
                            )
                    ) {
                        Text(
                            text = item.name,
                            color = if (selected == index) PedalYellow else PedalTextMuted,
                            fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PedalBgSection),
                border = BorderStroke(1.dp, PedalBorder),
                shape = RoundedCornerShape(PedalDimen.RadiusCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("기기: ${current.name}", color = PedalTextPrimary, fontWeight = FontWeight.Bold)
                    Text("파일 형식: ${current.fileFormat}", color = PedalTextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("노션 매핑: ${current.notionMapping}", color = PedalTextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text("비고: ${current.note}", color = PedalTextSecondary, style = MaterialTheme.typography.bodySmall)
                    PedalDivider()
                    Text("파일 공유 방법", color = PedalYellow, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PedalBgCard),
                        border = BorderStroke(1.dp, PedalBorder),
                        shape = RoundedCornerShape(PedalDimen.RadiusCard)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            current.shareMethod.lines()
                                .filter { it.isNotBlank() }
                                .forEachIndexed { index, line ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(PedalYellow, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = PedalTextOnYellow,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = line,
                                            color = PedalTextPrimary,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (current.name == "CatEye") {
                                    Text(
                                        text = "※ CatEye는 GPS 파일 직접 출력이 없어 Strava/Komoot 우회가 필요합니다.",
                                        color = PedalTextSecondary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                        }
                    }
                }
            }

            PedalStatusCard(
                message = "Notion 속성명은 공백/단위 포함 정확히 일치해야 하며, 불일치 시 400 오류가 발생할 수 있습니다.",
                isSuccess = false
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FirstLaunchBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PedalYellowBg),
        shape = RoundedCornerShape(PedalDimen.RadiusCard),
        border = BorderStroke(1.dp, PedalYellow)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("🚴", style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PedalLog에 오신 것을 환영합니다!", style = MaterialTheme.typography.titleSmall, color = PedalYellow, fontWeight = FontWeight.Bold)
                Text("Notion Token과 Database ID를 설정하면\ntrimm Cycling 기록이 자동으로 등록됩니다.", style = MaterialTheme.typography.bodySmall, color = PedalTextSecondary)
            }
        }
    }
}

@Composable
private fun NotionSection(
    uiState: SettingsViewModel.SettingsUiState,
    onTokenInput: (String) -> Unit,
    onTokenVisible: () -> Unit,
    onTokenSave: () -> Unit,
    onDbIdInput: (String) -> Unit,
    onDbIdSave: () -> Unit,
    onTestConnection: () -> Unit,
    onClearSettings: () -> Unit
) {
    SettingsSectionCard("NOTION 연동") {
        Text("Integration Token", style = MaterialTheme.typography.labelMedium, color = PedalYellow)
        uiState.savedToken?.let { saved ->
            val masked = saved.take(7) + "•".repeat(12) + saved.takeLast(4)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PedalSuccessBg),
                border = BorderStroke(0.8.dp, PedalSuccess),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isTokenVisible) saved else masked,
                        style = MaterialTheme.typography.bodySmall,
                        color = PedalSuccess
                    )
                    IconButton(onClick = onTokenVisible, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (uiState.isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            tint = PedalSuccess
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = uiState.tokenInput,
            onValueChange = onTokenInput,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (uiState.savedToken != null) "새 Token으로 교체하려면 입력" else "ntn_xxxxxxxxxxxxxxxx... (또는 secret_...)",
                    color = PedalTextMuted
                )
            },
            visualTransformation = if (uiState.isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            colors = notionFieldColors(),
            trailingIcon = {
                IconButton(onClick = onTokenVisible) {
                    Icon(if (uiState.isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = PedalYellow)
                }
            },
            singleLine = true,
            isError = uiState.tokenSaveResult is SettingsViewModel.SaveResult.ERROR,
            shape = RoundedCornerShape(PedalDimen.RadiusInput)
        )
        SaveResultMessage(uiState.tokenSaveResult)
        if (uiState.tokenInput.isNotBlank()) {
            PedalPrimaryButton(text = "Token 저장", onClick = onTokenSave, icon = Icons.Default.Save)
        }

        PedalDivider()

        OutlinedTextField(
            value = uiState.dbIdInput,
            onValueChange = onDbIdInput,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Notion DB URL 또는 ID를 붙여넣으세요") },
            colors = notionFieldColors(),
            singleLine = true,
            trailingIcon = if (uiState.savedDbId != null && uiState.dbIdInput == uiState.savedDbId) {
                { Icon(Icons.Default.CheckCircle, null, tint = PedalSuccess) }
            } else {
                null
            },
            isError = uiState.dbIdSaveResult is SettingsViewModel.SaveResult.ERROR,
            shape = RoundedCornerShape(PedalDimen.RadiusInput)
        )
        Text("URL 붙여넣기 시 ID 자동 추출", style = MaterialTheme.typography.labelSmall, color = PedalTextMuted)
        SaveResultMessage(uiState.dbIdSaveResult)
        if (uiState.dbIdInput.isNotBlank() && uiState.dbIdInput != uiState.savedDbId) {
            PedalPrimaryButton(text = "Database ID 저장", onClick = onDbIdSave, icon = Icons.Default.Save)
        }

        PedalDivider()

        when (val state = uiState.testState) {
            is SettingsViewModel.TestState.IDLE -> PedalPrimaryButton(text = "🔗  연동 상태 확인", onClick = onTestConnection)
            is SettingsViewModel.TestState.LOADING -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(color = PedalYellow, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Notion 연결 확인 중...", color = PedalTextPrimary)
                }
            }
            is SettingsViewModel.TestState.SUCCESS -> {
                PedalStatusCard(message = "✓  ${state.message}", isSuccess = true)
                PedalOutlineButton(text = "다시 확인", onClick = onTestConnection)
            }
            is SettingsViewModel.TestState.ERROR -> {
                PedalStatusCard(message = state.message, isSuccess = false)
                PedalPrimaryButton(text = "다시 시도", onClick = onTestConnection)
            }
        }

        PedalDivider()

        var showClearDialog by remember { mutableStateOf(false) }
        TextButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DeleteOutline, null, tint = PedalError, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Notion 설정 초기화", color = PedalError)
        }
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        onClearSettings()
                        showClearDialog = false
                    }) { Text("초기화", color = PedalError) }
                },
                dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("취소") } },
                title = { Text("Notion 설정 초기화") },
                text = { Text("저장된 Token과 Database ID가 모두 삭제됩니다.\n라이딩 기록(Room DB)은 유지됩니다.") },
                shape = RoundedCornerShape(PedalDimen.RadiusCard)
            )
        }
    }
}

@Composable
private fun AdsSection(adsRemoved: Boolean, onPurchase: () -> Unit) {
    SettingsSectionCard("광고") {
        if (adsRemoved) {
            Text("광고가 제거되었습니다. 감사합니다! 🎉", color = PedalSuccess)
        } else {
            SettingsRow(icon = Icons.Default.Block, label = "광고 제거", value = "1,100원", onClick = onPurchase)
            Text("일회성 구매 · 광고 배너 영구 제거", style = MaterialTheme.typography.labelSmall, color = PedalTextMuted)
        }
    }
}


@Composable
private fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = PedalYellow, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PedalBgCard),
            shape = RoundedCornerShape(PedalDimen.RadiusCard),
            border = BorderStroke(1.dp, PedalBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) { Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, value: String? = null, onClick: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PedalTextMuted, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = PedalTextPrimary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = PedalTextMuted) }
            if (onClick != null) {
                IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChevronRight, null, tint = PedalYellow)
                }
            }
        }
    }
}

@Composable
private fun SaveResultMessage(result: SettingsViewModel.SaveResult) {
    when (result) {
        is SettingsViewModel.SaveResult.SUCCESS -> Text("저장되었습니다", style = MaterialTheme.typography.labelSmall, color = PedalSuccess)
        is SettingsViewModel.SaveResult.ERROR -> Text(result.message, style = MaterialTheme.typography.labelSmall, color = PedalError)
        SettingsViewModel.SaveResult.IDLE -> Unit
    }
}

@Composable
private fun notionFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PedalYellow,
    unfocusedBorderColor = PedalBorder,
    errorBorderColor = PedalError,
    focusedContainerColor = PedalBgInput,
    unfocusedContainerColor = PedalBgInput,
    cursorColor = PedalYellow,
    focusedTextColor = PedalTextPrimary,
    unfocusedTextColor = PedalTextPrimary
)
