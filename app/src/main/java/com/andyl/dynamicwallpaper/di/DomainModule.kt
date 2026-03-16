package com.andyl.dynamicwallpaper.di

import com.andyl.dynamicwallpaper.domain.usecase.contract.AddPackUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ChangeActivePackUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.DeletePackUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetAllPacksUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetWallpaperConfigUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.SetWallpaperRuleUseCase
import com.andyl.dynamicwallpaper.domain.usecase.impl.AddPackUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.ApplyDynamicWallpaperUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.ChangeActivePackUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.DeletePackUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.DetectTimeOfDayUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.GetAllPacksUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.GetWallpaperConfigUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.ResolveWallpaperUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.SetWallpaperConfigUseCaseImpl
import com.andyl.dynamicwallpaper.worker.DynamicWallpaperWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val domainModule = module {

    factory<DetectTimeOfDayUseCase> {
        DetectTimeOfDayUseCaseImpl(
            get()
        )
    }

    factory<ResolveWallpaperUseCase> {
        ResolveWallpaperUseCaseImpl()
    }

    factory<GetAllPacksUseCase> {
        GetAllPacksUseCaseImpl(
            repository = get()
        )
    }

    factory<ChangeActivePackUseCase> {
        ChangeActivePackUseCaseImpl(
            get()
        )
    }

    factory<AddPackUseCase> {
        AddPackUseCaseImpl(
            get()
        )
    }

    factory<DeletePackUseCase> {
        DeletePackUseCaseImpl(
            get()
        )
    }

    factory<ApplyDynamicWallpaperUseCase> {
        ApplyDynamicWallpaperUseCaseImpl(
            locationRepository = get(),
            weatherRepository = get(),
            preferencesRepository = get(),
            detectTimeOfDayUseCase = get(),
            resolveWallpaperUseCase = get(),
            wallpaperRepository = get(),
        )
    }
    factory<SetWallpaperRuleUseCase> {
        SetWallpaperConfigUseCaseImpl(
            userPreferencesRepository = get()
        )
    }

    factory<DetectTimeOfDayUseCase> {
        DetectTimeOfDayUseCaseImpl(get())
    }

    factory<GetWallpaperConfigUseCase> {
        GetWallpaperConfigUseCaseImpl(get())
    }

    worker { DynamicWallpaperWorker(get(), get(), get()) }

}
