package com.andyl.dynamicwallpaper.domain.repository

import com.andyl.dynamicwallpaper.domain.model.GeoLocation
import com.andyl.dynamicwallpaper.domain.model.Weather

interface WeatherRepository {
    suspend fun getCurrentWeather(location: GeoLocation): Weather
}
