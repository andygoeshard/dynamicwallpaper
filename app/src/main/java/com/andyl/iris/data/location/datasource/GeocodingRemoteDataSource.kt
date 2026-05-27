package com.andyl.iris.data.location.datasource

import android.util.Log
import com.andyl.iris.data.location.dto.GeocodingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

class GeocodingRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun searchCity(query: String): GeocodingResponse? {
        if (query.length < 3) return null
        
        return try {
            Log.d("IRIS_GEO", "Searching city: '$query'")
            val response: HttpResponse = httpClient.get("https://geocoding-api.open-meteo.com/v1/search") {
                parameter("name", query)
                parameter("count", 10)
                parameter("language", "es")
                parameter("format", "json")
            }

            if (response.status.value in 200..299) {
                response.body<GeocodingResponse>()
            } else {
                Log.e("IRIS_GEO", "Geocoding Error: ${response.status.value}")
                null
            }
        } catch (e: Exception) {
            Log.e("IRIS_GEO", "Geocoding failed", e)
            null
        }
    }
}
