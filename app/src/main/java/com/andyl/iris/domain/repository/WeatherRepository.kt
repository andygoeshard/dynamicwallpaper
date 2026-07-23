package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.Weather

data class WeatherInfo(
    val weather: Weather,
    val sunrise: String? = null,
    val sunset: String? = null,
    val temperature: Double? = null
)

interface WeatherRepository {
    suspend fun getCurrentWeather(location: GeoLocation): WeatherInfo
}
