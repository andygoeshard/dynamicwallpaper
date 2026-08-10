package com.andyl.iris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Dotted-grid background drawn behind the home while in build mode. */
@Composable
fun DottedGridBackground(modifier: Modifier = Modifier) {
    val dotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    Canvas(modifier = modifier) {
        val spacing = 26.dp.toPx()
        val dotRadius = 2.dp.toPx()
        var x = spacing / 2
        while (x < size.width) {
            var y = spacing / 2
            while (y < size.height) {
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                y += spacing
            }
            x += spacing
        }
    }
}

/** Top bar shown in build mode with the cancel X. */
@Composable
fun BuildModeTopBar(onExit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Reordenar secciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
        IconButton(onClick = onExit) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancelar",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
