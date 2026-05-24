package com.andyl.iris.ui.searchscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.ui.searchscreen.SuggestedPack
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.andyl.iris.domain.mapper.toKey

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
    rules: Map<String, String>,
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
            uriBoth = rules["$baseKey - 3"],
            uriHome = rules["$baseKey - 1"],
            uriLock = rules["$baseKey - 2"]
        )
    } else SlotStatus()
}

@Composable
fun PackDetailList(
    pack: SuggestedPack,
    rules: Map<String, String>,
    dailyRules: Map<String, String>,
    fixedRules: Map<String, String> = emptyMap(),
    previewImages: List<String> = emptyList(),
    onSlotClick: (Weather?, TimeOfDay?, String?, String?, String) -> Unit,
    onDownloadFullPack: (SuggestedPack) -> Unit,
    onLongPressInstall: () -> Unit,
    onAddCustomTime: () -> Unit = {}
) {
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
                "10:00" to "Day",
                "18:00" to "Dusk",
                "22:00" to "Night"
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
                    val times = listOf("06:00", "10:00", "18:00", "22:00")
                    times.mapIndexed { i, time ->
                        SlotData(
                            label = "Alarm: $time",
                            fixedTime = time,
                            remoteUrl = previewImages.getOrNull(i % previewImages.size.coerceAtLeast(1))
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
                    val imagesPerWeather = if (previewImages.size >= 6) previewImages.size / 6 else 1
                    
                    Weather.all().flatMap { weather ->
                        val weatherIndex = Weather.all().indexOf(weather)
                        TimeOfDay.entries.map { time ->
                            val timeIndex = TimeOfDay.entries.indexOf(time)
                            val url = previewImages.getOrNull((weatherIndex * imagesPerWeather + timeIndex) % previewImages.size.coerceAtLeast(1))

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
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = { /* No-op, handled by pointerInput */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onLongPressInstall() },
                                onTap = { onDownloadFullPack(pack) }
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Install Full Pack", fontWeight = FontWeight.Bold)
                }
                Text(
                    "Tip: Long press to install into an existing pack.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
                )
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
                    .padding(vertical = 6.dp)
                    .clickable { onSlotClick(slot.weather, slot.time, slot.dayName, slot.fixedTime, label) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (status.hasAny) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (displayUri != null) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val statusText = when {
                            status.uriBoth != null -> "Active Alarm"
                            status.uriHome != null && status.uriLock != null -> "Home & Lock Alarms"
                            status.uriHome != null -> "Home Alarm"
                            status.uriLock != null -> "Lock Alarm"
                            else -> "Tap to search a photo"
                        }
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.hasAny) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    Icon(
                        imageVector = if (status.hasAny) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (status.hasAny) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (pack == SuggestedPack.Time) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onAddCustomTime() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add custom time override",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
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
