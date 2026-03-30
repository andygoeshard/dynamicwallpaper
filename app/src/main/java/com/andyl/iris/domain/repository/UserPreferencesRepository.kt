package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.WallpaperConfig


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
}

