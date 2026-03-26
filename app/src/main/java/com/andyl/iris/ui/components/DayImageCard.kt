package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.andyl.iris.R

@Composable
fun DayImageCard(
    dayName: String,
    homeUri: String? = null,    // Target 1 (Arriba)
    lockUri: String? = null,    // Target 2 (Abajo)
    bothUri: String? = null,    // Target 3 (Única)
    isToday: Boolean,
    onAddClick: () -> Unit,      // Cuando está vacío o querés agregar
    onHomeClick: () -> Unit,     // Click específico en la imagen de Inicio
    onLockClick: () -> Unit,     // Click específico en la imagen de Bloqueo
    onDeleteHome: () -> Unit,
    onDeleteLock: () -> Unit,
    onDeleteBoth: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp) // Ancho del Card completo
    ) {
        // Contenedor principal del slot (un poco más alto para la columna)
        Box(
            modifier = Modifier.size(height = 100.dp, width = 85.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. CASO: AMBOS SETEADOS (Una sola imagen grande)
                bothUri != null -> {
                    AsyncImageWrapper(
                        uri = bothUri,
                        onClick = onAddClick, // Si clickea, cambiamos la dupla
                        onDelete = onDeleteBoth,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 2. CASO: CAPAS SEPARADAS (Columna Vertical)
                homeUri != null || lockUri != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // IMAGEN DE INICIO (Arriba - Target 1)
                        AsyncImageWrapper(
                            uri = homeUri,
                            onClick = onHomeClick,
                            onDelete = onDeleteHome,
                            showTargetIcon = homeUri == null, // Icono si falta imagen
                            targetIcon = Icons.Default.Home,
                            modifier = Modifier
                                .weight(1f) // Ocupa la mitad
                                .fillMaxWidth()
                                .alpha(if (homeUri == null) 0.5f else 1f)
                        )

                        // Pequeño divisor visual o spacer
                        Spacer(modifier = Modifier.height(2.dp))

                        // IMAGEN DE BLOQUEO (Abajo - Target 2)
                        AsyncImageWrapper(
                            uri = lockUri,
                            onClick = onLockClick,
                            onDelete = onDeleteLock,
                            showTargetIcon = lockUri == null, // Icono si falta imagen
                            targetIcon = Icons.Default.Lock,
                            modifier = Modifier
                                .weight(1f) // Ocupa la otra mitad
                                .fillMaxWidth()
                                .alpha(if (lockUri == null) 0.5f else 1f)
                        )
                    }
                }

                // 3. CASO: VACÍO (El clásico cuadro con el "+")
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
private fun AsyncImageWrapper(
    uri: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    showTargetIcon: Boolean = false,
    targetIcon: ImageVector = Icons.Default.Home,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp), // Esquinas un poco más cerradas para la columna
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
                        .size(200, 200) // Miniatura liviana
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (showTargetIcon) {
                // Estado "fantasma" con icono de target (Home o Lock)
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
                // Placeholder gris simple (para el caso "both" si fallara)
                Box(Modifier.fillMaxSize().background(Color.Gray.copy(0.1f)))
            }
        }

        // Botón de borrar miniatura (reutilizamos tu lógica)
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