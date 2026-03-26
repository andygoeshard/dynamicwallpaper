package com.andyl.iris.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    // Estados para el flujo del picker y el diálogo
    var pendingUri by remember { mutableStateOf<String?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var targetPredefined by remember { mutableStateOf<Int?>(null) }

    // --- LÓGICA DE BUSQUEDA POR TARGET ---
    val keyHome = "${weather.toKey()} - $timeOfDay - 1"
    val keyLock = "${weather.toKey()} - $timeOfDay - 2"
    val keyBoth = "${weather.toKey()} - $timeOfDay - 3"

    val uriHome = state.rules[keyHome]
    val uriLock = state.rules[keyLock]
    val uriBoth = state.rules[keyBoth]

    val hasAnyImage = !uriHome.isNullOrEmpty() || !uriLock.isNullOrEmpty() || !uriBoth.isNullOrEmpty()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                if (targetPredefined != null) {
                    // Si ya sabemos a donde va (click en mitad especifica)
                    onEvent(WallpaperEvent.SetWallpaperRule(weather, timeOfDay, it.toString(), targetPredefined!!))
                } else {
                    // Si es nuevo o "Ambos", disparamos el Dialog
                    pendingUri = it.toString()
                    showTargetDialog = true
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    OutlinedCard(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasAnyImage) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- RENDERIZADO EN COLUMNA ---
            when {
                !uriBoth.isNullOrEmpty() -> {
                    // Caso: Ambos (Imagen completa)
                    Box(Modifier.fillMaxSize().clickable {
                        targetPredefined = null
                        launcher.launch(arrayOf("image/*"))
                    }) {
                        WallpaperPreviewImage(uri = uriBoth)
                    }
                }
                hasAnyImage -> {
                    // Caso: Columna Vertical (Home arriba, Lock abajo)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // MITAD SUPERIOR: HOME
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable {
                            targetPredefined = 1
                            launcher.launch(arrayOf("image/*"))
                        }) {
                            WallpaperPreviewImage(
                                uri = uriHome,
                                modifier = Modifier.fillMaxSize().alpha(if (uriHome == null) 0.5f else 1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp).background(Color.Black.copy(0.2f)))

                        // MITAD INFERIOR: LOCK
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable {
                            targetPredefined = 2
                            launcher.launch(arrayOf("image/*"))
                        }) {
                            WallpaperPreviewImage(
                                uri = uriLock,
                                modifier = Modifier.fillMaxSize().alpha(if (uriLock == null) 0.5f else 1f)
                            )
                        }
                    }
                }
                else -> {
                    // Caso: Vacío (Click abre Dialog)
                    Box(modifier = Modifier.fillMaxSize().clickable {
                        targetPredefined = null
                        launcher.launch(arrayOf("image/*"))
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Gradiente para legibilidad del texto
            if (hasAnyImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 60f
                            )
                        )
                )
            }

            // Texto inferior (Label)
            Text(
                text = label,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (hasAnyImage) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // --- DIÁLOGO ---
    if (showTargetDialog) {
        WallpaperTargetDialog(
            onDismiss = { showTargetDialog = false },
            onConfirm = { target ->
                pendingUri?.let { uri ->
                    onEvent(WallpaperEvent.SetWallpaperRule(weather, timeOfDay, uri, target))
                }
                showTargetDialog = false
            }
        )
    }
}

@Composable
private fun WallpaperPreviewImage(
    uri: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(300)
            .size(200, 300)
            .precision(Precision.EXACT)
            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}