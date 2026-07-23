package com.andyl.iris.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.andyl.iris.R
import com.andyl.iris.domain.model.TemperatureRange
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState

@Composable
fun TemperatureRuleSection(
    state: DynamicWallpaperUiState,
    isPremium: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEvent: (WallpaperEvent) -> Unit,
    onNavigateToSearch: (com.andyl.iris.domain.model.Weather?, com.andyl.iris.domain.model.TimeOfDay?, String?, String?, String?) -> Unit,
    onUpsellClick: () -> Unit = {}
) {
    Column {
        Surface(
            onClick = {
                if (isPremium) onToggleExpand()
                else onUpsellClick()
            },
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.temp_by_weather),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isPremium) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.temp_by_weather_desc),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isPremium) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "temp_arrow"
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded && isPremium,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(400)) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(400)) + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val ranges = listOf(
                    TemperatureRange.FREEZING,
                    TemperatureRange.COLD,
                    TemperatureRange.COOL,
                    TemperatureRange.WARM,
                    TemperatureRange.HOT
                )
                val times = listOf(
                    TimeOfDay.DAWN to stringResource(R.string.weather_cfg_card_dawn),
                    TimeOfDay.DAY to stringResource(R.string.weather_cfg_card_day),
                    TimeOfDay.DUSK to stringResource(R.string.weather_cfg_card_dusk),
                    TimeOfDay.NIGHT to stringResource(R.string.weather_cfg_card_night)
                )

                ranges.forEach { tempRange ->
                    TemperatureRangeCard(
                        tempRange = tempRange,
                        state = state,
                        times = times,
                        onEvent = onEvent,
                        onNavigateToSearch = onNavigateToSearch
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TemperatureRangeCard(
    tempRange: TemperatureRange,
    state: DynamicWallpaperUiState,
    times: List<Pair<TimeOfDay, String>>,
    onEvent: (WallpaperEvent) -> Unit,
    onNavigateToSearch: (com.andyl.iris.domain.model.Weather?, com.andyl.iris.domain.model.TimeOfDay?, String?, String?, String?) -> Unit
) {
    val emoji = when (tempRange) {
        TemperatureRange.FREEZING -> "\u2744\uFE0F"
        TemperatureRange.COLD -> "\uD83C\uDF2B\uFE0F"
        TemperatureRange.COOL -> "\uD83C\uDF43"
        TemperatureRange.WARM -> "\u2600\uFE0F"
        TemperatureRange.HOT -> "\uD83D\uDD25"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(tempRange.stringRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                times.forEach { (time, label) ->
                    TempSlotButton(
                        tempRange = tempRange,
                        timeOfDay = time,
                        label = label,
                        state = state,
                        onEvent = onEvent,
                        onNavigateToSearch = onNavigateToSearch,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TempSlotButton(
    tempRange: TemperatureRange,
    timeOfDay: TimeOfDay,
    label: String,
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit,
    onNavigateToSearch: (com.andyl.iris.domain.model.Weather?, com.andyl.iris.domain.model.TimeOfDay?, String?, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyBoth = "${tempRange.name}-${timeOfDay.name}"
    val keyHome = "$keyBoth-1"
    val keyLock = "$keyBoth-2"

    val uriBoth = state.temperatureRules[keyBoth]
    val uriHome = state.temperatureRules[keyHome]
    val uriLock = state.temperatureRules[keyLock]

    val hasAnyImage = !uriBoth.isNullOrEmpty() || !uriHome.isNullOrEmpty() || !uriLock.isNullOrEmpty()

    OutlinedCard(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasAnyImage) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !uriBoth.isNullOrEmpty() -> {
                    Box(Modifier.fillMaxSize().clickable {
                        onNavigateToSearch(null, timeOfDay, null, null, label)
                    }) {
                        TempPreviewImage(uri = uriBoth)
                    }
                }
                hasAnyImage -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable {
                            onNavigateToSearch(null, timeOfDay, null, null, "$label (H)")
                        }) {
                            TempPreviewImage(
                                uri = uriHome,
                                modifier = Modifier.fillMaxSize().alpha(if (uriHome == null) 0.5f else 1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp).background(Color.Black.copy(0.2f)))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable {
                            onNavigateToSearch(null, timeOfDay, null, null, "$label (L)")
                        }) {
                            TempPreviewImage(
                                uri = uriLock,
                                modifier = Modifier.fillMaxSize().alpha(if (uriLock == null) 0.5f else 1f)
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize().clickable {
                        onNavigateToSearch(null, timeOfDay, null, null, label)
                    }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (hasAnyImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 40f
                            )
                        )
                )
            }

            Text(
                text = label,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasAnyImage) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TempPreviewImage(
    uri: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(300)
            .size(200, 300)
            .precision(Precision.EXACT)
            .bitmapConfig(android.graphics.Bitmap.Config.ARGB_8888)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
