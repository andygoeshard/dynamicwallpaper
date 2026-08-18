package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperHistoryEntry
import com.andyl.iris.domain.model.Weather

interface UserPreferencesRepository {
    suspend fun getWallpaperConfig(packId: String? = null): WallpaperConfig
    suspend fun setWallpaperConfig(config: WallpaperConfig)
    suspend fun saveLastLocation(lat: Double, lon: Double)
    suspend fun getLastLocation(): GeoLocation?
    suspend fun saveManualLocation(location: GeoLocation, cityName: String)
    suspend fun getSavedCityName(): String?
    suspend fun saveCityName(name: String)
    suspend fun getCityName(): String?
    suspend fun getActivePackId(): String
    suspend fun setActivePackId(packId: String)
    suspend fun getAllPacks(): List<PackInfo>
    suspend fun addNewPack(): List<PackInfo>
    suspend fun deletePack(packId: String)
    suspend fun isFirstApplyGlobal(): Boolean
    suspend fun setGlobalFirstApplyDone()
    suspend fun setUseGps(enabled: Boolean)
    suspend fun shouldUseGps(): Boolean
    
    suspend fun saveLastWeather(weather: Weather)
    suspend fun getLastWeather(): Weather?
    suspend fun saveLastUpdateTime(time: Long)
    suspend fun getLastUpdateTime(): Long

    suspend fun incrementAppSuccessCount()
    suspend fun getAppSuccessCount(): Int
    suspend fun setRated(rated: Boolean)
    suspend fun hasRated(): Boolean

    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted()

    suspend fun hasBackgroundLocationDisclosureAcknowledged(): Boolean
    suspend fun setBackgroundLocationDisclosureAcknowledged()

    suspend fun refreshFeaturedPacks()
    suspend fun getFeaturedPackId(type: String): String?

    suspend fun getDarkModePreference(): Boolean?
    suspend fun setDarkModePref(pref: Boolean?)

    suspend fun getAccentColor(): String?
    suspend fun setAccentColor(color: String?)

    suspend fun getAmoledMode(): Boolean
    suspend fun setAmoledMode(enabled: Boolean)

    suspend fun getReduceAnimations(): Boolean
    suspend fun setReduceAnimations(enabled: Boolean)

    suspend fun getHapticsEnabled(): Boolean
    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun getSoundEnabled(): Boolean
    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun getUseWallpaperBackground(): Boolean
    suspend fun setUseWallpaperBackground(enabled: Boolean)
    suspend fun getLastAppliedWallpaper(): String?
    suspend fun saveLastAppliedWallpaper(uri: String?)
    suspend fun getBatterySaverEnabled(): Boolean
    suspend fun setBatterySaverEnabled(enabled: Boolean)

    suspend fun getUpdateIntervalMinutes(): Int
    suspend fun setUpdateIntervalMinutes(minutes: Int)

    suspend fun recordChange(weather: Weather?)
    suspend fun getChangesHistory(): Map<String, Int>
    suspend fun getWeatherChanges(): Map<String, Int>
    suspend fun recordPackChange(packId: String)
    suspend fun getPackChanges(): Map<String, Int>

    suspend fun getWallpaperHistory(): List<WallpaperHistoryEntry>
    suspend fun addWallpaperHistoryEntry(uri: String, weather: Weather?, timestamp: Long)
    suspend fun clearWallpaperHistory()

    suspend fun getWallpaperOverlayText(): String?
    suspend fun setWallpaperOverlayText(text: String?)
    suspend fun getOverlayTextEnabled(): Boolean
    suspend fun setOverlayTextEnabled(enabled: Boolean)

    suspend fun getPlaces(): List<com.andyl.iris.domain.model.GeoPlace>
    suspend fun setPlaces(places: List<com.andyl.iris.domain.model.GeoPlace>)

    suspend fun getRandomGalleryBucketId(): String?
    suspend fun setRandomGalleryBucketId(bucketId: String?)

    suspend fun getLiveVideoPath(): String?
    suspend fun setLiveVideoPath(path: String?)
    suspend fun getLiveVideoEnabled(): Boolean
    suspend fun setLiveVideoEnabled(enabled: Boolean)
    suspend fun setActiveVideoPackId(packId: String?)

    suspend fun getHomeSectionsOrder(): List<String>
    suspend fun setHomeSectionsOrder(order: List<String>)
}
