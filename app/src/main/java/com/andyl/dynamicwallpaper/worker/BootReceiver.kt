package com.andyl.dynamicwallpaper.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.andyl.dynamicwallpaper.domain.helper.AlarmHelper
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class BootReceiver : BroadcastReceiver() {

    // Usamos inject de Koin para traer el repo y leer los horarios guardados
    private val preferencesRepository: UserPreferencesRepository by inject(UserPreferencesRepository::class.java)

    override fun onReceive(context: Context, intent: Intent?) {
        // Chequeamos si el intent es nulo por las dudas
        val action = intent?.action

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("BootReceiver", ">>> Sistema reiniciado. Reprogramando TODO...")

            // 1. Reprogramamos el ciclo de 15 min (clima)
            DynamicWallpaperScheduler.schedule(context)

            // 2. Reprogramamos todas las alarmas de horarios fijos desde el JSON
            reScheduleAllAlarms(context)
        }
        else {
            // Caso Alarma: El AlarmManager nos despertó para un cambio de fondo EXACTO
            Log.i("BootReceiver", ">>> Alarma de horario fijo detectada. Ejecutando Worker inmediato.")

            val workRequest = OneTimeWorkRequestBuilder<DynamicWallpaperWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    private fun reScheduleAllAlarms(context: Context) {
        // Lanzamos una corrutina en IO porque leer el JSON/DataStore puede ser lento
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtenemos el pack activo (o el que corresponda)
                val config = preferencesRepository.getWallpaperConfig()

                Log.d("BootReceiver", ">>> Re-agendando ${config.fixedTimeRules.size} alarmas fijas.")

                config.fixedTimeRules.keys.forEach { time ->
                    // Usamos el Helper que creamos antes para registrar la alarma en el sistema
                    AlarmHelper.scheduleFixedTimeAlarm(context, time)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error re-programando alarmas: ${e.message}")
            }
        }
    }
}
