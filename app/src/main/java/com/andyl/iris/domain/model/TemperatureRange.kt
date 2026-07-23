package com.andyl.iris.domain.model

import com.andyl.iris.R

enum class TemperatureRange(
    val stringRes: Int,
    val minTemp: Double,
    val maxTemp: Double
) {
    FREEZING(R.string.temp_freezing, Double.NEGATIVE_INFINITY, 0.0),
    COLD(R.string.temp_cold, 0.0, 12.0),
    COOL(R.string.temp_cool, 12.0, 22.0),
    WARM(R.string.temp_warm, 22.0, 32.0),
    HOT(R.string.temp_hot, 32.0, Double.POSITIVE_INFINITY);

    companion object {
        fun fromTemperature(temp: Double): TemperatureRange = when {
            temp < 0.0 -> FREEZING
            temp < 12.0 -> COLD
            temp < 22.0 -> COOL
            temp < 32.0 -> WARM
            else -> HOT
        }
    }
}
