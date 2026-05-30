package com.andyl.iris.data.weather.api

import android.util.Log
import com.andyl.iris.data.weather.dto.WeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

class WeatherApi(
    private val client: HttpClient
) {
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherResponseDto {
        return try {
            val response: HttpResponse = client.get("https://api.open-meteo.com/v1/forecast") {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                parameter("current_weather", true)
                parameter("daily", "sunrise,sunset")
                parameter("timezone", "auto")
            }
            
            if (response.status.value in 200..299) {
                response.body()
            } else {
                val error = response.body<String>()
                Log.e("IRIS_WEATHER_API", "Error ${response.status.value}: $error")
                WeatherResponseDto()
            }
        } catch (e: Exception) {
            Log.e("IRIS_WEATHER_API", "Failed to fetch weather", e)
            WeatherResponseDto()
        }
    }
}
