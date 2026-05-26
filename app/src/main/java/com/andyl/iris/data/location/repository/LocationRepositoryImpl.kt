package com.andyl.iris.data.location.repository

import android.util.Log
import com.andyl.iris.data.location.datasource.AndroidLocationDataSource
import com.andyl.iris.data.location.datasource.NominatimRemoteDataSource
import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import java.util.concurrent.TimeUnit

class LocationRepositoryImpl(
    private val dataSource: AndroidLocationDataSource,
    private val remoteDataSource: NominatimRemoteDataSource,
    private val preferencesRepository: UserPreferencesRepository
) : LocationRepository {

    private var cachedGpsLocation: GeoLocation? = null
    private var lastGpsFetchTime: Long = 0
    private val GPS_CACHE_DURATION = TimeUnit.MINUTES.toMillis(60) // Fetch GPS at most once per hour

    override suspend fun getCurrentLocation(): GeoLocation {
        // 1. If user chose a manual city, use it immediately (Impact 0 on GPS/Battery)
        if (!preferencesRepository.shouldUseGps()) {
            val manualLocation = preferencesRepository.getLastLocation()
            if (manualLocation != null) {
                Log.d("LocationRepo", "Using manual location: ${manualLocation.latitude}")
                return manualLocation
            }
        }

        // 2. If using GPS, use cache if fresh enough to save battery
        val now = System.currentTimeMillis()
        if (cachedGpsLocation != null && (now - lastGpsFetchTime) < GPS_CACHE_DURATION) {
            Log.d("LocationRepo", "Using cached GPS location (Battery Saving)")
            return cachedGpsLocation!!
        }

        // 3. Fetch fresh GPS location
        Log.d("LocationRepo", "Fetching fresh GPS location...")
        val freshLocation = try {
            dataSource.getLastKnownLocation()
        } catch (e: Exception) {
            Log.e("LocationRepo", "GPS failed, using fallback from prefs", e)
            preferencesRepository.getLastLocation() ?: GeoLocation(-34.6037, -58.3816)
        }

        cachedGpsLocation = freshLocation
        lastGpsFetchTime = now
        
        // Update last location in prefs as well
        preferencesRepository.saveLastLocation(freshLocation.latitude, freshLocation.longitude)
        
        return freshLocation
    }

    override suspend fun searchCity(query: String): List<CityResult> {
        return remoteDataSource.searchCity(query).map {
            CityResult(it.displayName, it.lat.toDouble(), it.lon.toDouble())
        }
    }

    override suspend fun saveSelectedCity(city: CityResult) {
        Log.d("LocationRepo", "Guardando ciudad seleccionada: ${city.name}")
        preferencesRepository.setUseGps(false)
        preferencesRepository.saveLastLocation(city.lat, city.lon)
        preferencesRepository.saveCityName(city.name)
        // Invalidate GPS cache
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
