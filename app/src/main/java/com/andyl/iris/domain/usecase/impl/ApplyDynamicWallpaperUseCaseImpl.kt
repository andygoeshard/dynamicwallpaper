package com.andyl.iris.domain.usecase.impl

import android.util.Log
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.repository.WallpaperRepository
import com.andyl.iris.domain.repository.WeatherRepository
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.iris.domain.usecase.contract.ResolveWallpaperUseCase

class ApplyDynamicWallpaperUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val detectTimeOfDayUseCase: DetectTimeOfDayUseCase,
    private val resolveWallpaperUseCase: ResolveWallpaperUseCase,
    private val wallpaperRepository: WallpaperRepository,
) : ApplyDynamicWallpaperUseCase {

    override suspend operator fun invoke(packId: String?) {
        val config = preferencesRepository.getWallpaperConfig(packId)
        Log.d("DEBUG_WORKER", "Worker funcionando!")

        val currentWeather = if (config.enabledWeathers.isNotEmpty()) {
            try {
                Log.d("DEBUG_WORKER", "1. Obteniendo ubicación y clima...")
                val location = locationRepository.getCurrentLocation()
                val weather = weatherRepository.getCurrentWeather(location)

                if (config.enabledWeathers.contains(weather)) {
                    Log.d("DEBUG_WORKER", "2. Clima detectado y soportado: $weather")
                    weather
                } else {
                    Log.d("DEBUG_WORKER", "2. Clima $weather detectado pero NO habilitado en este pack.")
                    null
                }
            } catch (e: Exception) {
                Log.e("DEBUG_WORKER", "Error en sensor/API de clima, aplicando fallback", e)
                null
            }
        } else {
            Log.d("DEBUG_WORKER", "1. Clima desactivado globalmente para este pack.")
            null
        }

        val timeOfDay = detectTimeOfDayUseCase()

        // 1. Obtenemos la lista de reglas (ahora el Resolve devuelve List<WallpaperRule>)
        val rulesToApply = resolveWallpaperUseCase(currentWeather, timeOfDay, config)

        if (rulesToApply.isNotEmpty()) {
            // 2. Iteramos cada regla y la aplicamos a su target específico
            rulesToApply.forEach { rule ->
                if (rule.wallpaperId.value.isNotEmpty()) {
                    wallpaperRepository.applyWallpaper(
                        wallpaperId = rule.wallpaperId,
                        scaleMode = config.scaleMode,
                        target = rule.target
                    )
                    Log.d("DEBUG_WORKER", "Aplicado a target ${rule.target}: ${rule.wallpaperId}")
                }
            }
        } else {
            Log.e("DEBUG_WORKER", "No se encontraron reglas para: $timeOfDay y clima: $currentWeather")
        }
    }
}
