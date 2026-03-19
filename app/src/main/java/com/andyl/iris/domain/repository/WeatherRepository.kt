package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.model.Weather

interface WeatherRepository {
    suspend fun getCurrentWeather(location: GeoLocation): Weather
}
