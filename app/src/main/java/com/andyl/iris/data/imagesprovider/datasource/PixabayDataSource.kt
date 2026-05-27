package com.andyl.iris.data.imagesprovider.datasource

import com.andyl.iris.data.imagesprovider.dto.PixabayResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import android.util.Log

class PixabayRemoteDataSource(private val client: HttpClient) {

    private val API_KEY = "56042368-9ac229f439c251985951a5fb3"
    private val BASE_URL = "https://pixabay.com/api"

    suspend fun searchPhotos(query: String, perPage: Int = 30): Result<PixabayResponse> = runCatching {
        val response: HttpResponse = client.get(BASE_URL) {
            parameter("key", API_KEY)
            parameter("q", query)
            parameter("per_page", perPage)
            parameter("orientation", "vertical")
            parameter("image_type", "photo")
            parameter("safesearch", "true")
        }

        if (response.status.value in 200..299) {
            response.body<PixabayResponse>()
        } else {
            val errorBody = response.body<String>()
            throw Exception("Pixabay API Error ${response.status.value}: $errorBody")
        }
    }.onFailure {
        Log.e("PIXABAY_API", "Search error for query '$query'", it)
    }
}
