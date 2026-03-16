package com.andyl.dynamicwallpaper.domain.usecase.contract

import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.Weather

interface ResolveWallpaperUseCase {
    suspend operator fun invoke(
        weather: Weather?,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): WallpaperId
}

