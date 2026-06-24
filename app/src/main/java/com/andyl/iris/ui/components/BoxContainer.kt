package com.andyl.iris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.andyl.iris.ui.theme.CyberDark

@Composable
fun BoxContainer(
    modifier: Modifier = Modifier,
    useCyberStyle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    
    // Volvemos a bordes redondeados por compatibilidad visual
    val shape = RoundedCornerShape(24.dp)

    val backgroundColor = if (useCyberStyle) {
        CyberDark.copy(alpha = 0.8f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }

    val borderBrush = if (useCyberStyle) {
        Brush.linearGradient(listOf(colorPrimary.copy(alpha = 0.5f), colorSecondary.copy(alpha = 0.3f)))
    } else {
        Brush.linearGradient(listOf(
            if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
            if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
        ))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderBrush, shape)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
