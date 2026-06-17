package com.andyl.iris.domain.usecase.impl

import android.util.Log
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.repository.WallpaperRepository
import com.andyl.iris.domain.repository.WeatherRepository
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.iris.domain.usecase.contract.ResolveWallpaperUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ApplyDynamicWallpaperUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val detectTimeOfDayUseCase: DetectTimeOfDayUseCase,
    private val resolveWallpaperUseCase: ResolveWallpaperUseCase,
    private val wallpaperRepository: WallpaperRepository,
) : ApplyDynamicWallpaperUseCase {

    private val mutex = Mutex()

    override suspend operator fun invoke(packId: String?) {
        if (mutex.isLocked) {
            Log.d("IRIS_WORKER", "⏩ Skipping ApplyDynamicWallpaper: another process is running.")
            return
        }

        mutex.withLock {
            Log.d("IRIS_WORKER", "🚀 Starting ApplyDynamicWallpaper process...")
            val config = preferencesRepository.getWallpaperConfig(packId)
            
            // OPTIMIZATION: Check for weather-independent rules FIRST
            val now = java.time.LocalDateTime.now()
            val timeKey = "%02d:%02d".format(now.hour, now.minute)
            val dayName = now.dayOfWeek.name.lowercase()
            
            val hasFixedTime = config.fixedTimeRules.containsKey(timeKey) || 
                              config.fixedTimeRules.containsKey("$timeKey-1") || 
                              config.fixedTimeRules.containsKey("$timeKey-2")
            
            val hasDailyMatch = config.dailyRules.containsKey(dayName) || 
                               config.dailyRules.containsKey("$dayName-1") || 
                               config.dailyRules.containsKey("$dayName-2")

            var detectedWeather: com.andyl.iris.domain.model.Weather? = null
            var currentSunrise: String? = null
            var currentSunset: String? = null

            // Only fetch weather if we DON'T have an overriding fixed/daily rule 
            // OR if weather is actually enabled in this pack.
            if (!hasFixedTime && !hasDailyMatch && config.enabledWeathers.isNotEmpty()) {
                try {
                    Log.d("IRIS_WORKER", "1. Fetching location for weather-based update...")
                    val location = locationRepository.getCurrentLocation()
                    val weatherInfo = weatherRepository.getCurrentWeather(location)
                    
                    currentSunrise = weatherInfo.sunrise
                    currentSunset = weatherInfo.sunset
                    detectedWeather = weatherInfo.weather
                    
                    Log.d("IRIS_WORKER", "✅ Weather detected: $detectedWeather")
                    preferencesRepository.saveLastWeather(detectedWeather)
                } catch (e: Exception) {
                    Log.e("IRIS_WORKER", "❌ Weather fetch failed: ${e.message}")
                }
            } else {
                Log.d("IRIS_WORKER", "⏩ Skipping weather fetch (Fixed: $hasFixedTime, Daily: $hasDailyMatch, Enabled: ${config.enabledWeathers.size})")
            }

            val timeOfDay = detectTimeOfDayUseCase(currentSunrise, currentSunset)
            val finalWeather = if (detectedWeather != null && config.enabledWeathers.contains(detectedWeather)) {
                detectedWeather
            } else null

            preferencesRepository.saveLastUpdateTime(System.currentTimeMillis())

            val rulesToApply = resolveWallpaperUseCase(finalWeather, timeOfDay, config)
            Log.d("IRIS_WORKER", "4. Rules to apply: ${rulesToApply.size}")

            if (rulesToApply.isNotEmpty()) {
                rulesToApply.forEach { rule ->
                    if (rule.wallpaperId.value.isNotEmpty()) {
                        wallpaperRepository.applyWallpaper(
                            wallpaperId = rule.wallpaperId,
                            scaleMode = rule.scaleMode,
                            target = rule.target,
                            cropX = rule.cropX,
                            cropY = rule.cropY,
                            cropScale = rule.cropScale
                        )
                    }
                }
            }
        }
    }
}
