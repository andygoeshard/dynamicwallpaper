package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.usecase.contract.ResolveWallpaperUseCase

class ResolveWallpaperUseCaseImpl : ResolveWallpaperUseCase {

    override suspend operator fun invoke(
        weather: Weather?,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): List<WallpaperRule> {
        val now = java.time.LocalDateTime.now()
        val rulesToApply = mutableListOf<WallpaperRule>()

        // 1. REGLAS POR HORA FIJA (HH:mm, HH:mm-1, HH:mm-2)
        val timeKey = "%02d:%02d".format(now.hour, now.minute)

        // Prioridad 1: "Ambos" (Key limpia: "14:00")
        config.fixedTimeRules[timeKey]?.let { uri ->
            if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 3))
        }

        // Prioridad 2: Específicos (Key con sufijo: "14:00-1" o "14:00-2")
        if (rulesToApply.isEmpty()) {
            config.fixedTimeRules["$timeKey-1"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 1))
            }
            config.fixedTimeRules["$timeKey-2"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 2))
            }
        }

        // Si hay regla por hora fija, cortamos acá (es la regla de mayor prioridad)
        if (rulesToApply.isNotEmpty()) return rulesToApply

        // 2. REGLAS POR DÍA (monday, monday-1, monday-2)
        val dayName = now.dayOfWeek.name.lowercase()

        config.dailyRules[dayName]?.let { uri ->
            if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 3))
        }

        if (rulesToApply.isEmpty()) {
            config.dailyRules["$dayName-1"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 1))
            }
            config.dailyRules["$dayName-2"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 2))
            }
        }

        if (rulesToApply.isNotEmpty()) return rulesToApply

        // 3. REGLAS POR CLIMA/HORA (Iris Core)
        val weatherMatches = config.rules.filter { rule ->
            val matchesWeather = weather != null && rule.weather == weather && config.enabledWeathers.contains(weather)
            val matchesTime = rule.timeOfDay == timeOfDay
            matchesWeather && matchesTime
        }

        if (weatherMatches.isNotEmpty()) return weatherMatches

        val timeMatches = config.rules.filter { it.timeOfDay == timeOfDay }
        if (timeMatches.isNotEmpty()) return timeMatches

        return config.rules.take(1)
    }
}
