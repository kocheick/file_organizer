package com.shevapro.filesorter

import android.app.Application
import android.util.Log
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.shevapro.filesorter.di.appModules
import com.shevapro.filesorter.service.AdService
import com.shevapro.filesorter.ui.viewmodel.MainViewModel
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class that initializes Koin for dependency injection
 */
class App : Application() {
    companion object {
        lateinit var instance: App

        // Expose the MainViewModel for components that need it directly
        lateinit var vm: MainViewModel
    }

    override fun onCreate() {
        super.onCreate()
        instance = this



        // Initialize WebView early to prevent issues
        initializeWebView()

        // Initialize AdMob
        MobileAds.initialize(this) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            // Log the initialization status for debugging
            Log.d("AdMob", "AdMob SDK initialization complete")

            // Log detailed adapter status
            statusMap.forEach { (adapter, status) ->
                Log.d("AdMob", "Adapter: $adapter, Status: ${status.initializationState}, " +
                        "Latency: ${status.latency}ms, Description: ${status.description}")
            }

            // Request test ads on physical devices
            val testDeviceIds = listOf(
                "ABCDEF012345678901234567890ABCDE", // Example test device ID - replace with your actual device ID if needed
                AdRequest.DEVICE_ID_EMULATOR // Emulator
            )
            val configuration = MobileAds.getRequestConfiguration().toBuilder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            Log.d("AdMob", "Test device IDs configured for ad requests")
        }

        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR) // Use ERROR level to avoid Koin debug logs
            androidContext(this@App)
            modules(appModules)
        }

        // Get the MainViewModel from Koin
        vm = get()

        // Initialize ad caching after Koin is initialized
        try {
            val adService: AdService = get()
            adService.initializeAds()
            Log.d("AdMob", "Ad caching initialized")
        } catch (e: Exception) {
            Log.e("AdMob", "Error initializing ad caching: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Initialize WebView early to prevent issues with AdMob
     * This helps avoid crashes related to WebViewRenderProcessClient
     */
    private fun initializeWebView() {
        try {
            Log.d("WebView", "Initializing WebView early")

            // Create a WebView instance to force initialization
            val webView = WebView(this)

            // Check WebView version and features
            val webViewPackage = WebViewCompat.getCurrentWebViewPackage(this)
            if (webViewPackage != null) {
                Log.d("WebView", "WebView package: ${webViewPackage.packageName}, version: ${webViewPackage.versionName}")
            } else {
                Log.w("WebView", "No WebView package found")
            }

            // Check for specific features
            val hasVisualStateCallback = WebViewFeature.isFeatureSupported(WebViewFeature.VISUAL_STATE_CALLBACK)
            Log.d("WebView", "WebView feature VISUAL_STATE_CALLBACK supported: $hasVisualStateCallback")

            // Clean up the WebView
            webView.destroy()

            Log.d("WebView", "WebView initialization completed")
        } catch (e: Exception) {
            Log.e("WebView", "Error initializing WebView: ${e.message}")
            e.printStackTrace()
        }
    }
}
