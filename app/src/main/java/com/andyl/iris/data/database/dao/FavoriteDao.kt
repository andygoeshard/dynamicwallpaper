package com.andyl.iris.data.database.dao

import androidx.room.*
import com.andyl.iris.data.database.entity.FavoriteImage
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_images ORDER BY addedDate DESC")
    fun getAllFavorites(): Flow<List<FavoriteImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(image: FavoriteImage)

    @Delete
    suspend fun deleteFavorite(image: FavoriteImage)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_images WHERE uri = :uri)")
    fun isFavorite(uri: String): Flow<Boolean>
}
