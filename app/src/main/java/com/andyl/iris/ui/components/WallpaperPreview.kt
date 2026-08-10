package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.andyl.iris.domain.model.ScaleMode

@Composable
fun WallpaperPreviewImage(
    uri: String?,
    scaleMode: ScaleMode,
    overlayText: String?,
    overlayEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val contentScale = when (scaleMode) {
        ScaleMode.CROP -> ContentScale.Crop
        ScaleMode.STRETCH -> ContentScale.FillBounds
        ScaleMode.FIT -> ContentScale.Fit
    }

    Box(modifier = modifier) {
        if (uri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(200)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        if (overlayEnabled && !overlayText.isNullOrBlank()) {
            WallpaperOverlayPreview(
                text = overlayText,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun WallpaperOverlayPreview(
    text: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val fontPx = maxWidth.value * 0.045f
        val scrimHeightPx = fontPx * 2.6f
        val topPx = maxHeight.value * 0.70f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = topPx.dp)
                .height(scrimHeightPx.dp)
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = fontPx.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
