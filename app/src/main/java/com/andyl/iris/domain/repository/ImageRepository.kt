package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.ImageResult

interface ImageRepository {
    suspend fun searchImages(query: String, forceRefresh: Boolean = false): Result<List<ImageResult>>
    suspend fun getRandomImages(query: String, count: Int = 30): Result<List<ImageResult>>
}
