package com.andyl.iris.data.imagesprovider.dto

import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayHit>
)

@Serializable
data class PixabayHit(
    val id: Int,
    val pageURL: String,
    val type: String,
    val tags: String,
    val previewURL: String,
    val webformatURL: String,
    val largeImageURL: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val user: String? = null
)
