package com.andyl.iris.di

import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
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
            get(),
            get(),
            get()
        )
    }

}