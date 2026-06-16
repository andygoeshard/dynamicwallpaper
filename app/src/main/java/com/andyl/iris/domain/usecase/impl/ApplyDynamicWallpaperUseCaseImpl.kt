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
        
        var currentSunrise: String? = null
        var currentSunset: String? = null
        var detectedWeather: com.andyl.iris.domain.model.Weather? = null

        if (config.enabledWeathers.isNotEmpty()) {
            try {
                Log.d("IRIS_WORKER", "1. Fetching location...")
                val location = locationRepository.getCurrentLocation()
                
                Log.d("IRIS_WORKER", "2. Fetching weather for ${location.latitude}, ${location.longitude}")
                val weatherInfo = weatherRepository.getCurrentWeather(location)
                
                currentSunrise = weatherInfo.sunrise
                currentSunset = weatherInfo.sunset
                detectedWeather = weatherInfo.weather

                Log.d("IRIS_WORKER", "✅ Weather detected: $detectedWeather")
                
                // Save last known weather for UI
                preferencesRepository.saveLastWeather(detectedWeather)
                
            } catch (e: Exception) {
                Log.e("IRIS_WORKER", "❌ Error fetching location/weather: ${e.message}", e)
            }
        }

        val timeOfDay = detectTimeOfDayUseCase(currentSunrise, currentSunset)
        Log.d("IRIS_WORKER", "3. Time of day: $timeOfDay (Sunrise: $currentSunrise, Sunset: $currentSunset)")

        // Check if detected weather is enabled in the current pack
        val finalWeather = if (detectedWeather != null && config.enabledWeathers.contains(detectedWeather)) {
            detectedWeather
        } else {
            Log.d("IRIS_WORKER", "⚠️ Weather $detectedWeather not enabled or null, using time-only logic.")
            null
        }

        preferencesRepository.saveLastUpdateTime(System.currentTimeMillis())

        val rulesToApply = resolveWallpaperUseCase(finalWeather, timeOfDay, config)
        Log.d("IRIS_WORKER", "4. Rules to apply: ${rulesToApply.size}")

        if (rulesToApply.isNotEmpty()) {
            rulesToApply.forEach { rule ->
                if (rule.wallpaperId.value.isNotEmpty()) {
                    Log.d("IRIS_WORKER", "Applying wallpaper: ${rule.wallpaperId.value} with mode ${rule.scaleMode}")
                    val result = wallpaperRepository.applyWallpaper(
                        wallpaperId = rule.wallpaperId,
                        scaleMode = rule.scaleMode,
                        target = rule.target,
                        cropX = rule.cropX,
                        cropY = rule.cropY,
                        cropScale = rule.cropScale
                    )
                    if (result.isSuccess) {
                        Log.d("IRIS_WORKER", "✅ SUCCESS!")
                    } else {
                        Log.e("IRIS_WORKER", "❌ FAILED: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        }
    }
}
