package com.andyl.iris.di

import com.andyl.iris.ui.theme.ThemeManager
import org.koin.dsl.module

val uiModule = module {
    single { ThemeManager() }
}

val appModules = listOf(
    networkModule,
    domainModule,
    dataModule,
    viewModelModule,
    uiModule,
)
