package com.andyl.dynamicwallpaper.domain.usecase.impl

import android.util.Log
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase

class ResolveWallpaperUseCaseImpl : ResolveWallpaperUseCase {

    override suspend operator fun invoke(
        weather: Weather?,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): WallpaperId {
        val now = java.time.LocalDateTime.now()

        val currentTimeString = String.format("%02d:%02d", now.hour, now.minute)
        config.fixedTimeRules[currentTimeString]?.let { return WallpaperId(it) }

        val daysList = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val currentDay = daysList[now.dayOfWeek.value % 7]
        config.dailyRules[currentDay]?.let { uri ->
            Log.d("RESOLVE", ">>> Match Día: $currentDay")
            return WallpaperId(uri)
        }

        if (weather != null && config.enabledWeathers.contains(weather)) {
            config.rules.firstOrNull { it.weather == weather && it.timeOfDay == timeOfDay }
                ?.let {
                    Log.d("RESOLVE", ">>> Match Clima ($weather) + Momento ($timeOfDay)")
                    return it.wallpaperId
                }
        }

        config.rules.firstOrNull { it.timeOfDay == timeOfDay }
            ?.let {
                Log.d("RESOLVE", ">>> Match Momento (Fallback Clima): $timeOfDay")
                return it.wallpaperId
            }

        return config.rules.firstOrNull()?.wallpaperId ?: WallpaperId("")
    }
}
