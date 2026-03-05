package com.andyl.dynamicwallpaper.domain.usecase.impl

import android.util.Log
import com.andyl.dynamicwallpaper.data.time.datasource.TimeOfDayDataSource
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.usecase.contract.DetectTimeOfDayUseCase

class DetectTimeOfDayUseCaseImpl(
    private val dataSource: TimeOfDayDataSource
) : DetectTimeOfDayUseCase {

    override operator fun invoke(): TimeOfDay {
        val time = dataSource.getTimeOfDay()
        Log.d("DetectTimeUseCase", "Momento del día detectado para el wallpaper: $time")
        return time
    }
}