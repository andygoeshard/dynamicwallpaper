package com.andyl.iris.data.database.repository

import com.andyl.iris.data.database.dao.FavoriteDao
import com.andyl.iris.data.database.entity.FavoriteImage
import com.andyl.iris.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class FavoriteRepositoryImpl(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {
    override fun getAllFavorites(): Flow<List<FavoriteImage>> = favoriteDao.getAllFavorites()
    
    override suspend fun addFavorite(image: FavoriteImage) {
        favoriteDao.insertFavorite(image)
    }

    override suspend fun removeFavorite(uri: String) {
        favoriteDao.deleteFavorite(FavoriteImage(uri = uri, source = ""))
    }

    override fun isFavorite(uri: String): Flow<Boolean> = favoriteDao.isFavorite(uri)
}
