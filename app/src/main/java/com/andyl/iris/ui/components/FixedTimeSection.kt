package com.andyl.iris.ui.components

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import java.util.Calendar

@Composable
fun FixedTimeSection(
    state: DynamicWallpaperUiState,
    onEvent: (WallpaperEvent) -> Unit
) {
    val context = LocalContext.current
    var selectedTimeForPicker by remember { mutableStateOf<String?>(null) }
    var pendingUri by remember { mutableStateOf<String?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var targetPredefined by remember { mutableStateOf<Int?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val time = selectedTimeForPicker ?: return@let
                val uriStr = it.toString()

                if (targetPredefined != null) {
                    val finalKey = when (targetPredefined) {
                        1 -> "$time-1"
                        2 -> "$time-2"
                        else -> time
                    }
                    // MANDAMOS SOLO EL SET. SIN DELETES PREVIOS.
                    onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, finalKey, uriStr))

                    targetPredefined = null
                    selectedTimeForPicker = null
                } else {
                    pendingUri = uriStr
                    showTargetDialog = true
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val groupedRules = state.fixedRules.entries.groupBy { it.key.split("-")[0] }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.fixed_time_section_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(text = stringResource(R.string.fixed_time_section_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            groupedRules.forEach { (displayTime, _) ->
                TimeRuleItem(
                    time = displayTime,
                    homeUri = state.fixedRules["$displayTime-1"],
                    lockUri = state.fixedRules["$displayTime-2"],
                    bothUri = state.fixedRules[displayTime],
                    onDeleteHome = { onEvent(WallpaperEvent.OnDeleteFixedTimeRule(context, "$displayTime-1")) },
                    onDeleteLock = { onEvent(WallpaperEvent.OnDeleteFixedTimeRule(context, "$displayTime-2")) },
                    onDeleteBoth = { onEvent(WallpaperEvent.OnDeleteFixedTimeRule(context, displayTime)) },
                    onHomeClick = { selectedTimeForPicker = displayTime; targetPredefined = 1; photoPickerLauncher.launch(arrayOf("image/*")) },
                    onLockClick = { selectedTimeForPicker = displayTime; targetPredefined = 2; photoPickerLauncher.launch(arrayOf("image/*")) },
                    onBothClick = { selectedTimeForPicker = displayTime; targetPredefined = 3; photoPickerLauncher.launch(arrayOf("image/*")) },
                    onTimeClick = { /* Picker para mover hora */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    selectedTimeForPicker = String.format("%02d:%02d", hour, minute)
                    targetPredefined = null
                    photoPickerLauncher.launch(arrayOf("image/*"))
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(0.15f), contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.btn_schedule_new_time), fontWeight = FontWeight.Bold)
        }
    }

    if (showTargetDialog) {
        WallpaperTargetDialog(
            onDismiss = { showTargetDialog = false },
            onConfirm = { target ->
                val time = selectedTimeForPicker
                val uri = pendingUri
                if (time != null && uri != null) {
                    val finalKey = when (target) {
                        1 -> "$time-1"
                        2 -> "$time-2"
                        else -> time
                    }
                    onEvent(WallpaperEvent.SetFixedTimeWallpaper(context, finalKey, uri))
                }
                showTargetDialog = false
                selectedTimeForPicker = null
                pendingUri = null
            }
        )
    }
}