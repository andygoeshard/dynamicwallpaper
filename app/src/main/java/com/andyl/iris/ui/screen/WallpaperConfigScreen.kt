package com.andyl.iris.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.billing.BillingManager
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.ui.components.PremiumUpsellSheet
import com.andyl.iris.ui.components.RatingDialog
import com.andyl.iris.ui.components.ScaleModeSelector
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperConfigScreen(
    viewModel: DynamicWallpaperViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> /* :) */ }

    if (uiState.showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.onEvent(WallpaperEvent.OnDismissRatingDialog) },
            onRate = { stars -> viewModel.onEvent(WallpaperEvent.OnRateApp(stars)) }
        )
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cfg_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back)) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- SECTION: LIVE STATUS ---
            item {
                StatusCard(
                    weather = uiState.currentWeather,
                    lastUpdate = uiState.lastUpdateTime,
                    nextUpdate = uiState.nextUpdateTime,
                    isLoading = uiState.isLoading,
                    onRefresh = { viewModel.onEvent(WallpaperEvent.OnManualRefresh) }
                )
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

            // --- SECTION: LOCATION SETTINGS ---
            item {
                ConfigSection(
                    title = stringResource(R.string.cfg_screen_city_settings_title),
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
                val premiumRepository = koinInject<PremiumRepository>()
                val isPremium = premiumRepository.isPremium()
                var showUpsellSheet by remember { mutableStateOf(false) }

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
                                        text = "Iris Pro Active",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "All features unlocked",
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

                if (showUpsellSheet) {
                    val billingManager = koinInject<BillingManager>()
                    PremiumUpsellSheet(
                        billingManager = billingManager,
                        onDismiss = { showUpsellSheet = false }
                    )
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
                                } catch (_: Exception) {}
                                viewModel.onEvent(WallpaperEvent.ClearMessages)
                            }
                        }
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cfg_about_privacy)) },
                            leadingContent = { Icon(Icons.Default.Lock, null) },
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/15OiIEyzMrf3s96Ias2Q-ACLhlITbEoAieeEoSyozeqM/edit?usp=sharing"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
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
                                style = MaterialTheme.typography.headlineSmall
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
