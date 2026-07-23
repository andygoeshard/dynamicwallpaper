package com.andyl.iris.di

import androidx.room.Room
import com.andyl.iris.data.database.IrisDatabase
import com.andyl.iris.data.imagesprovider.repository.UnifiedImageRepositoryImpl
import com.andyl.iris.data.location.datasource.AndroidLocationDataSource
import com.andyl.iris.data.location.datasource.GeocodingRemoteDataSource
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
import com.andyl.iris.data.database.repository.FavoriteRepositoryImpl
import com.andyl.iris.data.imagesprovider.repository.LocalImageRepositoryImpl
import com.andyl.iris.domain.repository.FavoriteRepository
import com.andyl.iris.domain.repository.LocalImageRepository
import com.andyl.iris.data.premium.PremiumRepositoryImpl
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.billing.BillingManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import com.andyl.iris.data.download.repository.DownloadRepositoryImpl
import com.andyl.iris.domain.repository.DownloadRepository

val dataModule = module {

    single<DownloadRepository> {
        DownloadRepositoryImpl()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            IrisDatabase::class.java,
            "iris_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<IrisDatabase>().imageCacheDao() }
    single { get<IrisDatabase>().favoriteDao() }

    single<FavoriteRepository> {
        FavoriteRepositoryImpl(get())
    }

    single<LocalImageRepository> {
        LocalImageRepositoryImpl(androidContext())
    }

    single<ImageRepository> {
        UnifiedImageRepositoryImpl(
            unsplashDataSource = get(),
            pexelsDataSource = get(),
            pixabayDataSource = get(),
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
            remoteDataSource = get(),
            preferencesRepository = get()
        )
    }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            api = get()
        )
    }

    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(
            context = androidContext(),
            premiumRepository = get()
        )
    }

    single<WallpaperRepository> {
        WallpaperRepositoryImpl(
            context = androidContext()
        )
    }

    single { GeocodingRemoteDataSource(get()) }

    single {
        BillingManager(androidContext())
    }

    single<PremiumRepository> {
        PremiumRepositoryImpl(
            context = androidContext(),
            billingManager = get()
        )
    }

}
