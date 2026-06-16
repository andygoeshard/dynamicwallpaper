package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.ImageResult

interface LocalImageRepository {
    suspend fun getLocalImages(): List<ImageResult>
}
