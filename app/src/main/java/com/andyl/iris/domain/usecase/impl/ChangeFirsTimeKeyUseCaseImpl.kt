package com.andyl.iris.domain.usecase.impl

import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.usecase.contract.ChangeFirstTimeKeyUseCase

class ChangeFirsTimeKeyUseCaseImpl(private val repository: UserPreferencesRepository): ChangeFirstTimeKeyUseCase  {
    override suspend fun invoke() {
        repository.setGlobalFirstApplyDone()
    }
}