package com.andyl.iris.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.domain.model.DailyForecast
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.LocationRepository
import com.andyl.iris.domain.repository.WeatherInfo
import com.andyl.iris.domain.repository.WeatherRepository
import com.andyl.iris.ui.components.AppWallpaperBackground
import com.andyl.iris.ui.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastScreen(onBack: () -> Unit) {
    val locationRepository: LocationRepository = koinInject()
    val weatherRepository: WeatherRepository = koinInject()
    val prefsRepository: com.andyl.iris.domain.repository.UserPreferencesRepository = koinInject()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf<WeatherInfo?>(null) }
    var forecast by remember { mutableStateOf<List<DailyForecast>>(emptyList()) }
    var useWallpaperBackground by remember { mutableStateOf(false) }
    var lastAppliedWallpaper by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        useWallpaperBackground = prefsRepository.getUseWallpaperBackground()
        lastAppliedWallpaper = prefsRepository.getLastAppliedWallpaper()
        runCatching {
            val location = locationRepository.getCurrentLocation()
            val currentWeather = weatherRepository.getCurrentWeather(location)
            val daily = weatherRepository.getDailyForecast(location, 7)
            current to daily
        }.onSuccess { (currentWeather, daily) ->
            current = currentWeather
            forecast = daily
            error = false
        }.onFailure {
            error = true
        }
        loading = false
    }

    AppWallpaperBackground(
        wallpaperUri = lastAppliedWallpaper,
        enabled = useWallpaperBackground
    ) {
    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_forecast_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back)) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when {
                loading -> {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                error -> {
                    item {
                        Text(
                            text = stringResource(R.string.weather_forecast_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                else -> {
                    item {
                        CurrentWeatherHero(
                            current = current,
                            today = forecast.firstOrNull()
                        )
                    }

                    item {
                        ConfigSection(title = stringResource(R.string.weather_forecast_week)) {
                            if (forecast.isEmpty()) {
                                EmptyState(
                                    emoji = "🌤️",
                                    message = stringResource(R.string.weather_forecast_empty)
                                )
                            } else {
                                forecast.forEach { day ->
                                    ForecastDayRow(day)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun CurrentWeatherHero(
    current: WeatherInfo?,
    today: DailyForecast?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val weather = current?.weather ?: today?.weather
            if (weather != null) {
                Text(
                    text = weatherEmoji(weather),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = stringResource(weather.stringRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            val temp = current?.temperature
            val max = today?.tempMax
            val min = today?.tempMin
            val tempText = when {
                temp != null && max != null && min != null -> "${temp.roundToInt()}°"
                else -> null
            }
            val rangeText = when {
                max != null && min != null -> "${min.roundToInt()}° / ${max.roundToInt()}°"
                max != null -> "${max.roundToInt()}°"
                else -> null
            }

            if (tempText != null || rangeText != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (tempText != null) {
                        Text(
                            text = tempText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (rangeText != null) {
                        Text(
                            text = stringResource(R.string.weather_forecast_range, rangeText),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            val precip = today?.precipitationProbability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeroDetail(
                    icon = Icons.Default.WaterDrop,
                    label = stringResource(R.string.weather_forecast_precip),
                    value = if (precip != null) "$precip%" else "—"
                )
                HeroDetail(
                    icon = Icons.Default.WbSunny,
                    label = stringResource(R.string.weather_forecast_sunrise),
                    value = formatTime(current?.sunrise)
                )
                HeroDetail(
                    icon = Icons.Default.Cloud,
                    label = stringResource(R.string.weather_forecast_sunset),
                    value = formatTime(current?.sunset)
                )
            }
        }
    }
}

@Composable
private fun HeroDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ForecastDayRow(day: DailyForecast) {
    val isToday = day.date == LocalDate.now().toString()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDayLabel(day.date),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = weatherEmoji(day.weather),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6f)
        )
        val precip = day.precipitationProbability
        Text(
            text = if (precip != null && precip > 0) "$precip%" else "—",
            style = MaterialTheme.typography.labelMedium,
            color = if (precip != null && precip >= 50) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(0.6f)
        )
        val min = day.tempMin?.roundToInt()
        val max = day.tempMax?.roundToInt()
        Text(
            text = when {
                min != null && max != null -> "${min}° · ${max}°"
                max != null -> "${max}°"
                else -> "—"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun weatherEmoji(weather: Weather): String = when (weather) {
    Weather.Clear -> "☀️"
    Weather.Cloudy -> "☁️"
    Weather.Rain -> "🌧️"
    Weather.Snow -> "❄️"
    Weather.Fog -> "🌫️"
    Weather.Storm -> "⛈️"
}

private fun formatDayLabel(date: String): String {
    return runCatching {
        val day = LocalDate.parse(date)
        if (day == LocalDate.now()) {
            val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
            day.format(formatter)
        } else {
            val formatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
            day.format(formatter)
        }
    }.getOrDefault(date)
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return iso.substringAfter('T', iso).take(5)
}
