package com.andyl.iris.domain.mapper

import com.andyl.iris.data.weather.dto.CurrentWeatherDto
import com.andyl.iris.domain.model.Weather

fun CurrentWeatherDto.toDomain(): Weather =
    when (weatherCode) {
        0 -> Weather.Clear
        1, 2, 3 -> Weather.Cloudy
        45, 48 -> Weather.Fog
        51, 53, 55, 61, 63, 65 -> Weather.Rain
        71, 73, 75 -> Weather.Snow
        95, 96, 99 -> Weather.Storm
        else -> Weather.Cloudy
    }

fun Weather.toKey(): String = when (this) {
    Weather.Clear -> "CLEAR"
    Weather.Cloudy -> "CLOUDY"
    Weather.Rain -> "RAIN"
    Weather.Snow -> "SNOW"
    Weather.Fog -> "FOG"
    Weather.Storm -> "STORM"
}

fun weatherFromKey(key: String): Weather = when (key) {
    "CLEAR" -> Weather.Clear
    "CLOUDY" -> Weather.Cloudy
    "RAIN" -> Weather.Rain
    "SNOW" -> Weather.Snow
    "FOG" -> Weather.Fog
    "STORM" -> Weather.Storm
    else -> Weather.Clear // fallback seguro
}
