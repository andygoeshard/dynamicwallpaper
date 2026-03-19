package com.andyl.iris.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherDto(
    @SerialName("weathercode")
    val weatherCode: Int
)
