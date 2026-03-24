package com.andyl.iris.ui.components

import android.R.attr.rotation
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andyl.iris.R

@Composable
fun IrisLoadingLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "IrisLoading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing), // Más natural que el Linear
            repeatMode = RepeatMode.Restart
        ),
        label = "LogoRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineHalf),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    // 3. Alpha para el texto (parpadeo suave)
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TextAlpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IrisLogo(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                },
            color = color
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.loading).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = color.copy(alpha = textAlpha)
        )
    }
}

val SineHalf = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)