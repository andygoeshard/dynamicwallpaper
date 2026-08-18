package com.andyl.iris.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.andyl.iris.domain.helper.isVideoUri

@Composable
fun AppWallpaperBackground(
    wallpaperUri: String?,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (enabled && wallpaperUri != null) {
            AnimatedContent(
                targetState = wallpaperUri,
                transitionSpec = {
                    (fadeIn(tween(450)) togetherWith fadeOut(tween(450)))
                        .using(SizeTransform(clip = false))
                },
                label = "wallpaperBg"
            ) { uri ->
                if (isVideoUri(uri)) {
                    VideoWallpaperThumbnail(uri = uri, modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uri)
                            .crossfade(300)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.45f else 0.72f),
                                MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.65f else 0.9f)
                            )
                        )
                    )
            )
        }
        content()
    }
}
