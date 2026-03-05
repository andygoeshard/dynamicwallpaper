package com.andyl.dynamicwallpaper.domain.usecase.contract

import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.Weather

interface SetWallpaperRuleUseCase {
    suspend operator fun invoke(
        config: WallpaperConfig
    )
}

