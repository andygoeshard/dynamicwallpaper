package com.andyl.iris.di

import androidx.room.Room
import com.andyl.iris.data.database.IrisDatabase
import com.andyl.iris.data.imagesprovider.repository.UnifiedImageRepositoryImpl
import com.andyl.iris.data.location.datasource.AndroidLocationDataSource
import com.andyl.iris.data.location.datasource.NominatimRemoteDataSource
import com.andyl.iris.data.location.repository.LocationRepositoryImpl
import com.andyl.iris.data.time.datasource.TimeOfDayDataSource
import com.andyl.iris.data.userpreferences.repository.UserPreferencesRepositoryImpl
import com.andyl.iris.data.wallpaper.repository.WallpaperRepositoryImpl
import com.andyl.iris.data.weather.api.WeatherApi
import com.andyl.iris.data.weather.repository.WeatherRepositoryImpl
import com.andyl.iris.domain.repository.ImageRepository
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.domain.repository.WallpaperRepository
import com.andyl.iris.domain.repository.WeatherRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            IrisDatabase::class.java,
            "iris_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<IrisDatabase>().imageCacheDao() }

    single<ImageRepository> {
        UnifiedImageRepositoryImpl(
            unsplashDataSource = get(),
            pexelsDataSource = get(),
            cacheDao = get()
        )
    }

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
