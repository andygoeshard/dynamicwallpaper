package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.WallpaperId


interface WallpaperRepository {
    suspend fun applyWallpaper(wallpaperId: WallpaperId, scaleMode: ScaleMode, target: Int): Result<Unit>
}
