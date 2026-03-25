package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.usecase.contract.ResolveWallpaperUseCase

class ResolveWallpaperUseCaseImpl : ResolveWallpaperUseCase {

    override suspend operator fun invoke(
        weather: Weather?,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): WallpaperId {
        val now = java.time.LocalDateTime.now()

        val timeKey = "%02d:%02d".format(now.hour, now.minute)
        config.fixedTimeRules[timeKey]?.let { return WallpaperId(it) }

        val dayName = now.dayOfWeek.name.lowercase()

        config.dailyRules[dayName]?.let { uri ->
            if (uri.isNotEmpty()) return WallpaperId(uri)
        }

        if (weather != null && config.enabledWeathers.contains(weather)) {
            config.rules.firstOrNull { it.weather == weather && it.timeOfDay == timeOfDay }
                ?.let { return it.wallpaperId }
        }

        config.rules.firstOrNull { it.timeOfDay == timeOfDay }
            ?.let { return it.wallpaperId }

        return config.rules.firstOrNull()?.wallpaperId ?: WallpaperId("")
    }
}
