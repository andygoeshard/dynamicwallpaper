package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.PackInfo

interface AddPackUseCase{
    suspend operator fun invoke(): List<PackInfo>
}