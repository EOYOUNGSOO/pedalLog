package app.pedallog.android.ui.component

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.pedallog.android.BuildConfig
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun PedalAdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isLoaded by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    if (hasFailed) {
        Spacer(modifier = modifier.height(0.dp))
        return
    }

    if (!isLoaded) {
        Spacer(modifier = modifier.height(50.dp))
    }

    AndroidView(
        modifier = modifier.heightIn(min = 0.dp, max = 50.dp),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = if (BuildConfig.DEBUG) {
                    "ca-app-pub-3940256099942544/6300978111"
                } else {
                    BuildConfig.ADMOB_BANNER_ID
                }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        isLoaded = true
                        hasFailed = false
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        hasFailed = true
                        isLoaded = false
                    }
                }
                val adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(
                        AdMobAdapter::class.java,
                        Bundle().apply { putString("npa", "1") }
                    )
                    .build()
                loadAd(adRequest)
            }
        }
    )
}
