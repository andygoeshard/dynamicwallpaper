package com.andyl.iris.ui.searchscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.data.imagesprovider.dto.UnsplashImage
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.usecase.contract.InstallPredefinedPackUseCase
import com.andyl.iris.domain.usecase.impl.DownloadWallpaperUseCase
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val remoteDataSource: UnsplashRemoteDataSource,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val installPredefinedPackUseCase: InstallPredefinedPackUseCase,
    val wallpaperViewModel: DynamicWallpaperViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val searchCache = mutableMapOf<String, List<UnsplashImage>>()
    private val packPreviewCache = mutableMapOf<String, List<String>>()

    init {
        setupSearchDebounce()
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(700)
                .filter { it.isNotBlank() && it.length > 2 }
                .distinctUntilChanged()
                .collect { query ->
                    if (searchCache.containsKey(query)) {
                        _uiState.update { it.copy(searchResults = searchCache[query]!!, isLoading = false) }
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    // 1. Navegación entre paquetes
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
            val baseQuery = if (predefined.isFullRandom) "" else predefined.categoryQuery
            val usedIds = mutableSetOf<String>()
            
            val previewQueries = when {
                predefined.type == PackType.WEEKLY -> {
                    listOf("monday", "wednesday", "friday", "sunday").map { if (baseQuery.isEmpty()) it else "$baseQuery $it" }
                }
                predefined.isTimeBased -> {
                    TimeOfDay.entries.map { if (baseQuery.isEmpty()) it.queryTerm else "$baseQuery ${it.queryTerm}" }
                }
                else -> {
                    listOf(
                        Weather.Clear to TimeOfDay.DAY,
                        Weather.Rain to TimeOfDay.NIGHT,
                        Weather.Snow to TimeOfDay.DAY,
                        Weather.Cloudy to TimeOfDay.DUSK
                    ).map { (w, t) -> 
                        if (baseQuery.isEmpty()) "${w.queryTerm} ${t.queryTerm}" 
                        else "$baseQuery ${w.queryTerm} ${t.queryTerm}" 
                    }
                }
            }

            val urls = previewQueries.map { query ->
                async {
                    val images = remoteDataSource.getRandomPhotos(query, count = 3).getOrNull() ?: emptyList()
                    val unique = images.find { it.id !in usedIds } ?: images.firstOrNull()
                    unique?.let { usedIds.add(it.id) }
                    unique?.urls?.small
                }
            }.awaitAll().filterNotNull().map { "$it&ar=9:16&fit=crop" }

            if (urls.isNotEmpty()) {
                packPreviewCache[packId] = urls
                _uiState.update { it.copy(previewImages = urls) }
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
            
            // Map fixed times to TimeOfDay terms if possible to keep consistency
            val fixedTimeTerm = when (slot.fixedTime) {
                "06:00" -> TimeOfDay.DAWN.queryTerm
                "09:00", "10:00" -> TimeOfDay.DAY.queryTerm
                "18:00" -> TimeOfDay.DUSK.queryTerm
                "21:00", "22:00" -> TimeOfDay.NIGHT.queryTerm
                else -> ""
            }

            val finalQuery = listOf(baseQuery, weatherTerm, timeTerm, dayTerm, fixedTimeTerm)
                .filter { it.isNotEmpty() }
                .joinToString(" ")
                .trim()

            _searchQuery.value = finalQuery.ifEmpty {
                slot.label
                    .replace("Override: ", "")
                    .replace("Time: ", "")
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

    fun installPack(suggestedPack: SuggestedPack, onSuccess: () -> Unit) {
        val predefinedPack = PredefinedPacks.packs.find { it.id == suggestedPack.id } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            installPredefinedPackUseCase(predefinedPack)
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

    // 2. Búsqueda en API (Internal)
    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        remoteDataSource.searchPhotos(query)
            .onSuccess { res -> 
                searchCache[query] = res.results
                _uiState.update { it.copy(searchResults = res.results, isLoading = false) } 
            }
            .onFailure { e -> 
                _uiState.update { it.copy(error = e.message, isLoading = false) } 
            }
    }

    // 3. LA POSTA: Descarga e Impacto en el Pack
    fun confirmAndDownload(context: android.content.Context, image: UnsplashImage, target: Int) {
        val slot = _uiState.value.activeSlot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val file = downloadUseCase.execute(
                url = image.urls.full,
                fileName = "iris_${System.currentTimeMillis()}"
            )

            if (file != null) {
                if (slot.fixedTime != null) {
                    wallpaperViewModel.onEvent(
                        WallpaperEvent.SetFixedTimeWallpaper(
                            context = context,
                            time = slot.fixedTime,
                            uri = file.absolutePath,
                            target = target
                        )
                    )
                } else if (slot.weather != null && slot.time != null) {
                    val currentPack = _uiState.value.currentPack
                    val isTimeBased = currentPack == SuggestedPack.Time || 
                        (currentPack is SuggestedPack.Predefined && 
                         PredefinedPacks.packs.find { it.id == currentPack.packId }?.isTimeBased == true)

                    if (isTimeBased) {
                        com.andyl.iris.domain.model.Weather.all().forEach { w ->
                            wallpaperViewModel.onEvent(
                                WallpaperEvent.SetWallpaperRule(
                                    weather = w,
                                    timeOfDay = slot.time,
                                    wallpaperUri = file.absolutePath,
                                    target = target
                                )
                            )
                        }
                    } else {
                        wallpaperViewModel.onEvent(
                            WallpaperEvent.SetWallpaperRule(
                                weather = slot.weather,
                                timeOfDay = slot.time,
                                wallpaperUri = file.absolutePath,
                                target = target
                            )
                        )
                    }
                } else if (slot.dayName != null) {
                    wallpaperViewModel.onEvent(
                        WallpaperEvent.SetDailyWallpaper(
                            dayName = slot.dayName,
                            uri = file.absolutePath,
                            target = target
                        )
                    )
                }

                _uiState.update { it.copy(isLoading = false, activeSlot = null, searchResults = emptyList()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al descargar imagen") }
            }
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
