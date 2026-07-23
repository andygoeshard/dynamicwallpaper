package com.andyl.iris

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.andyl.iris.billing.BillingManager
import com.andyl.iris.di.appModules
import com.andyl.iris.worker.IrisWallpaperScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import org.koin.android.ext.android.get

class IrisWallpaperApp : Application(), Configuration.Provider {

    private lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@IrisWallpaperApp)
            workManagerFactory()
            modules(appModules)
        }

        billingManager = get()
        billingManager.startConnection()

        IrisWallpaperScheduler.schedule(this)
        com.andyl.iris.domain.helper.AlarmHelper.scheduleMidnightRefresh(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        if (::billingManager.isInitialized) {
            billingManager.endConnection()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(get<KoinWorkerFactory>())
            .build()
}
