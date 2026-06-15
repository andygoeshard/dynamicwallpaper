package com.andyl.iris.ui.searchscreen

import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<ImageResult> = emptyList(),
    val previewImages: List<String?> = emptyList(), // Store dynamic previews for the selected pack
    val error: String? = null,

    val currentPack: SuggestedPack? = null,
    val activeSlot: WallpaperSlot? = null,

    val downloadProgress: Float? = null,
    val showPackSelectionDialog: Boolean = false,
    val availablePacks: List<com.andyl.iris.domain.model.PackInfo> = emptyList()
)

data class WallpaperSlot(
    val weather: Weather?,
    val time: TimeOfDay?,
    val dayName: String?,
    val fixedTime: String?,
    val label: String
)
