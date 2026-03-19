package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.WallpaperConfig

interface GetWallpaperConfigUseCase {
    suspend operator fun invoke(packId: String?): WallpaperConfig
}
