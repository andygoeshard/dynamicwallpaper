 package com.andyl.iris.domain.mapper

import com.andyl.iris.data.weather.dto.CurrentWeatherDto
import com.andyl.iris.domain.model.Weather

fun CurrentWeatherDto.toDomain(): Weather =
    when (weatherCode) {
        0, 1 -> Weather.Clear // 0: Clear sky, 1: Mainly clear
        2, 3 -> Weather.Cloudy // 2: Partly cloudy, 3: Overcast
        45, 48 -> Weather.Fog // Fog and depositing rime fog
        51, 53, 55, 56, 57 -> Weather.Rain // Drizzle
        61, 63, 65, 66, 67 -> Weather.Rain // Rain
        71, 73, 75, 77, 80, 81, 82, 85, 86 -> Weather.Snow // Snow and Rain showers
        95, 96, 99 -> Weather.Storm // Thunderstorm
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
