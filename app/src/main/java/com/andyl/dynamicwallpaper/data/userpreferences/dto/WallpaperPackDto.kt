package com.andyl.dynamicwallpaper.data.userpreferences.dto

import com.andyl.dynamicwallpaper.domain.mapper.weatherFromKey
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.WallpaperRule
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperPackDto(
    val id: String,
    val name: String,
    val updateIntervalMinutes: Int = 15,
    val weatherRules: List<WallpaperRuleDto>,
    val dailyRules: Map<String, String> = emptyMap(),
    val fixedTimeRules: Map<String, String> = emptyMap(),
    val enabledWeathers: List<String> = listOf("CLEAR", "CLOUDY", "RAIN", "SNOW", "FOG", "STORM")
)

fun WallpaperPackDto.toDomain(activeId: String): WallpaperConfig {
    return WallpaperConfig(
        id = this.id,
        name = this.name,
        updateIntervalMinutes = this.updateIntervalMinutes,
        rules = this.weatherRules.map {
            WallpaperRule(
                weather = weatherFromKey(it.weather),
                timeOfDay = TimeOfDay.valueOf(it.timeOfDay),
                wallpaperId = WallpaperId(it.uri)
            )
        },
        dailyRules = this.dailyRules,
        fixedTimeRules = this.fixedTimeRules,
        enabledWeathers = this.enabledWeathers.map { weatherFromKey(it) }.toSet(),
        activePackId = activeId
    )
}