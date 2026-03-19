package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.ChangeActivePackUseCase

class ChangeActivePackUseCaseImpl(
    private val repository: UserPreferencesRepository
) : ChangeActivePackUseCase {
    override suspend fun invoke(packId: String) {
        repository.setActivePackId(packId)
    }
}
