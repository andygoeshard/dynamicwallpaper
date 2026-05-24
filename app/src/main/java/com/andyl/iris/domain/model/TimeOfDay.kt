package com.andyl.iris.domain.model

enum class TimeOfDay(val queryTerm: String) {
    DAWN("dawn sunrise"),
    DUSK("sunset dusk"),
    DAY("daylight"),
    NIGHT("night")
}