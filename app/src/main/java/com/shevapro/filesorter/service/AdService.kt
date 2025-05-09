package com.shevapro.filesorter.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class to hold cached banner ad information
 */
data class CachedBannerAd(
    val adView: AdView,
    val loadTime: Long = System.currentTimeMillis(),
    val isLoaded: AtomicBoolean = AtomicBoolean(false),
    val loadAttempts: AtomicInteger = AtomicInteger(0)
)

/**
 * Service for managing ad operations
 */
class AdService(private val context: Context) {
    companion object {
        private const val TAG = "AdService"

        // Test banner ad unit ID - Replace with your real ID in production
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        // Frequency caps
        private const val INTERSTITIAL_MIN_INTERVAL_MS = 10 * 60 * 1000 // 10 minutes

        // Ad cache expiration time
        private const val AD_CACHE_EXPIRATION_MS = 30 * 60 * 1000 // 30 minutes

        // Max retry attempts for ad loading
        private const val MAX_RETRY_ATTEMPTS = 3

        // Retry delay in milliseconds (exponential backoff)
        private const val INITIAL_RETRY_DELAY_MS = 1000L
    }

    // Flag to track if WebView is properly initialized
    private var isWebViewCompatible = true

    // Cache for banner ads
    private val bannerAdCache = ConcurrentHashMap<String, CachedBannerAd>()

    // Coroutine scope for background operations
    private val adScope = CoroutineScope(Dispatchers.IO)

    // Track loading state to prevent duplicate loads
    private val isPreloadingBannerAd = AtomicBoolean(false)
    private val isPreloadingInterstitialAd = AtomicBoolean(false)

