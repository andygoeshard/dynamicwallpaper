package com.andyl.iris.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andyl.iris.data.database.dao.ImageCacheDao
import com.andyl.iris.data.database.entity.CachedImage

@Database(entities = [CachedImage::class], version = 2, exportSchema = false)
abstract class IrisDatabase : RoomDatabase() {
    abstract fun imageCacheDao(): ImageCacheDao
}
