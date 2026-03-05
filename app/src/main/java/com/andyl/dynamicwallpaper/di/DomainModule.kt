package com.andyl.dynamicwallpaper.di

import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ChangePackUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetWallpaperConfigUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.SetWallpaperRuleUseCase
import com.andyl.dynamicwallpaper.domain.usecase.impl.ApplyDynamicWallpaperUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.ChangePackUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.DetectTimeOfDayUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.GetWallpaperConfigUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.ResolveWallpaperUseCaseImpl
import com.andyl.dynamicwallpaper.domain.usecase.impl.SetWallpaperConfigUseCaseImpl
import com.andyl.dynamicwallpaper.worker.DynamicWallpaperWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val domainModule = module {

    factory<DetectTimeOfDayUseCase> {
        DetectTimeOfDayUseCaseImpl(get())
    }

    factory<ResolveWallpaperUseCase> {
        ResolveWallpaperUseCaseImpl()
    }
    factory<ChangePackUseCase> {
        ChangePackUseCaseImpl(get())
    }

    factory<ApplyDynamicWallpaperUseCase> {
        ApplyDynamicWallpaperUseCaseImpl(
            locationRepository = get(),
            weatherRepository = get(),
            preferencesRepository = get(),
            detectTimeOfDayUseCase = get(),
            resolveWallpaperUseCase = get(),
            wallpaperRepository = get(),
            get()
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