    // Handler for main thread operations
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        // Check WebView compatibility on initialization
        checkWebViewCompatibility()
    }

    /**
     * Check if WebView features required by AdMob are available
     * Sets isWebViewCompatible flag accordingly
     */
    private fun checkWebViewCompatibility() {
        try {
            // Check if WebView is available on the device
            val webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(context)
            if (webViewPackageInfo == null) {
                Log.w(TAG, "No WebView package found on device")
                isWebViewCompatible = false
                return
            }

            // Check if specific WebView features are available
            val hasRenderProcessClient = WebViewFeature.isFeatureSupported(WebViewFeature.VISUAL_STATE_CALLBACK)

            isWebViewCompatible = hasRenderProcessClient
            Log.d(TAG, "WebView compatibility check: $isWebViewCompatible (package: ${webViewPackageInfo.packageName}, version: ${webViewPackageInfo.versionName})")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WebView compatibility: ${e.message}")
            isWebViewCompatible = false
            e.printStackTrace()
        }
    }

    // For interstitial frequency capping
    private var lastInterstitialShownTime = 0L
    private var cachedInterstitialAd: InterstitialAd? = null

    /**
     * Create and load an ad request
     * @return AdRequest object ready to be used with ad views
     */
    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Initialize all ad types at app launch
     * This should be called early in the app lifecycle
     */
    fun initializeAds() {
        Log.d(TAG, "Initializing all ad types")

        // Preload banner ads
        preloadBannerAd(TEST_BANNER_AD_UNIT_ID)

        // Preload interstitial ad
        preloadInterstitialAd()

        // Schedule periodic refresh of cached ads
        scheduleAdRefresh()
    }

    /**
     * Schedule periodic refresh of cached ads to ensure they're always fresh
     */
    private fun scheduleAdRefresh() {
        mainHandler.postDelayed({
            refreshExpiredAds()
            scheduleAdRefresh()
        }, (AD_CACHE_EXPIRATION_MS / 2).toLong()) // Refresh halfway through expiration time
    }

    /**
     * Refresh any expired ads in the cache
     */
    private fun refreshExpiredAds() {
        val currentTime = System.currentTimeMillis()

        // Check banner ads
        bannerAdCache.entries.forEach { entry ->
            val cachedAd = entry.value
            if (currentTime - cachedAd.loadTime > AD_CACHE_EXPIRATION_MS) {
                Log.d(TAG, "Banner ad for ${entry.key} expired, refreshing")
                preloadBannerAd(entry.key)
            }
        }

        // Check interstitial ad
        if (cachedInterstitialAd != null &&
            currentTime - lastInterstitialShownTime > AD_CACHE_EXPIRATION_MS) {
            Log.d(TAG, "Interstitial ad expired, refreshing")
            preloadInterstitialAd()
        }
    }

    /**
     * Preload a banner ad for later use
     * @param adUnitId The ad unit ID to preload
     */
    fun preloadBannerAd(adUnitId: String) {
        // Skip if already preloading
        if (isPreloadingBannerAd.getAndSet(true)) {
            Log.d(TAG, "Already preloading banner ad, skipping")
            return
        }

        try {
            Log.d(TAG, "Preloading banner ad for $adUnitId")

            // Check if we already have a cached ad that's still valid
            val cachedAd = bannerAdCache[adUnitId]
            if (cachedAd != null &&
                cachedAd.isLoaded.get() &&
                System.currentTimeMillis() - cachedAd.loadTime < AD_CACHE_EXPIRATION_MS) {
                Log.d(TAG, "Banner ad for $adUnitId already cached and valid, skipping preload")
                isPreloadingBannerAd.set(false)
                return
            }

            // Create a new AdView on the main thread
            mainHandler.post {
                val adView = AdView(context).apply {
                    this.adUnitId = adUnitId
                    this.setAdSize(AdSize.BANNER)

                    // Set up ad listener
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "Preloaded banner ad loaded successfully for $adUnitId")
                            val cached = bannerAdCache[adUnitId]
                            cached?.isLoaded?.set(true)
                            isPreloadingBannerAd.set(false)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(TAG, "Banner ad failed to preload for $adUnitId: ${error.message}")
                            val cached = bannerAdCache[adUnitId]
                            val attempts = cached?.loadAttempts?.incrementAndGet() ?: 0

                            if (attempts < MAX_RETRY_ATTEMPTS) {
                                // Retry with exponential backoff
                                val delayMs = INITIAL_RETRY_DELAY_MS * (1 shl (attempts - 1))
                                Log.d(TAG, "Will retry banner ad preload after $delayMs ms (attempt $attempts)")
                                mainHandler.postDelayed({
                                    isPreloadingBannerAd.set(false)
                                    preloadBannerAd(adUnitId)
                                }, delayMs)
                            } else {
                                Log.e(TAG, "Giving up on preloading banner ad after $attempts attempts")
                                isPreloadingBannerAd.set(false)
                            }
                        }
                    }
                }

                // Store in cache before loading
                val cachedBannerAd = CachedBannerAd(adView)
                bannerAdCache[adUnitId] = cachedBannerAd

                // Load the ad
                val adRequest = createAdRequest()
                adView.loadAd(adRequest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading banner ad: ${e.message}")
            e.printStackTrace()
            isPreloadingBannerAd.set(false)
        }
    }

    /**
     * Get a cached banner ad if available
     * @param adUnitId The ad unit ID to get
     * @return A new AdView with the same properties as the cached one, or null if not available
     */
    fun getCachedBannerAd(adUnitId: String): AdView? {
        val cachedAd = bannerAdCache[adUnitId]
        if (cachedAd != null &&
            cachedAd.isLoaded.get() &&
            System.currentTimeMillis() - cachedAd.loadTime < AD_CACHE_EXPIRATION_MS) {
            Log.d(TAG, "Using cached banner ad properties for $adUnitId")

            // Create a new AdView with the same properties instead of reusing the cached one
            // This avoids the "already has a parent" issue
            val newAdView = AdView(context).apply {
                this.adUnitId = adUnitId
                this.setAdSize(AdSize.BANNER)

                // Set up ad listener
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "New banner ad (from cache properties) loaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e(TAG, "New banner ad (from cache properties) failed to load: ${error.message}")
                        preloadBannerAd(adUnitId)
                    }
                }
            }

            // Preload a replacement ad for next time
            preloadBannerAd(adUnitId)

            // Load an ad into the new AdView using the same request as the cached one
            val adRequest = createAdRequest()
            newAdView.loadAd(adRequest)

            return newAdView
        }

        // If no valid cached ad, preload one for next time
        if (!isPreloadingBannerAd.get()) {
            preloadBannerAd(adUnitId)
        }

        return null
    }

    /**
     * Load an ad into the provided banner AdView
     * @param adView The AdView to load the ad into
     */
    fun loadBannerAd(adView: AdView) {
        try {
            val adUnitId = adView.adUnitId ?: TEST_BANNER_AD_UNIT_ID

            // Load a new ad
            val adRequest = createAdRequest()
            adView.loadAd(adRequest)

            Log.d(TAG, "Loading new banner ad for $adUnitId")

            // Ensure we have a preloaded ad for next time
            if (!isPreloadingBannerAd.get()) {
                preloadBannerAd(adUnitId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad: ${e.message}")
        }
    }

    /**
     * Preload an interstitial ad for later use
     */
    fun preloadInterstitialAd() {
        // Skip if already preloading
        if (isPreloadingInterstitialAd.getAndSet(true)) {
            Log.d(TAG, "Already preloading interstitial ad, skipping")
            return
        }

        try {
            // If we already have a cached ad, no need to load another
            if (cachedInterstitialAd != null) {
                Log.d(TAG, "Interstitial ad already cached, skipping preload")
                isPreloadingInterstitialAd.set(false)
                return
            }

            Log.d(TAG, "Starting to preload interstitial ad")
            val adRequest = createAdRequest()
            InterstitialAd.load(
                context,
                TEST_INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        cachedInterstitialAd = interstitialAd
                        Log.d(TAG, "Interstitial ad loaded successfully")
                        isPreloadingInterstitialAd.set(false)

                        // Set up full screen callback to handle ad closing
                        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial ad was dismissed")
                                // Preload the next ad when this one is dismissed
                                preloadInterstitialAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Interstitial ad showed full screen content")
                                // Clear the cached ad reference since it's been shown
                                cachedInterstitialAd = null
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        cachedInterstitialAd = null
                        Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}, code: ${loadAdError.code}")

                        // Retry with exponential backoff for network errors
                        if (loadAdError.code == 2 || loadAdError.code == 3) { // Network error or timeout
                            val retryAttempt = 1 // We could track this more precisely
                            val delayMs = INITIAL_RETRY_DELAY_MS * (1 shl (retryAttempt - 1))
                            Log.d(TAG, "Will retry interstitial ad load after $delayMs ms")
                            mainHandler.postDelayed({
                                isPreloadingInterstitialAd.set(false)
                                preloadInterstitialAd()
                            }, delayMs)
                        } else {
                            isPreloadingInterstitialAd.set(false)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading interstitial ad: ${e.message}")
            e.printStackTrace()
            isPreloadingInterstitialAd.set(false)
        }
    }

    /**
     * Check if we can show an interstitial ad based on frequency cap
     * @return true if we can show an ad, false otherwise
     */
    fun canShowInterstitial(): Boolean {
        val now = Date().time
        return now - lastInterstitialShownTime > INTERSTITIAL_MIN_INTERVAL_MS
    }

    /**
     * Check if an interstitial ad is loaded and ready to show
     * @return true if an ad is ready, false otherwise
     */
    fun isInterstitialReady(): Boolean {
        return cachedInterstitialAd != null
    }

    /**
     * Record that an interstitial was shown at the current time
     */
    fun recordInterstitialShown() {
        lastInterstitialShownTime = Date().time
    }

    /**
     * Get the cached interstitial ad
     * @return The cached interstitial ad, or null if none is loaded
     */
    fun getInterstitialAd(): InterstitialAd? {
        val ad = cachedInterstitialAd
        cachedInterstitialAd = null // Clear the cache after returning
        return ad
    }
}
