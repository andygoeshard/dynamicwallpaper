package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.TimeOfDay

interface DetectTimeOfDayUseCase {
    operator fun invoke(sunrise: String? = null, sunset: String? = null): TimeOfDay
}
