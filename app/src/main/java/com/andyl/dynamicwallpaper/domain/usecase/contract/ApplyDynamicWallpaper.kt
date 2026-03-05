package com.andyl.dynamicwallpaper.domain.usecase.contract

interface ApplyDynamicWallpaperUseCase {
    suspend operator fun invoke(packId: String? = null)
}
