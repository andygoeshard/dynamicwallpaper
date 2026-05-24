package com.andyl.iris.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_images")
data class CachedImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val provider: String,
    val imageUrlSmall: String,
    val imageUrlFull: String,
    val imageId: String,
    val description: String? = null, // Metadatos para búsqueda semántica local
    val timestamp: Long = System.currentTimeMillis()
)
