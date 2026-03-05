package com.andyl.dynamicwallpaper.domain.usecase.contract

import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig

interface GetWallpaperConfigUseCase {
    suspend operator fun invoke(packId: String?): WallpaperConfig
}
