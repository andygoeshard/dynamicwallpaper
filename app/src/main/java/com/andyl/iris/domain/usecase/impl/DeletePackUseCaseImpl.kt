package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.DeletePackUseCase

class DeletePackUseCaseImpl(private val repository: UserPreferencesRepository) : DeletePackUseCase {
    override suspend fun invoke(packId: String) {
        repository.deletePack(packId)
    }
}