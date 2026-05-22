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
    val full: String,    // Para descargar y poner de wallpaper
    val small: String,   // Para la grilla de búsqueda (ahorra datos)
    val thumb: String    // Para previsualizaciones mínimas
)