package com.andyl.iris.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.andyl.iris.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackSelectorSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val packs = state.availablePacks
    if (packs.isEmpty()) return

    val hasMultiplePacks = packs.size > 1
    val totalItems = if (hasMultiplePacks) 10000 else 1
    val startIndex = (totalItems / 2) - ((totalItems / 2) % packs.size)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    var showRenameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    val density = LocalDensity.current
    var rowWidthPx by remember { mutableIntStateOf(0) }

    val horizontalPadding = remember(rowWidthPx, hasMultiplePacks) {
        if (rowWidthPx == 0 || !hasMultiplePacks) 0.dp
        else {
            val rowWidthDp = with(density) { rowWidthPx.toDp() }
            val estimatedChipWidth = 110.dp
            (rowWidthDp / 2) - (estimatedChipWidth / 2)
        }
    }

    LaunchedEffect(state.editingPackId) {
        val targetPackIndex = packs.indexOfFirst { it.id == state.editingPackId }
        if (targetPackIndex != -1) {
            val currentVisibleIndex = listState.firstVisibleItemIndex
            val currentOffset = currentVisibleIndex % packs.size
            val baseIndex = currentVisibleIndex - currentOffset
            val finalTargetIndex = baseIndex + targetPackIndex
            if (finalTargetIndex != currentVisibleIndex) {
                listState.animateScrollToItem(finalTargetIndex)
            }
        }
    }

    if (showRenameDialog) {
        LaunchedEffect(state.editingPackId) {
            val currentPack = packs.find { it.id == state.editingPackId }
            if (currentPack != null) {
                tempName = currentPack.name
            }
        }

        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.pack_sel_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.acc_pack_selected)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pack_sel_my_packages) + " (${packs.size}/10)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (packs.size < 10) {
                            TextButton(
                                onClick = {
                                    onEvent(WallpaperEvent.OnAddNewPack)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_add), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(packs) { pack ->
                            val isSelected = pack.id == state.editingPackId
                            Surface(
                                onClick = {
                                    onEvent(WallpaperEvent.OnSelectFromPackManager(pack.id))
                                    tempName = pack.name
                                },
                                shape = MaterialTheme.shapes.medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pack.name, modifier = Modifier.weight(1f), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    if (packs.size > 1) {
                                        IconButton(onClick = { onEvent(WallpaperEvent.OnDeletePack(pack.id)); tempName = "" }) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = tempName.isNotBlank(), onClick = { onEvent(WallpaperEvent.OnRenamePack(tempName)); showRenameDialog = false }) { Text(
                    stringResource(R.string.pack_selector_save_changes)
                ) }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text(
                stringResource(R.string.btn_close)
            ) } }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- FLECHA IZQUIERDA ---
        if (hasMultiplePacks) {
            IconButton(onClick = {
                coroutineScope.launch {
                    val targetIndex = listState.firstVisibleItemIndex - 1
                    val safeIndex = if (targetIndex < 0) (totalItems / 2) else targetIndex
                    val packId = packs[((safeIndex % packs.size) + packs.size) % packs.size].id
                    onEvent(WallpaperEvent.OnChangePack(packId, -1))
                    listState.animateScrollToItem(safeIndex)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { rowWidthPx = it.size.width },
            contentAlignment = Alignment.Center
        ) {
            if (rowWidthPx > 0) {
                LazyRow(
                    state = listState,
                    flingBehavior = snapBehavior,
                    userScrollEnabled = hasMultiplePacks,
                    modifier = if (hasMultiplePacks) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val itemCount = if (hasMultiplePacks) totalItems else 1

                    items(itemCount) { index ->
                        val pack = packs[index % packs.size]
                        val isBeingEdited = pack.id == state.editingPackId
                        val isCurrentlyActive = pack.id == state.activePackId

                        FilterChip(
                            selected = isBeingEdited,
                            onClick = {
                                if (isBeingEdited) {
                                    tempName = pack.name
                                    showRenameDialog = true
                                } else if (hasMultiplePacks) {
                                    val currentVisible = listState.firstVisibleItemIndex
                                    val direction = if (index > currentVisible) 1 else -1
                                    onEvent(WallpaperEvent.OnChangePack(pack.id, direction))
                                    coroutineScope.launch { listState.animateScrollToItem(index) }
                                }
                            },
                            label = {
                                Text(
                                    text = pack.name,
                                    fontWeight = if (isCurrentlyActive) FontWeight.Black else FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                containerColor = Color.Transparent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isBeingEdited,
                                borderColor = Color.Transparent,
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        }

        // --- FLECHA DERECHA ---
        if (hasMultiplePacks) {
            IconButton(onClick = {
                coroutineScope.launch {
                    val targetIndex = listState.firstVisibleItemIndex + 1
                    val packId = packs[targetIndex % packs.size].id
                    onEvent(WallpaperEvent.OnChangePack(packId, 1))
                    listState.animateScrollToItem(targetIndex)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}