package com.andyl.iris.data.location.datasource

import com.andyl.iris.data.location.dto.NominatimResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class NominatimRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun searchCity(query: String): List<NominatimResponse> {
        return httpClient.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", 5)
            // Nominatim requiere identificarse
            header("User-Agent", "DynamicWallpaperApp/1.0")
        }.body()
    }
}
