package com.andyl.iris.ui.searchscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.repository.ImageRepository
import com.andyl.iris.domain.usecase.contract.InstallPredefinedPackUseCase
import com.andyl.iris.domain.usecase.impl.DownloadWallpaperUseCase
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.FlowPreview

import com.andyl.iris.data.database.entity.FavoriteImage
import com.andyl.iris.domain.model.DownloadStatus
import com.andyl.iris.domain.model.DownloadTask
import com.andyl.iris.domain.repository.DownloadRepository
import com.andyl.iris.domain.repository.FavoriteRepository
import com.andyl.iris.domain.repository.LocalImageRepository
import com.andyl.iris.domain.repository.WallpaperRepository
import com.andyl.iris.domain.model.WallpaperId

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val imageRepository: ImageRepository,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val installPredefinedPackUseCase: InstallPredefinedPackUseCase,
    private val downloadRepository: DownloadRepository,
    private val favoriteRepository: FavoriteRepository,
    private val localImageRepository: LocalImageRepository,
    private val wallpaperRepository: WallpaperRepository,
    val wallpaperViewModel: DynamicWallpaperViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val packPreviewCache = mutableMapOf<String, Pair<List<String?>, List<String?>>>()

    init {
        setupSearchDebounce()
        observeDownloads()
        observeFavorites()
        loadLocalImages()
    }

    fun refreshLocalImages() {
        loadLocalImages()
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private fun loadLocalImages() {
        viewModelScope.launch {
            try {
                val images = localImageRepository.getLocalImages()
                _uiState.update { it.copy(localImages = images) }
            } catch (e: Exception) {
                android.util.Log.e("SearchVM", "Error loading local images", e)
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.getAllFavorites().collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
    }

    fun toggleFavorite(image: ImageResult) {
        viewModelScope.launch {
            val isFav = _uiState.value.favorites.any { it.uri == image.urlFull }
            if (isFav) {
                favoriteRepository.removeFavorite(image.urlFull)
            } else {
                favoriteRepository.addFavorite(
                    FavoriteImage(
                        uri = image.urlFull,
                        source = image.provider,
                        thumbnailUrl = image.urlSmall
                    )
                )
            }
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
        if (index != 2) {
            _uiState.update { it.copy(currentPack = null) }
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            downloadRepository.activeTasks.collect { tasks ->
                _uiState.update { it.copy(downloadTasks = tasks) }
            }
        }
    }

    fun toggleDownloads(show: Boolean) {
        _uiState.update { it.copy(showDownloads = show) }
    }

    fun removeDownloadTask(taskId: String) {
        downloadRepository.removeTask(taskId)
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.isNotBlank() && it.length > 2 }
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun selectPack(pack: SuggestedPack?) {
        _uiState.update { 
            it.copy(
                currentPack = pack, 
                activeSlot = null, 
                searchResults = emptyList(),
                previewImages = emptyList(),
                previewFullUrls = emptyList()
            ) 
        }
        _searchQuery.value = ""

        if (pack is SuggestedPack.Predefined) {
            val cached = packPreviewCache[pack.id]
            if (cached != null) {
                _uiState.update { it.copy(previewImages = cached.first, previewFullUrls = cached.second) }
            } else {
                fetchPreviewImages(pack.id)
            }
        }
    }

    private fun fetchPreviewImages(packId: String) {
        val predefined = PredefinedPacks.packs.find { it.id == packId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val baseQuery = if (predefined.isFullRandom) "wallpaper 4k high resolution" else predefined.categoryQuery
            
            val queries = when {
                predefined.type == PackType.WEEKLY -> {
                    listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        .map { "$baseQuery $it" }
                }
                predefined.isTimeBased -> {
                    TimeOfDay.entries.map { "$baseQuery ${it.queryTerm}" }
                }
                else -> {
                    Weather.all().flatMap { w ->
                        TimeOfDay.entries.map { t ->
                            "$baseQuery ${w.queryTerm} ${t.queryTerm}"
                        }
                    }
                }
            }

            // Fetch images in parallel
            val results = queries.map { q ->
                async {
                    imageRepository.getRandomImages(q, count = 20).getOrNull() ?: emptyList()
                }
            }.awaitAll()

            // Unique selection logic similar to installer
            val usedIds = mutableSetOf<String>()
            val smallUrls = mutableListOf<String?>()
            val fullUrls = mutableListOf<String?>()
            
            results.forEach { slotResults ->
                val choice = slotResults.shuffled().find { it.id !in usedIds }
                    ?: slotResults.firstOrNull()
                
                if (choice != null) {
                    usedIds.add(choice.id)
                    smallUrls.add(formatUrl(choice, isSmall = true))
                    fullUrls.add(choice.urlFull)
                } else {
                    smallUrls.add(null)
                    fullUrls.add(null)
                }
            }

            if (smallUrls.any { it != null }) {
                packPreviewCache[packId] = smallUrls to fullUrls
                _uiState.update { it.copy(previewImages = smallUrls, previewFullUrls = fullUrls, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun formatUrl(image: ImageResult, isSmall: Boolean): String {
        return when (image.provider) {
            "unsplash" -> {
                // Ensure preview framing matches raw download framing
                val size = if (isSmall) "w=600" else "w=1200"
                if (image.urlSmall.contains("?")) {
                    image.urlSmall.substringBefore("&") + "&$size&q=80"
                } else {
                    "${image.urlSmall}?$size&q=80"
                }
            }
            "pixabay" -> {
                if (isSmall) image.urlSmall else image.urlFull
            }
            else -> {
                // Pexels 'large' framing matches 'original' framing
                image.urlSmall
            }
        }
    }

    fun selectSlot(slot: WallpaperSlot?) {
        _uiState.update { it.copy(activeSlot = slot) }
        if (slot != null) {
            _searchQuery.value = ""
            // When selecting a slot, we default to LOCAL gallery (Index 0)
            _uiState.update { it.copy(searchResults = emptyList(), currentTab = 0) }
        } else {
            _searchQuery.value = ""
            // Return to Packs (Index 2) or wherever makes sense
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun installPack(suggestedPack: SuggestedPack, targetId: String? = null, onSuccess: () -> Unit) {
        val predefinedPack = PredefinedPacks.packs.find { it.id == suggestedPack.id } ?: return
        
        // Prevent double installation of the same pack
        if (_uiState.value.downloadTasks.any { it.id.contains(predefinedPack.id) && it.status is DownloadStatus.Downloading }) {
            return
        }

        val currentPreviews = _uiState.value.previewFullUrls

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showPackSelectionDialog = false, error = null) }
            installPredefinedPackUseCase(predefinedPack, targetId, currentPreviews)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, currentPack = null, successMessage = "Pack installed successfully!") }
                    wallpaperViewModel.onEvent(WallpaperEvent.OnLoadInitialConfig)
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
                }
        }
    }

    fun onLongPressInstall() {
        viewModelScope.launch {
            val packs = wallpaperViewModel.uiState.value.availablePacks
            _uiState.update { it.copy(showPackSelectionDialog = true, availablePacks = packs) }
        }
    }

    fun dismissPackSelection() {
        _uiState.update { it.copy(showPackSelectionDialog = false) }
    }

    fun selectImage(image: ImageResult?) {
        _uiState.update { it.copy(selectedImage = image) }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        imageRepository.searchImages(query)
            .onSuccess { res -> 
                _uiState.update { it.copy(searchResults = res, isLoading = false) } 
            }
            .onFailure { e -> 
                _uiState.update { it.copy(error = e.message, isLoading = false) } 
            }
    }

    fun confirmAndDownload(
        context: android.content.Context, 
        image: ImageResult, 
        target: Int, 
        scaleMode: com.andyl.iris.domain.model.ScaleMode,
        cropX: Float? = null,
        cropY: Float? = null,
        cropScale: Float? = null
    ) {
        val slot = _uiState.value.activeSlot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Get the source path (download if remote)
            val sourcePath = if (image.provider == "local") {
                image.urlFull
            } else {
                val downloadResult = downloadUseCase.execute(
                    url = image.urlFull,
                    fileName = "iris_temp_${System.currentTimeMillis()}"
                )
                downloadResult.getOrNull()?.absolutePath
            }

            if (sourcePath == null) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to obtain image source") }
                return@launch
            }

            // 2. Apply Crop and save to a NEW file if parameters are present
            val finalPath = try {
                if (cropX != null && cropY != null && cropScale != null) {
                    val cropResult = wallpaperRepository.cropAndSaveWallpaper(
                        WallpaperId(sourcePath), cropX, cropY, cropScale
                    )
                    cropResult.getOrNull() ?: sourcePath
                } else {
                    sourcePath
                }
            } catch (e: Exception) {
                Log.e("SearchVM", "Error cropping wallpaper", e)
                sourcePath
            }

            // 3. Process the final file
            try {
                processDownloadedFile(context, finalPath, target, slot, scaleMode, cropX, cropY, cropScale)
                _uiState.update { it.copy(successMessage = "Wallpaper set successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to apply wallpaper: ${e.message}") }
            }
            
            _uiState.update { it.copy(
                isLoading = false, 
                activeSlot = null, 
                searchResults = emptyList(),
                currentTab = if (it.currentPack != null) 2 else it.currentTab
            ) }
            _searchQuery.value = ""
        }
    }

    private fun processDownloadedFile(
        context: android.content.Context, 
        path: String, 
        target: Int, 
        slot: WallpaperSlot, 
        scaleMode: com.andyl.iris.domain.model.ScaleMode,
        cropX: Float? = null,
        cropY: Float? = null,
        cropScale: Float? = null
    ) {
        val finalScaleMode = if (cropScale != null) com.andyl.iris.domain.model.ScaleMode.FIT else scaleMode
        val finalCropX = if (cropScale != null) null else cropX
        val finalCropY = if (cropScale != null) null else cropY
        val finalCropScale = if (cropScale != null) null else cropScale

        if (slot.fixedTime != null) {
            wallpaperViewModel.onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, slot.fixedTime, path, target))
        } else if (slot.weather != null && slot.time != null) {
            val currentPack = _uiState.value.currentPack
            val isTimeBased = currentPack == SuggestedPack.Time || 
                (currentPack is SuggestedPack.Predefined && PredefinedPacks.packs.find { it.id == currentPack.packId }?.isTimeBased == true)

            if (isTimeBased) {
                Weather.all().forEach { w ->
                    wallpaperViewModel.onEvent(WallpaperEvent.SetWallpaperRule(w, slot.time, path, target, finalScaleMode, finalCropX, finalCropY, finalCropScale))
                }
            } else {
                wallpaperViewModel.onEvent(WallpaperEvent.SetWallpaperRule(slot.weather, slot.time, path, target, finalScaleMode, finalCropX, finalCropY, finalCropScale))
            }
        } else if (slot.dayName != null) {
            wallpaperViewModel.onEvent(WallpaperEvent.SetDailyWallpaper(context, slot.dayName, path, target))
        }
    }
}

sealed class SuggestedPack(val id: String, val name: String, val description: String) {
    object Days : SuggestedPack("days", "Weekly Calendar", "Configure your week with a photo for each day.")
    object Weather : SuggestedPack("weather", "Weather Conditions", "Set unique photos for Sun, Rain, Clouds and more.")
    object Time : SuggestedPack("time", "Daily Cycle", "Customize your background based on the time of day.")
    
    data class Predefined(val packId: String, val packName: String, val packDescription: String) : 
        SuggestedPack(packId, packName, packDescription)
}
