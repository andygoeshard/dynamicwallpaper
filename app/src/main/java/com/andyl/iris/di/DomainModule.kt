package com.andyl.iris.di

import com.andyl.iris.domain.usecase.contract.AddPackUseCase
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.ChangeActivePackUseCase
import com.andyl.iris.domain.usecase.contract.DeletePackUseCase
import com.andyl.iris.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.iris.domain.usecase.contract.GetAllPacksUseCase
import com.andyl.iris.domain.usecase.contract.GetWallpaperConfigUseCase
import com.andyl.iris.domain.usecase.contract.ResolveWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.SetWallpaperRuleUseCase
import com.andyl.iris.domain.usecase.impl.AddPackUseCaseImpl
import com.andyl.iris.domain.usecase.impl.ApplyDynamicWallpaperUseCaseImpl
import com.andyl.iris.domain.usecase.impl.ChangeActivePackUseCaseImpl
import com.andyl.iris.domain.usecase.impl.DeletePackUseCaseImpl
import com.andyl.iris.domain.usecase.impl.DetectTimeOfDayUseCaseImpl
import com.andyl.iris.domain.usecase.impl.GetAllPacksUseCaseImpl
import com.andyl.iris.domain.usecase.impl.GetWallpaperConfigUseCaseImpl
import com.andyl.iris.domain.usecase.impl.ResolveWallpaperUseCaseImpl
import com.andyl.iris.domain.usecase.impl.SetWallpaperConfigUseCaseImpl
import com.andyl.iris.worker.IrisWallpaperWorker
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

    worker { IrisWallpaperWorker(get(), get(), get()) }

}
