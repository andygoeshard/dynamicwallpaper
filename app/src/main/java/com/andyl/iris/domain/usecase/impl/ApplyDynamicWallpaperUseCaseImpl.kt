package com.andyl.iris.domain.usecase.impl

import android.app.WallpaperManager
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import com.andyl.iris.domain.helper.LiveTarget
import com.andyl.iris.domain.helper.LiveTargetStore
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

    // Rules can reference videos as: a plain local path (already copied by the
    // UI), a content:// URI (document/MediaStore picker), an https URL (remote
    // pack) or a file:// URI. Normalize everything to a playable local path so
    // IrisLiveWallpaperService can actually render it. Returns null when the
    // video could not be materialized locally.
    private suspend fun ensureLocalVideo(rawUri: String): String? {
        return try {
            when {
                rawUri.startsWith("file://") -> rawUri.removePrefix("file://")
                rawUri.startsWith("/") -> rawUri
                else -> {
                    val dir = java.io.File(context.filesDir, "live_video").apply { mkdirs() }
                    // Protect the incoming file (when it is already local)
                    // from being cleaned up as stale.
                    cleanupOldLiveVideos(dir, keepBesidesActive = rawUri.takeIf { it.startsWith("/") })
                    // Keep the original extension (gifs picked as "videos"
                    // would otherwise be misnamed .mp4).
                    val ext = if (rawUri.startsWith("content://")) {
                        try {
                            context.contentResolver.query(
                                android.net.Uri.parse(rawUri),
                                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                            )?.use { c ->
                                if (c.moveToFirst()) {
                                    c.getString(0)?.substringAfterLast('.', "")?.lowercase()?.take(5)
                                } else null
                            }
                        } catch (_: Exception) {
                            null
                        }
                    } else {
                        rawUri.substringBefore('#').substringBefore('?').substringAfterLast('.', "").take(5)
                    }
                    val out = java.io.File(dir, "rule_${System.currentTimeMillis()}.${ext?.ifEmpty { null } ?: "mp4"}")
                    val input = if (rawUri.startsWith("content://")) {
                        context.contentResolver.openInputStream(android.net.Uri.parse(rawUri))
                    } else {
                        val connection = java.net.URL(rawUri).openConnection() as java.net.HttpURLConnection
                        connection.connectTimeout = 15_000
                        connection.readTimeout = 30_000
                        connection.inputStream
                    }
                    if (input == null) return null
                    input.use { stream ->
                        out.outputStream().use { o -> stream.copyTo(o) }
                    }
                    out.absolutePath
                }
            }
        } catch (e: Exception) {
            Log.e("IRIS_WORKER", "ensureLocalVideo failed", e)
            null
        }
    }

    // Old rule_/local_/custom_ files accumulate forever. Keep only files from
    // the last MAX_VIDEO_AGE_MS and free the rest.
    private suspend fun cleanupOldLiveVideos(dir: java.io.File, keepBesidesActive: String? = null) {
        try {
            val now = System.currentTimeMillis()
            val activeVideo = preferencesRepository.getLiveVideoPath()
            dir.listFiles()?.forEach { f ->
                val stale = (now - f.lastModified()) > MAX_VIDEO_AGE_MS
                if (stale && f.absolutePath != activeVideo && f.absolutePath != keepBesidesActive) {
                    f.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("IRIS_WORKER", "cleanupOldLiveVideos failed", e)
        }
    }

    private fun isLiveWallpaperActive(): Boolean {
        return try {
            val info = WallpaperManager.getInstance(context).wallpaperInfo
            info?.packageName == context.packageName
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun isBatteryLow(): Boolean {
        val threshold = preferencesRepository.getBatterySaverThreshold()
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

                val activeVideoPath = preferencesRepository.getLiveVideoPath()
                var appliedSystemUri: String? = null
                var homeIsVideo = false
                val liveWallpaperActive = isLiveWallpaperActive()
                rulesToApply.forEach { rule ->
                    if (rule.wallpaperId.value.isNotEmpty()) {
                        if (isVideoUri(rule.wallpaperId.value)) {
                            val videoPath = ensureLocalVideo(rule.wallpaperId.value)
                            if (videoPath == null) {
                                Log.e("IRIS_WORKER", "⚠️ Video rule could not be materialized, skipping: ${rule.wallpaperId.value}")
                                return@forEach
                            }
                            Log.d("IRIS_WORKER", "🎬 Video rule active, switching live video to $videoPath")
                            preferencesRepository.setLiveVideoPath(videoPath)
                            preferencesRepository.setLiveVideoEnabled(true)
                            preferencesRepository.setLiveStaticPath(null)
                            LiveTargetStore.write(context, LiveTarget(isVideo = true, path = videoPath))
                            if (rule.target != 2 && appliedSystemUri == null) {
                                appliedSystemUri = videoPath
                                homeIsVideo = true
                            }
                        } else if (liveWallpaperActive) {
                            // The live wallpaper service is the active wallpaper.
                            // Applying a static bitmap via WallpaperManager would
                            // replace the live wallpaper and kill the video.
                            // Instead, hand the photo to the service so it paints
                            // it as a static frame.
                            val resolved = wallpaperRepository.resolvePhotoPath(rule.wallpaperId).getOrNull()
                            Log.d("IRIS_WORKER", "🖼️ Live wallpaper active, routing photo to service: $resolved")
                            if (!resolved.isNullOrEmpty()) {
                                preferencesRepository.setLiveStaticPath(resolved)
                                preferencesRepository.setLiveVideoEnabled(false)
                                preferencesRepository.setLiveVideoPath(null)
                                LiveTargetStore.write(context, LiveTarget(isVideo = false, path = resolved))
                                if (rule.target != 2 && appliedSystemUri == null) {
                                    appliedSystemUri = resolved
                                }
                            }
                        } else {
                            val resolved = wallpaperRepository.applyWallpaper(
                                wallpaperId = rule.wallpaperId,
                                scaleMode = rule.scaleMode,
                                target = rule.target,
                                cropX = rule.cropX,
                                cropY = rule.cropY,
                                cropScale = rule.cropScale
                            ).getOrNull()
                            if (!resolved.isNullOrEmpty()) {
                                LiveTargetStore.write(context, LiveTarget(isVideo = false, path = resolved))
                            }
                            if (rule.target != 2 && appliedSystemUri == null && !resolved.isNullOrEmpty()) {
                                appliedSystemUri = resolved
                            }
                        }
                    }
                }

                // A photo rule took over the home screen: leave live video mode
                // so the next rule (photo or video) can be applied too.
                // Otherwise a single video would lock the wallpaper forever.
                if (appliedSystemUri != null && !homeIsVideo) {
                    preferencesRepository.setLiveVideoEnabled(false)
                }

                // If a live video is the active wallpaper, do NOT let an image
                // rule's URI overwrite the "last applied" used by the home
                // preview. Keep pointing to the live video so the UI matches
                // what the user actually sees.
                val storedUri = if (homeIsVideo) {
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

private const val MAX_VIDEO_AGE_MS = 14L * 24 * 60 * 60 * 1000
