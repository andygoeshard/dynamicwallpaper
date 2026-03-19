package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.AddPackUseCase

class AddPackUseCaseImpl(private val repository: UserPreferencesRepository): AddPackUseCase {
    override suspend fun invoke(): List<PackInfo> {
        return repository.addNewPack()
    }
}