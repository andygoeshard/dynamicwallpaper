package com.andyl.iris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IrisLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(
        modifier = modifier.aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)

        val hexRadius = size.minDimension * 0.22f
        val lineLength = size.minDimension * 0.3f
        val stroke = size.minDimension * 0.015f

        val startFactor = 0.2f  // cuánto crece hacia atrás
        val endFactor = 0.9f    // cuánto crece hacia adelante

        // 🔵 Núcleo con glow sutil (sin cambios)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = center,
                radius = hexRadius * 1f
            ),
            radius = hexRadius * 1.2f,
            center = center
        )

        drawCircle(
            color = color,
            radius = hexRadius * 0.35f,
            center = center
        )

        val rotationOffset = Math.toRadians(50.0)

        repeat(6) { i ->
            val angle = Math.toRadians(i * 60.0) + rotationOffset

            // punto base en el hexágono
            val base = Offset(
                x = center.x + cos(angle).toFloat() * hexRadius,
                y = center.y + sin(angle).toFloat() * hexRadius
            )

            val dirAngle = angle + Math.toRadians(90.0)
            val dx = cos(dirAngle).toFloat()
            val dy = sin(dirAngle).toFloat()

            val lineStart = Offset(
                base.x - dx * lineLength * startFactor,
                base.y - dy * lineLength * startFactor
            )
            val lineEnd = Offset(
                base.x + dx * lineLength * endFactor,
                base.y + dy * lineLength * endFactor
            )

            val lineBrush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent, // Fade total en el extremo 'start'
                    0.5f to color.copy(alpha = 0.8f), // Transición rápida a semisólido
                    1.0f to color // Totalmente sólido en el extremo 'end'
                ),
                // Alineamos los puntos del gradiente con los extremos de la línea
                start = lineStart,
                end = lineEnd
            )

            drawLine(
                brush = lineBrush, // Usamos el Brush en lugar de 'color'
                start = lineStart,
                end = lineEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}