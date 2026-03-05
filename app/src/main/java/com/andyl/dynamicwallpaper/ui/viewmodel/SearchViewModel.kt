package com.andyl.dynamicwallpaper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.dynamicwallpaper.domain.model.CityResult
import com.andyl.dynamicwallpaper.domain.repository.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<CityResult>> = _searchQuery
        .debounce(500) // Evita llamadas excesivas a la API
        .filter { it.length > 2 }
        .mapLatest { query -> repository.searchCity(query) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}