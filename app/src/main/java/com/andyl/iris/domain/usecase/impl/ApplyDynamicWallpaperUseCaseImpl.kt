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
        Log.d("IRIS_WORKER", "🚀 Starting ApplyDynamicWallpaper process...")
        val config = preferencesRepository.getWallpaperConfig(packId)
        Log.d("IRIS_WORKER", "Using pack: ${config.name} (ID: ${config.id})")

        val currentWeather = if (config.enabledWeathers.isNotEmpty()) {
            try {
                Log.d("IRIS_WORKER", "1. Fetching location...")
                val location = locationRepository.getCurrentLocation()
                Log.d("IRIS_WORKER", "Location: ${location.latitude}, ${location.longitude}")
                
                Log.d("IRIS_WORKER", "2. Fetching weather...")
                val weather = weatherRepository.getCurrentWeather(location)

                if (config.enabledWeathers.contains(weather)) {
                    Log.d("IRIS_WORKER", "✅ Weather detected and supported: $weather")
                    weather
                } else {
                    Log.d("IRIS_WORKER", "⚠️ Weather $weather detected but NOT enabled in this pack.")
                    null
                }
            } catch (e: Exception) {
                Log.e("IRIS_WORKER", "❌ Error fetching location/weather: ${e.message}", e)
                null
            }
        } else {
            Log.d("IRIS_WORKER", "ℹ️ Weather features disabled for this pack.")
            null
        }

        val timeOfDay = detectTimeOfDayUseCase()
        Log.d("IRIS_WORKER", "3. Time of day: $timeOfDay")

        val rulesToApply = resolveWallpaperUseCase(currentWeather, timeOfDay, config)
        Log.d("IRIS_WORKER", "4. Rules found to apply: ${rulesToApply.size}")

        if (rulesToApply.isNotEmpty()) {
            rulesToApply.forEach { rule ->
                if (rule.wallpaperId.value.isNotEmpty()) {
                    Log.d("IRIS_WORKER", "Applying: ${rule.wallpaperId.value} to target ${rule.target}")
                    val result = wallpaperRepository.applyWallpaper(
                        wallpaperId = rule.wallpaperId,
                        scaleMode = config.scaleMode,
                        target = rule.target
                    )
                    if (result.isSuccess) {
                        Log.d("IRIS_WORKER", "✅ SUCCESS: Wallpaper changed!")
                    } else {
                        Log.e("IRIS_WORKER", "❌ FAILED: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.w("IRIS_WORKER", "⚠️ Rule found but has empty wallpaper URI.")
                }
            }
        } else {
            Log.e("IRIS_WORKER", "❌ No rules matched the current state ($currentWeather, $timeOfDay)")
        }
    }
}
