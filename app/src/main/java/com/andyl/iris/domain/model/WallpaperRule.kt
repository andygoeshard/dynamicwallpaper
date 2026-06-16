package com.andyl.iris.domain.model

data class WallpaperRule(
    val weather: Weather,
    val timeOfDay: TimeOfDay,
    val wallpaperId: WallpaperId,
    val scaleMode: ScaleMode = ScaleMode.CROP,
    val target: Int = 3,
    val cropX: Float? = null,
    val cropY: Float? = null,
    val cropScale: Float? = null
)
