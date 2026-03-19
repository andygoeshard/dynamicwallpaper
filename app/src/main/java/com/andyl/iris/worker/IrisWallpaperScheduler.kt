package com.andyl.iris.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object IrisWallpaperScheduler {
    private const val WORK_NAME = "dynamic_wallpaper_worker"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<IrisWallpaperWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}