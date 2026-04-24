package app.pedallog.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.pedallog.android.ui.navigation.PedalLogNavGraph
import app.pedallog.android.ui.theme.PedalLogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentUri = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }
        Log.d("PedalLog", "ACTION_SEND uri=$intentUri")
        setContent { PedalLogTheme { PedalLogNavGraph(intentUri) } }
    }
}
