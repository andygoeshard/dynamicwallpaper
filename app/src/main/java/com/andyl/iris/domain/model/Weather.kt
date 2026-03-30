package com.andyl.iris.domain.model

import androidx.annotation.StringRes
import com.andyl.iris.R

sealed class Weather(@param:StringRes val stringRes: Int) {
    data object Clear : Weather(R.string.weather_clear)
    data object Cloudy : Weather(R.string.weather_cloudy)
    data object Rain : Weather(R.string.weather_rain)
    data object Snow : Weather(R.string.weather_snow)
    data object Fog : Weather(R.string.weather_fog)
    data object Storm : Weather(R.string.weather_storm)

    companion object {
        fun all(): Set<Weather> = setOf(Clear, Cloudy, Rain, Snow, Fog, Storm)
    }
}
