package com.andyl.iris.data.weather.api

import com.andyl.iris.data.weather.dto.WeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class WeatherApi(
    private val client: HttpClient
) {
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponseDto {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current_weather", true)
        }.body()
    }
}
