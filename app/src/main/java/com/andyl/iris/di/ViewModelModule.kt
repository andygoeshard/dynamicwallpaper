package com.andyl.iris.di

import com.andyl.iris.ui.searchscreen.SearchViewModel
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        DynamicWallpaperViewModel(
            applyDynamicWallpaperUseCase = get(),
            setWallpaperRuleUseCase = get(),
            getWallpaperConfigUseCase = get(),
            changeActivePackUseCase = get(),
            getAllPacksUseCase = get(),
            addPackUseCase = get(),
            deletePackUseCase = get(),
            getFirstTimeKeyUseCase = get(),
            changeFirstTimeKeyUseCase = get(),
            locationRepository = get(),
            get()
        )
    }
    viewModel { params ->
        SearchViewModel(
            imageRepository = get(),
            downloadUseCase = get(),
            installPredefinedPackUseCase = get(),
            downloadRepository = get(),
            favoriteRepository = get(),
            localImageRepository = get(),
            wallpaperRepository = get(),
            wallpaperViewModel = params.get()
        )
    }

}
