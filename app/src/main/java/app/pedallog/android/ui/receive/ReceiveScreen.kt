package app.pedallog.android.ui.receive

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pedallog.android.data.model.FileFormat
import app.pedallog.android.data.model.ReceivedFile
import app.pedallog.android.ui.component.PedalCard
import app.pedallog.android.ui.component.PedalDivider
import app.pedallog.android.ui.component.PedalOutlineButton
import app.pedallog.android.ui.component.PedalPrimaryButton
import app.pedallog.android.ui.component.PedalAppBar
import app.pedallog.android.ui.theme.PedalBgDark
import app.pedallog.android.ui.theme.PedalBgSection
import app.pedallog.android.ui.theme.PedalDivider as PedalDividerColor
import app.pedallog.android.ui.theme.PedalError
import app.pedallog.android.ui.theme.PedalErrorBg
import app.pedallog.android.ui.theme.PedalInfo
import app.pedallog.android.ui.theme.PedalInfoBg
import app.pedallog.android.ui.theme.PedalSuccess
import app.pedallog.android.ui.theme.PedalSuccessBg
import app.pedallog.android.ui.theme.PedalTextMuted
import app.pedallog.android.ui.theme.PedalTextOnYellow
import app.pedallog.android.ui.theme.PedalTextPrimary
import app.pedallog.android.ui.theme.PedalTextSecondary
import app.pedallog.android.ui.theme.PedalYellow
import app.pedallog.android.ui.theme.PedalYellowBg

@Suppress("UNUSED_PARAMETER")
@Composable
fun ReceiveScreen(
    intentUri: Uri?,
    onParsed: (Long) -> Unit,
    onNavigateToExisting: (Long) -> Unit,
    onError: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ReceiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(intentUri) {
        if (intentUri != null) {
            viewModel.handleUri(intentUri)
        }
    }

    Scaffold(
        topBar = {
            PedalAppBar(
                title = "파일 수신 중",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = PedalBgDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val step = uiState.step) {
                ReceiveViewModel.ReceiveStep.WAITING ->
                    if (intentUri == null) {
                        IdleReceiveContent(onBackClick = onBackClick)
                    } else {
                        ReceiveProgressContent(
                            step = uiState.step,
                            progress = uiState.progress
                        )
                    }

                ReceiveViewModel.ReceiveStep.DETECTING,
                ReceiveViewModel.ReceiveStep.COPYING,
                ReceiveViewModel.ReceiveStep.VALIDATING,
                ReceiveViewModel.ReceiveStep.PARSING,
                ReceiveViewModel.ReceiveStep.IMAGING -> {
                    ReceiveProgressContent(
                        step = uiState.step,
                        progress = uiState.progress
                    )
                }

                is ReceiveViewModel.ReceiveStep.SUCCESS -> {
                    ReceiveSuccessContent(
                        receivedFile = step.file,
                        onNext = {
                            uiState.sessionId?.let(onParsed)
                        }
                    )
                }

                is ReceiveViewModel.ReceiveStep.ERROR -> {
                    ReceiveErrorContent(
                        message = step.message,
                        onRetry = { viewModel.handleUri(intentUri) },
                        onBack = onBackClick
                    )
                }

            }
        }
    }
}

