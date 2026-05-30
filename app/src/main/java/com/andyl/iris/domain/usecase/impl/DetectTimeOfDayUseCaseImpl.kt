package com.andyl.iris.domain.usecase.impl

import android.util.Log
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.usecase.contract.DetectTimeOfDayUseCase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DetectTimeOfDayUseCaseImpl : DetectTimeOfDayUseCase {

    override operator fun invoke(sunrise: String?, sunset: String?): TimeOfDay {
        val now = LocalDateTime.now()
        
        if (sunrise == null || sunset == null) {
            val hour = now.hour
            val time = when (hour) {
                in 6..8 -> TimeOfDay.DAWN
                in 9..17 -> TimeOfDay.DAY
                in 18..20 -> TimeOfDay.DUSK
                else -> TimeOfDay.NIGHT
            }
            Log.d("IRIS_TIME", "No sun times available. Hardcoded fallback: $time")
            return time
        }

        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val sunriseTime = LocalDateTime.parse(sunrise, formatter)
            val sunsetTime = LocalDateTime.parse(sunset, formatter)

            val time = when {
                now.isAfter(sunriseTime.minusHours(1)) && now.isBefore(sunriseTime.plusHours(1)) -> TimeOfDay.DAWN
                now.isAfter(sunsetTime.minusHours(1)) && now.isBefore(sunsetTime.plusHours(1)) -> TimeOfDay.DUSK
                now.isAfter(sunriseTime.plusHours(1)) && now.isBefore(sunsetTime.minusHours(1)) -> TimeOfDay.DAY
                else -> TimeOfDay.NIGHT
            }
            Log.d("IRIS_TIME", "Sun-based detection: $time (Sunrise: ${sunriseTime.toLocalTime()}, Sunset: ${sunsetTime.toLocalTime()})")
            time
        } catch (e: Exception) {
            Log.e("IRIS_TIME", "Error parsing sun times", e)
            TimeOfDay.DAY
        }
    }
}
