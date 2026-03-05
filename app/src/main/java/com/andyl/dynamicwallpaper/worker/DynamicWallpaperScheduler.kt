package com.andyl.dynamicwallpaper.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DynamicWallpaperScheduler {
    private const val WORK_NAME = "dynamic_wallpaper_worker"

    fun schedule(context: Context) {
        Log.d("WallpaperScheduler", "Programando worker cada 15 minutos (mínimo permitido)...")

        val request = PeriodicWorkRequestBuilder<DynamicWallpaperWorker>(
            1, TimeUnit.HOURS,
            10, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}