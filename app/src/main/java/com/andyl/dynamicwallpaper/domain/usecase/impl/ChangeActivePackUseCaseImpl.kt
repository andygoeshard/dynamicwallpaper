package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.ChangeActivePackUseCase

class ChangeActivePackUseCaseImpl(
    private val repository: UserPreferencesRepository
) : ChangeActivePackUseCase {
    override suspend fun invoke(packId: String) {
        repository.setActivePackId(packId)
    }
}
