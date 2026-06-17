package com.andyl.iris.ui.searchscreen.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.andyl.iris.R
import com.andyl.iris.domain.model.DownloadStatus
import com.andyl.iris.domain.model.DownloadTask
import kotlinx.coroutines.delay

@Composable
fun DownloadStatusSection(
    tasks: List<DownloadTask>,
    showDownloads: Boolean = false,
    onRemoveTask: (String) -> Unit,
    onToggleShow: (Boolean) -> Unit = {}
) {
    AnimatedVisibility(
        visible = tasks.isNotEmpty() && showDownloads,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onToggleShow(false) },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
                    .clickable(enabled = false) {}, 
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.active_downloads),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${tasks.size} task(s) in progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row {
                            if (tasks.any { it.status is DownloadStatus.Success || it.status is DownloadStatus.Error }) {
                                IconButton(
                                    onClick = { 
                                        tasks.filter { it.status is DownloadStatus.Success || it.status is DownloadStatus.Error }
                                            .forEach { onRemoveTask(it.id) }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear completed", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { onToggleShow(false) }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            DownloadStatusCard(task = task, onDismiss = { onRemoveTask(task.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadStatusCard(
    task: DownloadTask,
    onDismiss: () -> Unit
) {
    val containerColor = when (task.status) {
        is DownloadStatus.Success -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        is DownloadStatus.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentColor = when (task.status) {
        is DownloadStatus.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        is DownloadStatus.Error -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.fillMaxWidth(),
        border = if (task.status is DownloadStatus.Downloading) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) 
            else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val status = task.status) {
                    is DownloadStatus.Downloading -> {
                        CircularProgressIndicator(
                            progress = { status.progress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 3.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "${(status.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    is DownloadStatus.Success -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(32.dp)
                        )
                        LaunchedEffect(task.id) {
                            delay(4000)
                            onDismiss()
                        }
                    }
                    is DownloadStatus.Error -> {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DownloadStatus.Idle -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.packName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                when (val status = task.status) {
                    is DownloadStatus.Downloading -> {
                        Text(
                            stringResource(R.string.downloading_images, status.current, status.total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is DownloadStatus.Success -> {
                        Text(
                            stringResource(R.string.installation_complete),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is DownloadStatus.Error -> {
                        Text(
                            status.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 2
                        )
                    }
                    DownloadStatus.Idle -> {
                        Text(stringResource(R.string.preparing), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            IconButton(
                onClick = onDismiss, 
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
