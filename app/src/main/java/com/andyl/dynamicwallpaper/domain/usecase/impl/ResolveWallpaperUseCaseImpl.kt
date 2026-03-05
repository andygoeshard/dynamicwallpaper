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

        // 1. PRIORIDAD MÁXIMA: Horarios Fijos (HH:mm)
        val currentTimeString = String.format("%02d:%02d", now.hour, now.minute)
        config.fixedTimeRules[currentTimeString]?.let { uri ->
            Log.d("RESOLVE", ">>> Prioridad 1: Horario Fijo detectado ($currentTimeString)")
            return WallpaperId(uri)
        }

        // 2. SEGUNDA PRIORIDAD: Día de la semana (MONDAY, etc.)
        val currentDay = now.dayOfWeek.name // Devuelve "MONDAY", "TUESDAY", etc.
        config.dailyRules[currentDay]?.let { uri ->
            Log.d("RESOLVE", ">>> Prioridad 2: Regla por Día detectada ($currentDay)")
            return WallpaperId(uri)
        }

        // 3. TERCERA PRIORIDAD: Clima + Momento del día (Solo si el clima NO está muteado)
        if (config.enabledWeathers.contains(weather)) {
            config.rules.firstOrNull {
                it.weather == weather && it.timeOfDay == timeOfDay
            }?.let { rule ->
                Log.d("RESOLVE", ">>> Prioridad 3: Clima activo detectado ($weather - $timeOfDay)")
                return rule.wallpaperId
            }
        } else {
            Log.d("RESOLVE", ">>> Clima $weather está MUTEADO. Saltando a fallback.")
        }

        // 4. FALLBACK: Si el clima está muteado o no hay reglas, buscamos cualquier cosa para el momento del día
        config.rules.firstOrNull {
            it.timeOfDay == timeOfDay
        }?.let { rule ->
            Log.d("RESOLVE", ">>> Fallback: Usando cualquier regla para $timeOfDay")
            return rule.wallpaperId
        }

        // 5. ÚLTIMO RECURSO: La primera regla que aparezca
        return config.rules.firstOrNull()?.wallpaperId
            ?: throw IllegalStateException("WallpaperConfig sin reglas")
    }
}
