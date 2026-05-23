package com.andyl.iris.ui.searchscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.iris.data.imagesprovider.datasource.UnsplashRemoteDataSource
import com.andyl.iris.data.imagesprovider.dto.UnsplashImage
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.usecase.contract.InstallPredefinedPackUseCase
import com.andyl.iris.domain.usecase.impl.DownloadWallpaperUseCase
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val remoteDataSource: UnsplashRemoteDataSource,
    private val downloadUseCase: DownloadWallpaperUseCase,
    private val installPredefinedPackUseCase: InstallPredefinedPackUseCase,
    val wallpaperViewModel: DynamicWallpaperViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    // 1. Navegación entre paquetes
    fun selectPack(pack: SuggestedPack?) {
        _uiState.update { it.copy(currentPack = pack, activeSlot = null, searchResults = emptyList()) }
    }

    fun selectSlot(slot: WallpaperSlot?) {
        _uiState.update { it.copy(activeSlot = slot) }
        slot?.let { search(it.label) }
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

    // 2. Búsqueda en API
    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            remoteDataSource.searchPhotos(query)
                .onSuccess { res -> _uiState.update { it.copy(searchResults = res.results, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    // 3. LA POSTA: Descarga e Impacto en el Pack
    fun confirmAndDownload(image: UnsplashImage, target: Int) {
        val slot = _uiState.value.activeSlot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Bajamos el file al internal storage
            val file = downloadUseCase.execute(
                url = image.urls.full,
                fileName = "iris_${System.currentTimeMillis()}"
            )

            if (file != null) {
                // Usamos el evento que ya tenés en tu VM principal
                if (slot.weather != null && slot.time != null) {
                    wallpaperViewModel.onEvent(
                        WallpaperEvent.SetWallpaperRule(
                            weather = slot.weather,
                            timeOfDay = slot.time,
                            wallpaperUri = file.absolutePath,
                            target = target
                        )
                    )
                } else if (slot.dayName != null) {
                    wallpaperViewModel.onEvent(
                        WallpaperEvent.SetDailyWallpaper(
                            dayName = slot.dayName,
                            uri = file.absolutePath,
                            target = target
                        )
                    )
                }

                // Limpiamos y volvemos
                _uiState.update { it.copy(isLoading = false, activeSlot = null, searchResults = emptyList()) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al descargar imagen") }
            }
        }
    }
}

sealed class SuggestedPack(val id: String, val name: String, val description: String) {
    object Days : SuggestedPack("days", "Weekly Pack", "A photo for each day of the week")
    object Weather : SuggestedPack("weather", "Weather Pack", "Photos for Sun, Rain, Clouds, etc.")
    object Time : SuggestedPack("time", "Time Pack", "Morning, Afternoon, Night and Dawn")
    
    // Dynamic predefined packs
    data class Predefined(val packId: String, val packName: String, val packDescription: String) : 
        SuggestedPack(packId, packName, packDescription)
}
