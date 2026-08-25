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
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase

class IrisWallpaperWorker(
    private val context: Context,
    params: WorkerParameters,
    private val applyUseCase: ApplyDynamicWallpaperUseCase,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val ERROR_NOTIFICATION_ID = 102
        private const val CHANNEL_ID = "iris_wallpaper_service"
        private const val ERROR_CHANNEL_ID = "iris_wallpaper_errors"
    }

    override suspend fun doWork(): Result {
        Log.d("IrisWorker", "🚀 Worker starting... ID: ${id}")

        val notificationsEnabled = preferencesRepository.getNotificationsEnabled()

        // Android requires Foreground for reliable background tasks
        try {
            setForeground(createForegroundInfo(notificationsEnabled))
        } catch (e: Exception) {
            Log.e("IrisWorker", "Failed to set foreground", e)
        }

        return try {
            applyUseCase()
            Log.d("IrisWorker", "✅ Wallpaper update successful")
            Result.success()
        } catch (e: Exception) {
            Log.e("IrisWorker", "❌ Error in Worker: ${e.message}", e)
            notifyError(e.message ?: "Unknown error")
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(notificationsEnabled: Boolean): ForegroundInfo {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(context.getString(R.string.notif_desc))
            .setSmallIcon(R.mipmap.ic_iris)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)

        // If the user disabled notifications, make the foreground notification
        // dismissable and transient instead of permanently ongoing.
        if (notificationsEnabled) {
            builder.setOngoing(true)
        } else {
            builder.setOngoing(false)
            builder.setAutoCancel(true)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, builder.build(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, builder.build())
        }
    }

    private fun notifyError(message: String) {
        try {
            createErrorChannel()
            val notification = NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notif_error_title))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_iris)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(ERROR_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("IrisWorker", "Failed to show error notification", e)
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

    private fun createErrorChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_error_channel_name)
            val descriptionText = context.getString(R.string.notif_error_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(ERROR_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}