package com.andyl.iris.domain.usecase.contract

interface DeletePackUseCase {
    suspend operator fun invoke(packId: String)
}