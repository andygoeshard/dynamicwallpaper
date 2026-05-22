package com.andyl.iris.ui.searchscreen.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.domain.model.PackType
import com.andyl.iris.domain.model.TimeOfDay
import com.andyl.iris.domain.model.Weather
import com.andyl.iris.ui.searchscreen.SuggestedPack
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.andyl.iris.domain.mapper.toKey

@Composable
fun PackDetailList(
    pack: SuggestedPack,
    rules: Map<String, String>,
    dailyRules: Map<String, String>,
    onSlotClick: (Weather?, TimeOfDay?, String?, String) -> Unit,
    onDownloadFullPack: (SuggestedPack) -> Unit
) {
    val context = LocalContext.current
    
    fun getExistingUri(slot: SlotData): String? {
        return if (slot.dayName != null) {
            dailyRules[slot.dayName] ?: dailyRules["${slot.dayName}-3"]
        } else if (slot.weather != null && slot.time != null) {
            rules["${slot.weather.toKey()} - ${slot.time} - 3"] ?:
            rules["${slot.weather.toKey()} - ${slot.time} - 1"]
        } else null
    }

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
            TimeOfDay.entries.map { time ->
                SlotData(
                    label = "Time: ${time.name}",
                    weather = Weather.Clear,
                    time = time
                )
            }
        }
        is SuggestedPack.Predefined -> {
            val predefined = PredefinedPacks.packs.find { it.id == pack.id }
            if (predefined?.isRandom == true) {
                if (predefined.type == PackType.WEEKLY) {
                    listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                        .map { day ->
                            SlotData(
                                label = day.replaceFirstChar { it.uppercase() },
                                dayName = day,
                                remoteUrl = predefined.previewUrl
                            )
                        }
                } else {
                    Weather.all().flatMap { weather ->
                        TimeOfDay.entries.map { time ->
                            SlotData(
                                label = "",
                                labelRes = weather.stringRes,
                                weather = weather,
                                time = time,
                                remoteUrl = predefined.previewUrl
                            )
                        }
                    }
                }
            } else {
                val weatherSlots = predefined?.weatherRules?.map { rule ->
                    SlotData(
                        label = "",
                        labelRes = rule.weather.stringRes,
                        weather = rule.weather,
                        time = rule.timeOfDay,
                        remoteUrl = rule.imageUrl
                    )
                } ?: emptyList()
                
                val dailySlots = predefined?.dailyRules?.map { rule ->
                    SlotData(
                        label = rule.dayName.replaceFirstChar { it.uppercase() },
                        dayName = rule.dayName,
                        remoteUrl = rule.imageUrl
                    )
                } ?: emptyList()
                
                weatherSlots + dailySlots
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
                    onClick = { onDownloadFullPack(pack) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Install Full Pack", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        items(slots) { slot ->
            val label = if (slot.labelRes != null) {
                stringResource(slot.labelRes) + (if (slot.time != null) " - ${slot.time.name}" else "")
            } else {
                slot.label
            }
            
            val isPredefinedPreview = pack is SuggestedPack.Predefined
            val existingUri = if (isPredefinedPreview) null else getExistingUri(slot)
            val displayUri = existingUri ?: slot.remoteUrl

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSlotClick(slot.weather, slot.time, slot.dayName, label) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (existingUri != null) 
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
                        Text(
                            text = if (existingUri != null) "Wallpaper configured" else "Tap to search a photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (existingUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    Icon(
                        imageVector = if (existingUri != null) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (existingUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    val remoteUrl: String? = null
)
