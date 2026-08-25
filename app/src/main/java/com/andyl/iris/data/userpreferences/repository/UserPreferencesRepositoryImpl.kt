package com.andyl.iris.data.userpreferences.repository

import android.content.Context
import com.andyl.iris.data.userpreferences.dto.WallpaperRuleDto
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.mapper.weatherFromKey
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.model.WallpaperHistoryEntry
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.repository.PremiumRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import com.andyl.iris.data.userpreferences.dto.WallpaperPackDto
import com.andyl.iris.data.userpreferences.dto.toDomain
import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.ScaleMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(
    context: Context,
    private val premiumRepository: PremiumRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    private val prefs = context.getSharedPreferences(
        "user_preferences",
        Context.MODE_PRIVATE
    )
    private val json = Json { encodeDefaults = true }

    override suspend fun getWallpaperConfig(packId: String?): WallpaperConfig = withContext(ioDispatcher){
        val targetId = packId ?: getActivePackId()
        val rawPack = prefs.getString("${KEY_PACK_DATA}_$targetId", null)

        if (rawPack != null) {
            val dto = json.decodeFromString<WallpaperPackDto>(rawPack)
            dto.toDomain(targetId)
        } else {
            val legacyRules = loadLegacyRules(targetId)
            val rules = legacyRules.ifEmpty { defaultRules() }

            WallpaperConfig(
                id = targetId,
                name = "Pack $targetId",
                rules = rules,
                activePackId = getActivePackId(),
                scaleMode = ScaleMode.CROP
            )
        }
    }

    override suspend fun setWallpaperConfig(config: WallpaperConfig) = withContext(ioDispatcher) {
        val dto = WallpaperPackDto(
            id = config.id,
            name = config.name,
            updateIntervalMinutes = config.updateIntervalMinutes,
            weatherRules = config.rules.map {
                WallpaperRuleDto(
                    weather = it.weather.toKey(),
                    timeOfDay = it.timeOfDay.name,
                    uri = it.wallpaperId.value,
                    target = it.target,
                    scaleMode = it.scaleMode.name,
                    cropX = it.cropX,
                    cropY = it.cropY,
                    cropScale = it.cropScale
                )
            },
            dailyRules = config.dailyRules,
            fixedTimeRules = config.fixedTimeRules,
            temperatureRules = config.temperatureRules,
            enabledWeathers = config.enabledWeathers.map { it.toKey() },
            scaleMode = config.scaleMode.name
        )

        val serialized = json.encodeToString(dto)
        prefs.edit {
            putString("${KEY_PACK_DATA}_${config.id}", serialized)
            putString(KEY_ACTIVE_PACK_ID, config.activePackId)
        }
    }

    private fun loadLegacyRules(packId: String): List<WallpaperRule> {
        val raw = prefs.getString("wallpaper_rules_$packId", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<WallpaperRuleDto>>(raw).map {
                WallpaperRule(
                    weather = weatherFromKey(it.weather),
                    timeOfDay = TimeOfDay.valueOf(it.timeOfDay),
                    wallpaperId = WallpaperId(it.uri)
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getActivePackId(): String {
        return prefs.getString(KEY_ACTIVE_PACK_ID, "1") ?: "1"
    }

    override suspend fun setActivePackId(packId: String) {
        prefs.edit { putString(KEY_ACTIVE_PACK_ID, packId) }
    }


    // --- MÉTODOS DE UBICACIÓN Y CIUDAD ---

    override suspend fun saveLastLocation(lat: Double, lon: Double) {
        prefs.edit {
            putString(KEY_LAST_LAT, lat.toString())
                .putString(KEY_LAST_LON, lon.toString())
        }
    }

    override suspend fun getLastLocation(): GeoLocation? {
        val lat = prefs.getString(KEY_LAST_LAT, null)?.toDoubleOrNull()
        val lon = prefs.getString(KEY_LAST_LON, null)?.toDoubleOrNull()
        return if (lat != null && lon != null) GeoLocation(lat, lon) else null
    }

    override suspend fun saveManualLocation(location: GeoLocation, cityName: String) {
        prefs.edit {
            putString(KEY_LAST_LAT, location.latitude.toString())
                .putString(KEY_LAST_LON, location.longitude.toString())
                .putString(KEY_CITY_NAME, cityName)
        }
    }

    override suspend fun saveCityName(name: String) {
        prefs.edit { putString(KEY_CITY_NAME, name) }
    }

    override suspend fun getCityName(): String? {
        return prefs.getString(KEY_CITY_NAME, null)
    }

    // Este lo pedía el ViewModel para el init
    override suspend fun getSavedCityName(): String? = getCityName()

    private fun defaultRules(): List<WallpaperRule> {
        val weathers = listOf(
            Weather.Clear, Weather.Cloudy, Weather.Rain,
            Weather.Snow, Weather.Fog, Weather.Storm
        )
        val times = listOf(TimeOfDay.DAWN, TimeOfDay.DAY, TimeOfDay.DUSK, TimeOfDay.NIGHT)

        return weathers.flatMap { w ->
            times.map { t -> WallpaperRule(w, t, WallpaperId("")) }
        }
    }

    override suspend fun addNewPack(): List<PackInfo> {
        val allPacks = getAllPacks()
        val maxPacks = premiumRepository.getMaxCustomPacks()

        if (allPacks.size >= maxPacks) {
            throw IllegalStateException("Límite de $maxPacks paquetes alcanzado")
        }

        val newId = System.currentTimeMillis().toString()
        val newName = "Pack ${allPacks.size + 1}"

        val newConfig = WallpaperConfig(
            id = newId,
            name = newName,
            rules = defaultRules(),
            activePackId = getActivePackId(),
            scaleMode = ScaleMode.CROP
        )

        setWallpaperConfig(newConfig)
        return getAllPacks()
    }

    override suspend fun deletePack(packId: String) {
        val allPacks = getAllPacks()
        if (allPacks.size <= 1) throw IllegalStateException("No puedes borrar todos los paquetes")

        val activeId = getActivePackId()
        if (packId == activeId) {
            val nextAvailable = allPacks.firstOrNull { it.id != packId }
            nextAvailable?.let { setActivePackId(it.id) }
        }

        prefs.edit {
            remove("${KEY_PACK_DATA}_$packId")
            remove("wallpaper_rules_$packId")
        }
    }

    override suspend fun getAllPacks(): List<PackInfo> = withContext(ioDispatcher) {
        val activeId = getActivePackId()
        val allEntries = prefs.all

        val savedPacks = allEntries.filter { (key, _) ->
            key.startsWith(KEY_PACK_DATA)
        }.map { (key, value) ->
            val packId = key.removePrefix("${KEY_PACK_DATA}_")
            val rawJson = value as? String

            val packName = if (rawJson != null) {
                try {
                    val dto = json.decodeFromString<WallpaperPackDto>(rawJson)
                    dto.name
                } catch (e: Exception) { "Pack $packId" }
            } else {
                "Pack $packId"
            }

            PackInfo(
                id = packId,
                name = packName,
                isActive = packId == activeId
            )
        }.sortedBy { it.id }

        savedPacks.ifEmpty {
            val defaultPacks = listOf(
                PackInfo("1", "Pack 1", isActive = true),
                PackInfo("2", "Pack 2", isActive = false),
                PackInfo("3", "Pack 3", isActive = false)
            )
            defaultPacks
        }
    }

    override suspend fun isFirstApplyGlobal(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_GLOBAL_FIRST_APPLY, true)
    }

    override suspend fun setGlobalFirstApplyDone() = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_GLOBAL_FIRST_APPLY, false) }
    }

    override suspend fun setUseGps(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_USE_GPS, enabled) }
    }

    override suspend fun shouldUseGps(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_USE_GPS, true)
    }

    override suspend fun saveLastWeather(weather: Weather) = withContext(ioDispatcher) {
        prefs.edit { putString(KEY_LAST_WEATHER, weather.toKey()) }
    }

    override suspend fun getLastWeather(): Weather? = withContext(ioDispatcher) {
        prefs.getString(KEY_LAST_WEATHER, null)?.let { weatherFromKey(it) }
    }

    override suspend fun saveLastUpdateTime(time: Long) = withContext(ioDispatcher) {
        prefs.edit { putLong(KEY_LAST_UPDATE_TIME, time) }
    }

    override suspend fun getLastUpdateTime(): Long = withContext(ioDispatcher) {
        prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)
    }

    override suspend fun incrementAppSuccessCount() = withContext(ioDispatcher) {
        val current = prefs.getInt(KEY_SUCCESS_COUNT, 0)
        prefs.edit { putInt(KEY_SUCCESS_COUNT, current + 1) }
    }

    override suspend fun getAppSuccessCount(): Int = withContext(ioDispatcher) {
        prefs.getInt(KEY_SUCCESS_COUNT, 0)
    }

    override suspend fun setRated(rated: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_HAS_RATED, rated) }
    }

    override suspend fun hasRated(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_HAS_RATED, false)
    }

    override suspend fun isOnboardingCompleted(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override suspend fun setOnboardingCompleted() = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, true) }
    }

    override suspend fun hasBackgroundLocationDisclosureAcknowledged(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_BACKGROUND_LOCATION_DISCLOSURE, false)
    }

    override suspend fun setBackgroundLocationDisclosureAcknowledged() = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_BACKGROUND_LOCATION_DISCLOSURE, true) }
    }

    override suspend fun refreshFeaturedPacks() = withContext(ioDispatcher) {
        val freePacks = PredefinedPacks.packs.filter { !it.isPremium && it.season == null }
        if (freePacks.isEmpty()) return@withContext
        val cal = java.util.Calendar.getInstance()
        val size = freePacks.size
        val dayIndex = (cal.get(java.util.Calendar.YEAR) * 366 + cal.get(java.util.Calendar.DAY_OF_YEAR)) % size
        val monthIndex = cal.get(java.util.Calendar.MONTH) % size
        val yearIndex = cal.get(java.util.Calendar.YEAR) % size

        var d = dayIndex
        var m = monthIndex
        while (m == yearIndex || m == d) m = (m + 1) % size
        while (d == yearIndex || d == m) d = (d + 1) % size

        prefs.edit {
            putString(KEY_FEATURED_DAY, freePacks[d].id)
            putString(KEY_FEATURED_MONTH, freePacks[m].id)
            putString(KEY_FEATURED_YEAR, freePacks[yearIndex].id)
        }
    }

    override suspend fun getFeaturedPackId(type: String): String? = withContext(ioDispatcher) {
        val key = when (type) {
            "day" -> KEY_FEATURED_DAY
            "month" -> KEY_FEATURED_MONTH
            "year" -> KEY_FEATURED_YEAR
            else -> return@withContext null
        }
        prefs.getString(key, null)
    }

    override suspend fun getDarkModePreference(): Boolean? = withContext(ioDispatcher) {
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    }

    override suspend fun setDarkModePref(pref: Boolean?) = withContext(ioDispatcher) {
        prefs.edit {
            if (pref == null) remove(KEY_DARK_MODE) else putBoolean(KEY_DARK_MODE, pref)
        }
    }

    override suspend fun getAccentColor(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_ACCENT_COLOR, null)
    }

    override suspend fun setAccentColor(color: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (color == null) remove(KEY_ACCENT_COLOR) else putString(KEY_ACCENT_COLOR, color)
        }
    }

    override suspend fun getAmoledMode(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_AMOLED_MODE, false)
    }

    override suspend fun setAmoledMode(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_AMOLED_MODE, enabled) }
    }

    override suspend fun getReduceAnimations(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_REDUCE_ANIMATIONS, false)
    }

    override suspend fun setReduceAnimations(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_REDUCE_ANIMATIONS, enabled) }
    }

    override suspend fun getHapticsEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_HAPTICS, true)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_HAPTICS, enabled) }
    }

    override suspend fun getSoundEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_SOUND, true)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_SOUND, enabled) }
    }

    override suspend fun getUseWallpaperBackground(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_APP_BACKGROUND, false)
    }

    override suspend fun setUseWallpaperBackground(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_APP_BACKGROUND, enabled) }
    }

    override suspend fun getLastAppliedWallpaper(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_LAST_WALLPAPER, null)
    }

    override suspend fun saveLastAppliedWallpaper(uri: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (uri == null) remove(KEY_LAST_WALLPAPER) else putString(KEY_LAST_WALLPAPER, uri)
        }
    }

    override suspend fun getBatterySaverEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_BATTERY_SAVER, false)
    }

    override suspend fun setBatterySaverEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_BATTERY_SAVER, enabled) }
    }

    override suspend fun getBatterySaverThreshold(): Int = withContext(ioDispatcher) {
        prefs.getInt(KEY_BATTERY_SAVER_THRESHOLD, DEFAULT_BATTERY_SAVER_THRESHOLD)
    }

    override suspend fun setBatterySaverThreshold(threshold: Int) = withContext(ioDispatcher) {
        prefs.edit { putInt(KEY_BATTERY_SAVER_THRESHOLD, threshold.coerceIn(10, 50)) }
    }

    override suspend fun getNotificationsEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
    }

    override suspend fun getUpdateIntervalMinutes(): Int = withContext(ioDispatcher) {
        prefs.getInt(KEY_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL_MINUTES)
    }

    override suspend fun setUpdateIntervalMinutes(minutes: Int) = withContext(ioDispatcher) {
        prefs.edit { putInt(KEY_UPDATE_INTERVAL, minutes) }
    }

    override suspend fun recordChange(weather: Weather?) = withContext(ioDispatcher) {
        val today = java.time.LocalDate.now().toString()
        val daily = jsonToIntMap(prefs.getString(KEY_STATS_HISTORY, null)).toMutableMap()
        daily[today] = (daily[today] ?: 0) + 1
        prefs.edit { putString(KEY_STATS_HISTORY, json.encodeToString(daily)) }

        if (weather != null) {
            val weatherMap = jsonToIntMap(prefs.getString(KEY_STATS_WEATHER, null)).toMutableMap()
            val key = weather.toKey()
            weatherMap[key] = (weatherMap[key] ?: 0) + 1
            prefs.edit { putString(KEY_STATS_WEATHER, json.encodeToString(weatherMap)) }
        }
    }

    override suspend fun getChangesHistory(): Map<String, Int> = withContext(ioDispatcher) {
        jsonToIntMap(prefs.getString(KEY_STATS_HISTORY, null))
    }

    override suspend fun getWeatherChanges(): Map<String, Int> = withContext(ioDispatcher) {
        jsonToIntMap(prefs.getString(KEY_STATS_WEATHER, null))
    }

    override suspend fun recordPackChange(packId: String) = withContext(ioDispatcher) {
        val packMap = jsonToIntMap(prefs.getString(KEY_STATS_PACKS, null)).toMutableMap()
        packMap[packId] = (packMap[packId] ?: 0) + 1
        prefs.edit { putString(KEY_STATS_PACKS, json.encodeToString(packMap)) }
    }

    override suspend fun getPackChanges(): Map<String, Int> = withContext(ioDispatcher) {
        jsonToIntMap(prefs.getString(KEY_STATS_PACKS, null))
    }

    override suspend fun getWallpaperHistory(): List<WallpaperHistoryEntry> = withContext(ioDispatcher) {
        val raw = prefs.getString(KEY_WALLPAPER_HISTORY, null)
        if (raw.isNullOrBlank()) emptyList()
        else runCatching {
            json.decodeFromString<List<WallpaperHistoryEntry>>(raw)
        }.getOrDefault(emptyList())
    }

    override suspend fun addWallpaperHistoryEntry(uri: String, weather: Weather?, timestamp: Long) = withContext(ioDispatcher) {
        if (uri.isBlank()) return@withContext
        val current = getWallpaperHistory().toMutableList()
        current.removeAll { it.uri == uri }
        current.add(0, WallpaperHistoryEntry(uri, weather?.toKey(), timestamp))
        prefs.edit { putString(KEY_WALLPAPER_HISTORY, json.encodeToString(current.take(8))) }
    }

    override suspend fun clearWallpaperHistory() = withContext(ioDispatcher) {
        prefs.edit { remove(KEY_WALLPAPER_HISTORY) }
    }

    override suspend fun getWallpaperOverlayText(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_OVERLAY_TEXT, null)
    }

    override suspend fun setWallpaperOverlayText(text: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (text.isNullOrBlank()) remove(KEY_OVERLAY_TEXT) else putString(KEY_OVERLAY_TEXT, text.trim())
        }
    }

    override suspend fun getOverlayTextEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    override suspend fun setOverlayTextEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_OVERLAY_ENABLED, enabled) }
    }

    override suspend fun getPlaces(): List<com.andyl.iris.domain.model.GeoPlace> = withContext(ioDispatcher) {
        val raw = prefs.getString(KEY_PLACES, null)
        if (raw.isNullOrBlank()) emptyList() else runCatching {
            json.decodeFromString<List<com.andyl.iris.domain.model.GeoPlace>>(raw)
        }.getOrDefault(emptyList())
    }

    override suspend fun setPlaces(places: List<com.andyl.iris.domain.model.GeoPlace>) = withContext(ioDispatcher) {
        prefs.edit { putString(KEY_PLACES, json.encodeToString(places)) }
    }

    override suspend fun getRandomGalleryBucketId(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_GALLERY_BUCKET, null)
    }

    override suspend fun setRandomGalleryBucketId(bucketId: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (bucketId.isNullOrBlank()) remove(KEY_GALLERY_BUCKET) else putString(KEY_GALLERY_BUCKET, bucketId)
        }
    }

    override suspend fun getLiveVideoPath(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_LIVE_VIDEO_PATH, null)
    }

    override suspend fun setLiveVideoPath(path: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (path.isNullOrBlank()) remove(KEY_LIVE_VIDEO_PATH) else putString(KEY_LIVE_VIDEO_PATH, path)
        }
    }

    override suspend fun getLiveVideoEnabled(): Boolean = withContext(ioDispatcher) {
        prefs.getBoolean(KEY_LIVE_VIDEO_ENABLED, false)
    }

    override suspend fun setLiveVideoEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        prefs.edit { putBoolean(KEY_LIVE_VIDEO_ENABLED, enabled) }
    }

    override suspend fun getLiveStaticPath(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_LIVE_STATIC_PATH, null)
    }

    override suspend fun setLiveStaticPath(path: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (path.isNullOrBlank()) remove(KEY_LIVE_STATIC_PATH) else putString(KEY_LIVE_STATIC_PATH, path)
        }
    }

    override suspend fun setActiveVideoPackId(packId: String?) = withContext(ioDispatcher) {
        prefs.edit {
            if (packId.isNullOrBlank()) remove(KEY_ACTIVE_VIDEO_PACK) else putString(KEY_ACTIVE_VIDEO_PACK, packId)
        }
    }

    override suspend fun getHomeSectionsOrder(): List<String> = withContext(ioDispatcher) {
        prefs.getString(KEY_HOME_SECTIONS, null)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.size == 4 }
            ?: DEFAULT_HOME_SECTIONS
    }

    override suspend fun setHomeSectionsOrder(order: List<String>) = withContext(ioDispatcher) {
        prefs.edit { putString(KEY_HOME_SECTIONS, order.joinToString(",")) }
    }

    private fun jsonToIntMap(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            json.decodeFromString<Map<String, Int>>(raw)
        }.getOrDefault(emptyMap())
    }

    companion object {
        private const val KEY_PACK_DATA = "wallpaper_pack_data"
        private const val KEY_RULES = "wallpaper_rules"
        private const val KEY_ACTIVE_PACK_ID = "active_pack_id"
        private const val KEY_LAST_LAT = "last_latitude"
        private const val KEY_LAST_LON = "last_longitude"
        private const val KEY_CITY_NAME = "last_city_name"
        private const val KEY_GLOBAL_FIRST_APPLY = "global_first_apply_done"
        private const val KEY_USE_GPS = "use_gps"
        private const val KEY_LAST_WEATHER = "last_weather"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"
        private const val KEY_SUCCESS_COUNT = "app_success_count"
        private const val KEY_HAS_RATED = "has_rated"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_BACKGROUND_LOCATION_DISCLOSURE = "background_location_disclosure_accepted"
        private const val KEY_FEATURED_DAY = "featured_pack_day"
        private const val KEY_FEATURED_MONTH = "featured_pack_month"
        private const val KEY_FEATURED_YEAR = "featured_pack_year"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_AMOLED_MODE = "amoled_mode"
        private const val KEY_REDUCE_ANIMATIONS = "reduce_animations"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_APP_BACKGROUND = "app_background_enabled"
        private const val KEY_LAST_WALLPAPER = "last_applied_wallpaper"
        private const val KEY_BATTERY_SAVER = "battery_saver_enabled"
        private const val KEY_BATTERY_SAVER_THRESHOLD = "battery_saver_threshold"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_UPDATE_INTERVAL = "update_interval_minutes"
        private const val KEY_STATS_HISTORY = "stats_changes_history"
        private const val KEY_STATS_WEATHER = "stats_weather_changes"
        private const val KEY_STATS_PACKS = "stats_pack_changes"
        private const val KEY_WALLPAPER_HISTORY = "wallpaper_history"
        private const val KEY_OVERLAY_TEXT = "overlay_text"
        private const val KEY_OVERLAY_ENABLED = "overlay_text_enabled"
        private const val KEY_PLACES = "geofence_places"
        private const val KEY_GALLERY_BUCKET = "random_gallery_bucket"
        private const val KEY_LIVE_VIDEO_PATH = "live_video_path"
        private const val KEY_LIVE_VIDEO_ENABLED = "live_video_enabled"
        private const val KEY_LIVE_STATIC_PATH = "live_static_path"
        private const val KEY_ACTIVE_VIDEO_PACK = "active_video_pack_id"
        private const val KEY_HOME_SECTIONS = "home_sections_order"
        private val DEFAULT_HOME_SECTIONS = listOf("day", "weather", "fixed", "temperature")
        private const val DEFAULT_UPDATE_INTERVAL_MINUTES = 60
        private const val DEFAULT_BATTERY_SAVER_THRESHOLD = 20
    }
}

