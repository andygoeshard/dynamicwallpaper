package com.andyl.dynamicwallpaper

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.andyl.dynamicwallpaper.di.appModules
import com.andyl.dynamicwallpaper.worker.DynamicWallpaperScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import org.koin.android.ext.android.get

class DynamicWallpaperApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DynamicWallpaperApp)
            workManagerFactory()
            modules(appModules)
        }

        DynamicWallpaperScheduler.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(get<KoinWorkerFactory>())
            .build()
}
