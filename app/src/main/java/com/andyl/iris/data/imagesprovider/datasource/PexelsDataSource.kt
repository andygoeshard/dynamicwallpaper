package com.andyl.iris.data.imagesprovider.datasource

import com.andyl.iris.data.imagesprovider.dto.PexelsResponse
import com.andyl.iris.data.imagesprovider.dto.PexelsPhoto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import android.util.Log

class PexelsRemoteDataSource(private val client: HttpClient) {

    private val ACCESS_KEY = "ZRCMOMjMmu7CwGOApCse2ADHB1xpNhNjzCFbZXqUiy52Z0knB2WX2lO7"
    private val BASE_URL = "https://api.pexels.com/v1"

    suspend fun searchPhotos(query: String, perPage: Int = 30): Result<PexelsResponse> = runCatching {
        val response: HttpResponse = client.get("$BASE_URL/search") {
            header("Authorization", ACCESS_KEY)
            parameter("query", query)
            parameter("per_page", perPage)
            parameter("orientation", "portrait")
        }

        if (response.status.value in 200..299) {
            response.body<PexelsResponse>()
        } else {
            val errorBody = response.body<String>()
            throw Exception("Pexels API Error ${response.status.value}: $errorBody")
        }
    }.onFailure {
        Log.e("PEXELS_API", "Search error for query '$query'", it)
    }

    suspend fun getCuratedPhotos(perPage: Int = 30): Result<List<PexelsPhoto>> = runCatching {
        val response: HttpResponse = client.get("$BASE_URL/curated") {
            header("Authorization", ACCESS_KEY)
            parameter("per_page", perPage)
        }

        if (response.status.value in 200..299) {
            response.body<PexelsResponse>().photos
        } else {
            throw Exception("Pexels API Error ${response.status.value}")
        }
    }.onFailure {
        Log.e("PEXELS_API", "Curated photos error", it)
    }
}
