package com.andyl.iris.domain.model

sealed class Weather {
    data object Clear : Weather()
    data object Cloudy : Weather()
    data object Rain : Weather()
    data object Snow : Weather()
    data object Fog : Weather()
    data object Storm : Weather()

    companion object {
        fun all(): Set<Weather> = setOf(Clear, Cloudy, Rain, Snow, Fog, Storm)
    }
}
