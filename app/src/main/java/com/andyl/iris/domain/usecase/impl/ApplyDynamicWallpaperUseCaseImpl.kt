package com.andyl.iris.domain.usecase.impl

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.andyl.iris.domain.helper.isVideoUri
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
    private val context: Context,
) : ApplyDynamicWallpaperUseCase {

    private val mutex = Mutex()

    // If the video rule references a content:// URI (e.g. picked via the
    // document picker), copy it to internal storage so IrisLiveWallpaperService
    // (which only knows how to play local files) can actually play it.
    private fun ensureLocalVideo(rawUri: String): String {
        if (!rawUri.startsWith("content://")) return rawUri
        return try {
            val dir = java.io.File(context.filesDir, "live_video").apply { mkdirs() }
            val out = java.io.File(dir, "rule_${System.currentTimeMillis()}.mp4")
            context.contentResolver.openInputStream(android.net.Uri.parse(rawUri))?.use { input ->
                out.outputStream().use { o -> input.copyTo(o) }
            }
            out.absolutePath
        } catch (e: Exception) {
            Log.e("IRIS_WORKER", "ensureLocalVideo failed", e)
            rawUri
        }
    }

    private fun isBatteryLow(threshold: Int = 20): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            ?: return false
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return level >= 0 && level < threshold
    }

    override suspend operator fun invoke(packId: String?) {
        if (mutex.isLocked) {
            Log.d("IRIS_WORKER", "⏩ Skipping ApplyDynamicWallpaper: another process is running.")
            return
        }

        mutex.withLock {
            Log.d("IRIS_WORKER", "🚀 Starting ApplyDynamicWallpaper process...")
            preferencesRepository.refreshFeaturedPacks()

            var effectivePackId = packId
            runCatching {
                val places = preferencesRepository.getPlaces()
                if (places.isNotEmpty()) {
                    val loc = locationRepository.getCurrentLocation()
                    // Priority: place with the smallest radius among those that match
                    // (inside a normal zone, or outside an inverted zone).
                    val matched = places.filter { place ->
                        val inside = place.contains(loc.latitude, loc.longitude)
                        if (place.invert) !inside else inside
                    }
                    val place = matched.minByOrNull { it.radiusMeters }
                    if (place != null && place.packId != null) {
                        Log.d("IRIS_WORKER", "📍 Geofence hit: '${place.name}' -> pack ${place.packId}" +
                            " | invert=${place.invert} radius=${place.radiusMeters}")
                        effectivePackId = place.packId
                    }
                }
            }

            val isVideoConfig = effectivePackId?.startsWith("video_") == true

            if (!isVideoConfig) {
            val config = preferencesRepository.getWallpaperConfig(effectivePackId)
            
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

            val batterySaver = preferencesRepository.getBatterySaverEnabled()
            if (batterySaver && !hasFixedTime && !hasDailyMatch && isBatteryLow()) {
                Log.d("IRIS_WORKER", "🔋 Battery saver active + low battery: skipping weather-based update to save power.")
                return
            }

            var detectedWeather: com.andyl.iris.domain.model.Weather? = null
            var currentSunrise: String? = null
            var currentSunset: String? = null
            var currentTemperature: Double? = null

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
                    currentTemperature = weatherInfo.temperature
                    
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

            val rulesToApply = resolveWallpaperUseCase(finalWeather, timeOfDay, config, currentTemperature)
            Log.d("IRIS_WORKER", "4. Rules to apply: ${rulesToApply.size}")

            if (rulesToApply.isNotEmpty()) {
                val systemRule = rulesToApply.firstOrNull { it.target == 3 }
                    ?: rulesToApply.firstOrNull { it.target == 1 }
                    ?: rulesToApply.firstOrNull { it.target == 2 }

                val liveVideoEnabled = preferencesRepository.getLiveVideoEnabled()
                val activeVideoPath = preferencesRepository.getLiveVideoPath()
                var appliedSystemUri: String? = null
                rulesToApply.forEach { rule ->
                    if (rule.wallpaperId.value.isNotEmpty()) {
                        if (isVideoUri(rule.wallpaperId.value)) {
                            val videoPath = ensureLocalVideo(rule.wallpaperId.value)
                            Log.d("IRIS_WORKER", "🎬 Video rule active, switching live video to $videoPath")
                            preferencesRepository.setLiveVideoPath(videoPath)
                            preferencesRepository.setLiveVideoEnabled(true)
                            if (rule.target != 2 && appliedSystemUri == null) {
                                appliedSystemUri = videoPath
                            }
                        } else if (!liveVideoEnabled) {
                            val resolved = wallpaperRepository.applyWallpaper(
                                wallpaperId = rule.wallpaperId,
                                scaleMode = rule.scaleMode,
                                target = rule.target,
                                cropX = rule.cropX,
                                cropY = rule.cropY,
                                cropScale = rule.cropScale
                            ).getOrNull()
                            if (rule.target != 2 && appliedSystemUri == null && !resolved.isNullOrEmpty()) {
                                appliedSystemUri = resolved
                            }
                        }
                    }
                }

                // If a live video is the active wallpaper, do NOT let an image
                // rule's URI overwrite the "last applied" used by the home
                // preview. Keep pointing to the live video so the UI matches
                // what the user actually sees.
                val storedUri = if (liveVideoEnabled) {
                    appliedSystemUri ?: activeVideoPath
                } else {
                    appliedSystemUri ?: systemRule?.wallpaperId?.value
                }
                preferencesRepository.saveLastAppliedWallpaper(storedUri)
                preferencesRepository.addWallpaperHistoryEntry(
                    uri = storedUri.orEmpty(),
                    weather = finalWeather,
                    timestamp = System.currentTimeMillis()
                )
            }
            }
        }
    }
}
