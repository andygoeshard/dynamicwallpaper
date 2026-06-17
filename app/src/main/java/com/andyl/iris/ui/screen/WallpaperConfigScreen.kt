package com.andyl.iris.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.ui.components.ScaleModeSelector
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperConfigScreen(
    viewModel: DynamicWallpaperViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cfg_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back)) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECCIÓN DE AJUSTE DE IMAGEN ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.cfg_screen_img_settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ScaleModeSelector(
                    selectedMode = uiState.scaleMode,
                    onModeSelected = { newMode ->
                        viewModel.onEvent(WallpaperEvent.UpdateScaleMode(mode = newMode))
                    }
                )

                Text(
                    stringResource(R.string.cfg_screen_img_settings_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // --- SECCIÓN DE STATUS ACTUAL ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.live_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.onEvent(WallpaperEvent.OnManualRefresh) }) {
                            Icon(Icons.Default.Refresh, stringResource(R.string.refresh), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Current Weather: ${uiState.currentWeather?.javaClass?.simpleName ?: "Unknown"}", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Last Update: ${uiState.lastUpdateTime}", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.isLoading) {
                            Text(
                                stringResource(R.string.applying_changes), 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- SECCIÓN DE UBICACIÓN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.cfg_screen_city_settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.auto_gps), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = uiState.useGps,
                        onCheckedChange = { viewModel.onEvent(WallpaperEvent.OnToggleGps(it)) },
                        enabled = !uiState.isLoading
                    )
                }
            }

            if (uiState.useGps) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.gps_active),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = searchQuery.ifEmpty { "No city selected" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_city_placeholder)) },
                        enabled = !uiState.isLoading,
                        leadingIcon = { 
                            if (uiState.isSearchingCity) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, null) 
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, stringResource(R.string.clear))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )

                    if (searchResults.isNotEmpty()) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            searchResults.take(5).forEach { city ->
                                ListItem(
                                    headlineContent = { Text(city.name, style = MaterialTheme.typography.bodyMedium) },
                                    modifier = Modifier.clickable {
                                        viewModel.onEvent(WallpaperEvent.OnSelectCity(city))
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.useGps) {
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(stringResource(R.string.refresh_gps))
                }
            }
        }
    }
}
