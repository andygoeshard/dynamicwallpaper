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
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .build() // Removed setRequiresBatteryNotLow to be more aggressive
            )
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Better to UPDATE than KEEP to ensure new constraints/intervals apply
            request
        )
    }
}