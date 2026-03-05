package com.andyl.dynamicwallpaper.ui.state

import com.andyl.dynamicwallpaper.domain.model.Weather

data class DynamicWallpaperUiState(
    val isLoading: Boolean = false,
    val isApplied: Boolean = false,
    val error: String? = null,
    val packName: String = "",
    val rules: Map<String, String> = emptyMap(),
    val dailyRules: Map<String, String> = emptyMap(),
    val fixedRules: Map<String, String> = emptyMap(),
    val enabledWeathers: Set<Weather> = emptySet(),
    val editingPackId: String = "1",
    val activePackId: String = "1"
)

