package com.andyl.dynamicwallpaper.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase

class DynamicWallpaperWorker(
    context: Context,
    params: WorkerParameters,
    private val applyUseCase: ApplyDynamicWallpaperUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.e("WallpaperWorker", ">>> ¡WORKER VIVO! (ID: ${id})")
        Log.wtf("WallpaperWorker TEST", "EJECUTANDO WORKER AHORA")

        return try {
            applyUseCase()
            Log.d("WallpaperWorker", ">>> Caso de uso ejecutado con éxito")
            Result.success()
        } catch (e: Exception) {
            Log.e("WallpaperWorker", ">>> Error en Worker: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
