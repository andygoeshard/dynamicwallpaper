package com.andyl.dynamicwallpaper.domain.usecase.impl

import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.DeletePackUseCase

class DeletePackUseCaseImpl(private val repository: UserPreferencesRepository) : DeletePackUseCase {
    override suspend fun invoke(packId: String) {
        repository.deletePack(packId)
    }
}