package com.andyl.dynamicwallpaper.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.dynamicwallpaper.ui.event.WallpaperEvent
import com.andyl.dynamicwallpaper.ui.state.DynamicWallpaperUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackSelectorSection(
    state : DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val packs = state.availablePacks
    if (packs.isEmpty()) return
    val totalItems = Int.MAX_VALUE
    val startIndex = (totalItems / 2) - ((totalItems / 2) % packs.size)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    var showRenameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Renombrar Pack") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    label = { Text("Nuevo nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(WallpaperEvent.OnRenamePack(tempName))
                    showRenameDialog = false
                }) { Text("Guardar") }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FLECHA IZQUIERDA
        IconButton(onClick = {
            coroutineScope.launch {
                val targetIndex = listState.firstVisibleItemIndex - 1
                val packId = packs[targetIndex % packs.size].id
                onEvent(WallpaperEvent.OnChangePack(packId, -1))
                listState.animateScrollToItem(targetIndex)
            }
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(totalItems) { index ->
                val pack = packs[index % packs.size]
                val isBeingEdited = pack.id == state.editingPackId
                val isCurrentlyActive = pack.id == state.activePackId

                FilterChip(
                    selected = isBeingEdited,
                    onClick = {
                        if (isBeingEdited) {
                            tempName = pack.name
                            showRenameDialog = true
                        } else {
                            val currentVisible = listState.firstVisibleItemIndex
                            val direction = if (index > currentVisible) 1 else -1
                            onEvent(WallpaperEvent.OnChangePack(pack.id, direction))
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                    },
                    label = {
                        Text(
                            text = pack.name,
                            fontWeight = if (isCurrentlyActive) FontWeight.ExtraBold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isBeingEdited,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        selectedBorderWidth = 2.dp
                    )
                )
            }
        }

        // FLECHA DERECHA
        IconButton(onClick = {
            coroutineScope.launch {
                val targetIndex = listState.firstVisibleItemIndex + 1
                val packId = packs[targetIndex % packs.size].id
                onEvent(WallpaperEvent.OnChangePack(packId, 1))

                listState.animateScrollToItem(targetIndex)
            }
        }){
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}