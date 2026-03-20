package com.andyl.iris.ui.components

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.andyl.iris.ui.components.IrisLogo
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

class GenerarIconoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun generarPngFielAlCodigo() {
        val tamaño = 1024 // Lo hacemos grande para que no se pixele nada
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // 1. Bitmap con transparencia total
        val bitmap = Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // 2. DrawScope manual para ejecutar TU lógica tal cual la escribiste
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(context),
            layoutDirection = LayoutDirection.Ltr,
            canvas = androidx.compose.ui.graphics.Canvas(canvas),
            size = Size(tamaño.toFloat(), tamaño.toFloat())
        ) {
            // --- TU CÓDIGO TAL CUAL ---
            val center = Offset(size.width / 2f, size.height / 2f)
            val hexRadius = size.minDimension * 0.22f
            val lineLength = size.minDimension * 0.3f
            val stroke = size.minDimension * 0.015f
            val color = Color(0xFF4DE1C1)
            val colorCentro = Color.White

            val startFactor = 0.2f
            val endFactor = 0.9f

            // Núcleo con tu glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                    center = center,
                    radius = hexRadius * 1f
                ),
                radius = hexRadius * 1.2f,
                center = center
            )
            drawCircle(color = colorCentro, radius = hexRadius * 0.35f, center = center)

            val rotationOffset = Math.toRadians(50.0)
            repeat(6) { i ->
                val angle = Math.toRadians(i * 60.0) + rotationOffset
                val base = Offset(
                    x = center.x + cos(angle).toFloat() * hexRadius,
                    y = center.y + sin(angle).toFloat() * hexRadius
                )
                val dirAngle = angle + Math.toRadians(90.0)
                val dx = cos(dirAngle).toFloat()
                val dy = sin(dirAngle).toFloat()

                val lineStart = Offset(base.x - dx * lineLength * startFactor, base.y - dy * lineLength * startFactor)
                val lineEnd = Offset(base.x + dx * lineLength * endFactor, base.y + dy * lineLength * endFactor)

                drawLine(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(0.0f to Color.Transparent, 0.5f to color.copy(alpha = 0.8f), 1.0f to color),
                        start = lineStart, end = lineEnd
                    ),
                    start = lineStart, end = lineEnd,
                    strokeWidth = stroke, cap = StrokeCap.Round
                )
            }
        }

        // 3. Guardar en Downloads
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "logo_iris_original2.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    }
}