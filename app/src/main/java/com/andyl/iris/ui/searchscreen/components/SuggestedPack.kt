package com.andyl.iris.ui.searchscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.andyl.iris.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.PredefinedPack
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.ui.searchscreen.SuggestedPack

@Composable
fun SuggestedPacksList(
    heroes: Map<String, String> = emptyMap(),
    premiumRepository: PremiumRepository,
    onPackClick: (SuggestedPack) -> Unit,
    onUpsellClick: () -> Unit = {}
) {
    val isPremium by remember { mutableStateOf(premiumRepository.isPremium()) }
    val categories = remember {
        listOf(
            SuggestedPack.Days,
            SuggestedPack.Weather,
            SuggestedPack.Time
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- WEATHER BASED SECTION ---
        item {
            PackSectionHeader(stringResource(R.string.weather_atmosphere), stringResource(R.string.weather_atmosphere_desc))
            PackHorizontalRow(PredefinedPacks.weatherPacks, heroes, isPremium, onPackClick, onUpsellClick)
        }

        // --- WEEKLY SECTION ---
        item {
            PackSectionHeader(stringResource(R.string.weekly_calendars), stringResource(R.string.weekly_calendars_desc))
            PackHorizontalRow(PredefinedPacks.weeklyPacks, heroes, isPremium, onPackClick, onUpsellClick)
        }

        // --- TIME BASED SECTION ---
        item {
            PackSectionHeader(stringResource(R.string.time_overrides), stringResource(R.string.time_overrides_desc))
            PackHorizontalRow(PredefinedPacks.timePacks, heroes, isPremium, onPackClick, onUpsellClick)
        }

        // --- RANDOM SECTION ---
        item {
            PackSectionHeader(stringResource(R.string.surprise_random), stringResource(R.string.surprise_random_desc))
            PackHorizontalRow(PredefinedPacks.randomPacks, heroes, isPremium, onPackClick, onUpsellClick)
        }

        // --- TEMPERATURE SECTION ---
        item {
            PackSectionHeader(stringResource(R.string.temp_by_weather), stringResource(R.string.temp_by_weather_desc))
            PackHorizontalRow(PredefinedPacks.temperaturePacks, heroes, isPremium, onPackClick, onUpsellClick)
        }

        // --- MANUAL CONFIGURATION ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.manual_configuration),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        items(categories) { category ->
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                CategoryItem(category = category, onClick = { onPackClick(category) })
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PackSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PackHorizontalRow(
    packs: List<PredefinedPack>, 
    heroes: Map<String, String>,
    isPremium: Boolean,
    onPackClick: (SuggestedPack) -> Unit,
    onUpsellClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(packs) { pack ->
            val isLocked = pack.isPremium && !isPremium
            PredefinedPackCard(pack, heroes[pack.id], isLocked) {
                if (isLocked) {
                    onUpsellClick()
                } else {
                    onPackClick(SuggestedPack.Predefined(pack.id, pack.name, pack.description))
                }
            }
        }
    }
}

@Composable
private fun PredefinedPackCard(
    pack: PredefinedPack,
    heroUrl: String?,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .width(160.dp)
                .height(240.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = heroUrl ?: pack.previewUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Elegant gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                ),
                                startY = 300f
                            )
                        )
                )

                // Premium lock overlay
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.premium_pack_locked),
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                    // PRO badge
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PRO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Type Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            pack.isTimeBased -> stringResource(R.string.badge_time)
                            pack.type == PackType.WEEKLY -> stringResource(R.string.badge_weekly)
                            pack.type == PackType.TEMPERATURE -> stringResource(R.string.badge_temp)
                            pack.isFullRandom -> stringResource(R.string.badge_mix)
                            else -> stringResource(R.string.badge_weather)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = pack.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = pack.description,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun CategoryItem(
    category: SuggestedPack,
    onClick: () -> Unit
) {
    val icon = when (category) {
        SuggestedPack.Days -> Icons.Default.DateRange
        SuggestedPack.Weather -> Icons.Default.AddCircle
        SuggestedPack.Time -> Icons.Default.Create
        else -> Icons.Default.AddCircle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
