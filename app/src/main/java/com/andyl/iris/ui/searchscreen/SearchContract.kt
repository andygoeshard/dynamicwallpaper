package com.andyl.iris.ui.searchscreen

import com.andyl.iris.data.imagesprovider.dto.UnsplashImage
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<UnsplashImage> = emptyList(),
    val error: String? = null,

    val currentPack: SuggestedPack? = null,
    val activeSlot: WallpaperSlot? = null,

    val downloadProgress: Float? = null
)

data class WallpaperSlot(
    val weather: Weather?,
    val time: TimeOfDay?,
    val dayName: String?, // Agregá este
    val label: String
)