package com.andyl.dynamicwallpaper.domain.repository

import com.andyl.dynamicwallpaper.domain.model.GeoLocation
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig


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
}

