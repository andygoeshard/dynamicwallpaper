package com.andyl.iris.domain.model

import androidx.annotation.StringRes
import com.andyl.iris.R

sealed class Weather(@param:StringRes val stringRes: Int, val queryTerm: String) {
    data object Clear : Weather(R.string.weather_clear, "clear sky")
    data object Cloudy : Weather(R.string.weather_cloudy, "cloudy overcast")
    data object Rain : Weather(R.string.weather_rain, "rainy rain")
    data object Snow : Weather(R.string.weather_snow, "snowy winter")
    data object Fog : Weather(R.string.weather_fog, "foggy misty")
    data object Storm : Weather(R.string.weather_storm, "thunderstorm lightning")

    companion object {
        fun all(): Set<Weather> = setOf(Clear, Cloudy, Rain, Snow, Fog, Storm)
    }
}
