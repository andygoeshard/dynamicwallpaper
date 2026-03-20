package com.andyl.iris.ui.components

import android.R.attr.rotation
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andyl.iris.R

@Composable
fun IrisLoadingLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "IrisLoading")

    // Animación de rotación infinita
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing), // 1.5 seg por vuelta
            repeatMode = RepeatMode.Restart
        ),
        label = "LogoRotation"
    )
    Column{
        IrisLogo(
            modifier = Modifier.size(200.dp).graphicsLayer { rotationZ = rotation },
            color = color
        )

        Spacer(modifier.size(16.dp))

        Text(stringResource(R.string.loading))
    }
}