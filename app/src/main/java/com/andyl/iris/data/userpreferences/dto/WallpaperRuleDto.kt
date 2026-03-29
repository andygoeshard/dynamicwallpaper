package com.andyl.iris.data.userpreferences.dto

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperRuleDto(
    val weather: String,
    val timeOfDay: String,
    val uri: String,
    val target: Int = 3
)

