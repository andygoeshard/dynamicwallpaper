package com.andyl.dynamicwallpaper.domain.usecase.contract

import com.andyl.dynamicwallpaper.domain.model.TimeOfDay


interface DetectTimeOfDayUseCase {
    operator fun invoke(): TimeOfDay
}
