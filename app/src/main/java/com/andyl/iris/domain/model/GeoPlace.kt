package com.andyl.iris.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoPlace(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double = 2000.0,
    val packId: String? = null,
    val invert: Boolean = false
) {
    fun distanceMetersTo(lat: Double, lon: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat - latitude)
        val dLon = Math.toRadians(lon - longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(lat)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    fun contains(lat: Double, lon: Double): Boolean = distanceMetersTo(lat, lon) <= radiusMeters
}
