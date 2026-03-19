package com.andyl.iris.domain.model

data class WallpaperRule(
    val weather: Weather,
    val timeOfDay: TimeOfDay,
    val wallpaperId: WallpaperId,
    val scaleMode: ScaleMode = ScaleMode.CROP
)