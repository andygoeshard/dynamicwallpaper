package com.andyl.iris.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.andyl.iris.data.database.entity.CachedImage

@Dao
interface ImageCacheDao {
    @Query("SELECT * FROM cached_images WHERE `query` = :searchQuery")
    suspend fun getImagesByQuery(searchQuery: String): List<CachedImage>

    @Query("""
        SELECT * FROM cached_images 
        WHERE `query` LIKE '%' || :searchQuery || '%' 
        OR description LIKE '%' || :searchQuery || '%'
        GROUP BY imageId 
        ORDER BY timestamp DESC 
        LIMIT 60
    """)
    suspend fun predictImages(searchQuery: String): List<CachedImage>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImages(images: List<CachedImage>)

    @Query("DELETE FROM cached_images WHERE timestamp < :expiryTime")
    suspend fun clearOldCache(expiryTime: Long)
}
