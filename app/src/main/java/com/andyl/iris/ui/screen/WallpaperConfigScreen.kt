package com.andyl.iris.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.andyl.iris.R
import kotlinx.coroutines.launch
import com.andyl.iris.billing.BillingManager
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.mapper.weatherFromKey
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.WallpaperHistoryEntry
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.ui.components.AppWallpaperBackground
import com.andyl.iris.ui.components.EmptyState
import com.andyl.iris.ui.components.PremiumUpsellSheet
import com.andyl.iris.ui.components.RatingDialog
import com.andyl.iris.ui.components.ScaleModeSelector
import com.andyl.iris.ui.components.WallpaperPreviewImage
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState
import com.andyl.iris.ui.theme.AccentOptions
import com.andyl.iris.ui.theme.CyberCyan
import com.andyl.iris.ui.theme.CyberCyanLight
import com.andyl.iris.ui.theme.CyberGreen
import com.andyl.iris.ui.theme.CyberGreenLight
import com.andyl.iris.ui.theme.LocalReduceAnimations
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperConfigScreen(
    viewModel: DynamicWallpaperViewModel,
    onBack: () -> Unit,
    onOpenStats: () -> Unit = {},
    onOpenWeather: () -> Unit = {},
    onOpenAchievements: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = koinInject<com.andyl.iris.ui.sound.SoundManager>()
    val premiumRepository = koinInject<PremiumRepository>()
    val userPreferencesRepository = koinInject<UserPreferencesRepository>()
    val locationRepository = koinInject<LocationRepository>()
    val isPremium by premiumRepository.observePremiumStatus().collectAsState(initial = premiumRepository.isPremium())
    var showUpsellSheet by remember { mutableStateOf(false) }

    var overlayText by remember { mutableStateOf("") }
    var overlayEnabled by remember { mutableStateOf(false) }
    var batterySaverEnabled by remember { mutableStateOf(false) }
    var galleryBucketId by remember { mutableStateOf<String?>(null) }
    var galleryAlbums by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showAlbumPicker by remember { mutableStateOf(false) }
    var places by remember { mutableStateOf<List<com.andyl.iris.domain.model.GeoPlace>>(emptyList()) }
    var placeBusy by remember { mutableStateOf(false) }
    var showPlaceDialog by remember { mutableStateOf(false) }
    var placeDialogRadiusKm by remember { mutableFloatStateOf(2f) }
    var placeDialogInvert by remember { mutableStateOf(false) }
    var placeEditingId by remember { mutableStateOf<String?>(null) }
    var pendingPlaceLat by remember { mutableStateOf(0.0) }
    var pendingPlaceLon by remember { mutableStateOf(0.0) }
    var packs by remember { mutableStateOf<List<com.andyl.iris.domain.model.PackInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        overlayText = userPreferencesRepository.getWallpaperOverlayText() ?: ""
        overlayEnabled = userPreferencesRepository.getOverlayTextEnabled()
        batterySaverEnabled = userPreferencesRepository.getBatterySaverEnabled()
        galleryBucketId = userPreferencesRepository.getRandomGalleryBucketId()
        places = userPreferencesRepository.getPlaces()
        packs = userPreferencesRepository.getAllPacks()
    }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(WallpaperEvent.ClearMessages)
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(WallpaperEvent.ClearMessages)
        }
    }

    LaunchedEffect(uiState.pendingUndoUri) {
        uiState.pendingUndoUri?.let {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.undo_message),
                actionLabel = context.getString(R.string.undo_action),
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(WallpaperEvent.OnUndoWallpaper)
            } else {
                viewModel.onEvent(WallpaperEvent.OnClearPendingUndo)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.onEvent(WallpaperEvent.OnManualRefresh)
        }
    }

    if (uiState.showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.onEvent(WallpaperEvent.OnDismissRatingDialog) },
            onRate = { stars -> viewModel.onEvent(WallpaperEvent.OnRateApp(stars)) }
        )
    }

    AppWallpaperBackground(
        wallpaperUri = uiState.lastAppliedWallpaper,
        enabled = uiState.useWallpaperBackground
    ) {
    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val wallpaperBg = uiState.useWallpaperBackground
            TopAppBar(
                title = { Text(stringResource(R.string.cfg_screen_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (wallpaperBg) Color.Transparent else MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back)
                    )
                },
                actions = {
                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = onOpenWeather,
                        icon = Icons.Default.WbSunny,
                        contentDescription = stringResource(R.string.weather_forecast_title)
                    )
                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = onOpenStats,
                        icon = Icons.Default.BarChart,
                        contentDescription = stringResource(R.string.stats_title)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION: LIVE STATUS ---
            item {
                StatusCard(
                    weather = uiState.currentWeather,
                    lastUpdate = uiState.lastUpdateTime,
                    nextUpdate = uiState.nextUpdateTime,
                    isLoading = uiState.isLoading,
                    onRefresh = {
                        if (uiState.hapticsEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        if (uiState.soundEnabled) {
                            soundManager.playClick()
                        }
                        viewModel.onEvent(WallpaperEvent.OnManualRefresh)
                    }
                )
            }

            // --- SECTION: QUICK SETTINGS ---
            item {
                ConfigSection(title = stringResource(R.string.cfg_quick_settings_title)) {
                    QuickSettingsCard(
                        useWallpaperBackground = uiState.useWallpaperBackground,
                        reduceAnimations = uiState.reduceAnimations,
                        hapticsEnabled = uiState.hapticsEnabled,
                        soundEnabled = uiState.soundEnabled,
                        onToggleWallpaperBackground = {
                            viewModel.onEvent(WallpaperEvent.OnToggleWallpaperBackground(it))
                        },
                        onToggleReduceAnimations = {
                            viewModel.onEvent(WallpaperEvent.OnToggleReduceAnimations(it))
                        },
                        onToggleHaptics = {
                            viewModel.onEvent(WallpaperEvent.OnToggleHaptics(it))
                        },
                        onToggleSound = {
                            viewModel.onEvent(WallpaperEvent.OnToggleSound(it))
                        }
                    )
                }
            }

            // --- SECTION: IMAGE ADJUSTMENT ---
            item {
                ConfigSection(
                    title = stringResource(R.string.cfg_screen_img_settings_title),
                    description = stringResource(R.string.cfg_screen_img_settings_text)
                ) {
                    ScaleModeSelector(
                        selectedMode = uiState.scaleMode,
                        onModeSelected = { newMode ->
                            viewModel.onEvent(WallpaperEvent.UpdateScaleMode(mode = newMode))
                        }
                    )
                }
            }

            // --- SECTION: WALLPAPER TEXT OVERLAY ---
            item {
                ExpandableSection(
                    icon = Icons.Default.TextFields,
                    title = stringResource(R.string.overlay_title),
                    description = stringResource(R.string.overlay_desc)
                ) {
                    SettingToggleRow(
                        icon = Icons.Default.Title,
                        title = stringResource(R.string.overlay_enable),
                        description = stringResource(R.string.overlay_enable_desc),
                        checked = overlayEnabled,
                        onCheckedChange = { enabled ->
                            overlayEnabled = enabled
                            scope.launch { userPreferencesRepository.setOverlayTextEnabled(enabled) }
                        }
                    )

                    OutlinedTextField(
                        value = overlayText,
                        onValueChange = { newText ->
                            overlayText = newText
                            scope.launch { userPreferencesRepository.setWallpaperOverlayText(newText) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.overlay_text_label)) },
                        singleLine = true,
                        enabled = overlayEnabled
                    )

                    Text(
                        text = stringResource(R.string.overlay_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- SECTION: GALLERY RANDOM SOURCE ---
            item {
                ConfigSection(
                    title = stringResource(R.string.gallery_random_title),
                    description = stringResource(R.string.gallery_random_desc)
                ) {
                    val albumName = galleryAlbums.find { it.first == galleryBucketId }?.second
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhotoAlbum,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = albumName ?: stringResource(R.string.gallery_random_all),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.gallery_random_current),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryAlbums = queryGalleryAlbums(context)
                                showAlbumPicker = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.gallery_random_choose))
                        }
                        if (galleryBucketId != null) {
                            OutlinedButton(
                                onClick = {
                                    galleryBucketId = null
                                    scope.launch { userPreferencesRepository.setRandomGalleryBucketId(null) }
                                }
                            ) {
                                Text(stringResource(R.string.gallery_random_reset))
                            }
                        }
                    }
                }
            }

            // --- SECTION: APPEARANCE ---
            item {
                ExpandableSection(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.cfg_theme_title),
                    description = stringResource(R.string.cfg_theme_desc)
                ) {
                    val isDarkMode = uiState.darkModePref ?: isSystemInDarkTheme()
                    ThemePreview(
                        darkTheme = isDarkMode,
                        amoled = uiState.amoledMode,
                        accentColor = uiState.accentColor
                    )

                    ThemeModeSelector(
                        selected = uiState.darkModePref,
                        onSelect = { viewModel.onEvent(WallpaperEvent.OnSetDarkModePref(it)) }
                    )

                    SettingToggleRow(
                        icon = Icons.Default.BrightnessHigh,
                        title = stringResource(R.string.cfg_theme_amoled),
                        description = stringResource(R.string.cfg_theme_amoled_desc),
                        checked = uiState.amoledMode,
                        onCheckedChange = { viewModel.onEvent(WallpaperEvent.OnToggleAmoledMode(it)) }
                    )

                    Text(
                        text = stringResource(R.string.cfg_theme_accent),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AccentColorSelector(
                        selected = uiState.accentColor,
                        onSelect = { viewModel.onEvent(WallpaperEvent.OnChangeAccentColor(it)) }
                    )
                }
            }

            // --- SECTION: LIVE PREVIEW ---
            item {
                ExpandableSection(
                    icon = Icons.Default.PhoneAndroid,
                    title = stringResource(R.string.live_preview_title),
                    description = stringResource(R.string.live_preview_subtitle)
                ) {
                    LivePreviewCard(
                        state = uiState,
                        overlayText = overlayText,
                        overlayEnabled = overlayEnabled
                    )
                }
            }

            // --- SECTION: UPCOMING CHANGES ---
            item {
                ExpandableSection(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.timeline_title)
                ) {
                    UpcomingChangesCard(state = uiState)
                }
            }

            // --- SECTION: WEATHER SIMULATOR ---
            item {
                ExpandableSection(
                    icon = Icons.Default.Cloud,
                    title = stringResource(R.string.weather_simulator_title),
                    description = stringResource(R.string.weather_simulator_desc)
                ) {
                    WeatherSimulatorSection(
                        state = uiState,
                        isPremium = isPremium,
                        onUpsellClick = { showUpsellSheet = true }
                    )
                }
            }

            // --- SECTION: PACK OVERVIEW GRID ---
            item {
                ExpandableSection(
                    icon = Icons.Default.GridView,
                    title = stringResource(R.string.pack_preview_title),
                    description = stringResource(R.string.pack_preview_desc)
                ) {
                    PackPreviewGrid(
                        state = uiState,
                        isPremium = isPremium,
                        onUpsellClick = { showUpsellSheet = true }
                    )
                }
            }

            // --- SECTION: WALLPAPER HISTORY ---
            item {
                ExpandableSection(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.history_title),
                    description = stringResource(R.string.history_desc)
                ) {
                    WallpaperHistorySection(
                        history = uiState.wallpaperHistory,
                        onRevert = { uri ->
                            if (uiState.hapticsEnabled) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            if (uiState.soundEnabled) {
                                soundManager.playConfirm()
                            }
                            viewModel.onEvent(WallpaperEvent.OnRevertWallpaper(uri))
                        },
                        onClear = { viewModel.onEvent(WallpaperEvent.OnClearWallpaperHistory) }
                    )
                }
            }

            // --- SECTION: ACHIEVEMENTS ---
            item {
                Card(
                    onClick = {
                        if (uiState.hapticsEnabled) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        if (uiState.soundEnabled) {
                            soundManager.playClick()
                        }
                        onOpenAchievements()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.ach_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.ach_section_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- SECTION: GEOFENCES ---
            item {
                ExpandableSection(
                    icon = Icons.Default.Place,
                    title = stringResource(R.string.places_title),
                    description = stringResource(R.string.places_desc)
                ) {
                    if (places.isEmpty()) {
                        Text(
                            text = stringResource(R.string.places_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    places.forEach { place ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(
                                        R.string.places_meta,
                                        packs.find { it.id == place.packId }?.name ?: (place.packId ?: "-"),
                                        (place.radiusMeters / 1000.0).toInt()
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (place.invert) {
                                    Text(
                                        text = stringResource(R.string.places_inverted),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(onClick = {
                                placeEditingId = place.id
                                placeDialogRadiusKm = (place.radiusMeters / 1000.0).toFloat()
                                placeDialogInvert = place.invert
                                pendingPlaceLat = place.latitude
                                pendingPlaceLon = place.longitude
                                showPlaceDialog = true
                            }) {
                                Icon(Icons.Default.Edit, stringResource(R.string.btn_edit), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                places = places.filterNot { it.id == place.id }
                                scope.launch { userPreferencesRepository.setPlaces(places) }
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }

                    Button(
                        onClick = {
                            if (!placeBusy) {
                                placeBusy = true
                                val errorMsg = context.getString(R.string.places_location_error)
                                scope.launch {
                                    runCatching {
                                        locationRepository.getCurrentLocation()
                                    }.onSuccess { loc ->
                                        placeEditingId = null
                                        placeDialogRadiusKm = 2f
                                        placeDialogInvert = false
                                        pendingPlaceLat = loc.latitude
                                        pendingPlaceLon = loc.longitude
                                        showPlaceDialog = true
                                    }.onFailure {
                                        snackbarHostState.showSnackbar(errorMsg)
                                    }
                                    placeBusy = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !placeBusy
                    ) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.places_add), fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.places_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- SECTION: LOCATION SETTINGS ---
            item {
                ExpandableSection(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.cfg_screen_city_settings_title)
                ) {
                    LocationSettings(
                        useGps = uiState.useGps,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        isSearching = uiState.isSearchingCity,
                        isLoading = uiState.isLoading,
                        onToggleGps = { viewModel.onEvent(WallpaperEvent.OnToggleGps(it)) },
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onSelectCity = { viewModel.onEvent(WallpaperEvent.OnSelectCity(it)) },
                        onRequestGps = {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    )
                }
            }

            // --- SECTION: BATTERY OPTIMIZATION ---
            item {
                ConfigSection(
                    title = stringResource(R.string.cfg_battery_title),
                    description = stringResource(R.string.cfg_battery_desc)
                ) {
                    SettingToggleRow(
                        icon = Icons.Default.BatterySaver,
                        title = stringResource(R.string.cfg_battery_saver_title),
                        description = stringResource(R.string.cfg_battery_saver_desc),
                        checked = batterySaverEnabled,
                        onCheckedChange = { enabled ->
                            batterySaverEnabled = enabled
                            scope.launch { userPreferencesRepository.setBatterySaverEnabled(enabled) }
                        }
                    )
                    Button(
                        onClick = { openBatterySettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Settings, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.cfg_battery_button))
                    }
                }
            }

            // --- SECTION: IRIS PRO ---
            item {
                ConfigSection(
                    title = stringResource(R.string.premium_title),
                    description = stringResource(R.string.premium_subtitle)
                ) {
                    if (isPremium) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.pro_active_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.pro_active_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showUpsellSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.premium_unlock),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- SECTION: ABOUT ---
            item {
                ConfigSection(title = stringResource(R.string.cfg_about_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cfg_about_version)) },
                            leadingContent = { Icon(Icons.Default.Info, null) }
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cfg_about_feedback)) },
                            leadingContent = { Icon(Icons.Default.Email, null) },
                            modifier = Modifier.clickable {
                                viewModel.onEvent(WallpaperEvent.OnFeedbackClicked)
                            }
                        )
                        
                        // We need a side effect to open email if VM says so
                        LaunchedEffect(uiState.successMessage) {
                            if (uiState.successMessage == "Opening feedback email...") {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:andreslumty@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Iris Feedback")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_no_email_app)) }
                                }
                                viewModel.onEvent(WallpaperEvent.ClearMessages)
                            }
                        }
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cfg_about_privacy)) },
                            leadingContent = { Icon(Icons.Default.Lock, null) },
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/15OiIEyzMrf3s96Ias2Q-ACLhlITbEoAieeEoSyozeqM/edit?usp=sharing"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_no_browser)) }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showUpsellSheet) {
            val billingManager = koinInject<BillingManager>()
            PremiumUpsellSheet(
                billingManager = billingManager,
                onDismiss = { showUpsellSheet = false }
            )
        }

        if (showAlbumPicker) {
            AlertDialog(
                onDismissRequest = { showAlbumPicker = false },
                title = { Text(stringResource(R.string.gallery_random_picker_title)) },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        item {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.gallery_random_all)) },
                                leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                                modifier = Modifier.clickable {
                                    galleryBucketId = null
                                    scope.launch { userPreferencesRepository.setRandomGalleryBucketId(null) }
                                    showAlbumPicker = false
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                        items(galleryAlbums) { (id, name) ->
                            ListItem(
                                headlineContent = { Text(name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                leadingContent = { Icon(Icons.Default.PhotoAlbum, null) },
                                modifier = Modifier.clickable {
                                    galleryBucketId = id
                                    scope.launch { userPreferencesRepository.setRandomGalleryBucketId(id) }
                                    showAlbumPicker = false
                                }
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAlbumPicker = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        if (showPlaceDialog) {
            AlertDialog(
                onDismissRequest = { showPlaceDialog = false },
                title = {
                    Text(
                        text = stringResource(
                            if (placeEditingId == null) R.string.places_dialog_add else R.string.places_dialog_edit
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.places_radius_label),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "%.1f km".format(placeDialogRadiusKm),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = placeDialogRadiusKm,
                            onValueChange = { placeDialogRadiusKm = it },
                            valueRange = 0.5f..20f,
                            steps = 38
                        )
                        SettingToggleRow(
                            icon = Icons.Default.SwapVert,
                            title = stringResource(R.string.places_invert_title),
                            description = stringResource(R.string.places_invert_desc),
                            checked = placeDialogInvert,
                            onCheckedChange = { placeDialogInvert = it }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val radiusMeters = (placeDialogRadiusKm * 1000).toDouble()
                            val editingId = placeEditingId
                            showPlaceDialog = false
                            scope.launch {
                                val activeId = userPreferencesRepository.getActivePackId()
                                val updated = if (editingId != null) {
                                    places.map {
                                        if (it.id == editingId) it.copy(
                                            radiusMeters = radiusMeters,
                                            invert = placeDialogInvert
                                        ) else it
                                    }
                                } else {
                                    val defaultName = context.getString(R.string.places_default_name, places.size + 1)
                                    places + com.andyl.iris.domain.model.GeoPlace(
                                        id = System.currentTimeMillis().toString(),
                                        name = defaultName,
                                        latitude = pendingPlaceLat,
                                        longitude = pendingPlaceLon,
                                        radiusMeters = radiusMeters,
                                        packId = activeId,
                                        invert = placeDialogInvert
                                    )
                                }
                                places = updated
                                userPreferencesRepository.setPlaces(updated)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPlaceDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }
    }
}

private fun queryGalleryAlbums(context: Context): List<Pair<String, String>> {
    val map = LinkedHashMap<String, String>()
    val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )
    runCatching {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idCol)
                val name = cursor.getString(nameCol)
                if (!id.isNullOrBlank() && !name.isNullOrBlank()) map[id] = name
            }
        }
    }
    return map.entries
        .map { it.key to it.value }
        .sortedBy { it.second.lowercase() }
}

@Composable
fun LivePreviewCard(
    state: DynamicWallpaperUiState,
    overlayText: String?,
    overlayEnabled: Boolean
) {
    val uri = remember(state.rules, state.currentWeather, state.lastAppliedWallpaper) {
        val tod = timeOfDayFor(LocalTime.now())
        previewFor(state, state.currentWeather, tod).first ?: state.lastAppliedWallpaper
    }
    val weather = state.currentWeather
    val weatherLabel = weather?.let { stringResource(it.stringRes) }
    val nowLabel = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(400.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                AnimatedContent(
                    targetState = uri,
                    transitionSpec = {
                        (fadeIn(tween(450)) togetherWith fadeOut(tween(450)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "livePreview"
                ) { currentUri ->
                    WallpaperPreviewImage(
                        uri = currentUri,
                        scaleMode = state.scaleMode,
                        overlayText = overlayText,
                        overlayEnabled = overlayEnabled,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .width(80.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (weather != null) {
                                Text(
                                    text = weatherEmoji(weather),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = weatherLabel ?: stringResource(R.string.cfg_status_weather_unknown),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = nowLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.live_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.live_preview_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun timeOfDayFor(time: LocalTime): TimeOfDay = when {
    time.isBefore(LocalTime.of(5, 0)) || !time.isBefore(LocalTime.of(21, 0)) -> TimeOfDay.NIGHT
    time.isBefore(LocalTime.of(8, 0)) -> TimeOfDay.DAWN
    time.isBefore(LocalTime.of(17, 0)) -> TimeOfDay.DAY
    else -> TimeOfDay.DUSK
}

@Composable
fun StatusCard(
    weather: Weather?,
    lastUpdate: String,
    nextUpdate: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        val reduceAnimations = LocalReduceAnimations.current
        val pulseTransition = rememberInfiniteTransition(label = "statusPulse")
        val pulseAlpha by pulseTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "pulseAlpha"
        )
        val emojiAlpha = if (isLoading && !reduceAnimations) pulseAlpha else 1f

        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.live_status),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        weather?.let {
                            Text(
                                text = when(it) {
                                    Weather.Clear -> "☀️"
                                    Weather.Rain -> "🌧️"
                                    Weather.Storm -> "⛈️"
                                    Weather.Snow -> "❄️"
                                    Weather.Fog -> "🌫️"
                                    else -> "☁️"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.alpha(emojiAlpha)
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            text = if (weather != null) stringResource(weather.stringRes) else stringResource(R.string.cfg_status_weather_unknown),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                } else {
                    IconButton(
                        onClick = onRefresh,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusInfoChip(
                    icon = Icons.Default.Info,
                    text = stringResource(R.string.cfg_status_last_update, lastUpdate),
                    modifier = Modifier.weight(1f)
                )
                StatusInfoChip(
                    icon = Icons.Default.Refresh,
                    text = stringResource(R.string.cfg_status_next_update, nextUpdate),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusInfoChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

private data class TimelineRow(
    val order: Long,
    val timeLabel: String,
    val titleRes: Int
)

private val dayNames = listOf(
    "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
)

private fun buildUpcomingChanges(
    state: DynamicWallpaperUiState,
    now: LocalDateTime = LocalDateTime.now()
): List<TimelineRow> {
    val rows = mutableListOf<TimelineRow>()
    val today = now.toLocalDate()
    val timeFormatter = DateTimeFormatter.ofPattern("EEE HH:mm", Locale.getDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.getDefault())

    state.fixedRules.keys.mapNotNull { it.split("-").firstOrNull() }.distinct().forEach { base ->
        val parts = base.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@forEach
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@forEach
        var dt = LocalDateTime.of(today, LocalTime.of(hour, minute))
        if (!dt.isAfter(now)) dt = dt.plusDays(1)
        rows += TimelineRow(
            order = dt.toEpochSecond(ZoneOffset.UTC),
            timeLabel = dt.format(timeFormatter),
            titleRes = R.string.timeline_fixed_time
        )
    }

    state.dailyRules.keys.mapNotNull { it.split("-").firstOrNull() }.distinct().forEach { day ->
        val dayIndex = dayNames.indexOf(day)
        if (dayIndex < 0) return@forEach
        val ahead = (dayIndex - today.dayOfWeek.value + 7) % 7
        val dt = today.plusDays(ahead.toLong())
        rows += TimelineRow(
            order = dt.atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
            timeLabel = dt.format(dateFormatter),
            titleRes = R.string.timeline_daily
        )
    }

    val next = state.nextUpdateTime
    if (next.isNotBlank() && next != "Never" && next != "Pending") {
        rows += TimelineRow(Long.MAX_VALUE, next, R.string.timeline_next_update)
    }

    return rows.sortedBy { it.order }
}

@Composable
fun UpcomingChangesCard(state: DynamicWallpaperUiState) {
    val rows = remember(state.fixedRules, state.dailyRules, state.nextUpdateTime) {
        buildUpcomingChanges(state)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.timeline_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))

            if (rows.isEmpty()) {
                EmptyState(
                    emoji = "🕐",
                    message = stringResource(R.string.timeline_empty)
                )
            } else {
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.timeLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = stringResource(row.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

private fun previewFor(
    state: DynamicWallpaperUiState,
    weather: Weather?,
    time: TimeOfDay?
): Pair<String?, Int> {
    if (weather == null || time == null) return null to 0
    val both = state.rules["${weather.toKey()} - $time - 3"]
    if (both?.wallpaperId?.value?.isNotEmpty() == true) return both.wallpaperId.value to 3
    val home = state.rules["${weather.toKey()} - $time - 1"]
    if (home?.wallpaperId?.value?.isNotEmpty() == true) return home.wallpaperId.value to 1
    val lock = state.rules["${weather.toKey()} - $time - 2"]
    if (lock?.wallpaperId?.value?.isNotEmpty() == true) return lock.wallpaperId.value to 2
    return null to 0
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherSimulatorSection(
    state: DynamicWallpaperUiState,
    isPremium: Boolean,
    onUpsellClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.weather_simulator_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!isPremium) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "PRO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Text(
            text = stringResource(R.string.weather_simulator_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isPremium) {
            ProLockedCard(
                title = stringResource(R.string.weather_simulator_pro_title),
                description = stringResource(R.string.weather_simulator_pro_desc),
                onUpsellClick = onUpsellClick
            )
            return@Column
        }

        var selectedWeather by remember { mutableStateOf<Weather?>(null) }
        var selectedTime by remember { mutableStateOf<TimeOfDay?>(null) }

        Text(
            text = stringResource(R.string.weather_simulator_weather),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Weather.all().forEach { weather ->
                SelectorChip(
                    label = "${weatherEmoji(weather)} ${stringResource(weather.stringRes)}",
                    selected = selectedWeather == weather,
                    onClick = { selectedWeather = weather }
                )
            }
        }

        Text(
            text = stringResource(R.string.weather_simulator_time),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                TimeOfDay.DAWN to stringResource(R.string.weather_cfg_card_dawn),
                TimeOfDay.DAY to stringResource(R.string.weather_cfg_card_day),
                TimeOfDay.DUSK to stringResource(R.string.weather_cfg_card_dusk),
                TimeOfDay.NIGHT to stringResource(R.string.weather_cfg_card_night)
            ).forEach { (time, label) ->
                SelectorChip(
                    label = label,
                    selected = selectedTime == time,
                    onClick = { selectedTime = time },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val (previewUri, target) = remember(selectedWeather, selectedTime, state.rules) {
            previewFor(state, selectedWeather, selectedTime)
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (previewUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(previewUri)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.weather_simulator_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        if (previewUri != null) {
            Text(
                text = when (target) {
                    1 -> stringResource(R.string.weather_simulator_target_home)
                    2 -> stringResource(R.string.weather_simulator_target_lock)
                    else -> stringResource(R.string.weather_simulator_target_both)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

private fun weatherEmoji(weather: Weather): String = when (weather) {
    Weather.Clear -> "☀️"
    Weather.Rain -> "🌧️"
    Weather.Storm -> "⛈️"
    Weather.Snow -> "❄️"
    Weather.Fog -> "🌫️"
    else -> "☁️"
}

@Composable
fun ProLockedCard(
    title: String,
    description: String,
    onUpsellClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUpsellClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.weather_simulator_unlock), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun timeLabelRes(time: TimeOfDay): Int = when (time) {
    TimeOfDay.DAWN -> R.string.weather_cfg_card_dawn
    TimeOfDay.DAY -> R.string.weather_cfg_card_day
    TimeOfDay.DUSK -> R.string.weather_cfg_card_dusk
    TimeOfDay.NIGHT -> R.string.weather_cfg_card_night
}

@Composable
fun PackPreviewGrid(
    state: DynamicWallpaperUiState,
    isPremium: Boolean,
    onUpsellClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.pack_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!isPremium) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "PRO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        Text(
            text = stringResource(R.string.pack_preview_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isPremium) {
            ProLockedCard(
                title = stringResource(R.string.pack_preview_pro_title),
                description = stringResource(R.string.pack_preview_pro_desc),
                onUpsellClick = onUpsellClick
            )
            return@Column
        }

        val times = listOf(TimeOfDay.DAWN, TimeOfDay.DAY, TimeOfDay.DUSK, TimeOfDay.NIGHT)

        Weather.all().forEach { weather ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(
                    modifier = Modifier.width(56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = weatherEmoji(weather), style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = stringResource(weather.stringRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                times.forEach { time ->
                    val uri = remember(weather, time, state.rules) {
                        previewFor(state, weather, time).first
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        if (uri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(200)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f),
                                    RoundedCornerShape(topStart = 6.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(timeLabelRes(time)),
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperHistorySection(
    history: List<WallpaperHistoryEntry>,
    onRevert: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.history_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (history.isEmpty()) {
            EmptyState(
                emoji = "🖼️",
                message = stringResource(R.string.history_empty)
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history) { entry ->
                    WallpaperHistoryThumb(entry = entry, onClick = { onRevert(entry.uri) })
                }
            }
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.history_clear))
            }
        }
    }
}

@Composable
private fun WallpaperHistoryThumb(
    entry: WallpaperHistoryEntry,
    onClick: () -> Unit
) {
    val weather = entry.weatherKey?.let { weatherFromKey(it) }

    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (entry.uri.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(entry.uri)
                        .crossfade(200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (weather != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(topStart = 6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = weatherEmoji(weather),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
        Text(
            text = formatHistoryTime(entry.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatHistoryTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
}

@Composable
fun ConfigSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        content()
    }
}

@Composable
fun ExpandableSection(
    icon: ImageVector,
    title: String,
    description: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "expandIcon"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun QuickSettingsCard(
    useWallpaperBackground: Boolean,
    reduceAnimations: Boolean,
    hapticsEnabled: Boolean,
    soundEnabled: Boolean,
    onToggleWallpaperBackground: (Boolean) -> Unit,
    onToggleReduceAnimations: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            QuickToggleRow(
                icon = Icons.Default.Wallpaper,
                title = stringResource(R.string.cfg_app_background),
                description = stringResource(R.string.cfg_app_background_desc),
                checked = useWallpaperBackground,
                onCheckedChange = onToggleWallpaperBackground
            )
            QuickDivider()
            QuickToggleRow(
                icon = Icons.Default.Speed,
                title = stringResource(R.string.cfg_theme_reduce_animations),
                description = stringResource(R.string.cfg_theme_reduce_animations_desc),
                checked = reduceAnimations,
                onCheckedChange = onToggleReduceAnimations
            )
            QuickDivider()
            QuickToggleRow(
                icon = Icons.Default.Vibration,
                title = stringResource(R.string.cfg_haptics),
                description = stringResource(R.string.cfg_haptics_desc),
                checked = hapticsEnabled,
                onCheckedChange = onToggleHaptics
            )
            QuickDivider()
            QuickToggleRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = stringResource(R.string.cfg_sound),
                description = stringResource(R.string.cfg_sound_desc),
                checked = soundEnabled,
                onCheckedChange = onToggleSound
            )
        }
    }
}

@Composable
private fun QuickDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun QuickToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun TopBarIconButton(
    glass: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (glass) glassSurfaceColor() else Color.Transparent,
        border = if (glass) glassBorder() else null
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription)
        }
    }
}

@Composable
fun glassSurfaceColor(): Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)

@Composable
fun glassBorder(): androidx.compose.foundation.BorderStroke =
    androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    )

@Composable
fun ThemeModeSelector(
    selected: Boolean?,
    onSelect: (Boolean?) -> Unit
) {
    val options = listOf(
        Triple(null as Boolean?, stringResource(R.string.theme_mode_system), Icons.Default.PhoneAndroid),
        Triple(false, stringResource(R.string.theme_mode_light), Icons.Default.LightMode),
        Triple(true, stringResource(R.string.theme_mode_dark), Icons.Default.DarkMode)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (mode, label, icon) ->
            val isSelected = selected == mode
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun ThemePreview(
    darkTheme: Boolean,
    amoled: Boolean,
    accentColor: String?
) {
    val context = LocalContext.current
    val accent = accentColor?.let { key -> AccentOptions.find { it.key == key } }
    val isDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val background = when {
        amoled && darkTheme -> Color.Black
        darkTheme -> Color(0xFF0A1A0A)
        else -> Color(0xFFF5F5F0)
    }
    val cardColor = when {
        amoled && darkTheme -> Color(0xFF121212)
        darkTheme -> Color(0xFF1A1A1A)
        else -> Color.White
    }
    val onBg = if (darkTheme) Color.White.copy(alpha = 0.92f) else Color(0xFF1A1A1A)
    val onBgMuted = if (darkTheme) Color.White.copy(alpha = 0.55f) else Color(0xFF1A1A1A).copy(alpha = 0.6f)

    val primary = if (accent != null) {
        if (darkTheme) accent.primaryDark else accent.primaryLight
    } else if (isDynamic) {
        if (darkTheme) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
    } else {
        if (darkTheme) CyberGreen else CyberGreenLight
    }
    val secondary = if (accent != null) {
        if (darkTheme) accent.secondaryDark else accent.secondaryLight
    } else if (isDynamic) {
        if (darkTheme) dynamicDarkColorScheme(context).secondary else dynamicLightColorScheme(context).secondary
    } else {
        if (darkTheme) CyberCyan else CyberCyanLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .offset(x = 40.dp, y = (-50).dp)
                .clip(RoundedCornerShape(50))
                .background(primary.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(110.dp)
                .offset(x = (-35).dp, y = 45.dp)
                .clip(RoundedCornerShape(50))
                .background(secondary.copy(alpha = 0.18f))
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "9:41",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = onBg
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.4f, 0.6f, 0.8f).forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(onBgMuted.copy(alpha = alpha))
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Iris",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onBg
            )
            Text(
                text = stringResource(R.string.cfg_theme_preview),
                style = MaterialTheme.typography.bodySmall,
                color = onBgMuted
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(listOf(primary, secondary))
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.accent_system),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, primary, RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.apply_wallpaper),
                        style = MaterialTheme.typography.labelMedium,
                        color = primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardColor)
            )
        }
    }
}

@Composable
fun AccentColorSelector(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (selected == null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            },
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(null) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.accent_system),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (selected == null) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentOptions.forEach { option ->
                AccentColorDot(
                    color = option.primaryDark,
                    isSelected = selected == option.key,
                    onClick = { onSelect(option.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AccentColorDot(
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) color.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 28.dp else 24.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.align(Alignment.Center).size(16.dp)
                )
            }
        }
    }
}

@Composable
fun LocationSettings(
    useGps: Boolean,
    searchQuery: String,
    searchResults: List<com.andyl.iris.domain.model.CityResult>,
    isSearching: Boolean,
    isLoading: Boolean,
    onToggleGps: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectCity: (com.andyl.iris.domain.model.CityResult) -> Unit,
    onRequestGps: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (useGps) Icons.Default.LocationOn else Icons.Default.Place,
                        null,
                        tint = if (useGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.auto_gps),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = useGps,
                    onCheckedChange = onToggleGps,
                    enabled = !isLoading
                )
            }
        }

        if (useGps) {
            OutlinedButton(
                onClick = onRequestGps,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.refresh_gps))
            }
        } else {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_city_placeholder)) },
                    enabled = !isLoading,
                    leadingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, null)
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, stringResource(R.string.clear))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                AnimatedVisibility(visible = searchResults.isNotEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        searchResults.take(5).forEach { city ->
                            ListItem(
                                headlineContent = { Text(city.name, style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.clickable {
                                    onSelectCity(city)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openBatterySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        context.startActivity(intent)
    }
}