@Composable
private fun IdleReceiveContent(onBackClick: () -> Unit) {
    Text(
        text = "다른 앱에서 TCX·GPX·FIT 파일을 공유하면\n이 화면에서 수신합니다.",
        style = MaterialTheme.typography.bodyLarge,
        color = PedalTextMuted,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    PedalOutlineButton(
        text = "뒤로",
        onClick = onBackClick
    )
}

@Composable
private fun ReceiveProgressContent(
    step: ReceiveViewModel.ReceiveStep,
    progress: Float
) {
    val stepTexts = listOf(
        "파일 감지 중...",
        "캐시에 복사 중...",
        "데이터 파싱 중...",
        "경로 이미지 생성 중...",
        "완료"
    )
    val currentStepIndex = when (step) {
        ReceiveViewModel.ReceiveStep.DETECTING -> 0
        ReceiveViewModel.ReceiveStep.COPYING -> 1
        ReceiveViewModel.ReceiveStep.VALIDATING,
        ReceiveViewModel.ReceiveStep.PARSING -> 2
        ReceiveViewModel.ReceiveStep.IMAGING -> 3
        else -> 0
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .background(PedalYellowBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = PedalYellow,
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(Modifier.height(28.dp))

    Text(
        text = stepTexts[currentStepIndex],
        style = MaterialTheme.typography.headlineSmall,
        color = PedalTextPrimary,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(32.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = listOf("파일 감지", "형식 파악", "데이터 추출", "경로 이미지", "완료")
        steps.forEachIndexed { index, _ ->
            val isDone = index < currentStepIndex
            val isCurrent = index == currentStepIndex

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 32.dp else 28.dp)
                    .background(
                        when {
                            isDone -> PedalSuccess
                            isCurrent -> PedalYellow
                            else -> PedalBgSection
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = PedalBgDark,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrent) PedalTextOnYellow
                        else PedalTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.width(24.dp),
                    color = if (isDone) PedalSuccess else PedalDividerColor,
                    thickness = 1.5.dp
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = PedalYellow,
        trackColor = PedalBgSection
    )
}

@Composable
private fun ReceiveSuccessContent(
    receivedFile: ReceivedFile,
    onNext: () -> Unit
) {
    val formatColor = when (receivedFile.format) {
        FileFormat.TCX -> PedalYellow
        FileFormat.GPX -> PedalSuccess
        FileFormat.FIT -> PedalInfo
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .background(
                when (receivedFile.format) {
                    FileFormat.TCX -> PedalYellowBg
                    FileFormat.GPX -> PedalSuccessBg
                    FileFormat.FIT -> PedalInfoBg
                },
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = formatColor,
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = receivedFile.originalName,
        style = MaterialTheme.typography.titleLarge,
        color = PedalYellow,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 2
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "trimm Cycling에서 공유됨",
        style = MaterialTheme.typography.bodySmall,
        color = PedalTextMuted
    )

    Spacer(Modifier.height(24.dp))

    PedalCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "파일 형식",
                    style = MaterialTheme.typography.bodySmall,
                    color = PedalTextMuted
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = when (receivedFile.format) {
                        FileFormat.TCX -> PedalYellowBg
                        FileFormat.GPX -> PedalSuccessBg
                        FileFormat.FIT -> PedalInfoBg
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = receivedFile.format.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = formatColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "파일 크기",
                    style = MaterialTheme.typography.bodySmall,
                    color = PedalTextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = receivedFile.displaySize,
                    style = MaterialTheme.typography.titleMedium,
                    color = PedalTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "지원 여부",
                    style = MaterialTheme.typography.bodySmall,
                    color = PedalTextMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "✓ 지원",
                    style = MaterialTheme.typography.titleMedium,
                    color = PedalSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        PedalDivider()
        Spacer(Modifier.height(12.dp))

        Text(
            text = receivedFile.format.description,
            style = MaterialTheme.typography.bodySmall,
            color = PedalTextMuted
        )
    }

    Spacer(Modifier.height(28.dp))

    PedalPrimaryButton(
        text = "→  라이딩 정보 확인",
        onClick = onNext,
        icon = Icons.AutoMirrored.Filled.ArrowForward
    )
}

@Composable
private fun ReceiveErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .background(PedalErrorBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = PedalError,
            modifier = Modifier.size(44.dp)
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = "파일 수신 실패",
        style = MaterialTheme.typography.headlineSmall,
        color = PedalError,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(12.dp))

    PedalCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = PedalTextSecondary,
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(28.dp))

    PedalPrimaryButton(
        text = "다시 시도",
        onClick = onRetry
    )
    Spacer(Modifier.height(12.dp))
    PedalOutlineButton(
        text = "홈으로 돌아가기",
        onClick = onBack
    )
}

