package com.andyl.iris.domain.model

data class ImageResult(
    val id: String,
    val urlSmall: String,
    val urlFull: String,
    val provider: String,
    val alt: String? = null
)
