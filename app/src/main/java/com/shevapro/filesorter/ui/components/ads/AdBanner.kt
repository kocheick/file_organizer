package com.shevapro.filesorter.ui.components.ads

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.shevapro.filesorter.service.AdService
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

private const val TAG = "AdBanner"

// Test banner ad unit ID - Replace with your real ID in production
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

/**
 * Composable to display a banner ad as part of the UI
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adService: AdService = koinInject()



    // Wrap the AdView in AndroidView composable
    AndroidView(
        factory = { context ->
            // Try to get a new AdView with cached ad properties
            val adViewFromCache = adService.getCachedBannerAd(AdService.TEST_BANNER_AD_UNIT_ID)

            // If we got an AdView from cache properties, use it; otherwise create a new one
            val adView = adViewFromCache ?: AdView(context).apply {
                // Set the ad unit ID
                adUnitId = AdService.TEST_BANNER_AD_UNIT_ID

                // Set the ad size
                setAdSize(AdSize.BANNER)

                // Set up ad listener
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Banner ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e(TAG, "Banner ad failed to load: ${error.message}")

                        // Ensure we preload for next time
                        adService.preloadBannerAd(AdService.TEST_BANNER_AD_UNIT_ID)
                    }
                }

                // Load a new ad
                adService.loadBannerAd(this)
            }

            // Log for debugging
            Log.d(TAG, "Banner ad view created/updated")

            adView
        },
        modifier = modifier.fillMaxWidth(),
        update = { adView ->
            // Nothing to update
        },
        onRelease = { adView ->
            // Clean up the AdView when the composable leaves composition
            adView.destroy()
        }
    )


}
