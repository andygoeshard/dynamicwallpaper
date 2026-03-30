package com.andyl.iris.ui.components

import android.R.attr.checked
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState


@Composable
fun WeatherConfigCard(
    state: DynamicWallpaperUiState,
    weather: Weather,
    isEnabled: Boolean,
    onEvent: (WallpaperEvent) -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val alpha by animateFloatAsState(if (isEnabled) 1f else 0.6f, label = "alpha")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().alpha(alpha)
            ) {
                Text(text = when(weather) {
                    Weather.Clear -> "☀️"
                    Weather.Rain -> "🌧️"
                    Weather.Storm -> "⛈️"
                    Weather.Snow -> "❄️"
                    Weather.Fog -> "🌫️"
                    else -> "☁️"
                }, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.width(12.dp))

                Text(
                    text = stringResource(weather.stringRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val times = listOf(
                            TimeOfDay.DAWN to stringResource(R.string.weather_cfg_card_dawn),
                            TimeOfDay.DAY to stringResource(R.string.weather_cfg_car_day),
                            TimeOfDay.DUSK to stringResource(R.string.weather_cfg_card_dusk),
                            TimeOfDay.NIGHT to stringResource(R.string.weather_cfg_card_night)
                        )

                        times.forEach { (time, label) ->
                            SelectWallpaperButton(
                                weather = weather,
                                timeOfDay = time,
                                label = label,
                                modifier = Modifier.weight(1f), // Se reparten el ancho
                                state = state,
                                onEvent = onEvent
                            )
                        }
                    }
                }
            }
        }
    }
}