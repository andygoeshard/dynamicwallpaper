package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.GeoLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): GeoLocation
    suspend fun searchCity(query: String): List<CityResult>
    suspend fun saveSelectedCity(city: CityResult)
    suspend fun getSavedCityName(): String?
}
