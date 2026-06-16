package com.andyl.iris.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_images")
data class FavoriteImage(
    @PrimaryKey val uri: String,
    val source: String, // "local", "unsplash", "pixabay", etc.
    val addedDate: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)
