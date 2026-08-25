package com.andyl.iris.ui.components

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.domain.helper.LiveTarget
import com.andyl.iris.domain.helper.LiveTargetStore
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.service.IrisLiveWallpaperService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File

@Composable
fun LocalVideosSection(
    onError: (String) -> Unit = {},
    onVideoApplied: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesRepository = koinInject<UserPreferencesRepository>()

    // Broad picker: every video container the device knows plus animated
    // images, which "video/*" alone hides in most file managers.
    val pickLauncher = rememberLauncherForActivityResult(
        object : ActivityResultContracts.GetContent() {
            override fun createIntent(context: Context, input: String): Intent {
                return super.createIntent(context, input).apply {
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("video/*", "image/gif", "image/webp")
                    )
                }
            }
        }
    ) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val dir = File(context.filesDir, "live_video").apply { mkdirs() }
                    // Keep the original extension (gifs picked here would
                    // otherwise be misnamed .mp4).
                    val ext = context.contentResolver.query(
                        it, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
                    )?.use { c ->
                        if (c.moveToFirst()) {
                            c.getString(0)?.substringAfterLast('.', "")?.lowercase()?.take(5)
                        } else null
                    }
                    val out = File(dir, "custom_${System.currentTimeMillis()}.${ext?.ifEmpty { null } ?: "mp4"}")
                    context.contentResolver.openInputStream(it)?.use { input ->
                        out.outputStream().use { o -> input.copyTo(o) }
                    } ?: throw Exception(context.getString(R.string.local_videos_error))
                    preferencesRepository.setLiveVideoPath(out.absolutePath)
                    preferencesRepository.setLiveVideoEnabled(true)
                    preferencesRepository.setActiveVideoPackId(null)
                    LiveTargetStore.write(context, LiveTarget(isVideo = true, path = out.absolutePath))
                    onVideoApplied?.invoke(out.absolutePath)
                    openLiveWallpaperPicker(context)
                }.onFailure { e ->
                    onError(e.message ?: context.getString(R.string.local_videos_error))
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.local_videos_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.local_videos_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { pickLauncher.launch("*/*") },
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp, vertical = 10.dp
                )
            ) {
                Text(stringResource(R.string.local_videos_pick), fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun openLiveWallpaperPicker(context: Context) {
    val component = ComponentName(context, IrisLiveWallpaperService::class.java)
    runCatching {
        context.startActivity(
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            }
        )
    }.onFailure {
        android.util.Log.e("LIVE_WALLPAPER", "ACTION_CHANGE_LIVE_WALLPAPER failed, fallback...", it)
        runCatching {
            context.startActivity(
                Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            )
        }.onFailure { e2 ->
            android.util.Log.e("LIVE_WALLPAPER", "Could not open live wallpaper picker", e2)
        }
    }
}
