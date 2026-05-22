package com.andyl.iris.data.imagesprovider.datasource

import com.andyl.iris.data.imagesprovider.dto.UnsplashResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class UnsplashRemoteDataSource(private val client: HttpClient) {

    private val ACCESS_KEY = "HGwYWODpC40PFOh4hfaTEDEPmYjEHbyD4JCkaU4px6o"
    private val BASE_URL = "https://api.unsplash.com"

    suspend fun searchPhotos(query: String, page: Int = 1): Result<UnsplashResponse> = runCatching {
        client.get("$BASE_URL/search/photos") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("page", page)
            parameter("per_page", 30)
            parameter("orientation", "portrait") // Solo fotos verticales para wallpapers
        }.body()
    }

    suspend fun getRandomPhotos(query: String, count: Int = 30): Result<List<com.andyl.iris.data.imagesprovider.dto.UnsplashImage>> = runCatching {
        client.get("$BASE_URL/photos/random") {
            header("Authorization", "Client-ID $ACCESS_KEY")
            parameter("query", query)
            parameter("count", count)
            parameter("orientation", "portrait")
        }.body()
    }
}
