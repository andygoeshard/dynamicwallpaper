package com.andyl.iris.data.location.repository

import android.util.Log
import com.andyl.iris.data.location.datasource.AndroidLocationDataSource
import com.andyl.iris.data.location.datasource.NominatimRemoteDataSource
import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository

class LocationRepositoryImpl(
    private val dataSource: AndroidLocationDataSource,
    private val remoteDataSource: NominatimRemoteDataSource,
    private val preferencesRepository: UserPreferencesRepository
) : LocationRepository {

    override suspend fun getCurrentLocation(): GeoLocation {
        val saved = preferencesRepository.getLastLocation()
        if (saved != null) return saved

        return dataSource.getLastKnownLocation()
    }

    override suspend fun searchCity(query: String): List<CityResult> {
        return remoteDataSource.searchCity(query).map {
            CityResult(it.displayName, it.lat.toDouble(), it.lon.toDouble())
        }
    }

    override suspend fun saveSelectedCity(city: CityResult) {
        Log.d("LocationRepo", "Guardando ciudad seleccionada: ${city.name}")
        preferencesRepository.saveLastLocation(city.lat, city.lon)
        preferencesRepository.saveCityName(city.name)
    }

    override suspend fun getSavedCityName(): String? {
        return preferencesRepository.getCityName()
    }
}