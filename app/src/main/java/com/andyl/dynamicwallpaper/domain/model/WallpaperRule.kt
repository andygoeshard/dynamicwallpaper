package com.andyl.dynamicwallpaper.domain.model

data class WallpaperRule(
    val weather: Weather,
    val timeOfDay: TimeOfDay,
    val wallpaperId: WallpaperId
)