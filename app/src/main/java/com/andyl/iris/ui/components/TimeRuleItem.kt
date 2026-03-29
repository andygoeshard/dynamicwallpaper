package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun TimeRuleItem(
    time: String,
    homeUri: String? = null,
    lockUri: String? = null,
    bothUri: String? = null,
    onDeleteHome: () -> Unit,
    onDeleteLock: () -> Unit,
    onDeleteBoth: () -> Unit,
    onHomeClick: () -> Unit,
    onLockClick: () -> Unit,
    onBothClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(height = 100.dp, width = 85.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                bothUri != null -> {
                    AsyncImageWrapper(
                        uri = bothUri,
                        onClick = onBothClick,
                        onDelete = onDeleteBoth,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AsyncImageWrapperTimeRuleItem(
                            uri = homeUri,
                            onClick = onHomeClick,
                            onDelete = onDeleteHome,
                            showTargetIcon = true,
                            targetIcon = Icons.Default.Home,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (homeUri == null) 0.5f else 1f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AsyncImageWrapperTimeRuleItem(
                            uri = lockUri,
                            onClick = onLockClick,
                            onDelete = onDeleteLock,
                            showTargetIcon = true,
                            targetIcon = Icons.Default.Lock,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (lockUri == null) 0.5f else 1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onTimeClick() }
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            val statusText = when {
                bothUri != null -> "Ambas pantallas"
                homeUri != null && lockUri != null -> "Configuración mixta"
                homeUri != null -> "Solo Inicio"
                lockUri != null -> "Solo Bloqueo"
                else -> "Sin imágenes"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AsyncImageWrapperTimeRuleItem(
    modifier: Modifier = Modifier,
    uri: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    showTargetIcon: Boolean = false,
    targetIcon: ImageVector = Icons.Default.Home,
) {
    Box(modifier = modifier) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            when {
                uri != null -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .crossfade(200)
                            .size(240, 240)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                showTargetIcon -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = targetIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        if (uri != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .padding(2.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}