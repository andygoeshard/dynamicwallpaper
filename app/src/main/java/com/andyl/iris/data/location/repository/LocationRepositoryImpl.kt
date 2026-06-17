package com.andyl.iris.data.location.repository

import android.util.Log
import com.andyl.iris.data.location.datasource.AndroidLocationDataSource
import com.andyl.iris.data.location.datasource.GeocodingRemoteDataSource
import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import java.util.concurrent.TimeUnit

class LocationRepositoryImpl(
    private val dataSource: AndroidLocationDataSource,
    private val remoteDataSource: GeocodingRemoteDataSource,
    private val preferencesRepository: UserPreferencesRepository
) : LocationRepository {

    private var cachedGpsLocation: GeoLocation? = null
    private var lastGpsFetchTime: Long = 0
    private val GPS_CACHE_DURATION = TimeUnit.MINUTES.toMillis(60)

    override suspend fun getCurrentLocation(): GeoLocation {
        if (!preferencesRepository.shouldUseGps()) {
            val manualLocation = preferencesRepository.getLastLocation()
            if (manualLocation != null) return manualLocation
        }

        val now = System.currentTimeMillis()
        if (cachedGpsLocation != null && (now - lastGpsFetchTime) < GPS_CACHE_DURATION) {
            return cachedGpsLocation!!
        }

        val freshLocation = try {
            kotlinx.coroutines.withTimeout(5000) {
                dataSource.getLastKnownLocation()
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "Location fetch timeout or error, using last known", e)
            preferencesRepository.getLastLocation() ?: GeoLocation(-34.6037, -58.3816)
        }

        cachedGpsLocation = freshLocation
        lastGpsFetchTime = now
        preferencesRepository.saveLastLocation(freshLocation.latitude, freshLocation.longitude)
        
        return freshLocation
    }

    override suspend fun searchCity(query: String): List<CityResult> {
        return remoteDataSource.searchCity(query)?.results?.map {
            val fullName = listOfNotNull(it.name, it.admin1, it.country).joinToString(", ")
            CityResult(fullName, it.latitude, it.longitude)
        } ?: emptyList()
    }

    override suspend fun saveSelectedCity(city: CityResult) {
        preferencesRepository.setUseGps(false)
        preferencesRepository.saveLastLocation(city.lat, city.lon)
        preferencesRepository.saveCityName(city.name)
        cachedGpsLocation = null
    }

    override suspend fun getSavedCityName(): String? {
        return preferencesRepository.getCityName()
    }

    override suspend fun setUseGps(enabled: Boolean) {
        preferencesRepository.setUseGps(enabled)
        if (enabled) cachedGpsLocation = null
    }

    override suspend fun shouldUseGps(): Boolean {
        return preferencesRepository.shouldUseGps()
    }
}
