package com.andyl.iris.domain.usecase.contract

interface GetFirstTimeKeyUseCase{
    suspend operator fun invoke(): Boolean
}