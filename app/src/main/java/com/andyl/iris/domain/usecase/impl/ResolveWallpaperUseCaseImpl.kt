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

        // 1. Reglas por Hora Exacta
        val timeKey = "%02d:%02d".format(now.hour, now.minute)
        config.fixedTimeRules[timeKey]?.let { uri ->
            if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 3, scaleMode = config.scaleMode))
        }

        if (rulesToApply.isEmpty()) {
            config.fixedTimeRules["$timeKey-1"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 1, scaleMode = config.scaleMode))
            }
            config.fixedTimeRules["$timeKey-2"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 2, scaleMode = config.scaleMode))
            }
        }
        if (rulesToApply.isNotEmpty()) return rulesToApply

        // 2. Reglas Diarias
        val dayName = now.dayOfWeek.name.lowercase()
        config.dailyRules[dayName]?.let { uri ->
            if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 3, scaleMode = config.scaleMode))
        }

        if (rulesToApply.isEmpty()) {
            config.dailyRules["$dayName-1"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 1, scaleMode = config.scaleMode))
            }
            config.dailyRules["$dayName-2"]?.let { uri ->
                if (uri.isNotEmpty()) rulesToApply.add(WallpaperRule(Weather.Clear, timeOfDay, WallpaperId(uri), target = 2, scaleMode = config.scaleMode))
            }
        }
        if (rulesToApply.isNotEmpty()) return rulesToApply

        // 3. Reglas por Clima/Momento
        val weatherToMatch = weather ?: Weather.Clear
        val weatherMatches = config.rules.filter { rule ->
            // If weather is null (disabled), we don't filter by weather, we just match time later
            val matchesWeather = rule.weather == weatherToMatch && config.enabledWeathers.contains(weatherToMatch)
            val matchesTime = rule.timeOfDay == timeOfDay
            matchesWeather && matchesTime
        }

        if (weatherMatches.isNotEmpty()) return weatherMatches

        // Fallback 1: Reglas que coincidan con el momento del día ignorando el clima
        val timeMatches = config.rules.filter { it.timeOfDay == timeOfDay && it.wallpaperId.value.isNotEmpty() }
        if (timeMatches.isNotEmpty()) return timeMatches

        // Fallback 2: Cualquier regla que tenga una imagen válida
        val anyMatches = config.rules.filter { it.wallpaperId.value.isNotEmpty() }
        if (anyMatches.isNotEmpty()) return anyMatches.take(1)

        return emptyList()
    }
}
