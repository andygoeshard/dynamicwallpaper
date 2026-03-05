package com.andyl.dynamicwallpaper.domain.usecase.impl

import android.util.Log
import com.andyl.dynamicwallpaper.domain.repository.LocationRepository
import com.andyl.dynamicwallpaper.domain.repository.UserPreferencesRepository
import com.andyl.dynamicwallpaper.domain.repository.WallpaperRepository
import com.andyl.dynamicwallpaper.domain.repository.WeatherRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.DetectTimeOfDayUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ResolveWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.SetWallpaperRuleUseCase

class ApplyDynamicWallpaperUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val detectTimeOfDayUseCase: DetectTimeOfDayUseCase,
    private val resolveWallpaperUseCase: ResolveWallpaperUseCase,
    private val wallpaperRepository: WallpaperRepository,
    private val setWallpaperRuleUseCase: SetWallpaperRuleUseCase
) : ApplyDynamicWallpaperUseCase {

    override suspend operator fun invoke(packId: String?) {
        Log.d("DEBUG_WORKER", "1. Obteniendo ubicación...")
        val location = locationRepository.getCurrentLocation()

        Log.d("DEBUG_WORKER", "2. Obteniendo clima para: $location")
        val weather = weatherRepository.getCurrentWeather(location)

        val timeOfDay = detectTimeOfDayUseCase()
        Log.d("DEBUG_WORKER", "3. Momento del día: $timeOfDay")

        val config = preferencesRepository.getWallpaperConfig(packId)
        val wallpaperId = resolveWallpaperUseCase(weather, timeOfDay, config)

        Log.d("DEBUG_WORKER", "4. Wallpaper resuelto: $wallpaperId")

        if (wallpaperId != null) {
            wallpaperRepository.applyWallpaper(wallpaperId)
            Log.d("DEBUG_WORKER", "5. ¡Wallpaper aplicado con éxito!")
        } else {
            Log.e("DEBUG_WORKER", "5. ERROR: No se encontró un wallpaper para estas condiciones")
        }
    }
}
