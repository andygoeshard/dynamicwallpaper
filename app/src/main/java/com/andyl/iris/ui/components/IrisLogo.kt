package com.andyl.iris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LogoDynamicMinimalista(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    backgroundColor: Color = Color.Black
) {
    Canvas(
        modifier = modifier.background(backgroundColor)
    ) {
        val center = Offset(size.width / 2, size.height / 2)

        val hexRadius = size.minDimension * 0.22f
        val lineLength = size.minDimension * 0.3f
        val stroke = size.minDimension * 0.015f

        val startFactor = 0.3f  // cuánto crece hacia atrás
        val endFactor = 0.9f    // cuánto crece hacia adelante

        // 🔵 Núcleo con glow sutil (solo acá)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = center,
                radius = hexRadius * 1.2f
            ),
            radius = hexRadius * 1.2f,
            center = center
        )

        drawCircle(
            color = color,
            radius = hexRadius * 0.35f,
            center = center
        )

        // 📐 6 lados del hexágono (pero como segmentos separados)
        val rotationOffset = Math.toRadians(50.0)

        repeat(6) { i ->
            val angle = Math.toRadians(i * 60.0) + rotationOffset

            // punto base en el hexágono
            val base = Offset(
                x = center.x + cos(angle).toFloat() * hexRadius,
                y = center.y + sin(angle).toFloat() * hexRadius
            )

            // dirección perpendicular (para que no apunten al centro)
            val dirAngle = angle + Math.toRadians(90.0)

            val dx = cos(dirAngle).toFloat()
            val dy = sin(dirAngle).toFloat()

            val start = Offset(
                base.x - dx * lineLength * startFactor,
                base.y - dy * lineLength * startFactor
            )

            val end = Offset(
                base.x + dx * lineLength * endFactor,
                base.y + dy * lineLength * endFactor
            )

            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = stroke,
                cap = StrokeCap.Round // si querés más punta: Butt
            )
        }
    }
}