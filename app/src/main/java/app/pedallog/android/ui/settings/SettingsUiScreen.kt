package app.pedallog.android.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
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
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
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
            AppInfoSection {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FirstLaunchBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PedalYellowBg),
        shape = RoundedCornerShape(12.dp),
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
                    if (uiState.savedToken != null) "새 Token으로 교체하려면 입력" else "secret_xxxxxxxxxxxxxxxx...",
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
            isError = uiState.tokenSaveResult is SettingsViewModel.SaveResult.ERROR
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
            isError = uiState.dbIdSaveResult is SettingsViewModel.SaveResult.ERROR
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
                text = { Text("저장된 Token과 Database ID가 모두 삭제됩니다.\n라이딩 기록(Room DB)은 유지됩니다.") }
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
private fun AppInfoSection(onPrivacyPolicy: () -> Unit) {
    SettingsSectionCard("앱 정보") {
        SettingsRow(icon = Icons.Default.Description, label = "개인정보처리방침", onClick = onPrivacyPolicy)
        PedalDivider()
        SettingsRow(icon = Icons.Default.Info, label = "버전", value = BuildConfig.VERSION_NAME, onClick = null)
        PedalDivider()
        SettingsRow(icon = Icons.Default.DirectionsBike, label = "앱 컨셉", value = "라이딩 기록 허브 앱", onClick = null)
        PedalDivider()
        SettingsRow(icon = Icons.Default.Fingerprint, label = "패키지명", value = "app.pedallog.android", onClick = null)
    }
}

@Composable
private fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = PedalYellow, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PedalBgCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, PedalBorder)
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
