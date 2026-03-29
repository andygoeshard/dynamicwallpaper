package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun DayImageCard(
    dayName: String,
    homeUri: String? = null,
    lockUri: String? = null,
    bothUri: String? = null,
    isToday: Boolean,
    onAddClick: () -> Unit,
    onHomeClick: () -> Unit,
    onLockClick: () -> Unit,
    onDeleteHome: () -> Unit,
    onDeleteLock: () -> Unit,
    onDeleteBoth: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier.size(height = 100.dp, width = 85.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                bothUri != null -> {
                    AsyncImageWrapper(
                        uri = bothUri,
                        onClick = onAddClick,
                        onDelete = onDeleteBoth,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                homeUri != null || lockUri != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        AsyncImageWrapper(
                            uri = homeUri,
                            onClick = onHomeClick,
                            onDelete = onDeleteHome,
                            showTargetIcon = homeUri == null,
                            targetIcon = Icons.Default.Home,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (homeUri == null) 0.5f else 1f)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        AsyncImageWrapper(
                            uri = lockUri,
                            onClick = onLockClick,
                            onDelete = onDeleteLock,
                            showTargetIcon = lockUri == null,
                            targetIcon = Icons.Default.Lock,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (lockUri == null) 0.5f else 1f)
                        )
                    }
                }

                else -> {
                    Card(
                        onClick = onAddClick,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AsyncImageWrapper(
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
            if (uri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(200)
                        .size(200, 200)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (showTargetIcon) {
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
            } else {
                Box(Modifier.fillMaxSize().background(Color.Gray.copy(0.1f)))
            }
        }

        if (uri != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .padding(2.dp)
                    .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape)
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