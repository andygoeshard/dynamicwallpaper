package com.andyl.iris.ui.state

import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.Weather

data class DynamicWallpaperUiState(
    val availablePacks: List<PackInfo> = emptyList(),
    val slideDirection: Int = 1,
    val isLoading: Boolean = true,
    val isApplied: Boolean = false,
    val error: String? = null,
    val packName: String = "",
    val rules: Map<String, String> = emptyMap(),
    val dailyRules: Map<String, String> = emptyMap(),
    val fixedRules: Map<String, String> = emptyMap(),
    val enabledWeathers: Set<Weather> = emptySet(),
    val editingPackId: String = "1",
    val activePackId: String = "1",
    val isWeatherFeatureEnabled: Boolean = false,
    val isFirstTimeGlobal: Boolean = true,
    val showFirstTimeDialog: Boolean = true,
    val scaleMode: ScaleMode = ScaleMode.CROP
)
