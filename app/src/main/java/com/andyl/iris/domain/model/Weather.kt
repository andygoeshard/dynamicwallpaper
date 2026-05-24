package com.andyl.iris.domain.model

import com.andyl.iris.R

sealed class Weather(val stringRes: Int, val queryTerm: String) {
    data object Clear : Weather(R.string.weather_clear, "sunny")
    data object Cloudy : Weather(R.string.weather_cloudy, "cloudy")
    data object Rain : Weather(R.string.weather_rain, "rainy")
    data object Snow : Weather(R.string.weather_snow, "snowy")
    data object Fog : Weather(R.string.weather_fog, "foggy")
    data object Storm : Weather(R.string.weather_storm, "storm")

    companion object {
        fun all(): Set<Weather> = setOf(Clear, Cloudy, Rain, Snow, Fog, Storm)
    }
}
