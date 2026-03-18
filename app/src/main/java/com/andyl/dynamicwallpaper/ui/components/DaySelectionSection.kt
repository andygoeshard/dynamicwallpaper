package com.andyl.dynamicwallpaper.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.andyl.dynamicwallpaper.ui.event.WallpaperEvent
import com.andyl.dynamicwallpaper.ui.state.DynamicWallpaperUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DaySelectionSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val context = LocalContext.current
    val days = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    var selectedDayForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                selectedDayForPicker?.let { day ->
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    onEvent(WallpaperEvent.SetDailyWallpaper(day, it.toString()))
                }
            }
        }
    )

    // --- EL CÁLCULO DEFINITIVO ---
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val itemWidth = 85.dp

    // Restamos 32.dp del padding del Screen (16+16)
    // Y restamos 32.dp del padding interno de la GlassyBox (16+16)
    val availableWidth = configuration.screenWidthDp.dp - 64.dp
    val horizontalPadding = (availableWidth / 2) - (itemWidth / 2)
    // ----------------------------

    val totalItems = Int.MAX_VALUE
    val todayIndex = remember { java.time.LocalDate.now().dayOfWeek.value - 1 }
    val startIndex = (totalItems / 2) - ((totalItems / 2) % days.size) + todayIndex

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Calendario Semanal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // Aplicamos el padding calculado restando los 64dp totales de márgenes
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(totalItems) { index ->
                val dayIndex = (index % days.size)
                val dayName = days[dayIndex]
                val imageUri = state.dailyRules[dayName]
                val isToday = dayIndex == todayIndex

                DayImageCard(
                    dayName = dayName,
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