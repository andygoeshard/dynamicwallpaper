package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.PackInfo
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetAllPacksUseCase

class GetAllPacksUseCaseImpl(
    private val repository: UserPreferencesRepository
) : GetAllPacksUseCase {
    override suspend fun invoke(): List<PackInfo> {
        return repository.getAllPacks()
    }
}
