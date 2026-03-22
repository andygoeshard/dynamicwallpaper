package com.andyl.iris.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.andyl.iris.domain.helper.AlarmHelper
import com.andyl.iris.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class BootReceiver : BroadcastReceiver() {

    // Usamos inject de Koin para traer el repo y leer los horarios guardados
    private val preferencesRepository: UserPreferencesRepository by inject(UserPreferencesRepository::class.java)

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("BootReceiver", ">>> Sistema reiniciado. Reprogramando TODO...")

            IrisWallpaperScheduler.schedule(context)
            reScheduleAllAlarms(context)
        }
        else {
            Log.i("BootReceiver", ">>> Alarma de horario fijo detectada. Ejecutando Worker inmediato.")

            val workRequest = OneTimeWorkRequestBuilder<IrisWallpaperWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    private fun reScheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val config = preferencesRepository.getWallpaperConfig()

                Log.d("BootReceiver", ">>> Re-agendando ${config.fixedTimeRules.size} alarmas fijas.")

                config.fixedTimeRules.keys.forEach { time ->
                    AlarmHelper.scheduleFixedTimeAlarm(context, time)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error re-programando alarmas: ${e.message}")
            }
        }
    }
}
