package com.andyl.iris.di

import com.andyl.iris.ui.sound.SoundManager
import com.andyl.iris.ui.theme.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val uiModule = module {
    single { ThemeManager() }
    single { SoundManager(androidContext()) }
}

val appModules = listOf(
    networkModule,
    domainModule,
    dataModule,
    viewModelModule,
    uiModule,
)
