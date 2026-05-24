package com.andyl.iris.data.location.datasource

import android.util.Log
import com.andyl.iris.data.location.dto.NominatimResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse

class NominatimRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun searchCity(query: String): List<NominatimResponse> {
        return try {
            Log.d("IRIS_LOCATION", "Searching city: '$query'")
            val response: HttpResponse = httpClient.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("limit", 5)
                header("User-Agent", "IrisDynamicWallpaper/1.0 (contact: andy.l@example.com)")
            }

            if (response.status.value in 200..299) {
                val results = response.body<List<NominatimResponse>>()
                Log.d("IRIS_LOCATION", "✅ Found ${results.size} cities")
                results
            } else {
                Log.e("IRIS_LOCATION", "❌ Nominatim Error: ${response.status.value}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("IRIS_LOCATION", "❌ Location search failed", e)
            emptyList()
        }
    }
}
