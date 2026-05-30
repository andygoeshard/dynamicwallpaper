package com.andyl.iris.ui.searchscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.andyl.iris.domain.model.PredefinedPack
import com.andyl.iris.domain.model.PredefinedPacks
import com.andyl.iris.ui.searchscreen.SuggestedPack

@Composable
fun SuggestedPacksList(onPackClick: (SuggestedPack) -> Unit) {
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
            PackSectionHeader("Weather & Atmosphere", "Packs that evolve with your sky.")
            PackHorizontalRow(PredefinedPacks.weatherPacks, onPackClick)
        }

        // --- WEEKLY SECTION ---
        item {
            PackSectionHeader("Weekly Calendars", "A fresh look for every day of the week.")
            PackHorizontalRow(PredefinedPacks.weeklyPacks, onPackClick)
        }

        // --- TIME BASED SECTION ---
        item {
            PackSectionHeader("Time-Based Overrides", "Precise changes matching your routine.")
            PackHorizontalRow(PredefinedPacks.timePacks, onPackClick)
        }

        // --- RANDOM SECTION ---
        item {
            PackSectionHeader("Surprise & Random", "Unexpected beauty for every moment.")
            PackHorizontalRow(PredefinedPacks.randomPacks, onPackClick)
        }

        // --- MANUAL CONFIGURATION ---
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Manual Configuration",
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
private fun PackHorizontalRow(packs: List<PredefinedPack>, onPackClick: (SuggestedPack) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(packs) { pack ->
            PredefinedPackCard(pack) {
                onPackClick(SuggestedPack.Predefined(pack.id, pack.name, pack.description))
            }
        }
    }
}

@Composable
private fun PredefinedPackCard(
    pack: PredefinedPack,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .width(160.dp)
                .height(220.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = pack.previewUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Shadow overlay for readability if needed, though title is below
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f))
                            )
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = pack.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = pack.description,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
        else -> Icons.Default.Star
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
