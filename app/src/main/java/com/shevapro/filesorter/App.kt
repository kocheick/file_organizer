package com.shevapro.filesorter

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.shevapro.filesorter.di.appModules
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

        // Initialize AdMob
        MobileAds.initialize(this) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            // Log the initialization status if needed
        }

        // Initialize Koin
        startKoin {
            androidLogger(Level.ERROR) // Use ERROR level to avoid Koin debug logs
            androidContext(this@App)
            modules(appModules)
        }

        // Get the MainViewModel from Koin
        vm = get()
    }
}
