package com.andyl.dynamicwallpaper.domain.repository

import com.andyl.dynamicwallpaper.domain.model.WallpaperId


interface WallpaperRepository {
    suspend fun applyWallpaper(wallpaperId: WallpaperId): Result<Unit>
}
