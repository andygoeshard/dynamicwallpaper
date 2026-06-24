package com.andyl.iris.ui.searchscreen

import com.andyl.iris.data.database.entity.FavoriteImage
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.DownloadTask
import com.andyl.iris.domain.model.PackInfo

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<ImageResult> = emptyList(),
    val previewImages: List<String?> = emptyList(), 
    val previewFullUrls: List<String?> = emptyList(), 
    val error: String? = null,
    val successMessage: String? = null,

    val currentPack: SuggestedPack? = null,
    val activeSlot: WallpaperSlot? = null,

    val downloadProgress: Float? = null,
    val downloadTasks: List<DownloadTask> = emptyList(),
    val showDownloads: Boolean = false,
    val showPackSelectionDialog: Boolean = false,
    val availablePacks: List<PackInfo> = emptyList(),

    val favorites: List<FavoriteImage> = emptyList(),
    val localImages: List<ImageResult> = emptyList(),
    val currentTab: Int = 2, // 0: Local, 1: Favorites, 2: Packs, 3: Search Results
    val selectedImage: ImageResult? = null
)

data class WallpaperSlot(
    val weather: Weather?,
    val time: TimeOfDay?,
    val dayName: String?,
    val fixedTime: String?,
    val label: String
)
