package com.andyl.iris.ui.searchscreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.iris.ui.searchscreen.components.PackDetailList
import com.andyl.iris.ui.searchscreen.components.SuggestedPacksList
import com.andyl.iris.ui.searchscreen.components.WallpaperSearchResultItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val wallpaperState by viewModel.wallpaperViewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // LOCAL BACK NAVIGATION MANAGEMENT
    BackHandler(enabled = state.currentPack != null || state.activeSlot != null || state.showPackSelectionDialog) {
        if (state.showPackSelectionDialog) {
            viewModel.dismissPackSelection()
        } else if (state.activeSlot != null) {
            viewModel.selectSlot(null) // Go back to slot list
        } else {
            viewModel.selectPack(null) // Go back to suggested packs
        }
    }

    if (state.showPackSelectionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPackSelection() },
            title = { Text("Select Target Pack", fontWeight = FontWeight.Black) },
            text = {
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(state.availablePacks) { pack ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            onClick = { 
                                viewModel.installPack(state.currentPack!!, targetId = pack.id) {
                                    onNavigateHome()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text(pack.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPackSelection() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. SEARCH BAR WITH BETTER DESIGN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BACK BUTTON
                IconButton(
                    onClick = {
                        if (state.showPackSelectionDialog) {
                            viewModel.dismissPackSelection()
                        } else if (state.activeSlot != null) {
                            viewModel.selectSlot(null)
                        } else if (state.currentPack != null) {
                            viewModel.selectPack(null)
                        } else {
                            onNavigateHome()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            viewModel.onSearchQueryChanged(it)
                            if (it.isEmpty() && state.activeSlot == null) {
                                viewModel.selectPack(null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                "Search magic wallpapers...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                IconButton(onClick = { 
                                    viewModel.onSearchQueryChanged("")
                                    if (state.activeSlot == null) viewModel.selectPack(null)
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                }

                // 2. DYNAMIC CONTENT WITH ANIMATED SWITCHING
                AnimatedContent(
                    targetState = when {
                        state.searchResults.isNotEmpty() || state.activeSlot != null -> 0
                        state.currentPack != null -> 1
                        else -> 2
                    },
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "ContentTransition"
                ) { target ->
                    when (target) {
                        0 -> { // CASE A: API RESULTS
                            Column(modifier = Modifier.fillMaxSize()) {
                                state.activeSlot?.let {
                                    Surface(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "Target: ${it.label}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 32.dp)
                                ) {
                                    items(state.searchResults) { image ->
                                        WallpaperSearchResultItem(image) { uri, target, scaleMode ->
                                            viewModel.confirmAndDownload(context, image, target, scaleMode)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> { // CASE B: INSIDE A PACK (Slot list)
                            val currentPack = state.currentPack
                            if (currentPack != null) {
                                PackDetailList(
                                    pack = currentPack,
                                    rules = wallpaperState.rules,
                                    dailyRules = wallpaperState.dailyRules,
                                    fixedRules = wallpaperState.fixedRules,
                                    previewImages = state.previewImages,
                                    onSlotClick = { weather, time, dayName, fixedTime, label ->
                                        viewModel.selectSlot(WallpaperSlot(weather, time, dayName, fixedTime, label))
                                    },
                                    onDownloadFullPack = { pack ->
                                        viewModel.installPack(pack) {
                                            onNavigateHome()
                                        }
                                    },
                                    onLongPressInstall = {
                                        viewModel.onLongPressInstall()
                                    },
                                    onAddCustomTime = {
                                        val calendar = java.util.Calendar.getInstance()
                                        android.app.TimePickerDialog(context, { _, hour, minute ->
                                            val timeStr = String.format("%02d:%02d", hour, minute)
                                            viewModel.selectSlot(WallpaperSlot(null, null, null, timeStr, "Override: $timeStr"))
                                        }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
                                    }
                                )
                            }
                        }

                        2 -> { // CASE C: START (Suggested packs)
                            SuggestedPacksList { pack ->
                                viewModel.selectPack(pack)
                            }
                        }
                    }
                }
            }
        }
    }
}
