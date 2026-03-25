package com.andyl.iris.data.weather.repository

import com.andyl.iris.data.weather.api.WeatherApi
import com.andyl.iris.domain.mapper.toDomain
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.WeatherRepository
class WeatherRepositoryImpl(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getCurrentWeather(
        location: GeoLocation
    ): Weather {
        val response = api.getCurrentWeather(
            latitude = location.latitude,
            longitude = location.longitude
        )

        val current = response.currentWeather
            ?: return Weather.Cloudy

        return current.toDomain()
    }
}
