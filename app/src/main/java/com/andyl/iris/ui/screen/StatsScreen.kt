package com.andyl.iris.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
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
import com.andyl.iris.domain.mapper.weatherFromKey
import com.andyl.iris.domain.model.PackInfo
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.ui.components.AppWallpaperBackground
import com.andyl.iris.ui.components.EmptyState
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val repository: UserPreferencesRepository = koinInject()

    var totalChanges by remember { mutableStateOf(0) }
    var history by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var weatherChanges by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var activePack by remember { mutableStateOf<PackInfo?>(null) }
    var rulesCount by remember { mutableStateOf(0) }
    var useWallpaperBackground by remember { mutableStateOf(false) }
    var lastAppliedWallpaper by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalChanges = repository.getAppSuccessCount()
        history = repository.getChangesHistory()
        weatherChanges = repository.getWeatherChanges()
        val activeId = repository.getActivePackId()
        activePack = repository.getAllPacks().find { it.id == activeId }
        rulesCount = runCatching {
            val config = repository.getWallpaperConfig(activeId)
            config.rules.size + config.dailyRules.size + config.fixedTimeRules.size
        }.getOrDefault(0)
        useWallpaperBackground = repository.getUseWallpaperBackground()
        lastAppliedWallpaper = repository.getLastAppliedWallpaper()
    }

    val last7Days = remember(history) {
        val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
        (6 downTo 0).map { offset ->
            val date = LocalDate.now().minusDays(offset.toLong())
            DayStat(
                label = date.format(formatter),
                count = history[date.toString()] ?: 0
            )
        }
    }
    val maxDayCount = last7Days.maxOfOrNull { it.count } ?: 0

    AppWallpaperBackground(
        wallpaperUri = lastAppliedWallpaper,
        enabled = useWallpaperBackground
    ) {
    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title), fontWeight = FontWeight.Bold) },
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Leaderboard,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = totalChanges.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = stringResource(R.string.stats_total_changes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                ConfigSection(title = stringResource(R.string.stats_last_7_days)) {
                    if (maxDayCount == 0) {
                        EmptyState(
                            emoji = "📊",
                            message = stringResource(R.string.stats_no_data)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            last7Days.forEach { day ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = day.count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height((day.count.toFloat() / maxDayCount * 96).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = day.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ConfigSection(title = stringResource(R.string.stats_by_weather)) {
                    if (weatherChanges.isEmpty()) {
                        EmptyState(
                            emoji = "🌤️",
                            message = stringResource(R.string.stats_no_data)
                        )
                    } else {
                        weatherChanges.entries
                            .sortedByDescending { it.value }
                            .forEach { (key, count) ->
                                val weather = weatherFromKey(key)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (weather) {
                                            Weather.Clear -> "☀️"
                                            Weather.Rain -> "🌧️"
                                            Weather.Storm -> "⛈️"
                                            Weather.Snow -> "❄️"
                                            Weather.Fog -> "🌫️"
                                            else -> "☁️"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(weather.stringRes),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                    }
                }
            }

            item {
                ConfigSection(title = stringResource(R.string.stats_config)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activePack?.name ?: "-",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.stats_rules, rulesCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

private data class DayStat(
    val label: String,
    val count: Int
)
