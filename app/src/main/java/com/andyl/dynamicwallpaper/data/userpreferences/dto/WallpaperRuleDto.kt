package com.andyl.dynamicwallpaper.data.userpreferences.dto

import com.andyl.dynamicwallpaper.domain.mapper.weatherFromKey
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.WallpaperRule
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperRuleDto(
    val weather: String,
    val timeOfDay: String,
    val uri: String
)

private fun Map<String, String>.toWallpaperRules(): List<WallpaperRule> {
    return this.map { (key, uri) ->
        val parts = key.split(" - ")
        val weather = weatherFromKey(parts[0])
        val timeOfDay = TimeOfDay.valueOf(parts[1])
        WallpaperRule(weather, timeOfDay, WallpaperId(uri))
    }
}
