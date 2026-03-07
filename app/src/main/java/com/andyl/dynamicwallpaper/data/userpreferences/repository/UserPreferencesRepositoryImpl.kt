package com.andyl.dynamicwallpaper.data.userpreferences.repository

import android.content.Context
import com.andyl.dynamicwallpaper.data.userpreferences.dto.WallpaperRuleDto
import com.andyl.dynamicwallpaper.domain.mapper.toKey
import com.andyl.dynamicwallpaper.domain.mapper.weatherFromKey
import com.andyl.dynamicwallpaper.domain.model.GeoLocation
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.WallpaperRule
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import com.andyl.dynamicwallpaper.data.userpreferences.dto.WallpaperPackDto
import com.andyl.dynamicwallpaper.data.userpreferences.dto.toDomain
import com.andyl.dynamicwallpaper.domain.model.PackInfo

class UserPreferencesRepositoryImpl(
    context: Context
) : UserPreferencesRepository {

    private val prefs = context.getSharedPreferences(
        "user_preferences",
        Context.MODE_PRIVATE
    )

    private val json = Json { encodeDefaults = true }

    override suspend fun getWallpaperConfig(packId: String?): WallpaperConfig {
        val targetId = packId ?: getActivePackId()
        val rawPack = prefs.getString("${KEY_PACK_DATA}_$targetId", null)

        return if (rawPack != null) {
            // Caso 1: Ya existe el formato nuevo
            val dto = json.decodeFromString<WallpaperPackDto>(rawPack)
            dto.toDomain(targetId)
        } else {
            // Caso 2: Migración o Pack Nuevo
            val legacyRules = loadLegacyRules(targetId)
            val rules = legacyRules.ifEmpty { defaultRules() }

            WallpaperConfig(
                id = targetId,
                name = "Pack $targetId",
                rules = rules,
                activePackId = getActivePackId()
            )
        }
    }

    override suspend fun setWallpaperConfig(config: WallpaperConfig) {
        val dto = WallpaperPackDto(
            id = config.id,
            name = config.name,
            updateIntervalMinutes = config.updateIntervalMinutes,
            weatherRules = config.rules.map {
                WallpaperRuleDto(
                    weather = it.weather.toKey(),
                    timeOfDay = it.timeOfDay.name,
                    uri = it.wallpaperId.value
                )
            },
            dailyRules = config.dailyRules,
            fixedTimeRules = config.fixedTimeRules,
            enabledWeathers = config.enabledWeathers.map { it.toKey() }
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

    private fun saveRules(rules: List<WallpaperRule>, packId: String) {
        val serialized = json.encodeToString(
            rules.map {
                WallpaperRuleDto(
                    weather = it.weather.toKey(),
                    timeOfDay = it.timeOfDay.name,
                    uri = it.wallpaperId.value
                )
            }
        )
        prefs.edit { putString("${KEY_RULES}_$packId", serialized) }
    }

    private fun loadRules(packId: String): List<WallpaperRule> {
        val raw = prefs.getString("${KEY_RULES}_$packId", null) ?: return emptyList()
        return json.decodeFromString<List<WallpaperRuleDto>>(raw).map {
            WallpaperRule(
                weather = weatherFromKey(it.weather),
                timeOfDay = TimeOfDay.valueOf(it.timeOfDay),
                wallpaperId = WallpaperId(it.uri)
            )
        }
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

    override suspend fun getAllPacks(): List<PackInfo> {
        val activeId = getActivePackId()

        val allEntries = prefs.all

        return allEntries.filter { (key, _) ->
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
    }


    companion object {
        private const val KEY_PACK_DATA = "wallpaper_pack_data"
        private const val KEY_RULES = "wallpaper_rules"
        private const val KEY_ACTIVE_PACK_ID = "active_pack_id"
        private const val KEY_LAST_LAT = "last_latitude"
        private const val KEY_LAST_LON = "last_longitude"
        private const val KEY_CITY_NAME = "last_city_name"
    }
}
