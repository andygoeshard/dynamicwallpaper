package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.GetFirstTimeKeyUseCase

class GetFirstTimeKeyUseCaseImpl(private val repository: UserPreferencesRepository):
    GetFirstTimeKeyUseCase {
    override suspend fun invoke(): Boolean {
        return repository.isFirstApplyGlobal()
    }
}