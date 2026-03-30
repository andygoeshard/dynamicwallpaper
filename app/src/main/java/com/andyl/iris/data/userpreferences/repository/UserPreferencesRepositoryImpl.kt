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
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.UserPreferencesRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import com.andyl.iris.data.userpreferences.dto.WallpaperPackDto
import com.andyl.iris.data.userpreferences.dto.toDomain
import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.ScaleMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(
    context: Context,
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
                scaleMode = ScaleMode.FIT
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
                    uri = it.wallpaperId.value
                )
            },
            dailyRules = config.dailyRules,
            fixedTimeRules = config.fixedTimeRules,
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

        if (allPacks.size >= 10) {
            throw IllegalStateException("Límite de 10 paquetes alcanzado")
        }

        val newId = System.currentTimeMillis().toString()
        val newName = "Pack ${allPacks.size + 1}"

        val newConfig = WallpaperConfig(
            id = newId,
            name = newName,
            rules = defaultRules(),
            activePackId = getActivePackId(),
            scaleMode = ScaleMode.FIT
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


    companion object {
        private const val KEY_PACK_DATA = "wallpaper_pack_data"
        private const val KEY_RULES = "wallpaper_rules"
        private const val KEY_ACTIVE_PACK_ID = "active_pack_id"
        private const val KEY_LAST_LAT = "last_latitude"
        private const val KEY_LAST_LON = "last_longitude"
        private const val KEY_CITY_NAME = "last_city_name"
        private const val KEY_GLOBAL_FIRST_APPLY = "global_first_apply_done"
    }
}

