package com.andyl.dynamicwallpaper.di

import com.andyl.dynamicwallpaper.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        DynamicWallpaperViewModel(
            applyDynamicWallpaperUseCase = get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

}