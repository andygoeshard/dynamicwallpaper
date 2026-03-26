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

    // Estados para el flujo
    var selectedDayForPicker by remember { mutableStateOf<String?>(null) }
    var pendingUri by remember { mutableStateOf<String?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    // Si este es null, muestra el diálogo. Si tiene 1 o 2, va directo.
    var targetPredefined by remember { mutableStateOf<Int?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val day = selectedDayForPicker
                if (day != null) {
                    if (targetPredefined != null) {
                        // Si ya sabemos el target (1 o 2), mandamos directo
                        val finalKey = "$day-$targetPredefined"
                        onEvent(WallpaperEvent.SetDailyWallpaper(finalKey, it.toString()))
                    } else {
                        // Si es nuevo o "Ambos", guardamos URI y mostramos Dialog
                        pendingUri = it.toString()
                        showTargetDialog = true
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Lógica de Scroll (sin cambios) ---
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val itemWidth = 90.dp
    val horizontalPadding = remember(configuration.screenWidthDp) {
        val availableWidth = configuration.screenWidthDp.dp - 64.dp
        (availableWidth / 2) - (itemWidth / 2)
    }
    val todayIndex = remember { java.time.LocalDate.now().dayOfWeek.value - 1 }
    val totalItems = 10000
    val startIndex = remember { (totalItems / 2) - ((totalItems / 2) % days.size) + todayIndex }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.weekCalendar),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(count = totalItems, key = { it }) { index ->
                val dayIndex = (index % days.size)
                val dayName = days[dayIndex]

                val bothUri = state.dailyRules[dayName]
                val homeUri = state.dailyRules["$dayName-1"]
                val lockUri = state.dailyRules["$dayName-2"]

                DayImageCard(
                    dayName = dayName,
                    homeUri = homeUri,
                    lockUri = lockUri,
                    bothUri = bothUri,
                    isToday = dayIndex == todayIndex,
                    onAddClick = {
                        selectedDayForPicker = dayName
                        targetPredefined = null // No sabemos, que el Dialog decida
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onHomeClick = {
                        selectedDayForPicker = dayName
                        targetPredefined = 1 // Es para Home
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onLockClick = {
                        selectedDayForPicker = dayName
                        targetPredefined = 2 // Es para Lock
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onDeleteHome = { onEvent(WallpaperEvent.OnDeleteDayRule("$dayName-1")) },
                    onDeleteLock = { onEvent(WallpaperEvent.OnDeleteDayRule("$dayName-2")) },
                    onDeleteBoth = { onEvent(WallpaperEvent.OnDeleteDayRule(dayName)) }
                )
            }
        }
    }

    if (showTargetDialog) {
        WallpaperTargetDialog(
            onDismiss = { showTargetDialog = false },
            onConfirm = { target ->
                val day = selectedDayForPicker
                val uri = pendingUri
                if (day != null && uri != null) {
                    val finalKey = if (target == 3) day else "$day-$target"
                    onEvent(WallpaperEvent.SetDailyWallpaper(finalKey, uri))
                }
                showTargetDialog = false
            }
        )
    }
}