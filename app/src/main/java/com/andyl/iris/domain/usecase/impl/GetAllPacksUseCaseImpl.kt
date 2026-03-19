package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.GetAllPacksUseCase

class GetAllPacksUseCaseImpl(
    private val repository: UserPreferencesRepository
) : GetAllPacksUseCase {
    override suspend fun invoke(): List<PackInfo> {
        return repository.getAllPacks()
    }
}
