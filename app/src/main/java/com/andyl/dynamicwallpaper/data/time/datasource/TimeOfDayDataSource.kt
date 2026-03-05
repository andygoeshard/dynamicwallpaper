package com.andyl.dynamicwallpaper.data.time.datasource

import android.util.Log
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import java.time.LocalTime

class TimeOfDayDataSource(
) {
    fun getTimeOfDay(): TimeOfDay {
        val hour = LocalTime.now().hour
        Log.d("TimeDataSource", "Hora actual detectada: $hour")

        return when (hour) {
            in 6..8 -> TimeOfDay.DAWN
            in 9..17 -> TimeOfDay.DAY
            in 18..20 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }
    }

}
