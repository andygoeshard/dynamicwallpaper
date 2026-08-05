package com.andyl.iris.domain.model

data class DailyForecast(
    val date: String,
    val weather: Weather,
    val tempMax: Double?,
    val tempMin: Double?,
    val precipitationProbability: Int?
)
