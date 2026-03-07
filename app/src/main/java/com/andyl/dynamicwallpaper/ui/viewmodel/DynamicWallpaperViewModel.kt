package com.andyl.dynamicwallpaper.ui.viewmodel

import android.R.attr.action
import android.R.attr.data
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.dynamicwallpaper.domain.helper.AlarmHelper
import com.andyl.dynamicwallpaper.domain.mapper.toKey
import com.andyl.dynamicwallpaper.domain.mapper.weatherFromKey
import com.andyl.dynamicwallpaper.domain.model.CityResult
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.WallpaperConfig
import com.andyl.dynamicwallpaper.domain.model.WallpaperId
import com.andyl.dynamicwallpaper.domain.model.WallpaperRule
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.domain.repository.LocationRepository
import com.andyl.dynamicwallpaper.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.ChangeActivePackUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetAllPacksUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.GetWallpaperConfigUseCase
import com.andyl.dynamicwallpaper.domain.usecase.contract.SetWallpaperRuleUseCase
import com.andyl.dynamicwallpaper.ui.state.DynamicWallpaperUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections.emptyList

@OptIn(FlowPreview::class)
class DynamicWallpaperViewModel(
    private val applyDynamicWallpaperUseCase: ApplyDynamicWallpaperUseCase,
    private val setWallpaperRuleUseCase: SetWallpaperRuleUseCase,
    private val getWallpaperConfigUseCase: GetWallpaperConfigUseCase,
    private val changeActivePackUseCase: ChangeActivePackUseCase,
    private val getAllPacksUseCase: GetAllPacksUseCase,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DynamicWallpaperUiState())
    val uiState: StateFlow<DynamicWallpaperUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<CityResult>>(emptyList())
    val searchResults: StateFlow<List<CityResult>> = _searchResults

    init {
        loadInitialConfig()
        setupSearchDebounce()
        loadSavedCityName()
    }

    private fun loadSavedCityName() {
        viewModelScope.launch {
            val name = locationRepository.getSavedCityName() ?: ""
            _searchQuery.value = name
        }
    }

    private fun formatKey(weather: Weather, time: TimeOfDay) = "${weather.toKey()} - $time"
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length > 2 }
                .distinctUntilChanged()
                .collectLatest { query ->
                    val savedName = locationRepository.getSavedCityName()
                    if (query != savedName) {
                        runCatching { locationRepository.searchCity(query) }
                            .onSuccess { _searchResults.value = it }
                            .onFailure { _searchResults.value = emptyList() }
                    }
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length <= 2) _searchResults.value = emptyList()
    }

    fun selectCity(city: CityResult) {
        viewModelScope.launch {
            runCatching {
                locationRepository.saveSelectedCity(city)
                _searchResults.value = emptyList() // Limpiamos la lista para cerrar el buscador
                _searchQuery.value = city.name
                _uiState.update { it.copy(isApplied = true) }
            }.onFailure { e ->
                Log.e("WallpaperVM", "Error al guardar ciudad: ${e.message}")
            }
        }
    }

    fun applyWallpaper() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentEditingId = state.editingPackId // El pack que estamos viendo/editando

            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val configToSave = WallpaperConfig(
                    id = currentEditingId,
                    name = state.packName,
                    rules = state.rules.map { (key, uri) -> parseRuleFromKey(key, uri) },
                    dailyRules = state.dailyRules,
                    fixedTimeRules = state.fixedRules,
                    enabledWeathers = state.enabledWeathers,
                    activePackId = currentEditingId
                )
                setWallpaperRuleUseCase(configToSave)
                changeActivePackUseCase(currentEditingId)
                applyDynamicWallpaperUseCase(currentEditingId)
            }
                .onSuccess {
                    _uiState.update { state ->
                        val updatedPacks = state.availablePacks.map {
                            it.copy(isActive = it.id == currentEditingId)
                        }
                        state.copy(
                            isLoading = false,
                            isApplied = true,
                            activePackId = currentEditingId,
                            availablePacks = updatedPacks
                        )
                    }
                }
                .onFailure { t ->
                    Log.e("WallpaperVM", "Error en apply: ${t.message}", t)
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "valio pinga: ${t.message ?: "Error desconocido"}"
                    ) }
                }
        }
    }

    private fun loadInitialConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val allPacks = getAllPacksUseCase()
            val activeId = allPacks.find { it.isActive }?.id ?: "1"

            runCatching { getWallpaperConfigUseCase(activeId) }
                .onSuccess { config ->
                    val rulesMap = config.rules
                        .filter { it.wallpaperId.value.isNotEmpty() }
                        .associate { formatKey(it.weather, it.timeOfDay) to it.wallpaperId.value }

                    _uiState.update { it.copy(
                        availablePacks = allPacks,
                        rules = rulesMap,
                        packName = config.name,
                        dailyRules = config.dailyRules,
                        fixedRules = config.fixedTimeRules,
                        enabledWeathers = config.enabledWeathers,
                        activePackId = config.activePackId,
                        editingPackId = config.activePackId,
                        isLoading = false
                    ) }
                    Log.d("TEST_DAILY", "Keys recibidas: ${config.dailyRules.entries}")
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    fun changePack(packId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editingPackId = packId) }

            runCatching { getWallpaperConfigUseCase(packId) }
                .onSuccess { config ->
                    val rulesMap = config.rules
                        .filter { it.wallpaperId.value.isNotEmpty() }
                        .associate { formatKey(it.weather, it.timeOfDay) to it.wallpaperId.value }

                    _uiState.update { it.copy(
                        rules = rulesMap,
                        packName = config.name,
                        dailyRules = config.dailyRules,
                        fixedRules = config.fixedTimeRules,
                        enabledWeathers = config.enabledWeathers,
                        isLoading = false,
                        error = null
                    ) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }
    fun renamePack(newName: String) {
        _uiState.update { state ->
            val updatedPacks = state.availablePacks.map {
                if (it.id == state.editingPackId) it.copy(name = newName) else it
            }
            state.copy(
                packName = newName,
                availablePacks = updatedPacks
            )
        }
        saveCurrentConfigToRepo()
    }

    fun toggleWeatherEnabled(weather: Weather) {
        _uiState.update { currentState ->
            val newEnabled = if (currentState.enabledWeathers.contains(weather)) {
                currentState.enabledWeathers - weather
            } else {
                currentState.enabledWeathers + weather
            }
            currentState.copy(enabledWeathers = newEnabled)
        }
        saveCurrentConfigToRepo()
    }
    private fun saveCurrentConfigToRepo() = viewModelScope.launch { // Ahora devuelve el Job
        val state = _uiState.value
        val config = WallpaperConfig(
            id = state.editingPackId,
            name = state.packName,
            rules = state.rules.map { (key, uri) -> parseRuleFromKey(key, uri) },
            dailyRules = state.dailyRules,
            fixedTimeRules = state.fixedRules,
            enabledWeathers = state.enabledWeathers,
            activePackId = state.activePackId
        )
        setWallpaperRuleUseCase(config)
    }
    private fun parseRuleFromKey(key: String, uri: String): WallpaperRule {
        val parts = key.split(" - ") // El formato que usamos en formatKey()
        val weather = weatherFromKey(parts[0])
        val timeOfDay = TimeOfDay.valueOf(parts[1])
        return WallpaperRule(weather, timeOfDay, WallpaperId(uri))
    }

    fun setDailyWallpaper(dayName: String, uri: String) {
        _uiState.update { it.copy(dailyRules = it.dailyRules + (dayName to uri)) }
        Log.d("TEST_DAILY_Wallpaper", "Regla diaria guardada: $dayName -> $uri")
        saveCurrentConfigToRepo()
    }

    fun setFixedTimeWallpaper(context: Context, time: String, uri: String) {
        _uiState.update { currentState ->
            currentState.copy(fixedRules = currentState.fixedRules + (time to uri))
        }

        saveCurrentConfigToRepo()
        AlarmHelper.scheduleFixedTimeAlarm(context, time)

        Log.d("WallpaperVM", "Regla fija guardada y alarma programada para: $time")
    }

    fun setWallpaperRule(weather: Weather, timeOfDay: TimeOfDay, wallpaperUri: String) {
        _uiState.update { currentState ->
            val newRules = currentState.rules.toMutableMap()
            newRules[formatKey(weather, timeOfDay)] = wallpaperUri
            currentState.copy(rules = newRules)
        }
        saveCurrentConfigToRepo()
    }

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

}