package com.andyl.dynamicwallpaper.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.ui.components.DaySelectionSection
import com.andyl.dynamicwallpaper.ui.components.FixedTimeSection
import com.andyl.dynamicwallpaper.ui.components.PackSelectorSection
import com.andyl.dynamicwallpaper.ui.components.WeatherConfigCard
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

    // --- Lógica de Permisos Exactos (Sin cambios) ---
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
                    viewModel.requestExactAlarmPermission(context)
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
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ubicación")
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
                        onClick = { viewModel.applyWallpaper() },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PackSelectorSection(
                        viewModel = viewModel,
                    )
                }

                item { HorizontalDivider() }

                item {
                    DaySelectionSection(viewModel = viewModel)
                }

                item { HorizontalDivider() }

                item {
                    Text("Configuración por Clima", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Mutea climas para mantener el fondo anterior", style = MaterialTheme.typography.bodySmall)
                }

                val weathers = listOf(
                    Weather.Clear, Weather.Cloudy, Weather.Rain,
                    Weather.Snow, Weather.Fog, Weather.Storm
                )

                items(weathers) { weather ->
                    WeatherConfigCard(
                        weather = weather,
                        isEnabled = state.enabledWeathers.contains(weather),
                        onToggle = { viewModel.toggleWeatherEnabled(weather) }
                    )
                }

                item { HorizontalDivider() }

                item {
                    FixedTimeSection(viewModel = viewModel)
                }

                item { HorizontalDivider() }

                // 5. RESUMEN DE REGLAS (Mantenemos como estaba)
                item {
                    Text("Resumen de Archivos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(state.rules.toList()) { (config, uri) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(config, fontWeight = FontWeight.Bold)
                            // Limpiamos el URI para mostrar solo el nombre del archivo
                            Text(uri.split("/").last(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}