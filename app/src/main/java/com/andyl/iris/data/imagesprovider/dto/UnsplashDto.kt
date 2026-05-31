package com.andyl.iris.data.imagesprovider.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnsplashResponse(
    val results: List<UnsplashImage>
)

@Serializable
data class UnsplashImage(
    val id: String,
    val urls: UnsplashUrls,
    val alt_description: String? = null
)

@Serializable
data class UnsplashUrls(
    val raw: String? = null,
    val full: String,
    val regular: String? = null,
    val small: String,
    val thumb: String
)
