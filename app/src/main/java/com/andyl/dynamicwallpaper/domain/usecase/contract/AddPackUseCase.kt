package com.andyl.dynamicwallpaper.domain.usecase.contract

import com.andyl.dynamicwallpaper.domain.model.PackInfo

interface AddPackUseCase{
    suspend operator fun invoke(): List<PackInfo>
}