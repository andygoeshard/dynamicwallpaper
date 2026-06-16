package com.andyl.iris.domain.repository

import com.andyl.iris.data.database.entity.FavoriteImage
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<FavoriteImage>>
    suspend fun addFavorite(image: FavoriteImage)
    suspend fun removeFavorite(uri: String)
    fun isFavorite(uri: String): Flow<Boolean>
}
