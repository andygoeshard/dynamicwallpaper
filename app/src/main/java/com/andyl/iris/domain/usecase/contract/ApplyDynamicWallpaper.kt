package com.andyl.iris.domain.usecase.contract

interface ApplyDynamicWallpaperUseCase {
    suspend operator fun invoke(packId: String? = null)
}
