package com.andyl.dynamicwallpaper.data.location.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String
)