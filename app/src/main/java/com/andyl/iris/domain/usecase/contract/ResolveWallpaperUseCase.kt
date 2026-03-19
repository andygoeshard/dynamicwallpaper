package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.Weather

interface ResolveWallpaperUseCase {
    suspend operator fun invoke(
        weather: Weather?,
        timeOfDay: TimeOfDay,
        config: WallpaperConfig
    ): WallpaperId
}

