package com.andyl.iris.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.andyl.iris.R
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState

@Composable
fun SelectWallpaperButton(
    weather: Weather,
    timeOfDay: TimeOfDay,
    label: String,
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val key = "${weather.toKey()} - $timeOfDay"
    val currentUri = state.rules[key]
    val hasImage = !currentUri.isNullOrEmpty()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onEvent(WallpaperEvent.SetWallpaperRule(weather, timeOfDay, it.toString()))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    OutlinedCard(
        onClick = { launcher.launch(arrayOf("image/*")) },
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp), // Un poco más redondo queda mejor
        border = BorderStroke(
            width = 1.dp,
            color = if (hasImage) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentUri)
                        .crossfade(300)
                        .size(250, 400) // Tamaño optimizado para el A21s
                        .precision(Precision.EXACT)
                        .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                if (!hasImage) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasImage) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}