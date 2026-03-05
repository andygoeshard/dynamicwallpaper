package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.ChangePackUseCase

class ChangePackUseCaseImpl(
    private val repository: UserPreferencesRepository
) : ChangePackUseCase {
    override suspend fun invoke(packId: String): WallpaperConfig {
        repository.setActivePackId(packId)
        return repository.getWallpaperConfig(packId)
    }
}
