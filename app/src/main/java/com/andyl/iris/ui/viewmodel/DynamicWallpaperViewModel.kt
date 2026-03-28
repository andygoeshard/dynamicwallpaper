package com.andyl.iris.ui.viewmodel

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andyl.iris.domain.helper.AlarmHelper
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.mapper.weatherFromKey
import com.andyl.iris.domain.model.CityResult
import com.andyl.iris.domain.model.ScaleMode
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.WallpaperConfig
import com.andyl.iris.domain.model.WallpaperId
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.usecase.contract.AddPackUseCase
import com.andyl.iris.domain.usecase.contract.ApplyDynamicWallpaperUseCase
import com.andyl.iris.domain.usecase.contract.ChangeActivePackUseCase
import com.andyl.iris.domain.usecase.contract.DeletePackUseCase
import com.andyl.iris.domain.usecase.contract.GetAllPacksUseCase
import com.andyl.iris.domain.usecase.contract.GetWallpaperConfigUseCase
import com.andyl.iris.domain.usecase.contract.SetWallpaperRuleUseCase
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
    private val addPackUseCase: AddPackUseCase,
    private val deletePackUseCase: DeletePackUseCase,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DynamicWallpaperUiState())
    val uiState: StateFlow<DynamicWallpaperUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<CityResult>>(emptyList())
    val searchResults: StateFlow<List<CityResult>> = _searchResults

    init {
        Log.d("VM_CHECK", "Hash de la instancia: ${this.hashCode()}")
        viewModelScope.launch {
            delay(100)
            loadInitialConfig()
        }
        setupSearchDebounce()
        loadSavedCityName()
    }

    fun onEvent(event: WallpaperEvent){
        when(event){
            WallpaperEvent.OnApplyWallpaper -> {
                applyWallpaper()
            }

            is WallpaperEvent.OnChangePack -> changePack(event.packId, event.direction)

            WallpaperEvent.OnLoadInitialConfig -> loadInitialConfig()

            is WallpaperEvent.OnRenamePack -> renamePack(event.newName)

            is WallpaperEvent.OnSearchQueryChanged -> onSearchQueryChanged(event.newQuery)

            is WallpaperEvent.OnSelectCity -> selectCity(event.city)

            is WallpaperEvent.OnToggleWeather -> toggleWeatherEnabled(event.weather)

            is WallpaperEvent.RequestExactAlarmPermission -> requestExactAlarmPermission(event.context)

            is WallpaperEvent.SetDailyWallpaper -> setDailyWallpaper(event.dayName, event.uri)

            is WallpaperEvent.SetFixedTimeWallpaper -> setFixedTimeWallpaper(event.context, event.time, event.uri)

            is WallpaperEvent.SetWallpaperRule -> setWallpaperRule(event.weather, event.timeOfDay, event.wallpaperUri, event.target)

            WallpaperEvent.OnAddNewPack -> addNewPack()

            WallpaperEvent.OnToggleWeatherFeature -> toggleWeatherEnabled()

            is WallpaperEvent.OnDeletePack -> deletePack(event.packId)

            is WallpaperEvent.OnSelectFromPackManager -> selectPackFromManager(event.packId)
            is WallpaperEvent.OnDeleteDayRule -> deleteDailyWallpaper(event.dayName)
            is WallpaperEvent.OnDeleteFixedTimeRule -> deleteFixedTimeWallpaper(event.context,event.time)
            is WallpaperEvent.UpdateScaleMode -> updateScaleMode(event.mode)
        }
    }

    private fun updateScaleMode(mode: ScaleMode) {
        _uiState.update { currentState ->
            currentState.copy(scaleMode = mode)
        }
        saveCurrentConfigToRepo()
        Log.d("VM", "Modo de escala universal actualizado a: $mode")
    }

    // Location
    private fun loadSavedCityName() {
        viewModelScope.launch {
            val name = locationRepository.getSavedCityName() ?: ""
            _searchQuery.value = name
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length <= 2) _searchResults.value = emptyList()
    }
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

    private fun selectCity(city: CityResult) {
        viewModelScope.launch {
            runCatching {
                locationRepository.saveSelectedCity(city)
                _searchResults.value = emptyList()
                _searchQuery.value = city.name
                _uiState.update { it.copy(isApplied = true) }
            }.onFailure { e ->
                Log.e("WallpaperVM", "Error al guardar ciudad: ${e.message}")
            }
        }
    }

    // Wallpapers
    private fun applyWallpaper() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentEditingId = state.editingPackId 

            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val configToSave = WallpaperConfig(
                    id = currentEditingId,
                    name = state.packName,
                    rules = state.rules.map { (key, uri) -> parseRuleFromKey(key, uri) },
                    dailyRules = state.dailyRules,
                    fixedTimeRules = state.fixedRules,
                    enabledWeathers = state.enabledWeathers,
                    activePackId = currentEditingId,
                    scaleMode = state.scaleMode,
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

            runCatching {
                val allPacks = getAllPacksUseCase()
                val activeId = allPacks.find { it.isActive }?.id ?: "1"
                val config = getWallpaperConfigUseCase(activeId)

                val rulesMap = config.rules
                    .filter { it.wallpaperId.value.isNotEmpty() }
                    .associate { formatKey(it.weather, it.timeOfDay, it.target) to it.wallpaperId.value }

                Triple(allPacks, config, rulesMap)
            }.onSuccess { (allPacks, config, rulesMap) ->
                _uiState.update { it.copy(
                    availablePacks = allPacks,
                    rules = rulesMap,
                    packName = config.name,
                    dailyRules = config.dailyRules,
                    fixedRules = config.fixedTimeRules,
                    enabledWeathers = config.enabledWeathers,
                    activePackId = config.activePackId,
                    editingPackId = config.activePackId,
                    isLoading = false,
                    scaleMode = config.scaleMode,
                ) }
                Log.d("TEST_DAILY", "Keys recibidas: ${config.dailyRules.entries}")
            }.onFailure { t ->
                Log.e("VM", "Error inicializando: ${t.message}")
                _uiState.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }

    private fun changePack(packId: String, direction: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editingPackId = packId, slideDirection = direction) }

            runCatching { getWallpaperConfigUseCase(packId) }
                .onSuccess { config ->
                    val rulesMap = config.rules
                        .filter { it.wallpaperId.value.isNotEmpty() }
                        .associate { formatKey(it.weather, it.timeOfDay, it.target) to it.wallpaperId.value }

                    _uiState.update { it.copy(
                        rules = rulesMap,
                        packName = config.name,
                        dailyRules = config.dailyRules,
                        fixedRules = config.fixedTimeRules,
                        enabledWeathers = config.enabledWeathers,
                        isLoading = false,
                        error = null,
                        scaleMode = config.scaleMode,
                    ) }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    private fun addNewPack() {
        viewModelScope.launch {
            runCatching {
                    addPackUseCase()
            }.onSuccess { updatedPacks ->
                _uiState.update { it.copy(
                    availablePacks = updatedPacks,
                    editingPackId = updatedPacks.last().id
                )}
                onEvent(WallpaperEvent.OnChangePack(updatedPacks.last().id, 1))
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun deletePack(packId: String) {
        viewModelScope.launch {
            runCatching {
                deletePackUseCase(packId)
                getAllPacksUseCase()
            }.onSuccess { updatedPacks ->
                val nextPackToEdit = if (packId == _uiState.value.editingPackId) {
                    updatedPacks.first().id
                } else {
                    _uiState.value.editingPackId
                }
                _uiState.update { it.copy(availablePacks = updatedPacks) }
                onEvent(WallpaperEvent.OnChangePack(nextPackToEdit, 1))

            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun renamePack(newName: String) {
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

    private fun toggleWeatherEnabled(weather: Weather) {
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

    private fun selectPackFromManager(packId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                getWallpaperConfigUseCase(packId)
            }.onSuccess { config ->
                val rulesMap = config.rules
                    .filter { it.wallpaperId.value.isNotEmpty() }
                    .associate { formatKey(it.weather, it.timeOfDay, it.target) to it.wallpaperId.value }

                _uiState.update { it.copy(
                    rules = rulesMap,
                    packName = config.name,
                    editingPackId = packId,
                    dailyRules = config.dailyRules,
                    fixedRules = config.fixedTimeRules,
                    enabledWeathers = config.enabledWeathers,
                    isLoading = false,
                    scaleMode = config.scaleMode,
                ) }
            }.onFailure { t ->
                _uiState.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }

    private fun saveCurrentConfigToRepo() = viewModelScope.launch {
        val state = _uiState.value
        val config = WallpaperConfig(
            id = state.editingPackId,
            name = state.packName,
            rules = state.rules.map { (key, uri) -> parseRuleFromKey(key, uri) },
            dailyRules = state.dailyRules,
            fixedTimeRules = state.fixedRules,
            enabledWeathers = state.enabledWeathers,
            activePackId = state.activePackId,
            scaleMode = state.scaleMode
        )
        setWallpaperRuleUseCase(config)
    }
    private fun formatKey(weather: Weather, time: TimeOfDay, target: Int) =
        "${weather.toKey()} - $time - $target"

    private fun parseRuleFromKey(key: String, uri: String): WallpaperRule {
        val parts = key.split(" - ")
        val weather = weatherFromKey(parts[0])
        val timeOfDay = TimeOfDay.valueOf(parts[1])
        val target = parts.getOrNull(2)?.toIntOrNull() ?: 3

        return WallpaperRule(
            weather = weather,
            timeOfDay = timeOfDay,
            wallpaperId = WallpaperId(uri),
            target = target
        )
    }

    private fun setDailyWallpaper(key: String, uri: String) {
        _uiState.update { currentState ->
            val newDailyRules = currentState.dailyRules.toMutableMap()

            val dayName = key.split("-")[0]

            if (key.contains("-")) {
                newDailyRules.remove(dayName)
                newDailyRules[key] = uri
            } else {
                newDailyRules.remove("$dayName-1")
                newDailyRules.remove("$dayName-2")
                newDailyRules[dayName] = uri
            }

            currentState.copy(dailyRules = newDailyRules)
        }
        saveCurrentConfigToRepo()
    }

    private fun setFixedTimeWallpaper(context: Context, timeKey: String, uri: String) {
        _uiState.update { currentState ->
            val newFixedRules = currentState.fixedRules.toMutableMap()
            val timeBase = timeKey.split("-")[0]

            if (timeKey.contains("-")) {
                newFixedRules.remove(timeBase)
            } else {
                newFixedRules.remove("$timeBase-1")
                newFixedRules.remove("$timeBase-2")
            }

            newFixedRules[timeKey] = uri
            currentState.copy(fixedRules = newFixedRules)
        }

        saveCurrentConfigToRepo()

        val timeForAlarm = timeKey.split("-")[0]
        AlarmHelper.scheduleFixedTimeAlarm(context, timeForAlarm)
    }

    private fun deleteFixedTimeWallpaper(context: Context, timeKey: String) {
        _uiState.update { currentState ->
            val newFixedRules = currentState.fixedRules.toMutableMap()
            newFixedRules.remove(timeKey)
            currentState.copy(fixedRules = newFixedRules)
        }

        saveCurrentConfigToRepo()

        val timeForAlarm = timeKey.split("-")[0]
        AlarmHelper.cancelFixedTimeAlarm(context, timeForAlarm)
    }

    private fun deleteDailyWallpaper(dayName: String) {
        _uiState.update { currentState ->
            val newDailyRules = currentState.dailyRules.toMutableMap()
            newDailyRules.remove(dayName)
            currentState.copy(dailyRules = newDailyRules)
        }
        saveCurrentConfigToRepo()
    }

    fun setWallpaperRule(weather: Weather, timeOfDay: TimeOfDay, wallpaperUri: String, target: Int = 3) {
        _uiState.update { currentState ->
            val newRules = currentState.rules.toMutableMap()
            if (target == 3) {
                newRules.remove(formatKey(weather, timeOfDay, 1))
                newRules.remove(formatKey(weather, timeOfDay, 2))
            } else {
                newRules.remove(formatKey(weather, timeOfDay, 3))
            }

            newRules[formatKey(weather, timeOfDay, target)] = wallpaperUri
            currentState.copy(rules = newRules)
        }
        saveCurrentConfigToRepo()
    }

    private fun requestExactAlarmPermission(context: Context) {
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

    private fun toggleWeatherEnabled(){
        _uiState.update { curretState ->
            curretState.copy(isWeatherFeatureEnabled = !curretState.isWeatherFeatureEnabled)
        }
    }

//    private fun syncIfActive() {
//        val state = _uiState.value
//        if (state.editingPackId == state.activePackId) {
//            viewModelScope.launch {
//                applyDynamicWallpaperUseCase(state.activePackId)
//            }
//        }
//    }
}