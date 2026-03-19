package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.GetWallpaperConfigUseCase

class GetWallpaperConfigUseCaseImpl(
    private val repository: UserPreferencesRepository
) : GetWallpaperConfigUseCase {
    override suspend fun invoke(packId: String?): WallpaperConfig {
        return repository.getWallpaperConfig(
            packId = packId
        )
    }
}