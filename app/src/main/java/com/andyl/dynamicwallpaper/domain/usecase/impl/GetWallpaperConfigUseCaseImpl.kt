package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetWallpaperConfigUseCase

class GetWallpaperConfigUseCaseImpl(
    private val repository: UserPreferencesRepository
) : GetWallpaperConfigUseCase {
    override suspend fun invoke(packId: String?): WallpaperConfig {
        return repository.getWallpaperConfig(
            packId = packId
        )
    }
}