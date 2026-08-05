package com.andyl.iris.ui.components

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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaperUri)
                    .crossfade(300)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
