package com.andyl.iris.ui.searchscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    var searchQuery by remember { mutableStateOf("") }

    // LOCAL BACK NAVIGATION MANAGEMENT
    BackHandler(enabled = state.currentPack != null || state.activeSlot != null) {
        if (state.activeSlot != null) {
            viewModel.selectSlot(null) // Go back to slot list
        } else {
            viewModel.selectPack(null) // Go back to suggested packs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.length > 2) {
                    viewModel.search(it)
                } else if (it.isEmpty() && state.activeSlot == null) {
                    viewModel.selectPack(null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            placeholder = { 
                Text(
                    "Search awesome wallpapers...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        if (state.activeSlot == null) viewModel.selectPack(null)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. DYNAMIC CONTENT
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // CASE A: API RESULTS
                state.searchResults.isNotEmpty() || state.activeSlot != null -> {
                    Column {
                        state.activeSlot?.let {
                            Text(
                                "Configuring in: ${wallpaperState.packName}",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.searchResults) { image ->
                                WallpaperSearchResultItem(image) { uri, target ->
                                    viewModel.confirmAndDownload(image, target)
                                    searchQuery = ""
                                }
                            }
                        }
                    }
                }

                // CASE B: INSIDE A PACK (Slot list)
                state.currentPack != null -> {
                    PackDetailList(
                        pack = state.currentPack!!,
                        rules = wallpaperState.rules,
                        dailyRules = wallpaperState.dailyRules,
                        onSlotClick = { weather, time, dayName, label ->
                            viewModel.selectSlot(WallpaperSlot(weather, time, dayName, label))
                            searchQuery = label
                        },
                        onDownloadFullPack = { pack ->
                            viewModel.installPack(pack) {
                                onNavigateHome()
                            }
                        }
                    )
                }

                // CASE C: START (Suggested packs)
                else -> {
                    SuggestedPacksList { pack ->
                        viewModel.selectPack(pack)
                    }
                }
            }
        }
    }
}
