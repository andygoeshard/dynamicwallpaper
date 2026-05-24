package com.andyl.iris.data.imagesprovider.dto

import kotlinx.serialization.Serializable

@Serializable
data class PexelsResponse(
    val photos: List<PexelsPhoto>,
    val total_results: Int
)

@Serializable
data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val src: PexelsSource,
    val alt: String? = null
)

@Serializable
data class PexelsSource(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)
