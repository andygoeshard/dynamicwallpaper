package com.andyl.iris.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object IrisWallpaperScheduler {
    private const val WORK_NAME = "dynamic_wallpaper_worker"

    /** Interval <= 0 means "off": the periodic worker is cancelled. */
    fun schedule(context: Context, intervalMinutes: Int = 60) {
        if (intervalMinutes <= 0) {
            cancel(context)
            return
        }
        val request = PeriodicWorkRequestBuilder<IrisWallpaperWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES,
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
            ExistingPeriodicWorkPolicy.UPDATE, // Better to UPDATE than KEEP to ensure new constraints/intervals apply
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}