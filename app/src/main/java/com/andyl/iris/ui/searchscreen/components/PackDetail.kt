package com.andyl.iris.ui.searchscreen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andyl.iris.R
import com.andyl.iris.domain.mapper.toKey
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.domain.model.WallpaperRule
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.ui.components.CyberpunkLoadingBar
import com.andyl.iris.ui.searchscreen.SuggestedPack

private data class SlotStatus(
    val uriBoth: String? = null,
    val uriHome: String? = null,
    val uriLock: String? = null,
) {
    val hasAny = (uriBoth != null || uriHome != null || uriLock != null)
    val bestUri = uriBoth ?: uriHome ?: uriLock
}

private fun getSlotStatus(
    slot: SlotData,
    rules: Map<String, WallpaperRule>,
    dailyRules: Map<String, String>,
    fixedRules: Map<String, String>
): SlotStatus {
    return if (slot.dayName != null) {
        SlotStatus(
            uriBoth = dailyRules[slot.dayName] ?: dailyRules["${slot.dayName}-3"],
            uriHome = dailyRules["${slot.dayName}-1"],
            uriLock = dailyRules["${slot.dayName}-2"]
        )
    } else if (slot.fixedTime != null) {
        SlotStatus(
            uriBoth = fixedRules[slot.fixedTime],
            uriHome = fixedRules["${slot.fixedTime}-1"],
            uriLock = fixedRules["${slot.fixedTime}-2"]
        )
    } else if (slot.weather != null && slot.time != null) {
        val baseKey = "${slot.weather.toKey()} - ${slot.time}"
        SlotStatus(
            uriBoth = rules["$baseKey - 3"]?.wallpaperId?.value,
            uriHome = rules["$baseKey - 1"]?.wallpaperId?.value,
            uriLock = rules["$baseKey - 2"]?.wallpaperId?.value
        )
    } else SlotStatus()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackDetailList(
    pack: SuggestedPack,
    rules: Map<String, WallpaperRule>,
    dailyRules: Map<String, String>,
    fixedRules: Map<String, String> = emptyMap(),
    previewImages: List<String?> = emptyList(),
    isLoading: Boolean = false,
    premiumRepository: PremiumRepository,
    onSlotClick: (Weather?, TimeOfDay?, String?, String?, String) -> Unit,
    onDownloadFullPack: (SuggestedPack) -> Unit,
    onLongPressInstall: () -> Unit = {},
    onAddCustomTime: () -> Unit = {},
    onUpsellClick: () -> Unit = {}
) {
    val isPremium by premiumRepository.observePremiumStatus().collectAsState(initial = premiumRepository.isPremium())
    val isPackLocked = pack is SuggestedPack.Predefined && !premiumRepository.isPackUnlocked(pack.packId)
    // ... (slots calculation logic remains the same to not break functionality)
    val slots = when (pack) {
        SuggestedPack.Days -> {
            listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                .map { day ->
                    SlotData(
                        label = day.replaceFirstChar { it.uppercase() },
                        dayName = day
                    )
                }
        }
        SuggestedPack.Weather -> {
            Weather.all().flatMap { weather ->
                TimeOfDay.entries.map { time ->
                    SlotData(
                        label = "",
                        labelRes = weather.stringRes,
                        weather = weather,
                        time = time
                    )
                }
            }
        }
        SuggestedPack.Time -> {
            val defaultTimes = listOf(
                "06:00" to "Dawn",
                "09:00" to "Day",
                "18:00" to "Dusk",
                "21:00" to "Night"
            )
            
            val standardSlots = defaultTimes.map { (time, label) ->
                SlotData(
                    label = label,
                    fixedTime = time
                )
            }

            val customOverrideSlots = fixedRules.keys
                .map { it.split("-")[0] }
                .distinct()
                .filter { time -> defaultTimes.none { it.first == time } }
                .sorted()
                .map { time ->
                    SlotData(
                        label = "Override: $time",
                        fixedTime = time
                    )
                }

            standardSlots + customOverrideSlots
        }
        is SuggestedPack.Predefined -> {
            val predefined = PredefinedPacks.packs.find { it.id == pack.id }
            
            when {
                predefined?.isTimeBased == true -> {
                    TimeOfDay.entries.mapIndexed { i, tod ->
                        val time = when(tod) {
                            TimeOfDay.DAWN -> "06:00"
                            TimeOfDay.DAY -> "10:00"
                            TimeOfDay.DUSK -> "18:00"
                            TimeOfDay.NIGHT -> "22:00"
                        }
                        SlotData(
                            label = "Alarm: $time",
                            fixedTime = time,
                            remoteUrl = previewImages.getOrNull(i)
                        )
                    }
                }
                predefined?.type == PackType.WEEKLY -> {
                    listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        .mapIndexed { i, day ->
                            SlotData(
                                label = day.replaceFirstChar { it.uppercase() },
                                dayName = day,
                                remoteUrl = previewImages.getOrNull(i)
                            )
                        }
                }
                else -> {
                    val weatherList = Weather.all().toList()
                    val times = TimeOfDay.entries
                    weatherList.flatMap { weather ->
                        val weatherIndex = weatherList.indexOf(weather)
                        times.map { time ->
                            val timeIndex = times.indexOf(time)
                            val totalIndex = weatherIndex * times.size + timeIndex
                            val url = previewImages.getOrNull(totalIndex)

                            SlotData(
                                label = "",
                                labelRes = weather.stringRes,
                                weather = weather,
                                time = time,
                                remoteUrl = url
                            )
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                val buttonColor = when {
                    isPackLocked -> MaterialTheme.colorScheme.tertiaryContainer
                    isLoading -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                }
                val buttonContentColor = when {
                    isPackLocked -> MaterialTheme.colorScheme.onTertiaryContainer
                    isLoading -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onPrimary
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = buttonColor,
                    contentColor = buttonContentColor,
                    shadowElevation = if (isLoading || isPackLocked) 0.dp else 8.dp
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = if (isLoading) null else ripple(),
                                onClick = {
                                    if (isPackLocked) {
                                        onUpsellClick()
                                    } else if (!isLoading) {
                                        onDownloadFullPack(pack)
                                    }
                                },
                                onLongClick = { if (!isLoading && !isPackLocked) onLongPressInstall() },
                                enabled = !isLoading
                            )
                            .padding(vertical = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading) {
                            CyberpunkLoadingBar(
                                progress = null,
                                modifier = Modifier.width(100.dp),
                                label = null,
                                barHeight = 4.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(com.andyl.iris.R.string.processing).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                        } else if (isPackLocked) {
                            Text("PRO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(com.andyl.iris.R.string.premium_unlock), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(com.andyl.iris.R.string.install_pack), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.pro_tip_merge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp).align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        items(slots) { slot ->
            val label = if (slot.labelRes != null) {
                stringResource(slot.labelRes) + (if (slot.time != null) " - ${slot.time.name}" else "")
            } else {
                slot.label
            }
            
            val isPredefinedPreview = pack is SuggestedPack.Predefined
            val status = if (isPredefinedPreview) SlotStatus() else getSlotStatus(slot, rules, dailyRules, fixedRules)
            val displayUri = status.bestUri ?: slot.remoteUrl

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSlotClick(slot.weather, slot.time, slot.dayName, slot.fixedTime, label) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (status.hasAny) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (status.hasAny) 0.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (displayUri != null) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        }
                        Spacer(modifier = Modifier.width(18.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val statusText = when {
                            status.uriBoth != null -> stringResource(R.string.configured_both)
                            status.uriHome != null && status.uriLock != null -> stringResource(R.string.configured_both)
                            status.uriHome != null -> stringResource(R.string.home_only_label)
                            status.uriLock != null -> stringResource(R.string.lock_only_label)
                            else -> stringResource(R.string.tap_to_customize)
                        }
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (status.hasAny) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    if (status.hasAny) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (pack == SuggestedPack.Time) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    onClick = { onAddCustomTime() }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.add_custom_time_override),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

private data class SlotData(
    val label: String,
    val labelRes: Int? = null,
    val weather: Weather? = null,
    val time: TimeOfDay? = null,
    val dayName: String? = null,
    val fixedTime: String? = null,
    val remoteUrl: String? = null
)
