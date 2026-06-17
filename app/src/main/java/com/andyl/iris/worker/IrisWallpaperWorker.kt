package com.andyl.iris.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.andyl.iris.R
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase

class IrisWallpaperWorker(
    private val context: Context,
    params: WorkerParameters,
    private val applyUseCase: ApplyDynamicWallpaperUseCase
) : CoroutineWorker(context, params) {

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "iris_wallpaper_service"
    }

    override suspend fun doWork(): Result {
        Log.d("IrisWorker", "🚀 Worker starting... ID: ${id}")
        
        // Android requires Foreground for reliable background tasks
        try {
            setForeground(createForegroundInfo())
        } catch (e: Exception) {
            Log.e("IrisWorker", "Failed to set foreground", e)
        }

        return try {
            applyUseCase()
            Log.d("IrisWorker", "✅ Wallpaper update successful")
            Result.success()
        } catch (e: Exception) {
            Log.e("IrisWorker", "❌ Error in Worker: ${e.message}", e)
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(context.getString(R.string.notif_desc))
            .setSmallIcon(R.mipmap.ic_iris)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_name)
            val descriptionText = context.getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
