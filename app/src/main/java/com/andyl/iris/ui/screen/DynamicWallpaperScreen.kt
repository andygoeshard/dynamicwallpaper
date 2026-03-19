package com.andyl.iris.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.ui.components.DaySelectionSection
import com.andyl.iris.ui.components.FixedTimeSection
import com.andyl.iris.ui.components.BoxContainer
import com.andyl.iris.ui.components.LogoDynamicMinimalista
import com.andyl.iris.ui.components.PackSelectorSection
import com.andyl.iris.ui.components.WeatherSection
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
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
                navigationIcon = {
                    LogoDynamicMinimalista(
                        modifier = Modifier.size(300.dp),
                        color = Color(0xFF4DE1C1),
                        backgroundColor = Color.Black
                    )
                },
                title = {
                    Text("Dynamic Wallpaper", fontWeight = FontWeight.Black) },
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
            Box(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = state.editingPackId != state.activePackId,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Surface(
                        tonalElevation = 0.dp,
                        // Glassy effect para el botón
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = { viewModel.onEvent(WallpaperEvent.OnApplyWallpaper) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isLoading
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Activar este Paquete", fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
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
                    .padding(top = padding.calculateTopPadding())
            ) {
                PackSelectorSection(state = state, onEvent = { viewModel.onEvent(it) })

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
                    key(id) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { BoxContainer { DaySelectionSection(state = state, onEvent = { viewModel.onEvent(it) }) } }
                            item { BoxContainer { WeatherSection(state = state, onEvent = { viewModel.onEvent(it) }) } }
                            item { BoxContainer { FixedTimeSection(state = state, onEvent = { viewModel.onEvent(it) }) } }
                        }
                    }
                }
            }
        }
    }
}