package com.andyl.iris.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.andyl.iris.ui.theme.*

@Composable
fun CyberpunkLoadingBar(
    progress: Float?, // Null for indeterminate
    modifier: Modifier = Modifier,
    label: String? = "SYSTEM INITIALIZING...",
    barHeight: Dp = 12.dp,
    useRounded: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading")
    val density = LocalDensity.current
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    
    val animatedProgress by if (progress == null) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "IndeterminateProgress"
        )
    } else {
        rememberUpdatedState(progress)
    }

    val shape = if (useRounded) {
        RoundedCornerShape(barHeight / 2)
    } else {
        val cutSize = with(density) { (barHeight / 2).toPx() }
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width - cutSize, 0f)
            lineTo(size.width, cutSize)
            lineTo(size.width, size.height)
            lineTo(cutSize, size.height)
            lineTo(0f, size.height - cutSize)
            close()
        }
    }

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanLine"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = label,
                    color = colorPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (progress != null) "${(progress * 100).toInt()}%" else "BUSY",
                    color = colorPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .border(1.dp, colorPrimary.copy(alpha = 0.5f), shape)
                .padding(if (barHeight > 4.dp) 2.dp else 1.dp)
                .clip(shape)
                .background(CyberDark)
        ) {
            // Background scan lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineCount = 15
                val spacing = size.width / lineCount
                for (i in 0..lineCount) {
                    drawLine(
                        color = colorPrimary.copy(alpha = 0.1f),
                        start = Offset(i * spacing, 0f),
                        end = Offset(i * spacing - 10f, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(colorSecondary, colorPrimary)
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        // Highlight/Glow effect at the end of progress
                        if (animatedProgress > 0f) {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    ),
                                    startX = size.width - 20.dp.toPx(),
                                    endX = size.width
                                )
                            )
                        }
                    }
            )

            // Moving scan line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = size.width * scanLineOffset
                drawLine(
                    color = colorPrimary.copy(alpha = 0.4f),
                    start = Offset(x, 0f),
                    end = Offset(x - 5f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun CyberpunkBox(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    useRounded: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val colorTertiary = MaterialTheme.colorScheme.tertiary
    
    val shape = if (useRounded) {
        RoundedCornerShape(24.dp)
    } else {
        val cutSize = with(density) { 12.dp.toPx() }
        GenericShape { size, _ ->
            moveTo(0f, cutSize)
            lineTo(cutSize, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - cutSize)
            lineTo(size.width - cutSize, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, colorTertiary.copy(alpha = 0.5f))
                ),
                shape = shape
            )
            .background(CyberDark.copy(alpha = 0.9f), shape)
            .padding(16.dp),
        content = content
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun CyberpunkLoadingBarPreview() {
    Column(modifier = Modifier.padding(20.dp)) {
        CyberpunkLoadingBar(progress = 0.4f)
        Spacer(modifier = Modifier.height(20.dp))
        CyberpunkLoadingBar(progress = 0.8f, label = "DOWNLOADING ASSETS...")
        Spacer(modifier = Modifier.height(40.dp))
        CyberpunkBox(modifier = Modifier.fillMaxWidth()) {
            Text("INTERFACE ACTIVE", color = CyberGreen)
        }
    }
}
