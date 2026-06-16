package com.andyl.iris.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andyl.iris.data.database.dao.FavoriteDao
import com.andyl.iris.data.database.dao.ImageCacheDao
import com.andyl.iris.data.database.entity.CachedImage
import com.andyl.iris.data.database.entity.FavoriteImage

@Database(entities = [CachedImage::class, FavoriteImage::class], version = 3, exportSchema = false)
abstract class IrisDatabase : RoomDatabase() {
    abstract fun imageCacheDao(): ImageCacheDao
    abstract fun favoriteDao(): FavoriteDao
}
