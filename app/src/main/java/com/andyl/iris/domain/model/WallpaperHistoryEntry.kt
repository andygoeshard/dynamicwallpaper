package com.andyl.iris.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperHistoryEntry(
    val uri: String,
    val weatherKey: String?,
    val timestamp: Long
)
