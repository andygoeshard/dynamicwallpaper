package com.andyl.iris.data.userpreferences.dto

import com.andyl.iris.domain.mapper.weatherFromKey
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import kotlinx.serialization.Serializable


@Serializable
data class WallpaperPackDto(
    val id: String,
    val name: String,
    val updateIntervalMinutes: Int = 15,
    val weatherRules: List<WallpaperRuleDto>,
    val dailyRules: Map<String, String> = emptyMap(),
    val fixedTimeRules: Map<String, String> = emptyMap(),
    val enabledWeathers: List<String> = listOf("CLEAR", "CLOUDY", "RAIN", "SNOW", "FOG", "STORM"),
    val scaleMode: String = "FIT"
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
        activePackId = activeId,
        scaleMode = try {
            ScaleMode.valueOf(this.scaleMode)
        } catch (_: Exception) {
            ScaleMode.FIT
        }
    )
}