package com.andyl.iris.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.state.DynamicWallpaperUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DaySelectionSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val context = LocalContext.current
    val days = remember { listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday") }
    var selectedDayForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedDayForPicker?.let { day ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    onEvent(WallpaperEvent.SetDailyWallpaper(day, it.toString()))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val itemWidth = 85.dp
    val horizontalPadding = remember(configuration.screenWidthDp) {
        val availableWidth = configuration.screenWidthDp.dp - 64.dp
        (availableWidth / 2) - (itemWidth / 2)
    }

    val totalItems = 10000
    val todayIndex = remember { java.time.LocalDate.now().dayOfWeek.value - 1 }
    val startIndex = remember(days.size) {
        (totalItems / 2) - ((totalItems / 2) % days.size) + todayIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.weekCalendar),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                count = totalItems,
                key = { index -> index }
            ) { index ->
                val dayIndex = (index % days.size)
                val dayName = days[dayIndex]

                val translatedDay = when(dayName) {
                    "monday" -> stringResource(R.string.mon)
                    "tuesday" -> stringResource(R.string.tue)
                    "wednesday" -> stringResource(R.string.wed)
                    "thursday" -> stringResource(R.string.thu)
                    "friday" -> stringResource(R.string.fri)
                    "saturday" -> stringResource(R.string.sat)
                    "sunday" -> stringResource(R.string.sun)
                    else -> dayName
                }

                val imageUri = state.dailyRules[dayName]
                val isToday = dayIndex == todayIndex

                DayImageCard(
                    dayName = translatedDay,
                    imageUri = imageUri,
                    isToday = isToday,
                    onClick = {
                        selectedDayForPicker = dayName
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onDelete = { onEvent(WallpaperEvent.OnDeleteDayRule(dayName)) }
                )
            }
        }
    }
}