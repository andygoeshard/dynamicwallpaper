package com.andyl.iris.data.imagesprovider.datasource

import com.andyl.iris.data.imagesprovider.dto.UnsplashResponse
import com.andyl.iris.data.imagesprovider.dto.UnsplashImage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import android.util.Log

class UnsplashRemoteDataSource(private val client: HttpClient) {

    private val ACCESS_KEY = "HGwYWODpC40PFOh4hfaTEDEPmYjEHbyD4JCkaU4px6o"
    private val BASE_URL = "https://api.unsplash.com"

    suspend fun searchPhotos(query: String, page: Int = 1): Result<UnsplashResponse> = runCatching {
        val response: HttpResponse = client.get("$BASE_URL/search/photos") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("page", page)
            parameter("per_page", 30)
            parameter("orientation", "portrait")
        }
        
        logResponse("SearchPhotos", response)
        
        if (response.status.value in 200..299) {
            response.body<UnsplashResponse>()
        } else {
            val errorBody = response.body<String>()
            throw Exception("API Error ${response.status.value}: $errorBody")
        }
    }.onFailure {
        Log.e("IRIS_API", "Search error for query '$query'", it)
    }

    suspend fun getRandomPhotos(query: String, count: Int = 30): Result<List<UnsplashImage>> = runCatching {
        val response: HttpResponse = client.get("$BASE_URL/photos/random") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("count", count)
            parameter("orientation", "portrait")
        }

        logResponse("RandomPhotos", response)

        if (response.status.value in 200..299) {
            response.body<List<UnsplashImage>>()
        } else {
            val errorBody = response.body<String>()
            throw Exception("API Error ${response.status.value}: $errorBody")
        }
    }.onFailure {
        Log.e("IRIS_API", "Random error for query '$query'", it)
    }

    private fun logResponse(tag: String, response: HttpResponse) {
        val remaining = response.headers["X-Ratelimit-Remaining"]
        val limit = response.headers["X-Ratelimit-Limit"]
        Log.d("IRIS_API_STATS", "[$tag] Status: ${response.status.value} | RateLimit: $remaining/$limit")
        
        if (response.status.value == 403) {
            Log.e("IRIS_API_STATS", "❌ RATE LIMIT EXCEEDED! Unsplash only allows 50 requests per hour on free tier.")
        } else if (response.status.value == 401) {
            Log.e("IRIS_API_STATS", "❌ UNAUTHORIZED! Check your Access Key.")
        }
    }
}
