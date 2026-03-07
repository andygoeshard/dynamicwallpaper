package com.andyl.dynamicwallpaper.domain.usecase.impl

import android.util.Log
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase

class ResolveWallpaperUseCaseImpl : ResolveWallpaperUseCase {

    override suspend operator fun invoke(
        weather: Weather,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): WallpaperId {

        val now = java.time.LocalDateTime.now()

        // 1. Horarios Fijos (HH:mm)
        val currentTimeString = String.format("%02d:%02d", now.hour, now.minute)
        config.fixedTimeRules[currentTimeString]?.let { return WallpaperId(it) }

        // 2. Día de la semana (Normalizado a tus keys en español y minúsculas)
        val daysList = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val currentDay = daysList[now.dayOfWeek.value % 7]

        config.dailyRules[currentDay]?.let { uri ->
            Log.d("RESOLVE", ">>> Match Día: $currentDay")
            return WallpaperId(uri)
        }

        // 3. Clima + Momento (Solo si no está muteado)
        if (config.enabledWeathers.contains(weather)) {
            config.rules.firstOrNull { it.weather == weather && it.timeOfDay == timeOfDay }
                ?.let { return it.wallpaperId }
        }

        // 4. Fallback: Cualquier regla para el momento del día
        config.rules.firstOrNull { it.timeOfDay == timeOfDay }
            ?.let { return it.wallpaperId }

        // 5. ÚLTIMO RECURSO: Sin excepciones
        return config.rules.firstOrNull()?.wallpaperId
            ?: WallpaperId("") // Devolvemos ID vacío en lugar de explotar
    }
}
