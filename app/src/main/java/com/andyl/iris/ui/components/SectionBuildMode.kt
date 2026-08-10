package com.andyl.iris.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Full-screen build overlay for reordering the home sections. Shows the SAME cards
 * (collapsed) over a tela (scrim) that hides the rest of the UI, with an X / back
 * to exit. Reordering is handled by the reorderable library.
 */
@Composable
fun SectionBuildMode(
    sections: List<String>,
    titles: Map<String, String>,
    subtitles: Map<String, String>,
    sectionContent: @Composable (String) -> Unit,
    onReorder: (List<String>) -> Unit,
    onExit: () -> Unit
) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, toInfo ->
            onReorder(
                sections.toMutableList().apply {
                    add(toInfo.index, removeAt(from.index))
                }
            )
        }
    )

    BackHandler(onBack = onExit)

    // Tela: dark scrim that separates the build grid from the UI behind.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DottedGridBackground(modifier = Modifier.fillMaxSize())

                Column(modifier = Modifier.fillMaxSize()) {
                    BuildModeTopBar(onExit = onExit)

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(sections, key = { it }) { sectionId ->
                            ReorderableItem(state = reorderState, key = sectionId) { isDragging ->
                                CollapsibleSectionCard(
                                    sectionId = sectionId,
                                    title = titles[sectionId] ?: sectionId,
                                    subtitle = subtitles[sectionId],
                                    trailing = {
                                        Box(
                                            modifier = Modifier
                                                .longPressDraggableHandle()
                                                .padding(6.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DragHandle,
                                                contentDescription = "Arrastrar",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    forceCollapsed = true,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = if (isDragging) 1.02f else 1f
                                        scaleY = if (isDragging) 1.02f else 1f
                                        shadowElevation = if (isDragging) 12f else 0f
                                    }
                                ) {
                                    sectionContent(sectionId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
