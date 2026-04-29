package app.pedallog.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import app.pedallog.android.ui.navigation.PedalLogNavGraph
import app.pedallog.android.ui.theme.PedalLogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val shareUriState = mutableStateOf<Uri?>(null)

    /**
     * trimm에서 공유 → 이 Activity가 새로 실행된 경우 true.
     * true 이면 Notion 등록 완료 후 "앱 홈"으로 가야 함.
     * false(직접 실행)이면 이미 홈에 있으므로 단순 popBackStack만 해도 됨.
     */
    private val launchedByShare: Boolean
        get() = intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareUriState.value = extractShareUri(intent)

        setContent {
            PedalLogTheme {
                val intentUri by shareUriState
                PedalLogNavGraph(
                    intentUri = intentUri,
                    onNotionSuccess = if (launchedByShare) {
                        { navigateToHomeAfterSuccess() }
                    } else {
                        null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shareUriState.value = extractShareUri(intent)
    }

    /**
     * Notion 등록 완료 후 처리:
     * - 공유 인텐트로 진입한 경우: 현재 Activity를 닫고 PedalLog 메인을 새로 실행
     * - 일반 실행(앱 내부)인 경우: NavGraph의 onSuccess 콜백이 HOME으로 popUpTo 처리
     */
    fun navigateToHomeAfterSuccess() {
        if (launchedByShare) {
            // 공유 인텐트로 진입: 현재 화면 닫고 앱 홈을 새 Task로 실행
            val homeIntent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(homeIntent)
            finish()
        }
        // 일반 실행 시에는 NavGraph의 onSuccess(popUpTo HOME)가 처리하므로 별도 작업 없음
    }

    private fun extractShareUri(intent: Intent?): Uri? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val stream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
                stream ?: intent.data
            }
            Intent.ACTION_VIEW -> intent.data
            else -> intent.data
        }
    }
}
