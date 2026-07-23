package com.andyl.iris.data.weather.repository

import android.util.Log
import com.andyl.iris.data.weather.api.WeatherApi
import com.andyl.iris.domain.mapper.toDomain
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.WeatherInfo
import com.andyl.iris.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getCurrentWeather(
        location: GeoLocation
    ): WeatherInfo {
        return try {
            Log.d("IRIS_WEATHER", "Fetching weather for Lat: ${location.latitude}, Lon: ${location.longitude}")
            val response = api.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude
            )

            val current = response.currentWeather
            if (current == null) {
                Log.w("IRIS_WEATHER", "Weather response empty, defaulting to Cloudy")
                return WeatherInfo(Weather.Cloudy)
            }

            val domainWeather = current.toDomain()
            val sunrise = response.daily?.sunrise?.firstOrNull()
            val sunset = response.daily?.sunset?.firstOrNull()

            Log.d("IRIS_WEATHER", "✅ Weather fetched: ${domainWeather.javaClass.simpleName}, Sunrise: $sunrise, Sunset: $sunset, Temp: ${current.temperature}")
            
            WeatherInfo(
                weather = domainWeather,
                sunrise = sunrise,
                sunset = sunset,
                temperature = current.temperature
            )
        } catch (e: Exception) {
            Log.e("IRIS_WEATHER", "❌ Failed to fetch weather", e)
            WeatherInfo(Weather.Cloudy)
        }
    }
}
