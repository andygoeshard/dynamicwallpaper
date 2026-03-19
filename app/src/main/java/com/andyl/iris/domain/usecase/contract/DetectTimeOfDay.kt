package com.andyl.iris.domain.usecase.contract

import com.andyl.iris.domain.model.TimeOfDay


interface DetectTimeOfDayUseCase {
    operator fun invoke(): TimeOfDay
}
