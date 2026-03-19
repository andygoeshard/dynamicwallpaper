package com.andyl.iris.domain.model

data class WallpaperConfig(
    val id: String,
    val name: String,
    val updateIntervalMinutes: Int = 15,
    val rules: List<WallpaperRule> = emptyList(),
    val dailyRules: Map<String, String> = emptyMap(),
    val fixedTimeRules: Map<String, String> = emptyMap(),
    val enabledWeathers: Set<Weather> = Weather.all(),
    val activePackId: String,
    val scaleMode: ScaleMode = ScaleMode.FIT
)