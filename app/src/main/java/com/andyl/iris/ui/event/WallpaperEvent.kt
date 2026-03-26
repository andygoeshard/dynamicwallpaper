package com.andyl.iris.ui.event

import android.content.Context
import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather

sealed interface WallpaperEvent{

    // City Events
    data class OnSearchQueryChanged(val newQuery: String): WallpaperEvent
    data class OnSelectCity(val city: CityResult): WallpaperEvent
    // Wallpaper Events
    object OnLoadInitialConfig: WallpaperEvent
    object OnApplyWallpaper: WallpaperEvent
    object OnToggleWeatherFeature: WallpaperEvent
    data class OnChangePack(val packId: String, val direction: Int) : WallpaperEvent
    data class OnRenamePack(val newName: String): WallpaperEvent
    data class OnToggleWeather(val weather: Weather): WallpaperEvent
    data class SetDailyWallpaper(val dayName: String, val uri: String): WallpaperEvent
    data class SetFixedTimeWallpaper(val context: Context, val time: String, val uri: String): WallpaperEvent
    data class SetWallpaperRule(val weather: Weather, val timeOfDay: TimeOfDay, val wallpaperUri: String, val target: Int): WallpaperEvent
    data class RequestExactAlarmPermission(val context: Context): WallpaperEvent
    object OnAddNewPack: WallpaperEvent
    data class OnDeletePack(val packId: String): WallpaperEvent
    data class OnSelectFromPackManager(val packId: String): WallpaperEvent
    data class OnDeleteDayRule(val dayName: String): WallpaperEvent
    data class OnDeleteFixedTimeRule(val context: Context, val time: String): WallpaperEvent
    data class UpdateScaleMode(val mode: ScaleMode) : WallpaperEvent
}


