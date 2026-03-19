package com.andyl.iris.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    @SerialName("current_weather")
    val currentWeather: CurrentWeatherDto? = null
)