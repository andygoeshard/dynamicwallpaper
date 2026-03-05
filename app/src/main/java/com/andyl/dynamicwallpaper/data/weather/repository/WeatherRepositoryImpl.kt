package com.andyl.dynamicwallpaper.data.weather.repository

import com.andyl.dynamicwallpaper.data.weather.api.WeatherApi
import com.andyl.dynamicwallpaper.domain.mapper.toDomain
import com.andyl.dynamicwallpaper.domain.model.GeoLocation
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.repository.WeatherRepository


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
            ?: return Weather.Cloudy // fallback visual razonable

        return current.toDomain()
    }
}
