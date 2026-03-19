package com.andyl.iris.domain.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.andyl.iris.worker.BootReceiver
import java.util.Calendar

object AlarmHelper {
    fun scheduleFixedTimeAlarm(context: Context, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Chequeo de seguridad para Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("ALARM_HELPER", "ERROR: No tenemos permiso para alarmas exactas.")
                // Opcional: Podés redirigir al usuario a Ajustes aquí
                return
            }
        }

        val intent = Intent(context, BootReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val parts = time.split(":")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("ALARM_HELPER", "Alarma programada con éxito para las $time")
        } catch (e: SecurityException) {
            Log.e("ALARM_HELPER", "SecurityException al programar alarma: ${e.message}")
        }
    }

    fun cancelFixedTimeAlarm(context: Context, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BootReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            time.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("ALARM_HELPER", ">>> Alarma cancelada para: $time")
        }
    }
}