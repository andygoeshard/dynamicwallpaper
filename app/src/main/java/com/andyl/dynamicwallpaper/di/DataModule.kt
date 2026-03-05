package com.andyl.dynamicwallpaper.di

import com.andyl.dynamicwallpaper.data.location.datasource.AndroidLocationDataSource
import com.andyl.dynamicwallpaper.data.location.datasource.NominatimRemoteDataSource
import com.andyl.dynamicwallpaper.data.location.repository.LocationRepositoryImpl
import com.andyl.dynamicwallpaper.data.time.datasource.TimeOfDayDataSource
import com.andyl.dynamicwallpaper.data.userpreferences.repository.UserPreferencesRepositoryImpl
import com.andyl.dynamicwallpaper.data.wallpaper.repository.WallpaperRepositoryImpl
import com.andyl.dynamicwallpaper.data.weather.api.WeatherApi
import com.andyl.dynamicwallpaper.data.weather.repository.WeatherRepositoryImpl
import com.andyl.dynamicwallpaper.domain.repository.LocationRepository
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.repository.WallpaperRepository
import com.andyl.dynamicwallpaper.domain.repository.WeatherRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        AndroidLocationDataSource(
            context = androidContext(),
            get()
        )
    }

    single {
        WeatherApi(
            client = get()
        )
    }

    single {
        TimeOfDayDataSource()
    }

    single<LocationRepository> {
        LocationRepositoryImpl(
            dataSource = get(),
            get(),
            get()
        )
    }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            api = get()
        )
    }

    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(
            context = androidContext()
        )
    }

    single<WallpaperRepository> {
        WallpaperRepositoryImpl(
            context = androidContext()
        )
    }

    single { NominatimRemoteDataSource(get()) }

}
