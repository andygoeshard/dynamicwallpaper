package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.model.PackInfo
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.AddPackUseCase

class AddPackUseCaseImpl(private val repository: UserPreferencesRepository): AddPackUseCase {
    override suspend fun invoke(): List<PackInfo> {
        return repository.addNewPack()
    }
}