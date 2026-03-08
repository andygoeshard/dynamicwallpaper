package com.andyl.dynamicwallpaper.domain.usecase.contract

interface DeletePackUseCase {
    suspend operator fun invoke(packId: String)
}