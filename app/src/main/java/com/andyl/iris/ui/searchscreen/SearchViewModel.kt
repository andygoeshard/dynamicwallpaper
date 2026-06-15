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

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val imageRepository: ImageRepository,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val installPredefinedPackUseCase: InstallPredefinedPackUseCase,
    val wallpaperViewModel: DynamicWallpaperViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val packPreviewCache = mutableMapOf<String, List<String?>>()

    init {
        setupSearchDebounce()
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
                previewImages = emptyList()
            ) 
        }
        _searchQuery.value = ""

        if (pack is SuggestedPack.Predefined) {
            val cached = packPreviewCache[pack.id]
            if (cached != null) {
                _uiState.update { it.copy(previewImages = cached) }
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
                    listOf("dawn", "day", "dusk", "night")
                        .map { "$baseQuery $it" }
                }
                else -> {
                    Weather.all().map { "$baseQuery ${it.queryTerm}" }
                }
            }

            val urls = queries.map { q ->
                async {
                    val results = imageRepository.getRandomImages(q, count = 1).getOrNull() ?: emptyList()
                    results.firstOrNull()?.let { formatUrl(it, isSmall = true) }
                }
            }.awaitAll()

            if (urls.any { it != null }) {
                packPreviewCache[packId] = urls
                _uiState.update { it.copy(previewImages = urls, isLoading = false) }
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
            val currentPack = _uiState.value.currentPack
            val baseQuery = if (currentPack is SuggestedPack.Predefined) {
                val predefined = PredefinedPacks.packs.find { it.id == currentPack.packId }
                if (predefined?.isFullRandom == true) "" else predefined?.categoryQuery ?: ""
            } else ""

            val weatherTerm = slot.weather?.queryTerm ?: ""
            val timeTerm = slot.time?.queryTerm ?: ""
            val dayTerm = slot.dayName ?: ""
            
            val fixedTimeTerm = when (slot.fixedTime) {
                "06:00" -> TimeOfDay.DAWN.queryTerm
                "10:00" -> TimeOfDay.DAY.queryTerm
                "18:00" -> TimeOfDay.DUSK.queryTerm
                "22:00" -> TimeOfDay.NIGHT.queryTerm
                else -> ""
            }

            val finalQuery = listOf(baseQuery, weatherTerm, timeTerm, dayTerm, fixedTimeTerm)
                .filter { it.isNotEmpty() }
                .joinToString(" ")
                .trim()

            _searchQuery.value = finalQuery.ifEmpty {
                slot.label.replace("Override: ", "").replace("Time: ", "")
            }
        } else {
            _searchQuery.value = ""
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showPackSelectionDialog = false) }
            installPredefinedPackUseCase(predefinedPack, targetId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, currentPack = null) }
                    wallpaperViewModel.onEvent(WallpaperEvent.OnLoadInitialConfig)
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
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

    fun confirmAndDownload(context: android.content.Context, image: ImageResult, target: Int, scaleMode: com.andyl.iris.domain.model.ScaleMode) {
        val slot = _uiState.value.activeSlot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val file = downloadUseCase.execute(
                url = image.urlFull,
                fileName = "iris_${System.currentTimeMillis()}"
            )

            if (file != null) {
                processDownloadedFile(context, file.absolutePath, target, slot, scaleMode)
                // We clear both active slot and search results to trigger the transition back to the slot list
                _uiState.update { it.copy(isLoading = false, activeSlot = null, searchResults = emptyList()) }
                _searchQuery.value = "" // Also clear the query to reset the search bar
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al descargar imagen") }
            }
        }
    }

    private fun processDownloadedFile(context: android.content.Context, path: String, target: Int, slot: WallpaperSlot, scaleMode: com.andyl.iris.domain.model.ScaleMode) {
        if (slot.fixedTime != null) {
            wallpaperViewModel.onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, slot.fixedTime, path, target))
        } else if (slot.weather != null && slot.time != null) {
            val currentPack = _uiState.value.currentPack
            val isTimeBased = currentPack == SuggestedPack.Time || 
                (currentPack is SuggestedPack.Predefined && PredefinedPacks.packs.find { it.id == currentPack.packId }?.isTimeBased == true)

            if (isTimeBased) {
                Weather.all().forEach { w ->
                    wallpaperViewModel.onEvent(WallpaperEvent.SetWallpaperRule(w, slot.time, path, target, scaleMode))
                }
            } else {
                wallpaperViewModel.onEvent(WallpaperEvent.SetWallpaperRule(slot.weather, slot.time, path, target, scaleMode))
            }
        } else if (slot.dayName != null) {
            wallpaperViewModel.onEvent(WallpaperEvent.SetDailyWallpaper(slot.dayName, path, target))
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
