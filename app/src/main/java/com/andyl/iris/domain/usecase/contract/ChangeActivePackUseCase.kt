package com.andyl.iris.domain.usecase.contract

interface ChangeActivePackUseCase {
    suspend operator fun invoke(packId: String)
}