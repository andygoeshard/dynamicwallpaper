package com.andyl.dynamicwallpaper.ui.screen

import android.R.attr.onClick
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.ui.components.DaySelectionSection
import com.andyl.dynamicwallpaper.ui.components.FixedTimeSection
import com.andyl.dynamicwallpaper.ui.components.PackSelectorSection
import com.andyl.dynamicwallpaper.ui.components.WeatherConfigCard
import com.andyl.dynamicwallpaper.ui.event.WallpaperEvent
import com.andyl.dynamicwallpaper.ui.viewmodel.DynamicWallpaperViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicWallpaperScreen(
    viewModel: DynamicWallpaperViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Precisión de Horarios") },
            text = { Text("Para que los fondos cambien en el segundo exacto, Android requiere un permiso especial de alarmas.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    viewModel.onEvent(WallpaperEvent.RequestExactAlarmPermission(context))
                }) { Text("Ir a Ajustes") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Ahora no") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dynamic Wallpaper", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(WallpaperEvent.OnAddNewPack) }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add new pack")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.error != null) {
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { viewModel.onEvent(WallpaperEvent.OnApplyWallpaper) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Text(if (state.isLoading) "Aplicando..." else "Actualizar Fondo Ahora")
                    }
                }
            }
        }
    ) { padding ->

        if (state.isLoading && state.rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                PackSelectorSection(
                    state = state,
                    onEvent = { event -> viewModel.onEvent(event) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                AnimatedContent(
                    targetState = state.editingPackId,
                    transitionSpec = {
                        if (state.slideDirection > 0) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "PackContentTransition",
                    modifier = Modifier.weight(1f)
                ) { id ->
                    key(id){
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                DaySelectionSection(
                                    state = state,
                                    onEvent = { event -> viewModel.onEvent(event) }
                                )
                            }

                            item { HorizontalDivider() }

                            item {
                                Surface(
                                    onClick = {
                                        viewModel.onEvent(WallpaperEvent.OnToggleWeatherFeature)
                                    },
                                    color = androidx.compose.ui.graphics.Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Configuración por Clima",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Toca para expandir y configurar",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        val rotation by androidx.compose.animation.core.animateFloatAsState(
                                            targetValue = if (state.isWeatherFeatureEnabled) 180f else 0f,
                                            label = "ArrowRotation"
                                        )

                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(rotation),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            item {
                                AnimatedVisibility(
                                    visible = state.isWeatherFeatureEnabled,
                                    enter = expandVertically(
                                        expandFrom = Alignment.Top,
                                        animationSpec = tween(durationMillis = 400)
                                    ) + fadeIn(),
                                    exit = shrinkVertically(
                                        shrinkTowards = Alignment.Top,
                                        animationSpec = tween(durationMillis = 400)
                                    ) + fadeOut()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val weathers = listOf(
                                            Weather.Clear, Weather.Cloudy, Weather.Rain,
                                            Weather.Snow, Weather.Fog, Weather.Storm
                                        )

                                        weathers.forEach { weather ->
                                            WeatherConfigCard(
                                                weather = weather,
                                                isEnabled = state.enabledWeathers.contains(weather),
                                                onToggle = { viewModel.onEvent(WallpaperEvent.OnToggleWeather(weather)) },
                                                state = state,
                                                onEvent = { event -> viewModel.onEvent(event) }
                                            )
                                        }

                                        Text(
                                            "Mutea climas individuales para mantener el fondo anterior.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                                        )
                                    }
                                }
                            }

                            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }

                            item {
                                FixedTimeSection(
                                    state = state,
                                    onEvent = { event -> viewModel.onEvent(event) }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}