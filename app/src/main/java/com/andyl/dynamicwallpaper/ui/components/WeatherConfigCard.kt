package com.andyl.dynamicwallpaper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.dynamicwallpaper.domain.mapper.toKey
import com.andyl.dynamicwallpaper.domain.model.TimeOfDay
import com.andyl.dynamicwallpaper.domain.model.Weather
import com.andyl.dynamicwallpaper.ui.event.WallpaperEvent
import com.andyl.dynamicwallpaper.ui.state.DynamicWallpaperUiState


@Composable
fun WeatherConfigCard(
    state: DynamicWallpaperUiState,
    weather: Weather,
    isEnabled: Boolean,
    onEvent: (WallpaperEvent) -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val alpha by animateFloatAsState(if (isEnabled) 1f else 0.5f, label = "alpha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = when(weather) {
                    Weather.Clear -> "☀️"
                    Weather.Rain -> "🌧️"
                    Weather.Storm -> "⛈️"
                    Weather.Snow -> "❄️"
                    Weather.Fog -> "🌫️"
                    else -> "☁️"
                })
                Spacer(Modifier.width(8.dp))
                Text(
                    text = weather.toKey().uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val times = listOf(
                    TimeOfDay.DAWN to "Amanecer",
                    TimeOfDay.DAY to "Día",
                    TimeOfDay.DUSK to "Tarde",
                    TimeOfDay.NIGHT to "Noche"
                )

                times.forEach { (time, label) ->
                    SelectWallpaperButton(
                        weather = weather,
                        timeOfDay = time,
                        label = label,
                        modifier = Modifier.weight(1f),
                        state = state,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}