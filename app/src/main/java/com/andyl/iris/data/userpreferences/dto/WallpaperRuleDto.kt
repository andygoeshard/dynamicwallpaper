package com.andyl.iris.data.userpreferences.dto

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperRuleDto(
    val weather: String,
    val timeOfDay: String,
    val uri: String,
    val target: Int = 3,
    val scaleMode: String? = null,
    val cropX: Float? = null,
    val cropY: Float? = null,
    val cropScale: Float? = null
)

