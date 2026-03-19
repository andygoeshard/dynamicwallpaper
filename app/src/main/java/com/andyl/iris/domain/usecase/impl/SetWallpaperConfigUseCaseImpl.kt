package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.SetWallpaperRuleUseCase

class SetWallpaperConfigUseCaseImpl(
    private val userPreferencesRepository: UserPreferencesRepository
) : SetWallpaperRuleUseCase {

    override suspend fun invoke(
        config: WallpaperConfig
    ) {
        userPreferencesRepository.setWallpaperConfig(config)
    }
}
