package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.WallpaperConfig

interface SetWallpaperRuleUseCase {
    suspend operator fun invoke(
        config: WallpaperConfig
    )
}

