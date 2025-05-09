package com.shevapro.filesorter.ui.components.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.shevapro.filesorter.service.AdService
import com.shevapro.filesorter.ui.getActivity
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

private const val TAG = "AdInterstitial"

/**
 * Shows an interstitial ad after an operation completes
 * This should be called from a screen where appropriate (e.g., after file operations)
 *
 * @param show Whether to show the ad or not
 * @param onAdClosed Callback to execute after the ad is closed
 */
@Composable
fun AdInterstitial(
    show: Boolean,
    onAdClosed: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context.getActivity() ?: return
    val adService: AdService = koinInject<AdService>()

    // This effect handles showing the interstitial ad when requested
    LaunchedEffect(show) {
        if (show && adService.canShowInterstitial()) {
            if (adService.isInterstitialReady()) {
                // We have an ad ready to show
                Log.d(TAG, "Interstitial ad is ready, showing it now")
                val interstitialAd = adService.getInterstitialAd()
                showInterstitialAd(activity, interstitialAd, onAdClosed)
                adService.recordInterstitialShown()
            } else {
                // No ad ready, but we want to show one - try to load one immediately
                Log.d(TAG, "No interstitial ad ready, attempting to load one now")
                adService.preloadInterstitialAd()
                // Wait a short time to see if ad loads quickly
                kotlinx.coroutines.delay(1000)
                // Check again if ad is ready
                if (adService.isInterstitialReady()) {
                    val interstitialAd = adService.getInterstitialAd()
                    showInterstitialAd(activity, interstitialAd, onAdClosed)
                    adService.recordInterstitialShown()
                } else {
                    Log.d(TAG, "Still no interstitial ad available after waiting")
                    onAdClosed()
                }
            }
        } else {
            // No ad to show, just call the callback
            if (show) {
                if (!adService.canShowInterstitial()) {
                    Log.d(TAG, "Skipping interstitial ad due to frequency cap")
                } else {
                    Log.d(TAG, "Interstitial ad not requested")
                }
                onAdClosed()
                // Preload for next time
                adService.preloadInterstitialAd()
            }
        }
    }

    // Remember to preload the next interstitial when this composable enters the composition
    LaunchedEffect(Unit) {
        if (!adService.isInterstitialReady()) {
            adService.preloadInterstitialAd()
        }
    }
}

/**
 * Helper function to show an interstitial ad with appropriate callbacks
 */
private fun showInterstitialAd(
    activity: Activity,
    interstitialAd: InterstitialAd?,
    onAdClosed: () -> Unit
) {
    if (interstitialAd == null) {
        Log.d(TAG, "Ad not loaded yet")
        onAdClosed()
        return
    }

    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "Ad was dismissed")
            onAdClosed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            Log.e(TAG, "Ad failed to show: ${adError.message}")
            onAdClosed()
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "Ad showed fullscreen content")
        }
    }

    interstitialAd.show(activity)
}
