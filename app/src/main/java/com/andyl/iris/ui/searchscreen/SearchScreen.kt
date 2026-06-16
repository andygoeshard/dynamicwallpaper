package com.andyl.iris.ui.searchscreen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.ui.searchscreen.components.DownloadStatusSection
import com.andyl.iris.ui.searchscreen.components.LocalImageThumbnail
import com.andyl.iris.ui.searchscreen.components.PackDetailList
import com.andyl.iris.ui.searchscreen.components.SuggestedPacksList
import com.andyl.iris.ui.searchscreen.components.WallpaperSearchResultItem
import com.andyl.iris.ui.searchscreen.components.WallpaperDetailSheet
import org.koin.androidx.compose.koinViewModel

import com.andyl.iris.domain.model.DownloadStatus
import com.andyl.iris.domain.model.DownloadTask
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val wallpaperState by viewModel.wallpaperViewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    // Permission handling for local gallery
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshLocalImages()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permission)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // LOCAL BACK NAVIGATION MANAGEMENT
    BackHandler(enabled = state.selectedImage != null || searchQuery.isNotEmpty() || state.currentPack != null || state.activeSlot != null || state.showPackSelectionDialog || state.showDownloads) {
        if (state.selectedImage != null) {
            viewModel.selectImage(null)
        } else if (searchQuery.isNotEmpty()) {
            viewModel.onSearchQueryChanged("")
        } else if (state.showDownloads) {
            viewModel.toggleDownloads(false)
        } else if (state.showPackSelectionDialog) {
            viewModel.dismissPackSelection()
        } else if (state.activeSlot != null) {
            viewModel.selectSlot(null) // Go back to slot list
        } else {
            viewModel.selectPack(null) // Go back to suggested packs
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 1. TOP BAR & TABS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (state.selectedImage != null) {
                                    viewModel.selectImage(null)
                                } else if (searchQuery.isNotEmpty()) {
                                    viewModel.onSearchQueryChanged("")
                                } else if (state.showDownloads) {
                                    viewModel.toggleDownloads(false)
                                } else if (state.showPackSelectionDialog) {
                                    viewModel.dismissPackSelection()
                                } else if (state.activeSlot != null) {
                                    viewModel.selectSlot(null)
                                } else if (state.currentPack != null) {
                                    viewModel.selectPack(null)
                                } else {
                                    onNavigateHome()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    viewModel.onSearchQueryChanged(it)
                                    if (it.isEmpty() && state.activeSlot == null) {
                                        // No action needed here usually
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { 
                                    Text(
                                        stringResource(com.andyl.iris.R.string.search_hint),
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
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(28.dp),
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

                        AnimatedVisibility(visible = state.downloadTasks.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.toggleDownloads(!state.showDownloads) },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (state.downloadTasks.any { it.status is DownloadStatus.Downloading }) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.showDownloads) Icons.Default.Close else Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Downloads",
                                        tint = if (state.showDownloads) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = state.currentTab.coerceAtMost(2),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            val current = state.currentTab.coerceAtMost(2)
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[current]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = state.currentTab == 0,
                            onClick = { 
                                viewModel.setTab(0) 
                                viewModel.onSearchQueryChanged("")
                            },
                            text = { Text("Local", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = state.currentTab == 1,
                            onClick = { 
                                viewModel.setTab(1) 
                                viewModel.onSearchQueryChanged("")
                            },
                            text = { Text("Favorites", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = state.currentTab == 2 || state.currentPack != null,
                            onClick = { 
                                viewModel.setTab(2) 
                                viewModel.onSearchQueryChanged("")
                                viewModel.selectPack(null)
                            },
                            text = { Text("Packs", fontWeight = FontWeight.Bold) }
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

                    AnimatedContent(
                        targetState = when {
                            state.searchResults.isNotEmpty() -> 3 // Search results always take over
                            state.currentTab == 0 -> 0 // Local Gallery
                            state.currentTab == 1 -> 1 // Favorites
                            state.currentPack != null -> 4 // Pack detail logic
                            else -> 2 // Packs list
                        },
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "MainContent",
                        modifier = Modifier.fillMaxSize()
                    ) { target ->
                        when (target) {
                            0 -> { // LOCAL GALLERY
                                if (state.localImages.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No images found on device", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.localImages) { image ->
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(0.7f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { 
                                                        viewModel.selectImage(image)
                                                    }
                                            ) {
                                                LocalImageThumbnail(
                                                    image = image,
                                                    isFavorite = state.favorites.any { it.uri == image.urlFull },
                                                    onToggleFavorite = { viewModel.toggleFavorite(image) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> { // FAVORITES
                                if (state.favorites.isEmpty()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No favorites yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.favorites) { favorite ->
                                            val image = ImageResult(
                                                id = favorite.uri,
                                                urlSmall = favorite.thumbnailUrl ?: favorite.uri,
                                                urlFull = favorite.uri,
                                                provider = favorite.source,
                                                alt = null
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(0.7f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { 
                                                        viewModel.selectImage(image)
                                                    }
                                            ) {
                                                LocalImageThumbnail(
                                                    image = image,
                                                    isFavorite = true,
                                                    onToggleFavorite = { viewModel.toggleFavorite(image) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> { // PACKS LIST
                                SuggestedPacksList { pack ->
                                    viewModel.selectPack(pack)
                                }
                            }

                            3 -> { // SEARCH RESULTS
                                Column(modifier = Modifier.fillMaxSize()) {
                                    state.activeSlot?.let {
                                        Surface(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                stringResource(com.andyl.iris.R.string.target_slot, it.label),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.searchResults) { image ->
                                            Box(
                                                modifier = Modifier
                                                    .aspectRatio(0.7f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { 
                                                        viewModel.selectImage(image)
                                                    }
                                            ) {
                                                LocalImageThumbnail(
                                                    image = image,
                                                    isFavorite = state.favorites.any { it.uri == image.urlFull },
                                                    onToggleFavorite = { viewModel.toggleFavorite(image) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            4 -> { // PACK DETAIL
                                val currentPack = state.currentPack
                                if (currentPack != null) {
                                    PackDetailList(
                                        pack = currentPack,
                                        rules = wallpaperState.rules,
                                        dailyRules = wallpaperState.dailyRules,
                                        fixedRules = wallpaperState.fixedRules,
                                        previewImages = state.previewImages,
                                        isLoading = state.isLoading,
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
                        }
                    }
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

            DownloadStatusSection(
                tasks = state.downloadTasks,
                showDownloads = state.showDownloads,
                onRemoveTask = { viewModel.removeDownloadTask(it) },
                onToggleShow = { viewModel.toggleDownloads(it) }
            )

            // Wallpaper Detail / Crop / Apply Overlay
            AnimatedVisibility(
                visible = state.selectedImage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                state.selectedImage?.let { image ->
                    WallpaperDetailSheet(
                        image = image,
                        isFavorite = state.favorites.any { it.uri == image.urlFull },
                        onToggleFavorite = { viewModel.toggleFavorite(image) },
                        onDismiss = { viewModel.selectImage(null) },
                        onConfirm = { _, target, scaleMode, cropX, cropY, cropScale ->
                            viewModel.confirmAndDownload(context, image, target, scaleMode, cropX, cropY, cropScale)
                            viewModel.selectImage(null)
                        }
                    )
                }
            }
        }
    }
}
