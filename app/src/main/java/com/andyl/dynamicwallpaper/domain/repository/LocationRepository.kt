package com.andyl.dynamicwallpaper.domain.repository

import com.andyl.dynamicwallpaper.domain.model.CityResult
import com.andyl.dynamicwallpaper.domain.model.GeoLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): GeoLocation
    suspend fun searchCity(query: String): List<CityResult>
    suspend fun saveSelectedCity(city: CityResult)
    suspend fun getSavedCityName(): String?
}
