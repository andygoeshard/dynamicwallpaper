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
import com.andyl.iris.ui.components.CyberpunkBox
import com.andyl.iris.ui.components.CyberpunkLoadingBar
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
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onToggleShow(false) },
            contentAlignment = Alignment.BottomCenter
        ) {
            CyberpunkBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
                    .clickable(enabled = false) {},
                borderColor = MaterialTheme.colorScheme.primary
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
    val borderColor = when (task.status) {
        is DownloadStatus.Success -> Color(0xFF2E7D32).copy(alpha = 0.5f)
        is DownloadStatus.Error -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    }

    CyberpunkBox(
        modifier = Modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.packName.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val status = task.status) {
                is DownloadStatus.Downloading -> {
                    CyberpunkLoadingBar(
                        progress = status.progress,
                        label = stringResource(R.string.downloading_images, status.current, status.total).uppercase(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is DownloadStatus.Success -> {
                    Text(
                        stringResource(R.string.installation_complete).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    LaunchedEffect(task.id) {
                        delay(4000)
                        onDismiss()
                    }
                }
                is DownloadStatus.Error -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            status.message.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 2,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                DownloadStatus.Idle -> {
                    CyberpunkLoadingBar(progress = null, label = "PREPARING...", modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
