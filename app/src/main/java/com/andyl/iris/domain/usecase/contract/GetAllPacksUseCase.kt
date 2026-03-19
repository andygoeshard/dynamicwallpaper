package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.PackInfo

interface GetAllPacksUseCase {
    suspend operator fun invoke(): List<PackInfo>
}