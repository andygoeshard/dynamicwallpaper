package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.WallpaperRule
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.SetWallpaperRuleUseCase

class SetWallpaperConfigUseCaseImpl(
    private val userPreferencesRepository: UserPreferencesRepository
) : SetWallpaperRuleUseCase {

    override suspend fun invoke(
        config: WallpaperConfig
    ) {
        userPreferencesRepository.setWallpaperConfig(config)
    }
}
