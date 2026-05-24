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

    // Persistent cache to avoid redundant API calls across the entire app session
    private val globalCache = mutableMapOf<String, List<UnsplashImage>>()

    suspend fun searchPhotos(query: String, page: Int = 1): Result<UnsplashResponse> = runCatching {
        val cacheKey = "search_$query"
        if (globalCache.containsKey(cacheKey)) {
            Log.d("IRIS_API", "📦 Using cached results for search: $query")
            return@runCatching UnsplashResponse(globalCache[cacheKey]!!)
        }

        val response: HttpResponse = client.get("$BASE_URL/search/photos") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("page", page)
            parameter("per_page", 30)
            parameter("orientation", "portrait")
        }
        
        logResponse("SearchPhotos", response)
        
        if (response.status.value in 200..299) {
            val body = response.body<UnsplashResponse>()
            globalCache[cacheKey] = body.results
            body
        } else {
            throw Exception("API Error ${response.status.value}")
        }
    }

    suspend fun getRandomPhotos(query: String, count: Int = 30): Result<List<UnsplashImage>> = runCatching {
        val cacheKey = "random_$query"
        if (globalCache.containsKey(cacheKey) && globalCache[cacheKey]!!.size >= count) {
            Log.d("IRIS_API", "📦 Using cached results for random: $query")
            return@runCatching globalCache[cacheKey]!!.take(count)
        }

        val response: HttpResponse = client.get("$BASE_URL/photos/random") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("count", count)
            parameter("orientation", "portrait")
        }

        logResponse("RandomPhotos", response)

        if (response.status.value in 200..299) {
            val body = response.body<List<UnsplashImage>>()
            globalCache[cacheKey] = body
            body
        } else {
            throw Exception("API Error ${response.status.value}")
        }
    }

    private fun logResponse(tag: String, response: HttpResponse) {
        val remaining = response.headers["X-Ratelimit-Remaining"]
        val limit = response.headers["X-Ratelimit-Limit"]
        Log.d("IRIS_API_STATS", "[$tag] Status: ${response.status.value} | RateLimit: $remaining/$limit")
    }
}
