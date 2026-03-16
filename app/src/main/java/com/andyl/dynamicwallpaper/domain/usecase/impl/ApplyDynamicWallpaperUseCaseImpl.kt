package com.andyl.dynamicwallpaper.domain.usecase.impl

import android.util.Log
import com.andyl.dynamicwallpaper.domain.repository.LocationRepository
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.repository.WallpaperRepository
import com.andyl.dynamicwallpaper.domain.repository.WeatherRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase

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

        val wallpaperId = resolveWallpaperUseCase(currentWeather, timeOfDay, config)

        if (wallpaperId != null) {
            wallpaperRepository.applyWallpaper(wallpaperId)
            Log.d("DEBUG_WORKER", "¡Wallpaper aplicado!: $wallpaperId")
        } else {
            Log.e("DEBUG_WORKER", "No se encontró wallpaper para: $timeOfDay y clima: $currentWeather")
        }
    }
}
