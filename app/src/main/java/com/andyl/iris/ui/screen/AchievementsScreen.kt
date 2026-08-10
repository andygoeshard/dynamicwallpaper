package com.andyl.iris.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.domain.model.Achievement
import com.andyl.iris.domain.model.AchievementDefinitions
import com.andyl.iris.domain.model.AchievementId
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.ui.components.AppWallpaperBackground
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val repository: UserPreferencesRepository = koinInject()

    var achievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var streak by remember { mutableStateOf(0) }
    var useWallpaperBackground by remember { mutableStateOf(false) }
    var lastAppliedWallpaper by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        useWallpaperBackground = repository.getUseWallpaperBackground()
        lastAppliedWallpaper = repository.getLastAppliedWallpaper()
        streak = computeStreak(repository.getChangesHistory())
        achievements = loadAchievements(repository)
    }

    val unlockedCount = achievements.count { it.unlocked }

    AppWallpaperBackground(
        wallpaperUri = lastAppliedWallpaper,
        enabled = useWallpaperBackground
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.ach_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back)) }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.ach_progress, unlockedCount, AchievementDefinitions.size),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { if (AchievementDefinitions.isEmpty()) 0f else unlockedCount.toFloat() / AchievementDefinitions.size },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🔥",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.ach_streak, streak),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (achievements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.ach_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(
                    count = achievements.size,
                    key = { index -> achievements[index].definition.id.name }
                ) { index ->
                    AchievementCard(achievement = achievements[index])
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val unlocked = achievement.unlocked
    val container = if (unlocked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.definition.emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.alpha(if (unlocked) 1f else 0.45f)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(achievement.definition.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (unlocked) {
                        Text(
                            text = stringResource(R.string.ach_unlocked_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(achievement.definition.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.ach_progress_value, achievement.value, achievement.definition.target),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private suspend fun loadAchievements(repository: UserPreferencesRepository): List<Achievement> {
    val totalChanges = repository.getAppSuccessCount()
    val daysActive = repository.getChangesHistory().size
    val distinctWeathers = repository.getWeatherChanges().size
    val packsCount = repository.getAllPacks().size
    val rulesCount = runCatching {
        val activeId = repository.getActivePackId()
        val config = repository.getWallpaperConfig(activeId)
        config.rules.size + config.dailyRules.size + config.fixedTimeRules.size + config.temperatureRules.size
    }.getOrDefault(0)

    val values = mapOf(
        AchievementId.FIRST_CHANGE to totalChanges,
        AchievementId.CHANGES_10 to totalChanges,
        AchievementId.CHANGES_50 to totalChanges,
        AchievementId.CHANGES_100 to totalChanges,
        AchievementId.DAYS_7 to daysActive,
        AchievementId.DAYS_30 to daysActive,
        AchievementId.WEATHER_3 to distinctWeathers,
        AchievementId.WEATHER_ALL to distinctWeathers,
        AchievementId.PACKS_5 to packsCount,
        AchievementId.RULES_10 to rulesCount
    )

    return AchievementDefinitions.map { definition ->
        Achievement(definition = definition, value = values[definition.id] ?: 0)
    }
}

private fun computeStreak(history: Map<String, Int>): Int {
    if (history.isEmpty()) return 0
    val dates = history.keys.mapNotNull { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }.toSet()
    if (dates.isEmpty()) return 0

    var streak = 0
    var day = java.time.LocalDate.now()
    if (!dates.contains(day)) {
        day = day.minusDays(1)
    }
    while (dates.contains(day)) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}
