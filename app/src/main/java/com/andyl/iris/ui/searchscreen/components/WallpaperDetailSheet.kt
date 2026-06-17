package com.andyl.iris.ui.searchscreen.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andyl.iris.R
import com.andyl.iris.domain.model.ImageResult
import com.andyl.iris.domain.model.ScaleMode

@Composable
fun WallpaperDetailSheet(
    image: ImageResult,
    isFavorite: Boolean,
    isLoading: Boolean = false,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (uri: String, target: Int, scaleMode: ScaleMode, cropX: Float?, cropY: Float?, cropScale: Float?) -> Unit
) {
    var selectedTarget by remember { mutableIntStateOf(3) }
    val selectedScaleMode by remember { mutableStateOf(ScaleMode.CROP) }
    
    // Zoom/Pan State
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full screen preview with zoom/pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 10f)
                        offset += pan
                    }
                }
        ) {
            AsyncImage(
                model = image.urlFull,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // Overlay controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close), tint = Color.White)
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite),
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.wallpaper_preview),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.wallpaper_preview_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.choose_destination),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailTargetOption(stringResource(R.string.target_both), isSelected = selectedTarget == 3, modifier = Modifier.weight(1f)) { selectedTarget = 3 }
                        DetailTargetOption(stringResource(R.string.target_home), isSelected = selectedTarget == 1, modifier = Modifier.weight(1f)) { selectedTarget = 1 }
                        DetailTargetOption(stringResource(R.string.target_lock), isSelected = selectedTarget == 2, modifier = Modifier.weight(1f)) { selectedTarget = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onConfirm(image.urlFull, selectedTarget, selectedScaleMode, offset.x, offset.y, scale) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.confirm_set), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTargetOption(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) 
            }
        },
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}
