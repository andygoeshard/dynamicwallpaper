package com.andyl.iris.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    @SerialName("current_weather")
    val currentWeather: CurrentWeatherDto? = null,
    val daily: DailyWeatherDto? = null
)

@Serializable
data class DailyWeatherDto(
    val time: List<String>? = null,
    @SerialName("weather_code")
    val weatherCode: List<Int>? = null,
    @SerialName("temperature_2m_max")
    val temperatureMax: List<Double>? = null,
    @SerialName("temperature_2m_min")
    val temperatureMin: List<Double>? = null,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null
)
