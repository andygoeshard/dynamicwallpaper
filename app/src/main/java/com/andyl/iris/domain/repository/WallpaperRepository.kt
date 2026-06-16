package com.andyl.iris.domain.repository

import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.WallpaperId


interface WallpaperRepository {
    suspend fun applyWallpaper(
        wallpaperId: WallpaperId,
        scaleMode: ScaleMode,
        target: Int,
        cropX: Float? = null,
        cropY: Float? = null,
        cropScale: Float? = null
    ): Result<Unit>

    suspend fun cropAndSaveWallpaper(
        wallpaperId: WallpaperId,
        cropX: Float,
        cropY: Float,
        cropScale: Float
    ): Result<String>
}
