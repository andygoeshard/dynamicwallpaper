package com.andyl.dynamicwallpaper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.ui.event.WallpaperEvent
import com.andyl.dynamicwallpaper.ui.state.DynamicWallpaperUiState

@Composable
fun WeatherSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    Column {
        Surface(
            onClick = { onEvent(WallpaperEvent.OnToggleWeatherFeature) },
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Configuración por Clima",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Toca para expandir y configurar",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                val rotation by animateFloatAsState(
                    targetValue = if (state.isWeatherFeatureEnabled) 180f else 0f,
                    label = "ArrowRotation"
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = state.isWeatherFeatureEnabled,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(400)) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(400)) + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val weathers = listOf(
                    Weather.Clear, Weather.Cloudy, Weather.Rain,
                    Weather.Snow, Weather.Fog, Weather.Storm
                )

                weathers.forEach { weather ->
                    WeatherConfigCard(
                        weather = weather,
                        isEnabled = state.enabledWeathers.contains(weather),
                        onToggle = { onEvent(WallpaperEvent.OnToggleWeather(weather)) },
                        state = state,
                        onEvent = onEvent
                    )
                }

                Text(
                    "Mutea climas individuales para mantener el fondo anterior.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
            }
        }
    }
}